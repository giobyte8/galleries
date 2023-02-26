package me.giobyte8.galleries.scanner;

import me.giobyte8.galleries.scanner.amqp.ScanRequestsListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SpringBootTest
@MockBean(ScanRequestsListener.class)
class ScannerApplicationTests {

    @Test
    void contextLoads() {
    }
}
