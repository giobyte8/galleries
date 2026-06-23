package me.giobyte8.galleries.persistence.models;

import lombok.Builder;
import lombok.Data;
import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

@Data
@Builder
@Node
public class Video {

    @Id
    private String path;

    @Version
    private Long version;

    private Long fileSize;

    private Instant lastModified;

    /// Represents the date and time when this Video was captured
    /// at the timezone where it was captured.
    private ZonedDateTime captureDateTime;

    /// Represents the instant in time when this Video was captured
    /// Useful for querying and sorting
    private Instant captureInstant;

    /// The capture date time as it was read from the video metadata,
    /// without any parsing or timezone conversion.
    private String rawCaptureDateTime;

    private Double gpsLatitude;
    private Double gpsLongitude;

    private String cameraMaker;
    private String cameraModel;

    @Builder.Default
    private MediaFileStatus status = MediaFileStatus.AVAILABLE;

    @Builder.Default
    private MediaFormat format = MediaFormat.Unknown;

    public void setMetadata(MFMetadata meta) {
        cameraMaker = meta.getCamMaker();
        cameraModel = meta.getCamModel();

        if (Objects.nonNull(meta.getGpsLatitude())) {
            gpsLatitude = meta.getGpsLatitude().doubleValue();
        }

        if (Objects.nonNull(meta.getGpsLongitude())) {
            gpsLongitude = meta.getGpsLongitude().doubleValue();
        }

        captureDateTime = meta.getCaptureDateTime();
        rawCaptureDateTime = meta.getRawCaptureDateTime();

        if (Objects.nonNull(captureDateTime)) {
            captureInstant = captureDateTime.toInstant();
        } else {
            captureInstant = null;
        }

        format = meta.getFormat();
    }
}

