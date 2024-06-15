package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.ContentDirStatus;
import me.giobyte8.galleries.scanner.services.PathService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ScanRequestsListenerTest {

    @Mock
    private MediaScannerService mScanner;

    @Mock
    private PathService pathService;

    @Mock
    private ContentDirDao dirDao;


    @InjectMocks
    private ScanRequestsListener scanReqListener;

    @Test
    void scanNonExistentDir() {
        String dirHPath = "fakeHashedPath";

        ScanRequest order = new ScanRequest(
                UUID.randomUUID(),
                dirHPath,
                new Date()
        );

        Mockito
                .when(dirDao.findById(dirHPath))
                .thenReturn(Optional.empty());

        scanReqListener.onScanRequest(order);

        // Scan should be cancelled and mocks never invoked
        verifyNoMoreInteractions(mScanner, pathService);
    }

    @Test
    void scanWrongStatusContentDir() {
        String dirHPath = "fakeHashedPath";

        ScanRequest order = new ScanRequest(
                UUID.randomUUID(),
                dirHPath,
                new Date()
        );

        ContentDir dir = new ContentDir();
        dir.setHashedPath(dirHPath);
        dir.setPath("/fake/test/path");
        dir.setStatus(ContentDirStatus.SCAN_IN_PROGRESS);

        Mockito
                .when(dirDao.findById(dirHPath))
                .thenReturn(Optional.of(dir));

        scanReqListener.onScanRequest(order);

        // Scan should be cancelled and mocks never invoked
        verifyNoMoreInteractions(mScanner, pathService);
    }

    @Test
    void dirScannedAfterScanRequestTime() {
        String dirHPath = "fakeHashedPath";
        Date scanReqDate = new Date();

        ScanRequest order = new ScanRequest(
                UUID.randomUUID(),
                dirHPath,
                scanReqDate
        );

        ContentDir dir = new ContentDir();
        dir.setHashedPath(dirHPath);
        dir.setPath("/fake/test/path");
        dir.setStatus(ContentDirStatus.SCAN_COMPLETE);
        dir.setLastScanStart(
                Date.from(scanReqDate.toInstant().plus(Duration.ofHours(1)))
        );

        Mockito
                .when(dirDao.findById(dirHPath))
                .thenReturn(Optional.of(dir));

        scanReqListener.onScanRequest(order);

        // Scan should be cancelled and mocks never invoked
        verifyNoMoreInteractions(mScanner, pathService);
    }

    @Test
    void scanUnavailableDirectory() {
        String dirHPath = "fakeHashedPath";
        Date scanReqDate = new Date();

        ScanRequest order = new ScanRequest(
                UUID.randomUUID(),
                dirHPath,
                scanReqDate
        );

        ContentDir dir = new ContentDir();
        dir.setHashedPath(dirHPath);
        dir.setPath("/fake/test/path");
        dir.setStatus(ContentDirStatus.SCAN_COMPLETE);

        Mockito
                .when(dirDao.findById(dirHPath))
                .thenReturn(Optional.of(dir));
        Mockito
                .when(pathService.toAbsolute(anyString()))
                        .thenReturn(Path.of("/invalid/abs/path"));

        scanReqListener.onScanRequest(order);

        // Scan should be cancelled and mocks never invoked
        verifyNoMoreInteractions(pathService, mScanner);
    }

    @Test
    void onScanRequestValidOrder() {
        Path testContentsRoot = Paths.get(
                "src/test/resources",
                "galleries"
        );

        String dirHPath = "invalid/path";
        ScanRequest scanRequest = new ScanRequest(
                UUID.randomUUID(),
                dirHPath,
                new Date()
        );

        ContentDir dir = new ContentDir();
        dir.setPath("/random/path");

        Mockito
                .when(dirDao.findById(dirHPath))
                .thenReturn(Optional.of(dir));
        Mockito
                .when(pathService.toAbsolute(anyString()))
                .thenReturn(testContentsRoot);

        scanReqListener.onScanRequest(scanRequest);

        Mockito
                .verify(pathService, times(1))
                .toAbsolute(anyString());
        Mockito
                .verify(mScanner, times(1))
                .scan(any(UUID.class), any(String.class));
    }
}