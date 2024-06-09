package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImgRowMapper {

    public Map<String, Object> asMap(Image image) {
        Map<String, Object> imgMap = new HashMap<>();
        imgMap.put("path", image.getPath());
        imgMap.put("contentHash", image.getContentHash());
        imgMap.put("datetimeOriginal", image.getDatetimeOriginal());
        imgMap.put("gpsLatitude", image.getGpsLatitude().doubleValue());
        imgMap.put("gpsLongitude", image.getGpsLongitude().doubleValue());
        imgMap.put("cameraMaker", image.getCameraMaker());
        imgMap.put("cameraModel", image.getCameraModel());
        imgMap.put("status", image.getStatus().toString());

        return imgMap;
    }

    public Image from(Map<String, Object> imgMap) {
        return Image.builder()
                .path((String) imgMap.get("path"))
                .contentHash((String) imgMap.get("contentHash"))
                .datetimeOriginal((LocalDateTime) imgMap.get("datetimeOriginal"))
                .gpsLatitude(BigDecimal.valueOf((Double) imgMap.get("gpsLatitude")))
                .gpsLongitude(BigDecimal.valueOf((Double) imgMap.get("gpsLongitude")))
                .cameraMaker((String) imgMap.get("cameraMaker"))
                .cameraModel((String) imgMap.get("cameraModel"))
                .status(ImageStatus.valueOf((String) imgMap.get("status")))
                .build();
    }
}
