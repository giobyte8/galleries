package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.Mockito.*;

public class LocalMediaScannerTest extends BaseIntegrationTest {

    @MockitoSpyBean
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
                .scanStarted(invalidDir);
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
                .scanStarted(caDir);
        verify(eventsHub, times(1))
                .scanCompleted(caDir);

        // Verify 2 images found
        verify(eventsHub, times(2))
                .imgFound(any(Image.class));

        verifyNoMoreInteractions(eventsHub);
    }
}
