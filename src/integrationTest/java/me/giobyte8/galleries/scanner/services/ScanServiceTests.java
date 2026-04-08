package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.metrics.Metric;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ScanServiceTests extends BaseIntegrationTest {

    @Autowired
    private ScanService scanService;

    @Autowired
    private DirectoryRepository dirRepo;

    @Autowired
    private ImageRepository imgRepo;

    @MockitoBean
    private MetricsService metricsService;

    @Test
    void test_scan_iphone_gallery() {
        var iPhoneDir = Directory.builder()
                .path("cameras/iPhone")
                .build();
        dirRepo.save(iPhoneDir);

        ScanRequest request = new ScanRequest(
                UUID.randomUUID(),
                "cameras/iPhone",
                LocalDateTime.now()
        );
        scanService.scan(request);

        // Verify three images were found
        assertThat(imgRepo.countByStatus(MediaFileStatus.AVAILABLE))
                .isEqualTo(3);

        // Verify no new directories were created
        assertThat(dirRepo.count())
                .isEqualTo(1);
    }

    @Test
    void test_scan_cameras_gallery_recursive() {
        var camerasDir = Directory.builder()
                .path("cameras")
                .recursive(true)
                .build();
        dirRepo.save(camerasDir);

        ScanRequest request = new ScanRequest(
                UUID.randomUUID(),
                "cameras",
                LocalDateTime.now()
        );
        scanService.scan(request);

        // Verify all images were found
        assertThat(imgRepo.countByStatus(MediaFileStatus.AVAILABLE))
                .isEqualTo(7);

        // Verify two new directories were created
        assertThat(dirRepo.count())
                .isEqualTo(3);
    }

    @Test
    void test_scan_cameras_gallery_non_recursive() {
        var camerasDir = Directory.builder()
                .path("cameras")
                .build();
        dirRepo.save(camerasDir);

        ScanRequest request = new ScanRequest(
                UUID.randomUUID(),
                "cameras",
                LocalDateTime.now()
        );
        scanService.scan(request);

        // Verify all images were found
        assertThat(imgRepo.countByStatus(MediaFileStatus.AVAILABLE))
                .isEqualTo(2);

        // Verify no new directories were created
        assertThat(dirRepo.count())
                .isEqualTo(1);
    }

    @Test
    void test_scan_cameras_gallery_with_metrics() {
        // Reuse the existing recursive test setup
        test_scan_cameras_gallery_recursive();

        // Verify MetricsService invocations
        verify(metricsService, times(3))
                .increment(Metric.SCAN_DIR_STARTED);
        verify(metricsService, times(3))
                .increment(Metric.SCAN_DIR_COMPLETED);
        verify(metricsService, times(7))
                .increment(Metric.SCAN_IMG_FOUND_NEW);
        verify(metricsService, times(7))
                .increment(Metric.THUMBS_REQUESTED_GEN);
        verify(metricsService, times(2))
                .increment(Metric.SCAN_DIR_FOUND);
    }
}
