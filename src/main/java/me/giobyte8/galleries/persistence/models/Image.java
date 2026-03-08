package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Data
@Builder
@Node
public class Image {

    @Id
    private String path;

    @Version
    private Long version;

    private String contentHash;

    private LocalDateTime datetimeOriginal;
    private Double gpsLatitude;
    private Double gpsLongitude;

    private String cameraMaker;
    private String cameraModel;

    @Builder.Default
    private ImageStatus status = ImageStatus.AVAILABLE;

    public void setMetadata(MFMetadata meta) {
        cameraMaker = meta.getCamMaker();
        cameraModel = meta.getCamModel();

        if (Objects.nonNull(meta.getGpsLatitude())) {
            gpsLatitude = meta.getGpsLatitude().doubleValue();
        }

        if (Objects.nonNull(meta.getGpsLongitude())) {
            gpsLongitude = meta.getGpsLongitude().doubleValue();
        }

        if (Objects.nonNull(meta.getDatetimeOriginal())) {
            datetimeOriginal = LocalDateTime.ofInstant(
                    meta.getDatetimeOriginal().toInstant(),
                    ZoneId.systemDefault()
            );
        }
    }
}
