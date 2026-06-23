package me.giobyte8.galleries.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.giobyte8.galleries.models.MediaFormat;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MFMetadata {

    /**
     * Represents the datetime and timezone at which the media was captured
     */
    private ZonedDateTime captureDateTime;

    /**
     * Capture date time value as is returned by media file metadata parsing
     * libraries (e.g. exiftool, drewnoakes). No additional conversion nor
     * parsing is applied.
     * <p>
     * This value usually represents date and time in the local timezone where
     * the photo/video was captured.
     */
    private String rawCaptureDateTime;


    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;

    private String camMaker;
    private String camModel;

    private MediaFormat format;
}
