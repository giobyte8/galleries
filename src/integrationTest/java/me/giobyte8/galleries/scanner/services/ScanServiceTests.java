package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
import me.giobyte8.galleries.scanner.repository.ImageRepository;
import me.giobyte8.galleries.scanner.repository.Neo4jEphemeralTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ScanServiceTests extends Neo4jEphemeralTest {

    @Autowired
    private ScanService scanService;

    @Autowired
    private DirectoryRepository dirRepo;

    @Autowired
    private ImageRepository imgRepo;

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
        assertThat(imgRepo.countBy(ImageStatus.AVAILABLE))
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
        assertThat(imgRepo.countBy(ImageStatus.AVAILABLE))
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
        assertThat(imgRepo.countBy(ImageStatus.AVAILABLE))
                .isEqualTo(2);

        // Verify two new directories were created
        assertThat(dirRepo.count())
                .isEqualTo(1);
    }
}
