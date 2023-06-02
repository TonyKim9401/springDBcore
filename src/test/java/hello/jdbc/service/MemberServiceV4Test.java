package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.aop.support.AopUtils.isAopProxy;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스 의존
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberServiceV4 memberService;

    @TestConfiguration
    static class TestConfig {

        /**
         * Spring Boot 제공 dataSource 사용
         * - application.properties 참조
         */
        private final DataSource dataSource;

        TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
//            return new MemberRepositoryV4_1(dataSource);
//            return new MemberRepositoryV4_2(dataSource);
            return new MemberRepositoryV5(dataSource);
        }
        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }

    @AfterEach
    void afterEach() {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @DisplayName("정상 이체")
    @Test
    void accountTransfer() {
        //given
        final Member memberA = new Member(MEMBER_A, 10000);
        final Member memberB = new Member(MEMBER_B, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        final Member findMemberA = memberRepository.findById(memberA.getMemberId());
        final Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }


    @DisplayName("이체중 예외 발생")
    @Test
    void accountTransferEx() {
        //given
        final Member memberA = new Member(MEMBER_A, 10000);
        final Member memberEx = new Member(MEMBER_EX, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberEx);


        //when
        assertThatThrownBy(
                () -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalArgumentException.class);

        //then
        final Member findMemberA = memberRepository.findById(memberA.getMemberId());
        final Member findMemberB = memberRepository.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }

    @Test
    void aopCheck() {
        /**
         * @Transactional 을 사용 하므로 인해
         * memberService -> memberService Proxy 객체가 생성되서
         * transactional 에 프록시 객체가 주입되어 로직을 실행하게 된다.
         * 따라서 Bean 으로 Proxy 가 등록되게 된다.
         */
        log.info("memberService class = {}", memberService.getClass());
        log.info("memberRepository class = {}", memberRepository.getClass());

        // AopUtils 사용
        assertThat(isAopProxy(memberService)).isTrue();
        assertThat(isAopProxy(memberRepository)).isFalse();
    }
}