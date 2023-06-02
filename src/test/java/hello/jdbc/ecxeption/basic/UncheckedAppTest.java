package hello.jdbc.ecxeption.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedAppTest {


    @Test
    void unchecked() {

        Controller controller = new Controller();

        // RuntimeSqlException 이 먼저 터짐
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(RuntimeSqlException.class);

    }

    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
//            e.printStackTrace(); -> 좋지 않은 방법
            log.info("ex", e);
        }
    }

    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }

    }

    static class Service {
        Repository repository = new Repository();
        NetWorkClient netWorkClient = new NetWorkClient();

        public void logic(){
            repository.call();
            netWorkClient.call();
        }

    }


    static class NetWorkClient {
        public void call(){
            throw new RuntimeConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSqlException(e);
            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }


    static class RuntimeConnectException extends RuntimeException{
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSqlException extends RuntimeException{
        public RuntimeSqlException(Throwable cause) {
            super(cause);
        }
    }

}
