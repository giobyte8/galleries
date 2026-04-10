package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.mappers.DirRowMapper;
import me.giobyte8.galleries.persistence.mappers.VideoRowMapper;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.models.Video;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Import({
        DirRowMapper.class,
        VideoRowMapper.class,
})
public class VideoRepositoryTests extends BaseIntegrationTest {

    private static final ZonedDateTime TEST_CAPTURE_DATE_TIME =
            ZonedDateTime.of(
                    2023,
                    6,
                    19,
                    12,
                    22,
                    33,
                    0,
                    ZoneOffset.of("-06:00")
            );

    private static final Instant TEST_CAPTURE_INSTANT =
            TEST_CAPTURE_DATE_TIME.toInstant();

    private static final String TEST_RAW_CAPTURE_DATE_TIME =
            "2023:06:19 18:22:33";

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private DirectoryRepository dirRepository;


    @Test
    void save() {
        Directory parent = Directory.builder()
                .path("test/videos")
                .build();
        dirRepository.save(parent);

        Video video = Video.builder()
                .path("test/videos/lake.mp4")
                .captureDateTime(TEST_CAPTURE_DATE_TIME)
                .captureInstant(TEST_CAPTURE_INSTANT)
                .rawCaptureDateTime(TEST_RAW_CAPTURE_DATE_TIME)
                .gpsLatitude(19.4023d)
                .gpsLongitude(-99.1806d)
                .cameraMaker("Samsung")
                .cameraModel("SM-S918B")
                .build();

        videoRepository.saveAsChild(parent, video);

        var dbVideoOpt = videoRepository.findByPath(video.getPath());
        assertTrue(dbVideoOpt.isPresent());
        assertEquals(
                TEST_CAPTURE_DATE_TIME,
                dbVideoOpt.orElseThrow().getCaptureDateTime()
        );
        assertEquals(
                TEST_CAPTURE_INSTANT,
                dbVideoOpt.orElseThrow().getCaptureInstant()
        );
        assertEquals(
                TEST_RAW_CAPTURE_DATE_TIME,
                dbVideoOpt.orElseThrow().getRawCaptureDateTime()
        );
    }

    @Test
    void saveMissingCaptureDateTime() {
        Directory parent = Directory.builder()
                .path("root/videos")
                .build();
        dirRepository.save(parent);

        Video video = Video.builder()
                .path("root/videos/random.mp4")
                .gpsLatitude(19.4023d)
                .gpsLongitude(-99.1806d)
                .cameraMaker("Samsung")
                .cameraModel("SM-S918B")
                .build();
        videoRepository.saveAsChild(parent, video);

        Video dbVideo = videoRepository
                .findByPath("root/videos/random.mp4")
                .orElseThrow();
        assertNull(dbVideo.getCaptureDateTime());
        assertNull(dbVideo.getCaptureInstant());
        assertNull(dbVideo.getRawCaptureDateTime());
    }

    @Test
    void updateDirChildrenStatus() {
        Directory dir = Directory.builder()
                .path("abc/videos")
                .build();
        dirRepository.save(dir);

        Video video1 = Video.builder()
                .path("abc/videos/video1.mp4")
                .status(MediaFileStatus.AVAILABLE)
                .build();
        videoRepository.saveAsChild(dir, video1);

        Video video2 = Video.builder()
                .path("abc/videos/video2.mov")
                .status(MediaFileStatus.AVAILABLE)
                .build();
        videoRepository.saveAsChild(dir, video2);

        long updatedCount = videoRepository.updateStatusByParent(
                dir,
                MediaFileStatus.NOT_FOUND
        );
        assertEquals(2, updatedCount);

        updatedCount = videoRepository.updateStatusByParent(
                dir,
                MediaFileStatus.NOT_FOUND
        );
        assertEquals(0, updatedCount);
    }

    @Test
    void deleteAndGetPaths() {
        Directory parent = Directory.builder()
                .path("abc/videos")
                .build();
        dirRepository.save(parent);

        var videoPath1 = "abc/videos/video1.mp4";
        var videoPath2 = "abc/videos/video2.mov";
        var videoPath3 = "abc/videos/video3.m4v";

        videoRepository.saveAsChild(
                parent,
                Video.builder()
                        .path(videoPath1)
                        .status(MediaFileStatus.VERIFYING)
                        .build()
        );
        videoRepository.saveAsChild(
                parent,
                Video.builder()
                        .path(videoPath2)
                        .status(MediaFileStatus.VERIFYING)
                        .build()
        );
        videoRepository.saveAsChild(
                parent,
                Video.builder()
                        .path(videoPath3)
                        .status(MediaFileStatus.AVAILABLE)
                        .build()
        );

        var deletedPaths = videoRepository.deleteAndGetPaths(
                parent,
                MediaFileStatus.VERIFYING
        );

        assertEquals(2, deletedPaths.size());
        assertTrue(deletedPaths.contains(videoPath1));
        assertTrue(deletedPaths.contains(videoPath2));
        assertFalse(deletedPaths.contains(videoPath3));
    }
}

