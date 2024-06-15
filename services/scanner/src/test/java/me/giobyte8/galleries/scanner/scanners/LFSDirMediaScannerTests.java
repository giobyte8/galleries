package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.metadata.ImgMetaExtractor;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.services.HashingService;
import me.giobyte8.galleries.scanner.services.PathService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LFSDirMediaScannerTests {

    @Mock
    private ScannerProps scannerProps;

    @Mock
    private PathService pathSvc;

    @Mock
    private HashingService hashingSvc;

    @Mock
    private ImgMetaExtractor imgMetaExtractor;

    @Mock
    private ScanMediaObserver scanMediaObserver;

    @InjectMocks
    private LFSDirMediaScanner dirMediaScanner;

    private final Path contentRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

    private final Set<String> mediaExtensions = new HashSet<>(
            List.of("jpg", "mp4")
    );

    @Test
    void scanInvalidDir() {
        Path absPath = Path
                .of(contentRoot.toString(), "invalid")
                .toAbsolutePath();

        Directory invalidDir = Directory.builder()
                .path(absPath.toString())
                .build();

        when(pathSvc.toAbsolute(invalidDir.getPath()))
                .thenReturn(absPath);

        ScanRequest scanReq = new ScanRequest(
                UUID.randomUUID(),
                invalidDir.getPath(),
                LocalDateTime.now()
        );
        dirMediaScanner.scan(scanReq, invalidDir);

        verify(scanMediaObserver, times(1))
                .onScanStarted(scanReq);

        verify(scanMediaObserver, times(1))
                .prepareForScanning(invalidDir);

        verify(scanMediaObserver, times(1))
                .onScanFailed(any(), any());

        verify(scanMediaObserver, times(1))
                .onScanCompleted(scanReq);

        verifyNoMoreInteractions(scanMediaObserver);
    }
}
