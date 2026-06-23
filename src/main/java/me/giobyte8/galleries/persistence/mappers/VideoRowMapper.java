package me.giobyte8.galleries.persistence.mappers;

import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.models.Video;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class VideoRowMapper {

    public Map<String, Object> asMap(Video video) {
        Map<String, Object> videoMap = new HashMap<>();
        videoMap.put("path", video.getPath());
        videoMap.put("version", video.getVersion());
        videoMap.put("fileSize", video.getFileSize());
        videoMap.put(
                "lastModified",
                Objects.isNull(video.getLastModified())
                        ? null
                        : OffsetDateTime.ofInstant(
                                video.getLastModified(),
                                ZoneOffset.UTC
                        )
        );

        videoMap.put(
                "captureDateTime",
                Objects.isNull(video.getCaptureDateTime())
                        ? null
                        : video.getCaptureDateTime().toOffsetDateTime()
        );
        videoMap.put(
                "captureInstant",
                Objects.isNull(video.getCaptureInstant())
                        ? null
                        : OffsetDateTime.ofInstant(
                                video.getCaptureInstant(),
                                ZoneOffset.UTC
                        )
        );
        videoMap.put("rawCaptureDateTime", video.getRawCaptureDateTime());

        videoMap.put("gpsLatitude", Objects.isNull(video.getGpsLatitude())
                ? null
                : video.getGpsLatitude()
        );
        videoMap.put("gpsLongitude", Objects.isNull(video.getGpsLongitude())
                ? null
                : video.getGpsLongitude()
        );

        videoMap.put("cameraMaker", video.getCameraMaker());
        videoMap.put("cameraModel", video.getCameraModel());
        videoMap.put("status", video.getStatus().toString());
        videoMap.put("format", video.getFormat().toString());
        return videoMap;
    }

    public Video from(Map<String, Object> videoMap) {
        Number version = (Number) videoMap.get("version");

        Video.VideoBuilder videoBuilder = Video.builder()
                .path((String) videoMap.get("path"))
                .version(version == null ? null : version.longValue())
                .cameraMaker((String) videoMap.get("cameraMaker"))
                .cameraModel((String) videoMap.get("cameraModel"))
                .rawCaptureDateTime(
                        (String) videoMap.get("rawCaptureDateTime")
                )
                .status(
                        MediaFileStatus.valueOf((String) videoMap.get("status"))
                );

        Number fileSize = (Number) videoMap.get("fileSize");
        videoBuilder.fileSize(fileSize == null ? null : fileSize.longValue());

        var lastModifiedObj = videoMap.get("lastModified");
        if (lastModifiedObj instanceof Instant lastModified) {
            videoBuilder.lastModified(lastModified);
        } else if (
                lastModifiedObj instanceof ZonedDateTime zdtLastModified
        ) {
            videoBuilder.lastModified(zdtLastModified.toInstant());
        } else if (
                lastModifiedObj instanceof OffsetDateTime lastModified
        ) {
            videoBuilder.lastModified(lastModified.toInstant());
        }

        var captureDateTimeObj = videoMap.get("captureDateTime");
        if (captureDateTimeObj instanceof ZonedDateTime captureDateTime) {
            videoBuilder.captureDateTime(captureDateTime);
        } else if (
                captureDateTimeObj instanceof OffsetDateTime captureDateTime
        ) {
            videoBuilder.captureDateTime(captureDateTime.toZonedDateTime());
        }

        var captureInstantObj = videoMap.get("captureInstant");
        if (captureInstantObj instanceof Instant captureInstant) {
            videoBuilder.captureInstant(captureInstant);
        } else if (
                captureInstantObj instanceof ZonedDateTime zdtCaptureInstant
        ) {
            videoBuilder.captureInstant(zdtCaptureInstant.toInstant());
        } else if (
                captureInstantObj instanceof OffsetDateTime captureInstant
        ) {
            videoBuilder.captureInstant(captureInstant.toInstant());
        }

        if (Objects.nonNull(videoMap.get("gpsLatitude"))) {
            videoBuilder.gpsLatitude((Double) videoMap.get("gpsLatitude"));
        }

        if (Objects.nonNull(videoMap.get("gpsLongitude"))) {
            videoBuilder.gpsLongitude((Double) videoMap.get("gpsLongitude"));
        }

        if (Objects.nonNull(videoMap.get("format"))) {
            videoBuilder.format(MediaFormat.valueOf((String) videoMap.get("format")));
        }

        return videoBuilder.build();
    }
}


