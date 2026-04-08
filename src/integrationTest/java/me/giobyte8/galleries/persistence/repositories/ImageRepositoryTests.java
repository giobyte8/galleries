package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.mappers.DirRowMapper;
import me.giobyte8.galleries.persistence.mappers.ImgRowMapper;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.ImageStatus;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Import({
        DirRowMapper.class,
        ImgRowMapper.class,
})
public class ImageRepositoryTests extends BaseIntegrationTest {

    private static final ZonedDateTime TEST_CAPTURE_DATE_TIME =
            ZonedDateTime.of(
                    2022,
                    6,
                    12,
                    13,
                    31,
                    12,
                    0,
                    ZoneOffset.of("-06:00")
            );

    private static final Instant TEST_CAPTURE_INSTANT =
            TEST_CAPTURE_DATE_TIME.toInstant();

    private static final String TEST_RAW_CAPTURE_DATE_TIME =
            "2022:06:12 13:31:12";

    @Autowired
    private ImageRepository imgRepository;

    @Autowired
    private DirectoryRepository dirRepository;

    @Test
    void findByPathAndContentHash() {
        var path = "test/path/image.jpg";
        var contentHash = "test_hash_12345";

        // Create a test directory and image
        Directory parent = Directory.builder()
                .path("test/path")
                .build();
        dirRepository.save(parent);
        var image = Image.builder()
                .path(path)
                .contentHash(contentHash)
                .build();
        imgRepository.saveAsChild(parent, image);

        // Retrieve image by path and content hash
        Image dbImg = imgRepository
                .findByPathAndContentHash(path, contentHash);
        assertEquals(image, dbImg);

        // Try retrieving with updated hash
        Image missingImg = imgRepository
                .findByPathAndContentHash(path, "different_hash");
        assertNull(missingImg);
    }

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
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .build();

        imgRepository.saveAsChild(parent, img);

        // Assert image was saved
        var dbImgOpt = imgRepository.findByPath(img.getPath());
        assertTrue(dbImgOpt.isPresent());
        assertEquals(
                TEST_CAPTURE_DATE_TIME,
                dbImgOpt.orElseThrow().getCaptureDateTime()
        );
        assertEquals(
                TEST_CAPTURE_INSTANT,
                dbImgOpt.orElseThrow().getCaptureInstant()
        );
        assertEquals(
                TEST_RAW_CAPTURE_DATE_TIME,
                dbImgOpt.orElseThrow().getRawCaptureDateTime()
        );

        // Assert image was added to parent dir
        imgRepository.
                findByParentId(parent.getId(), PageRequest.of(0, 10))
                .getContent()
                .stream()
                .filter(i -> i.equals(img))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Saved image should be present in parent dir"
                ));
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
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .build();
        imgRepository.saveAsChild(parent, img);

        Image dbImg = imgRepository.findByPath("random.jpg").orElseThrow();
        assertNull(dbImg.getGpsLatitude());
        assertNull(dbImg.getGpsLongitude());
    }

    @Test
    void saveMissingCaptureDateTime() {
        Directory parent = Directory.builder()
                .path("root/")
                .build();
        dirRepository.save(parent);

        Image img = Image.builder()
                .path("random.jpg")
                .contentHash("12345")
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .build();
        imgRepository.saveAsChild(parent, img);

        Image dbImg = imgRepository.findByPath("random.jpg").orElseThrow();
        assertNull(dbImg.getCaptureDateTime());
        assertNull(dbImg.getCaptureInstant());
        assertNull(dbImg.getRawCaptureDateTime());
    }

    @Test
    void findNonExistent() {
        String path = "test/not/found";
        var imgOpt = imgRepository.findByPath(path);
        assertTrue(imgOpt.isEmpty());
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
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.saveAsChild(dir, img1);

        Image img2 = Image.builder()
                .path("test_image_2.jpg")
                .contentHash("12345")
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.saveAsChild(dir, img2);

        // Verify both images are updated
        long updatedCount = imgRepository.updateStatusByParent(dir, ImageStatus.NOT_FOUND);
        assertEquals(
                2,
                updatedCount,
                "2 Images should be updated"
        );

        // Verify subsequent update affects 0 images
        updatedCount = imgRepository.updateStatusByParent(dir, ImageStatus.NOT_FOUND);
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
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.saveAsChild(parent, img1);

        Image img2 = Image.builder()
                .path("test_image_2.jpg")
                .contentHash("12345")
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.saveAsChild(parent, img2);

        Directory nestedDir = Directory.builder()
                .path("abc/test/nested")
                .recursive(true)
                .build();
        dirRepository.saveAsChild(parent, nestedDir);

        Image nestedImg1 = Image.builder()
                .path("nested_dir_img_1.jpg")
                .contentHash("12345")
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.saveAsChild(nestedDir, nestedImg1);

        Image nestedImg2 = Image.builder()
                .path("nested_dir_img_2.jpg")
                .contentHash("12345")
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.AVAILABLE)
                .build();
        imgRepository.saveAsChild(nestedDir, nestedImg2);

        // Update on 'parent' dir should impact only 2 images
        long updatedCount = imgRepository.updateStatusByParent(parent, ImageStatus.NOT_FOUND);
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
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.NOT_FOUND)
                .build();
        imgRepository.saveAsChild(parent, img1);

        Image img2 = Image.builder()
                .path("test_image_2.jpg")
                .contentHash("12345")
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(1d)
                .gpsLongitude(10d)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .status(ImageStatus.NOT_FOUND)
                .build();
        imgRepository.saveAsChild(parent, img2);

        long deleteCount = imgRepository.deleteByParentAndStatus(parent, ImageStatus.NOT_FOUND);
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

    @Test
    void findByParentPath() {
        prepareTestGraph();
        var dir2 = dirRepository.findByPath("root/dir2/").orElseThrow();
        var dir3 = dirRepository.findByPath("root/dir2/dir3/").orElseThrow();

        var img4 = imgRepository.findByPath("root/dir2/img4.jpg").orElseThrow();
        var img5 = imgRepository.findByPath("root/dir2/img5.jpg").orElseThrow();
        var img6 = imgRepository.findByPath("root/dir2/dir3/img6.jpg").orElseThrow();


        // Retrieve images in dir3, should return only img6:
        //
        //     dir3/
        //       \
        //       img6
        //
        var page = imgRepository.findByParentId(
                dir3.getId(),
                PageRequest.of(
                        0,
                        10,
                        Sort.by(ASC, "i.path")
                )
        );
        assertEquals(1, page.getNumberOfElements());
        assertEquals(1, page.getTotalElements());
        assertEquals(img6, page.getContent().getFirst());


        // Retrieve images in dir2, should return img4 and img5:
        //
        //   -|-----|------dir2/
        //    |     |        \
        //   img4  img5     dir3/
        //
        page = imgRepository.findByParentId(
                dir2.getId(),
                PageRequest.of(
                        1,
                        1,
                        Sort.by(ASC, "i.path")
                )
        );
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2, page.getTotalElements());
        assertEquals(img5, page.getContent().getFirst());


        // Retrieve images in dir2, in descending order by path,
        // should return img4 and img5:
        //
        //   -|-----|------dir2/
        //    |     |        \
        //   img4  img5     dir3/
        //
        page = imgRepository.findByParentId(
                dir2.getId(),
                PageRequest.of(
                        1,
                        1,
                        Sort.by(DESC, "i.path")
                )
        );
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2, page.getTotalElements());
        assertEquals(img4, page.getContent().getFirst());
    }

    @Test
    void findByParentPathRecursively() {
        prepareTestGraph();
        var dir2 = dirRepository.findByPath("root/dir2/").orElseThrow();

        // Retrieve images in dir2 recursively,
        var page = imgRepository.findByParentIdRecursively(
                dir2.getId(),
                PageRequest.of(0, 2)
        );
        assertEquals(2, page.getNumberOfElements());
        assertEquals(3, page.getTotalElements());


        // Retrieve next page
        page = imgRepository.findByParentIdRecursively(
                dir2.getId(),
                page.nextPageable()
        );
        assertEquals(1, page.getNumberOfElements());
        assertEquals(3, page.getTotalElements());
    }

    private void prepareTestGraph() {
        // Graphical representation of tested scenario
        //
        //      |-------|------ root/ ------------|
        //      |       |                         |
        //     img1    dir1/        |-----|------dir2/
        //            /    \        |     |        \
        //          img2  img3     img4  img5     dir3/
        //                                           \
        //                                          img6

        String pathImg1 = "root/img1.jpg";
        String pathImg2 = "root/dir1/img2.jpg";
        String pathImg3 = "root/dir1/img3.jpg";
        String pathImg4 = "root/dir2/img4.jpg";
        String pathImg5 = "root/dir2/img5.jpg";
        String pathImg6 = "root/dir2/dir3/img6.jpg";
        Directory root = createDir("root/");

        // root's children
        createImage(root, pathImg1, ImageStatus.AVAILABLE);
        Directory dir1 = createDir(root, "root/dir1/");
        Directory dir2 = createDir(root, "root/dir2/");

        // dir1's children
        createImage(dir1, pathImg2, ImageStatus.AVAILABLE);
        createImage(dir1, pathImg3, ImageStatus.AVAILABLE);

        // dir2's children
        createImage(dir2, pathImg4, ImageStatus.AVAILABLE);
        createImage(dir2, pathImg5, ImageStatus.AVAILABLE);
        Directory dir3 = createDir(dir2, "root/dir2/dir3/");

        // dir3's child
        createImage(dir3, pathImg6, ImageStatus.AVAILABLE);
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
            dirRepository.saveAsChild(parent, dir);
        } else {
            dirRepository.save(dir);
        }

        return dir;
    }

    @SuppressWarnings("UnusedReturnValue")
    private Image createImage(Directory parent, String path, ImageStatus status) {
        Image img = Image.builder()
                .path(path)
                .status(status)
                .build();
        imgRepository.saveAsChild(parent, img);
        return img;
    }
}
