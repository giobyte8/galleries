package me.giobyte8.galleries.scanner;

import me.giobyte8.galleries.scanner.amqp.ScanRequestsListener;
import me.giobyte8.galleries.scanner.services.MediaScannerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@MockBean(ScanRequestsListener.class)
class ScannerApplicationTests {

    @Autowired
    private MediaScannerService mScannerSvc;

    @Test
    void contextLoads() {
        assertThat(mScannerSvc).isNotNull();
    }
}
