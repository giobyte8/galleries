package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ImgRowMapper {

    public Map<String, Object> asMap(Image image) {
        Map<String, Object> imgMap = new HashMap<>();
        imgMap.put("path", image.getPath());
        imgMap.put("contentHash", image.getContentHash());

        // Add it to map even if its value is null
        imgMap.put("datetimeOriginal", image.getDatetimeOriginal());

        imgMap.put("gpsLatitude", Objects.isNull(image.getGpsLatitude())
                ? null
                : image.getGpsLatitude().doubleValue()
        );
        imgMap.put("gpsLongitude", Objects.isNull(image.getGpsLongitude())
                ? null
                : image.getGpsLongitude().doubleValue()
        );

        imgMap.put("cameraMaker", image.getCameraMaker());
        imgMap.put("cameraModel", image.getCameraModel());
        imgMap.put("status", image.getStatus().toString());
        return imgMap;
    }

    public Image from(Map<String, Object> imgMap) {
        Image.ImageBuilder imgBuilder = Image.builder()
                .path((String) imgMap.get("path"))
                .contentHash((String) imgMap.get("contentHash"))
                .cameraMaker((String) imgMap.get("cameraMaker"))
                .cameraModel((String) imgMap.get("cameraModel"))
                .status(ImageStatus.valueOf((String) imgMap.get("status")));

        if (Objects.nonNull(imgMap.get("datetimeOriginal"))) {
            imgBuilder.datetimeOriginal(
                    (LocalDateTime) imgMap.get("datetimeOriginal")
            );
        }

        if (Objects.nonNull(imgMap.get("gpsLatitude"))) {
            imgBuilder.gpsLatitude(
                    BigDecimal.valueOf((Double) imgMap.get("gpsLatitude"))
            );
        }

        if (Objects.nonNull(imgMap.get("gpsLongitude"))) {
            imgBuilder.gpsLongitude(
                    BigDecimal.valueOf((Double) imgMap.get("gpsLongitude"))
            );
        }

        return imgBuilder.build();
    }
}
