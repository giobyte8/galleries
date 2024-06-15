package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.model.DirStatus;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
import me.giobyte8.galleries.scanner.scanners.DirMediaScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanRequestsListenerTest {

    @Mock
    private DirMediaScanner mScanner;

    @Mock
    private DirectoryRepository dirRepository;

    @InjectMocks
    private ScanRequestsListener scanReqListener;

    @Test
    void scanNonExistentDir() {
        String dirPath = "/non/existent/path";

        ScanRequest scanReq = new ScanRequest(
                UUID.randomUUID(),
                dirPath,
                LocalDateTime.now()
        );

        Mockito
                .when(dirRepository.findBy(scanReq.dirPath()))
                .thenReturn(null);

        scanReqListener.onScanRequest(scanReq);

        // Scan should be cancelled and mock never invoked
        verify(mScanner, never()).scan(any(), any());
    }

    @Test
    void scanWrongStatusContentDir() {
        String dirPath = "test/dir";

        ScanRequest order = new ScanRequest(
                UUID.randomUUID(),
                dirPath,
                LocalDateTime.now()
        );

        Directory dir = Directory.builder()
                .path(dirPath)
                .status(DirStatus.SCAN_IN_PROGRESS)
                .build();

        Mockito
                .when(dirRepository.findBy(dirPath))
                .thenReturn(dir);

        scanReqListener.onScanRequest(order);

        // Scan should be cancelled and mocks never invoked
        verify(mScanner, never()).scan(any(), any());
    }

    @Test
    void onScanRequest() {
        String dirPath = "test/dir";
        Directory dir = Directory.builder()
                .path(dirPath)
                .build();

        ScanRequest scanRequest = new ScanRequest(
                UUID.randomUUID(),
                dirPath,
                LocalDateTime.now()
        );

        Mockito
                .when(dirRepository.findBy(dirPath))
                .thenReturn(dir);

        scanReqListener.onScanRequest(scanRequest);

        verify(mScanner, times(1))
                .scan(scanRequest, dir);
    }
}