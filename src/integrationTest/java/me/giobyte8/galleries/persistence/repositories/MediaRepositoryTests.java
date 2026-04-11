package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.mappers.DirRowMapper;
import me.giobyte8.galleries.persistence.mappers.ImgRowMapper;
import me.giobyte8.galleries.persistence.mappers.MediaRowMapper;
import me.giobyte8.galleries.persistence.mappers.VideoRowMapper;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.models.Video;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        DirRowMapper.class,
        ImgRowMapper.class,
        MediaRowMapper.class,
        VideoRowMapper.class,
})
public class MediaRepositoryTests extends BaseIntegrationTest {

    private static final ZonedDateTime TEST_CAPTURE_DATE_TIME =
            ZonedDateTime.of(
                    2024, 3, 15,
                    10, 30, 0, 0,
                    ZoneOffset.of("-06:00")
            );
    private static final Instant TEST_CAPTURE_INSTANT =
            TEST_CAPTURE_DATE_TIME.toInstant();

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private DirectoryRepository dirRepository;

    @Autowired
    private ImageRepository imgRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Test
    void findAllMediaByParentIdWithoutData() {
        var parent = createDir("root/");

        var page = mediaRepository.findAllMediaByParentId(
                parent.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void findAllMediaByParentIdOnlyImages() {
        var parent = createDir("images-only/");
        createImage(parent, "images-only/img1.jpg", MediaFileStatus.AVAILABLE);
        createImage(parent, "images-only/img2.jpg", MediaFileStatus.NOT_FOUND);

        var page = mediaRepository.findAllMediaByParentId(
                parent.getId(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "path"))
        );

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());

        var first = page.getContent().getFirst();
        assertEquals("IMAGE", first.mediaType());
        assertEquals(MediaFileStatus.AVAILABLE, first.status());
        assertEquals("images-only/img1.jpg", first.path());
        assertNull(first.captureInstant());
        assertNull(first.captureDateTime());
        assertNull(first.cameraMaker());

        var second = page.getContent().get(1);
        assertEquals("IMAGE", second.mediaType());
        assertEquals(MediaFileStatus.NOT_FOUND, second.status());
    }

    @Test
    void findAllMediaByParentIdOnlyVideos() {
        var parent = createDir("videos-only/");
        createVideo(parent, "videos-only/video1.mp4", MediaFileStatus.AVAILABLE);
        createVideo(parent, "videos-only/video2.mov", MediaFileStatus.NOT_FOUND);

        var page = mediaRepository.findAllMediaByParentId(
                parent.getId(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "path"))
        );

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals("VIDEO", page.getContent().get(0).mediaType());
        assertEquals(MediaFileStatus.AVAILABLE, page.getContent().get(0).status());
        assertEquals("VIDEO", page.getContent().get(1).mediaType());
        assertEquals(MediaFileStatus.NOT_FOUND, page.getContent().get(1).status());
    }

    @Test
    void findAllMediaByParentIdMixedMedia() {
        var parent = createDir("mixed/");
        createImage(parent, "mixed/img1.jpg", MediaFileStatus.AVAILABLE);
        createVideo(parent, "mixed/video1.mp4", MediaFileStatus.AVAILABLE);
        createImage(parent, "mixed/img2.jpg", MediaFileStatus.NOT_FOUND);
        createVideo(parent, "mixed/video2.mov", MediaFileStatus.VERIFYING);

        var firstPage = mediaRepository.findAllMediaByParentId(
                parent.getId(),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "path"))
        );
        assertEquals(4, firstPage.getTotalElements());
        assertEquals(2, firstPage.getContent().size());

        var secondPage = mediaRepository.findAllMediaByParentId(
                parent.getId(),
                PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "path"))
        );
        assertEquals(4, secondPage.getTotalElements());
        assertEquals(2, secondPage.getContent().size());
    }

    @Test
    void findAllMediaByParentIdMapsAllFields() {
        var parent = createDir("rich/");
        Image img = Image.builder()
                .path("rich/photo.jpg")
                .status(MediaFileStatus.AVAILABLE)
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime("2024:03:15 10:30:00")
                .gpsLatitude(19.4023)
                .gpsLongitude(-99.1806)
                .cameraMaker("Canon")
                .cameraModel("EOS R5")
                .fileSize(4096L)
                .build();
        imgRepository.saveAsChild(parent, img);

        var page = mediaRepository.findAllMediaByParentId(
                parent.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
        var item = page.getContent().getFirst();

        assertEquals("rich/photo.jpg", item.path());
        assertEquals("IMAGE", item.mediaType());
        assertEquals(MediaFileStatus.AVAILABLE, item.status());
        assertEquals(4096L, item.fileSize());
        assertNotNull(item.captureInstant());
        assertNotNull(item.captureDateTime());
        assertEquals("2024:03:15 10:30:00", item.rawCaptureDateTime());
        assertEquals(19.4023, item.gpsLatitude());
        assertEquals(-99.1806, item.gpsLongitude());
        assertEquals("Canon", item.cameraMaker());
        assertEquals("EOS R5", item.cameraModel());
        assertNotNull(item.version());
    }

    @Test
    void findAllMediaByParentIdRecursivelyMixedMedia() {
        var root = createDir("recursive/");
        var nested = createDir(root, "recursive/nested/");

        createImage(root, "recursive/root-img.jpg",
                MediaFileStatus.AVAILABLE);
        createVideo(root, "recursive/root-video.mp4",
                MediaFileStatus.AVAILABLE);
        createImage(nested, "recursive/nested/nested-img.jpg",
                MediaFileStatus.NOT_FOUND);
        createVideo(nested, "recursive/nested/nested-video.mov",
                MediaFileStatus.VERIFYING);

        var page = mediaRepository.findAllMediaByParentIdRecursively(
                root.getId(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "path"))
        );

        assertEquals(4, page.getTotalElements());
        assertEquals(4, page.getContent().size());

        long images = page.getContent().stream()
                .filter(item -> "IMAGE".equals(item.mediaType()))
                .count();
        long videos = page.getContent().stream()
                .filter(item -> "VIDEO".equals(item.mediaType()))
                .count();

        assertEquals(2, images);
        assertEquals(2, videos);
    }

    private Directory createDir(String path) {
        Directory dir = Directory.builder()
                .path(path)
                .build();
        return dirRepository.save(dir);
    }

    @SuppressWarnings("SameParameterValue")
    private Directory createDir(Directory parent, String path) {
        Directory dir = Directory.builder()
                .path(path)
                .build();
        return dirRepository.saveAsChild(parent, dir).orElseThrow();
    }

    @SuppressWarnings("UnusedReturnValue")
    private Image createImage(
            Directory parent,
            String path,
            MediaFileStatus status
    ) {
        Image img = Image.builder()
                .path(path)
                .status(status)
                .build();
        imgRepository.saveAsChild(parent, img);
        return img;
    }

    @SuppressWarnings("UnusedReturnValue")
    private Video createVideo(
            Directory parent,
            String path,
            MediaFileStatus status
    ) {
        Video video = Video.builder()
                .path(path)
                .status(status)
                .build();
        videoRepository.saveAsChild(parent, video);
        return video;
    }
}


