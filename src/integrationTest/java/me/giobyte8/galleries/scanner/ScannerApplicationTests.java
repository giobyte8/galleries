package me.giobyte8.galleries.scanner;

import me.giobyte8.galleries.scanner.scanners.MediaScanner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class ScannerApplicationTests extends BaseIntegrationTest {

    @Autowired
    private MediaScanner dirScanner;

    @Test
    void contextLoads() {
        assertNotNull(dirScanner);
    }
}
