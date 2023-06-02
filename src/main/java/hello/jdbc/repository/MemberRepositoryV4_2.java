package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;


/**
 * SQLExceptionTranslator 추가
 */
@Slf4j

public class MemberRepositoryV4_2 implements MemberRepository{

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;
    @Autowired
    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?,?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            // sql 실행 준비
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            // 파라미터 바인딩
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            // 실행
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, null);
        }

    }

    @Override
    public Member findById(String memberId)  {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();

            // next() 를 통해 반환된 데이터가 있는지 없는지 확인
            if (rs.next()) {
                final Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }

        } catch (SQLException e) {
            throw exTranslator.translate("findById", sql, e);
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, rs);
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            final int resultSize = pstmt.executeUpdate();

            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, rs);
        }
    }

    @Override
    public void delete(String memberId)  {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            final int resultSize = pstmt.executeUpdate();

            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, rs);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다!
        DataSourceUtils.releaseConnection(con, dataSource);

        /*
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
        */
        /*
        if (rs != null) {
            try {
                rs.close(); // SQLException
            } catch (SQLException e) {
                log.info("error ", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close(); // SQLException
            } catch (SQLException e) {
                log.info("error ", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        */
    }

    private Connection getConnection() throws SQLException {
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다.
        final Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }

}
