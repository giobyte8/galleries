package me.giobyte8.galleries.scanner.model;

import lombok.Builder;
import lombok.Data;
import me.giobyte8.galleries.scanner.dto.MFMetadata;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Data
@Builder
public class Image {

    private String path;
    private String contentHash;

    private LocalDateTime datetimeOriginal;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;

    private String cameraMaker;
    private String cameraModel;

    @Builder.Default
    private ImageStatus status = ImageStatus.AVAILABLE;

    public void setMetadata(MFMetadata meta) {
        gpsLatitude = meta.getGpsLatitude();
        gpsLongitude = meta.getGpsLongitude();
        cameraMaker = meta.getCamMaker();
        cameraModel = meta.getCamModel();

        if (Objects.nonNull(meta.getDatetimeOriginal())) {
            datetimeOriginal = LocalDateTime.ofInstant(
                    meta.getDatetimeOriginal().toInstant(),
                    ZoneId.systemDefault()
            );
        }
    }
}
