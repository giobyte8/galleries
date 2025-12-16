package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
    void saveMissingCoordinates() {
        Directory parent = Directory.builder()
                .path("root/")
                .build();
        dirRepository.save(parent);

        Image img = Image.builder()
                .path("random.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.now())
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .build();
        imgRepository.save(parent, img);

        Image dbImg = imgRepository.findBy("random.jpg");
        assertNull(dbImg.getGpsLatitude());
        assertNull(dbImg.getGpsLongitude());
    }

    @Test
    void saveMissingDatetimeOriginal() {
        Directory parent = Directory.builder()
                .path("root/")
                .build();
        dirRepository.save(parent);

        Image img = Image.builder()
                .path("random.jpg")
                .contentHash("12345")
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .build();
        imgRepository.save(parent, img);

        Image dbImg = imgRepository.findBy("random.jpg");
        assertNull(dbImg.getDatetimeOriginal());
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

    @Test
    void deleteAndGetPaths() {
        var imgPath1 = "test/gallery/img1.jpg";
        var imgPath2 = "test/gallery/img2.jpg";
        var imgPath3 = "test/gallery/img3.jpg";

        var parent = createDir("test/gallery");
        createImage(parent, imgPath1, ImageStatus.VERIFYING);
        createImage(parent, imgPath2, ImageStatus.VERIFYING);
        createImage(parent, imgPath3, ImageStatus.AVAILABLE);

        Set<String> deletedPaths = imgRepository
                .deleteAndGetPaths(parent, ImageStatus.VERIFYING);

        assertEquals(
                2,
                deletedPaths.size(),
                "2 Images should have been deleted"
        );
        assertTrue(
                deletedPaths.contains(imgPath1),
                "img1.jpg should have been deleted"
        );
        assertTrue(
                deletedPaths.contains(imgPath2),
                "img2.jpg should have been deleted"
        );

        assertFalse(
                deletedPaths.contains(imgPath3),
                "img3.jpg should haven't been deleted"
        );
    }

    @Test
    void multilevelDeleteAndGetPaths() {

        // Graphical representation of tested scenario
        //
        //      |-------|------ root/ ------------|
        //      |       |                         |
        //     img1    dir1/        |-----|------dir2/
        //            /    \        |     |        \
        //          img5  img6     img7  img8     dir3/
        //                                           \
        //                                          img9

        Directory root = createDir("root/");
        Directory dir1 = createDir(root, "dir1/");
        Directory dir2 = createDir(root, "dir2/");
        Directory dir3 = createDir(dir2, "dir3/");

        String pathImg1 = "root/img1.jpg";
        String pathImg2 = "root/img5.jpg";
        String pathImg6 = "root/img6.jpg";
        String pathImg7 = "root/img7.jpg";
        String pathImg8 = "root/img8.jpg";
        String pathImg9 = "root/img9.jpg";

        createImage(root, pathImg1, ImageStatus.AVAILABLE);
        createImage(dir1, pathImg2, ImageStatus.AVAILABLE);
        createImage(dir1, pathImg6, ImageStatus.AVAILABLE);

        createImage(dir2, pathImg7, ImageStatus.AVAILABLE);
        createImage(dir2, pathImg8, ImageStatus.AVAILABLE);

        createImage(dir3, pathImg9, ImageStatus.AVAILABLE);

        Set<String> deletedPaths = imgRepository
                .multilevelDeleteAndGetPaths(root);
        assertEquals(6, deletedPaths.size());
        assertTrue(deletedPaths.containsAll(Arrays.asList(
                pathImg1, pathImg2, pathImg6, pathImg7, pathImg8, pathImg9
        )));
    }

    @Test
    void multilevelDeleteAndGetPathsNoGrandchildren() {
        Directory root = createDir("root/");
        createDir(root, "dir1/");
        Directory dir2 = createDir(root, "dir2/");
        createDir(dir2, "dir3/");

        Set<String> deletedImgPaths = imgRepository
                .multilevelDeleteAndGetPaths(root);
        assertTrue(deletedImgPaths.isEmpty());
    }

    private Directory createDir(String path) {
        Directory dir = Directory.builder()
                .path(path)
                .build();

        return createDir(null, dir);
    }

    private Directory createDir(Directory parent, String path) {
        Directory dir = Directory.builder()
                .path(path)
                .build();

        return createDir(parent, dir);
    }

    private Directory createDir(Directory parent, Directory dir) {
        if (parent != null) {
            dirRepository.save(parent, dir);
        } else {
            dirRepository.save(dir);
        }

        return dir;
    }

    private Image createImage(Directory parent, String path, ImageStatus status) {
        Image img = Image.builder()
                .path(path)
                .status(status)
                .build();
        imgRepository.save(parent, img);
        return img;
    }
}
