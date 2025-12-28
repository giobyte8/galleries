package me.giobyte8.galleries.scanner.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Data
public class MFMetadata {

    /**
     * Original date and time when the photo was taken.
     * NOTE: The metadata-extractor library converts raw string into
     *       a localized Date using the system timezone.
     */
    private Date datetimeOriginal;

    /**
     * Raw original date and time string when the photo was taken.
     * Raw value as stored in file metadata (no conversion/parsing applied).
     * <br/>
     *
     * This usually represents date and time in the local time zone where
     * photo was taken.
     */
    private String datetimeOriginalRaw;

    /**
     * Time zone offset in format "+HH:MM" or "-HH:MM" (Relative to GMT).
     * <br/>
     *
     * This is taken from the 'original time zone', which should match
     * the time zone where the photo was taken.
     */
    private String tzOffset;

    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;

    private String camMaker;
    private String camModel;

    public Calendar dateTimeOriginal() {
        if (datetimeOriginal == null) return null;

        var cal = Calendar.getInstance();
        cal.setTime(datetimeOriginal);
        return cal;
    }

}
