package me.giobyte8.galleries.scanner;

import me.giobyte8.galleries.scanner.scanners.DirMediaScanner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class ScannerApplicationTests {

    @Autowired
    private DirMediaScanner dirScanner;

    @Test
    void contextLoads() {
        assertNotNull(dirScanner);
    }
}
