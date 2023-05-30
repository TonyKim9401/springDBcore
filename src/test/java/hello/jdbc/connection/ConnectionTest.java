package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        final Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        final Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={} , class={}", con1, con1.getClass());
        log.info("connection={} , class={}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션을 획득
        final DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        //커넥션 풀링
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // default value : 10
        /**
         * Pool 이 다 차게되면 나머지 요청들은 대기상태로 빠지고,
         * 일정 대기 시간이 지나면 오류로 처리한다.
         * (보통 대기시간은 짧게 한다)
         */
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);

        Thread.sleep(1000);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        final Connection con1 = dataSource.getConnection();
        final Connection con2 = dataSource.getConnection();
        log.info("connection={} , class={}", con1, con1.getClass());
        log.info("connection={} , class={}", con2, con2.getClass());
    }

}
