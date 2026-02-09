package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.scanner.scanners.listeners.ScanEventsHub;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class LocalMediaScannerTest extends BaseIntegrationTest {

    @MockitoBean
    private ScanEventsHub eventsHub;

    @Autowired
    private MediaScanner mediaScanner;

    @Test
    void when_scan_invalid_dir_then_handle_exception() {
        var invalidDir = Directory.builder()
                .path("invalid")
                .build();

        mediaScanner.scan(invalidDir);

        // Verify events hub interactions
        verify(eventsHub, times(1))
                .onScanStarted(invalidDir);
        verify(eventsHub, times(1))
                .onScanFailed(eq(invalidDir), any(IOException.class));
        verifyNoMoreInteractions(eventsHub);
    }

    @Test
    void when_scan_ca_then_emit_file_events() {
        var caDir = Directory.builder()
                .path("cameras/ca")
                .build();
        mediaScanner.scan(caDir);

        // Verify scan start/complete interactions
        verify(eventsHub, times(1))
                .onScanStarted(caDir);
        verify(eventsHub, times(1))
                .onScanCompleted(caDir);

        // Verify 2 images found
        verify(eventsHub, times(2))
                .onNewImageFound(eq(caDir), any(Image.class));

        verifyNoMoreInteractions(eventsHub);
    }

    @Test
    void when_scan_recursive_then_emit_dir_and_file_events() {
        var rootDir = Directory.builder()
                .path("cameras")
                .recursive(true)
                .build();
        mediaScanner.scan(rootDir);

        // Verify scan start/complete interactions
        verify(eventsHub, times(3))
                .onScanStarted(any(Directory.class));
        verify(eventsHub, times(3))
                .onScanCompleted(any(Directory.class));

        // Verify directories found
        verify(eventsHub, times(2))
                .onDirFound(eq(rootDir), any(Directory.class));

        // Verify images found
        verify(eventsHub, times(7))
                .onNewImageFound(any(Directory.class), any(Image.class));

        verifyNoMoreInteractions(eventsHub);
    }
}
