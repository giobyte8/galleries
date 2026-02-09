package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.scanner.scanners.MediaScanner;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ScanRequestsListenerTest {

    @Mock
    private MediaScanner mScanner;

    @Mock
    private DirectoryRepository dirRepository;

    @InjectMocks
    private ScanRequestsListener scanReqListener;

//    @Test
//    void scanNonExistentDir() {
//        String dirPath = "/non/existent/path";
//
//        ScanRequest scanReq = new ScanRequest(
//                UUID.randomUUID(),
//                dirPath,
//                LocalDateTime.now()
//        );
//
//        Mockito
//                .when(dirRepository.findBy(scanReq.path()))
//                .thenReturn(null);
//
//        scanReqListener.onScanRequest(scanReq);
//
//        // Scan should be cancelled and mock never invoked
//        verify(mScanner, never()).scan(any(), any());
//    }
//
//    @Test
//    void scanWrongStatusContentDir() {
//        String dirPath = "test/dir";
//
//        ScanRequest order = new ScanRequest(
//                UUID.randomUUID(),
//                dirPath,
//                LocalDateTime.now()
//        );
//
//        Directory dir = Directory.builder()
//                .path(dirPath)
//                .status(DirStatus.SCAN_IN_PROGRESS)
//                .build();
//
//        Mockito
//                .when(dirRepository.findBy(dirPath))
//                .thenReturn(dir);
//
//        scanReqListener.onScanRequest(order);
//
//        // Scan should be cancelled and mocks never invoked
//        verify(mScanner, never()).scan(any(), any());
//    }
//
//    @Test
//    void onScanRequest() {
//        String dirPath = "test/dir";
//        Directory dir = Directory.builder()
//                .path(dirPath)
//                .build();
//
//        ScanRequest scanRequest = new ScanRequest(
//                UUID.randomUUID(),
//                dirPath,
//                LocalDateTime.now()
//        );
//
//        Mockito
//                .when(dirRepository.findBy(dirPath))
//                .thenReturn(dir);
//
//        scanReqListener.onScanRequest(scanRequest);
//
//        verify(mScanner, times(1))
//                .scan(scanRequest, dir);
//    }
}