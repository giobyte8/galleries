package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.Fingerprint;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import me.giobyte8.galleries.scanner.metadata.MediaMetaExtractor;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import me.giobyte8.galleries.scanner.scanners.listeners.ScanEventsHub;
import me.giobyte8.galleries.scanner.services.FingerprintService;
import me.giobyte8.galleries.scanner.services.PathService;
import me.giobyte8.galleries.services.ImageService;
import me.giobyte8.galleries.services.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LocalMediaScannerTests {

    @TempDir
    Path tempDir;

    @Mock
    private ScannerProps scannerProps;

    @Mock
    private PathService pathSvc;

    @Mock
    private FingerprintService fingerprintSvc;

    @Mock
    private MediaMetaExtractor mediaMetaExtractor;

    @Mock
    private ImageService imageSvc;

    @Mock
    private VideoService videoSvc;

    @Mock
    private ScanEventsHub eventsHub;

    @Mock
    private MetricsService metricsSvc;

    @InjectMocks
    private LocalMediaScanner scanner;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(scannerProps.getImageFileExtensions())
                .thenReturn(Set.of("jpg"));
        lenient().when(scannerProps.getVideoFileExtensions())
                .thenReturn(Set.of("mp4"));

        lenient().when(pathSvc.toAbsolute(any(String.class))).thenAnswer(
                invocation -> {
            String relPath = invocation.getArgument(0, String.class);
            if (relPath == null || relPath.isBlank()) {
                return tempDir;
            }
            return tempDir.resolve(relPath);
        });

        lenient().when(pathSvc.toRelative(any(Path.class))).thenAnswer(
                invocation -> {
            Path absPath = invocation.getArgument(0, Path.class);
            return tempDir.relativize(absPath);
        });

        lenient().when(fingerprintSvc.forPath(any(Path.class))).thenReturn(
                Fingerprint.builder()
                        .fileSize(10L)
                        .lastModified(Instant.now())
                        .build()
        );

        lenient().when(imageSvc.findByPath(any(String.class)))
                .thenReturn(Optional.empty());
        lenient().when(videoSvc.findByPath(any(String.class)))
                .thenReturn(Optional.empty());

        lenient().when(mediaMetaExtractor.extract(any(Path.class))).thenReturn(
                MFMetadata.builder().build()
        );
    }

    @Test
    void hiddenImageFilesAreSkipped() throws IOException {
        Files.createFile(tempDir.resolve("visible.jpg"));
        Files.createFile(tempDir.resolve(".hidden.jpg"));

        scanner.scan(rootDir(false));

        verify(eventsHub, times(1)).onNewImageFound(
                any(Directory.class),
                argThat(image -> image.getPath().equals("visible.jpg"))
        );

        verify(eventsHub, never()).onNewImageFound(
                any(Directory.class),
                argThat(image -> image.getPath().equals(".hidden.jpg"))
        );
    }

    @Test
    void hiddenVideoFilesAreSkipped() throws IOException {
        Files.createFile(tempDir.resolve("visible.mp4"));
        Files.createFile(tempDir.resolve(".hidden.mp4"));

        scanner.scan(rootDir(false));

        verify(eventsHub, times(1)).onNewVideoFound(
                any(Directory.class),
                argThat(video -> video.getPath().equals("visible.mp4"))
        );

        verify(eventsHub, never()).onNewVideoFound(
                any(Directory.class),
                argThat(video -> video.getPath().equals(".hidden.mp4"))
        );
    }

    @Test
    void hiddenDirectoriesAreSkipped() throws IOException {
        Files.createDirectory(tempDir.resolve("visible_dir"));
        Files.createDirectory(tempDir.resolve(".hidden_dir"));

        Directory rootDir = rootDir(true);

        scanner.scan(rootDir);

        verify(eventsHub, times(1)).onDirFound(
                eq(rootDir),
                argThat(dir -> dir.getPath().equals("visible_dir"))
        );

        verify(eventsHub, never()).onDirFound(
                eq(rootDir),
                argThat(dir -> dir.getPath().equals(".hidden_dir"))
        );
    }

    private Directory rootDir(boolean recursive) {
        return Directory.builder()
                .path("")
                .recursive(recursive)
                .build();
    }
}



