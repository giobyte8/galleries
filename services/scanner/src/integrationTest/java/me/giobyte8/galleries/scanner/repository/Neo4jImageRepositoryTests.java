package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class Neo4jImageRepositoryTests extends Neo4jEphemeralTest {

    @Autowired
    private Neo4jImageRepository imgRepository;

    @Autowired
    private Neo4jDirectoryRepository dirRepository;

    @Test
    void save() {
        String dirPath = "test/dir/img/repo";
        Directory parent = Directory.builder()
                .path(dirPath)
                .build();
        dirRepository.save(parent);

        Image img = Image.builder()
                .path("random.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .build();

        imgRepository.save(parent, img);

        // Assert image was saved
        Image dbImg = imgRepository.findBy(img.getPath());
        assert dbImg != null;

        // Assert image was added to parent dir
    }

    @Test
    void findNonExistent() {
        String path = "test/not/found";
        Image img = imgRepository.findBy(path);
        assertNull(img);
    }

    @Test
    void updateDirChildrenStatus() {
        Directory dir = Directory.builder()
                .path("abc/test")
                .recursive(true)
                .build();
        dirRepository.save(dir);

        Image img1 = Image.builder()
                .path("test_image.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.save(dir, img1);

        Image img2 = Image.builder()
                .path("test_image_2.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.save(dir, img2);

        // Verify both images are updated
        long updatedCount = imgRepository.update(dir, ImageStatus.NOT_FOUND);
        assertEquals(
                2,
                updatedCount,
                "2 Images should be updated"
        );

        // Verify subsequent update affects 0 images
        updatedCount = imgRepository.update(dir, ImageStatus.NOT_FOUND);
        assertEquals(
                0,
                updatedCount,
                "Subsequent update should impact zero images"
        );
    }

    @Test
    void updateDirChildrenStatusVerifyGrandchildrenNotAffected() {
        Directory parent = Directory.builder()
                .path("abc/test")
                .recursive(true)
                .build();
        dirRepository.save(parent);

        Image img1 = Image.builder()
                .path("test_image.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.save(parent, img1);

        Image img2 = Image.builder()
                .path("test_image_2.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.save(parent, img2);

        Directory nestedDir = Directory.builder()
                .path("abc/test/nested")
                .recursive(true)
                .build();
        dirRepository.save(parent, nestedDir);

        Image nestedImg1 = Image.builder()
                .path("nested_dir_img_1.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.save(nestedDir, nestedImg1);

        Image nestedImg2 = Image.builder()
                .path("nested_dir_img_2.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.save(nestedDir, nestedImg2);

        // Update on 'parent' dir should impact only 2 images
        long updatedCount = imgRepository.update(parent, ImageStatus.NOT_FOUND);
        assertEquals(
                2,
                updatedCount,
                "Only 2 Images should have been updated"
        );
    }

    @Test
    void deleteByStatus() {
        Directory parent = Directory.builder()
                .path("abc/test")
                .recursive(true)
                .build();
        dirRepository.save(parent);

        Image img1 = Image.builder()
                .path("test_image.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.NOT_FOUND)
                .build();
        imgRepository.save(parent, img1);

        Image img2 = Image.builder()
                .path("test_image_2.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.NOT_FOUND)
                .build();
        imgRepository.save(parent, img2);

        long deleteCount = imgRepository.delete(parent, ImageStatus.NOT_FOUND);
        assertEquals(
                2,
                deleteCount,
                "2 Images should be deleted"
        );
    }
}
