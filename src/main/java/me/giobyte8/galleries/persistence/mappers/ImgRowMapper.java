package me.giobyte8.galleries.persistence.mappers;

import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ImgRowMapper {

    public Map<String, Object> asMap(Image image) {
        Map<String, Object> imgMap = new HashMap<>();
        imgMap.put("path", image.getPath());
        imgMap.put("version", image.getVersion());
        imgMap.put("contentHash", image.getContentHash());

        // Add it to map even if its value is null
        imgMap.put("captureDateTime", image.getCaptureDateTime());
        imgMap.put(
                "captureInstant",
                Objects.isNull(image.getCaptureInstant())
                        ? null
                        : OffsetDateTime.ofInstant(
                                image.getCaptureInstant(),
                                ZoneOffset.UTC
                        )
        );
        imgMap.put("rawCaptureDateTime", image.getRawCaptureDateTime());

        imgMap.put("gpsLatitude", Objects.isNull(image.getGpsLatitude())
                ? null
                : image.getGpsLatitude()
        );
        imgMap.put("gpsLongitude", Objects.isNull(image.getGpsLongitude())
                ? null
                : image.getGpsLongitude()
        );

        imgMap.put("cameraMaker", image.getCameraMaker());
        imgMap.put("cameraModel", image.getCameraModel());
        imgMap.put("status", image.getStatus().toString());
        return imgMap;
    }

    public Image from(Map<String, Object> imgMap) {
        Number version = (Number) imgMap.get("version");

        Image.ImageBuilder imgBuilder = Image.builder()
                .path((String) imgMap.get("path"))
                .version(version == null ? null : version.longValue())
                .contentHash((String) imgMap.get("contentHash"))
                .cameraMaker((String) imgMap.get("cameraMaker"))
                .cameraModel((String) imgMap.get("cameraModel"))
                .rawCaptureDateTime((String) imgMap.get("rawCaptureDateTime"))
                .status(MediaFileStatus.valueOf((String) imgMap.get("status")));

        var captureDateTimeObj = imgMap.get("captureDateTime");
        if (captureDateTimeObj instanceof ZonedDateTime captureDateTime) {
            imgBuilder.captureDateTime(captureDateTime);
        } else if (
                captureDateTimeObj instanceof OffsetDateTime captureDateTime
        ) {
            imgBuilder.captureDateTime(captureDateTime.toZonedDateTime());
        }

        var captureInstantObj = imgMap.get("captureInstant");
        if (captureInstantObj instanceof Instant captureInstant) {
            imgBuilder.captureInstant(captureInstant);
        } else if (captureInstantObj instanceof ZonedDateTime zdtCaptureInstant) {
            imgBuilder.captureInstant(zdtCaptureInstant.toInstant());
        } else if (
                captureInstantObj instanceof OffsetDateTime captureInstant
        ) {
            imgBuilder.captureInstant(captureInstant.toInstant());
        }

        if (Objects.nonNull(imgMap.get("gpsLatitude"))) {
            imgBuilder.gpsLatitude((Double) imgMap.get("gpsLatitude"));
        }

        if (Objects.nonNull(imgMap.get("gpsLongitude"))) {
            imgBuilder.gpsLongitude((Double) imgMap.get("gpsLongitude"));
        }

        return imgBuilder.build();
    }
}
