package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        //save
        final Member member = new Member("memberV5", 10000);
        repository.save(member);

        //findById
        final Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember);
        assertThat(findMember).isEqualTo(member);

        //update money: 10000 -> 999
        repository.update("memberV5", 999);
        final Member updateMember = repository.findById("memberV5");
        assertThat(updateMember.getMoney()).isEqualTo(999);

        //delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById("memberV5"))
                .isInstanceOf(NoSuchElementException.class);
    }

}