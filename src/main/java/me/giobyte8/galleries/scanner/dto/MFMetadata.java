package me.giobyte8.galleries.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MFMetadata {

    /**
     * Represents the instant and the timezone at which
     * the media was captured
     */
    private ZonedDateTime captureDateTime;

    /**
     * Capture date time value as is stored in the media file. No conversion
     * nor parsing is applied.
     * <p>
     * This value usually represents date and time in the local timezone where
     * the photo/video was captured.
     */
    private String rawCaptureDateTime;


    /**
     * Original date and time when the photo was taken.
     * NOTE: The metadata-extractor library converts raw string into
     *       a localized Date using the system timezone.
     */
    @Deprecated
    private Date datetimeOriginal;

    /**
     * Raw original date and time string when the photo was taken.
     * Raw value as stored in file metadata (no conversion/parsing applied).
     * <br/>
     *
     * This usually represents date and time in the local time zone where
     * photo was taken.
     */
    @Deprecated
    private String datetimeOriginalRaw;

    /**
     * Time zone offset in format "+HH:MM" or "-HH:MM" (Relative to GMT).
     * <br/>
     *
     * This is taken from the 'original time zone', which should match
     * the time zone where the photo was taken.
     */
    @Deprecated
    private String tzOffset;

    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;

    private String camMaker;
    private String camModel;

    @Deprecated
    public Calendar dateTimeOriginal() {
        if (datetimeOriginal == null) return null;

        var cal = Calendar.getInstance();
        cal.setTime(datetimeOriginal);
        return cal;
    }
}
