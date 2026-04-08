package me.giobyte8.galleries.scanner.scanners.listeners;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.models.ScanStatus;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import me.giobyte8.galleries.persistence.repositories.ScanStatsRepository;
import me.giobyte8.galleries.persistence.repositories.VideoRepository;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.services.ScanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for scan statistics tracking.
 * Verifies that ScanStats are correctly accumulated during scans and persisted to storage.
 */
public class StatsScanEventsListenerTest extends BaseIntegrationTest {

    @Autowired
    private ScanService scanService;

    @Autowired
    private DirectoryRepository dirRepository;

    @Autowired
    private ImageRepository imgRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ScanStatsRepository scanStatsRepository;

    @Test
    void test_scan_stats_tracked_and_persisted() {
        // Setup: create a directory to scan
        var camerasDir = Directory.builder()
                .path("cameras")
                .recursive(true)
                .build();
        dirRepository.save(camerasDir);

        UUID scanRequestId = UUID.randomUUID();
        ScanRequest scanRequest = new ScanRequest(
                scanRequestId,
                "cameras",
                LocalDateTime.now()
        );

        // Execute scan
        scanService.scan(scanRequest);

        // Verify images were found
        assertThat(imgRepository.countByStatus(MediaFileStatus.AVAILABLE))
                .isEqualTo(7);

        // Verify scan stats were persisted
        var scanStatsOpt = scanStatsRepository.findByScanRequestId(scanRequestId);
        assertThat(scanStatsOpt)
                .as("ScanStats should be persisted for the scan request")
                .isPresent();

        var scanStats = scanStatsOpt.get();
        assertThat(scanStats.getStatus())
                .isEqualTo(ScanStatus.COMPLETED);
        assertThat(scanStats.getPath())
                .isEqualTo("cameras");
        assertThat(scanStats.getNewImages())
                .isEqualTo(7);
        assertThat(scanStats.getFoundDirectories())
                .isEqualTo(2);
        assertThat(scanStats.getStartedAt())
                .isNotNull();
        assertThat(scanStats.getCompletedAt())
                .isNotNull();
        assertThat(scanStats.getCompletedAt())
                .isAfterOrEqualTo(scanStats.getStartedAt());
    }

    @Test
    void test_scan_stats_with_updated_images() {
        // Setup: create and populate a directory with images
        var iPhoneDir = Directory.builder()
                .path("cameras/iPhone")
                .build();
        dirRepository.save(iPhoneDir);

        UUID firstScanId = UUID.randomUUID();
        ScanRequest firstScan = new ScanRequest(
                firstScanId,
                "cameras/iPhone",
                LocalDateTime.now()
        );

        // First scan - all images are new
        scanService.scan(firstScan);

        assertThat(imgRepository.countByStatus(MediaFileStatus.AVAILABLE))
                .isEqualTo(3);

        var firstScanStats = scanStatsRepository.findByScanRequestId(firstScanId).orElseThrow();
        assertThat(firstScanStats.getNewImages()).isEqualTo(3);
        assertThat(firstScanStats.getUnchangedImages()).isEqualTo(0);
        assertThat(firstScanStats.getUpdatedImages()).isEqualTo(0);

        // Second scan - images are unchanged
        UUID secondScanId = UUID.randomUUID();
        ScanRequest secondScan = new ScanRequest(
                secondScanId,
                "cameras/iPhone",
                LocalDateTime.now()
        );

        scanService.scan(secondScan);

        var secondScanStats = scanStatsRepository.findByScanRequestId(secondScanId).orElseThrow();
        assertThat(secondScanStats.getNewImages())
                .as("Second scan should have no new images")
                .isEqualTo(0);
        assertThat(secondScanStats.getUnchangedImages())
                .as("Second scan should find all 3 images unchanged")
                .isEqualTo(3);
        assertThat(secondScanStats.getUpdatedImages()).isEqualTo(0);
    }

    @Test
    void test_scan_stats_tracked_for_videos() {
        var videosDir = Directory.builder()
                .path("videos")
                .build();
        dirRepository.save(videosDir);

        UUID scanRequestId = UUID.randomUUID();
        ScanRequest scanRequest = new ScanRequest(
                scanRequestId,
                "videos",
                LocalDateTime.now()
        );

        scanService.scan(scanRequest);

        assertThat(videoRepository.countByStatus(MediaFileStatus.AVAILABLE))
                .isEqualTo(4);

        var scanStats = scanStatsRepository
                .findByScanRequestId(scanRequestId)
                .orElseThrow();
        assertThat(scanStats.getNewVideos()).isEqualTo(4);
        assertThat(scanStats.getUnchangedVideos()).isEqualTo(0);
        assertThat(scanStats.getUpdatedVideos()).isEqualTo(0);
        assertThat(scanStats.getNotFoundVideos()).isEqualTo(0);
    }

    @Test
    void test_scan_stats_with_unchanged_videos() {
        var videosDir = Directory.builder()
                .path("videos")
                .build();
        dirRepository.save(videosDir);

        UUID firstScanId = UUID.randomUUID();
        ScanRequest firstScan = new ScanRequest(
                firstScanId,
                "videos",
                LocalDateTime.now()
        );
        scanService.scan(firstScan);

        UUID secondScanId = UUID.randomUUID();
        ScanRequest secondScan = new ScanRequest(
                secondScanId,
                "videos",
                LocalDateTime.now()
        );
        scanService.scan(secondScan);

        var secondScanStats = scanStatsRepository
                .findByScanRequestId(secondScanId)
                .orElseThrow();
        assertThat(secondScanStats.getNewVideos()).isEqualTo(0);
        assertThat(secondScanStats.getUnchangedVideos()).isEqualTo(4);
        assertThat(secondScanStats.getUpdatedVideos()).isEqualTo(0);
    }
}
