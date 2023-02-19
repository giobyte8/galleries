package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LocalFSScannerTests {

    @Mock
    private ScannerProps mScannerProps;

    @Mock
    private HashingService mHashingService;

    @InjectMocks
    private LocalFSScanner scanner;

    private final Path testContentsRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

    private final Set<String> mediaExtensions = new HashSet<>(
            List.of("jpg", "mp4")
    );

    @Test
    void scanInvalidDir() {
        Path fPath = Path.of("/invalid", "dir");
        OnFileFoundCB mOnFileFoundCB = Mockito.mock(OnFileFoundCB.class);

        assertThrows(
                IOException.class,
                () -> scanner.scan(fPath, false, mOnFileFoundCB)
        );
    }

    @Test
    void scanNonRecursive() throws IOException {
        Path fPath = Path
                .of(testContentsRoot.toString(), "cameras")
                .toAbsolutePath();

        Mockito
                .when(mScannerProps.getMediaFilesExtensions())
                .thenReturn(mediaExtensions);
        OnFileFoundCB mOnFileFoundCB = Mockito.mock(OnFileFoundCB.class);

        scanner.scan(
                fPath,
                false,
                mOnFileFoundCB
        );

        Mockito
                .verify(mOnFileFoundCB, times(2))
                .onFileFound(any(), any());
    }

    @Test
    void scanRecursively() throws IOException {
        Path fPath = Path
                .of(testContentsRoot.toString(), "cameras")
                .toAbsolutePath();

        Mockito
                .when(mScannerProps.getMediaFilesExtensions())
                .thenReturn(mediaExtensions);
        OnFileFoundCB mOnFileFoundCB = Mockito.mock(OnFileFoundCB.class);

        scanner.scan(
                fPath,
                true,
                mOnFileFoundCB
        );

        Mockito
                .verify(mOnFileFoundCB, times(4))
                .onFileFound(any(), any());
    }
}
