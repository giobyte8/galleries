package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import me.giobyte8.galleries.scanner.metadata.ImgMetaExtractor;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
import me.giobyte8.galleries.scanner.services.HashingService;
import me.giobyte8.galleries.scanner.services.PathService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private ScanEventsObserver scanEventsObserver;

    @Mock
    private DirectoryRepository dirRepository;

    @InjectMocks
    private LFSDirMediaScanner dirMediaScanner;

    private final Path testContentsRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

    private final Set<String> mediaExtensions = new HashSet<>(
            List.of("jpg", "mp4")
    );

    @Test
    void scanInvalidDir() {
        Path invalidDirPath = Path
                .of(testContentsRoot.toString(), "invalid")
                .toAbsolutePath();

        Directory invalidDir = Directory.builder()
                .path(invalidDirPath.toString())
                .build();

        when(pathSvc.toAbsolute(invalidDir.getPath()))
                .thenReturn(invalidDirPath);

        dirMediaScanner.scan(invalidDir);

        // Invalid dir path should cause scan process to finish
        // without updates to database
        verify(dirRepository, never()).save(invalidDir);
    }

    @Test
    void scanNonRecursive() throws IOException {
        Path camerasPath = Path
                .of(testContentsRoot.toString(), "cameras")
                .toAbsolutePath();
        Directory camerasDir = Directory.builder()
                .path(camerasPath.toString())
                .recursive(false)
                .build();

        when(pathSvc.toAbsolute(camerasDir.getPath()))
                .thenReturn(camerasPath);
        when(scannerProps.getMediaFilesExtensions())
                .thenReturn(mediaExtensions);

        when(pathSvc.toRelative(any()))
                .thenReturn(camerasPath);

        MFMetadata meta = new MFMetadata();
        meta.setDatetimeOriginal(new Date());
        when(imgMetaExtractor.extract(any()))
                .thenReturn(meta);

        dirMediaScanner.scan(camerasDir);

        // Verify two files were found during scan
        verify(scanEventsObserver, times(2))
                .onImageFound(any(), any());
    }

    @Test
    void scanRecursive() throws IOException {
        Path camerasPath = Path
                .of(testContentsRoot.toString(), "cameras")
                .toAbsolutePath();
        Path caPath = Path.of(camerasPath.toString(), "ca");

        Directory camerasDir = Directory.builder()
                .path(camerasPath.toString())
                .recursive(true)
                .build();

        // Prepare path service invocations for 'cameras' dir
        when(pathSvc.toAbsolute(camerasDir.getPath())).thenReturn(camerasPath);

        when(scannerProps.getMediaFilesExtensions())
                .thenReturn(mediaExtensions);

        // Will be invoked once for each found image
        when(pathSvc.toRelative(any()))
                .thenReturn(camerasPath);

        // Prepare path service invocations for 'ca' dir
        when(pathSvc.toRelative(caPath)).thenReturn(caPath);
        when(pathSvc.toAbsolute(caPath.toString())).thenReturn(caPath);

        // Meta extractor will be invoked for every found image
        MFMetadata meta = new MFMetadata();
        meta.setDatetimeOriginal(new Date());
        when(imgMetaExtractor.extract(any()))
                .thenReturn(meta);


        dirMediaScanner.scan(camerasDir);

        // Verify files were found during scan
        verify(scanEventsObserver, times(4))
                .onImageFound(any(), any());
    }
}
