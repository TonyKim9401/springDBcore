package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;


/**
 * JDBC - ConnectionParam
 */
@Slf4j
@Repository
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    @Autowired
    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
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
            log.error("db error ", e);
            throw e;
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, null);
        }

    }

    public Member findById(String memberId) throws SQLException {
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
            log.info("db error ", e);
            throw e;
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, rs);
        }
    }

    /**
     * 트랜잭션 원자성을 위해 하나의 Connection 만 사용
     */
    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
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
            log.info("db error ", e);
            throw e;
        } finally {
            // Connection 은 여기서 닫으면 안됨!
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void update(String memberId, int money) throws SQLException {
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
            log.info("db error ", e);
            throw e;
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, rs);
        }
    }

    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            final int resultSize = pstmt.executeUpdate();

            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            log.info("db error ", e);
            throw e;
        } finally {
            // Connection 은 여기서 닫으면 안됨!
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void delete(String memberId) throws SQLException {
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
            log.info("db error ", e);
            throw e;
        } finally {
            // 사용한 자원(리소스) 정리
            close(con, pstmt, rs);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);

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
        final Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }

}
