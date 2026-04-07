package me.giobyte8.galleries.scanner.metadata.reader;

import me.giobyte8.galleries.scanner.metadata.dto.GpsCoordinates;
import net.iakovlev.timeshape.TimeZoneEngine;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

public class TimeUtils {

    // Geo dataset is relatively expensive to load, hence initialize it only
    // if/when timezone lookup from GPS becomes necessary.
    private static volatile TimeZoneEngine tzEngine;

    private static TimeZoneEngine tzEngine() {
        if (tzEngine == null) {
            synchronized (TimeUtils.class) {
                if (tzEngine == null) {
                    tzEngine = TimeZoneEngine.initialize();
                }
            }
        }

        return tzEngine;
    }

    public static Optional<ZoneId> timezoneFor(GpsCoordinates coordinates) {
        return tzEngine().query(
                coordinates.latitude(),
                coordinates.longitude()
        );
    }

    public static boolean containsTz(String rawDatetime) {
        return extractTzOffset(rawDatetime).isPresent();
    }

    private static Optional<String> extractTzOffset(String rawDatetime) {
        if (!rawDatetime.contains("+") && !rawDatetime.contains("-")) {
            return Optional.empty();
        }

        var tzInfo = rawDatetime.substring(
                Math.max(
                        rawDatetime.lastIndexOf("+"),
                        rawDatetime.lastIndexOf("-")
                )
        );

        // Check for "+HH:MM" or "-HH:MM" format
        if (tzInfo.matches("^[+-]\\d{2}:\\d{2}$")) {
            return Optional.of(tzInfo);
        }

        // Check for "+HHMM" or "-HHMM" format
        if (tzInfo.matches("^[+-]\\d{4}$")) {
            return Optional.of(
                    tzInfo.substring(0, 3) + ":" + tzInfo.substring(3)
            );
        }

        return Optional.empty();
    }

    /**
     * Media files have different approaches when representing creation
     * datetime in its metadata. Is not always clear if timezone info is
     * provided or not.
     * <p>
     * This method tries to infer the timezone from a raw datetime value, and
     * if timezone is not found, fallback to UTC.
     * <p>
     * Input values are expected to follow one of following formats:
     * - "... +HH:MM" (e.g., "2024-01-01T12:00:00+02:00")
     * - "... -HH:MM" (e.g., "2024-01-01T12:00:00-06:00")
     *
     * @param rawDatetime Plain value as retrieved from media file metadata
     * @return Detected timezone or UTC default
     */
    public static TimeZone getTimeZone(String rawDatetime) {

        // Default to UTC timezone offset
        var tzOffset = extractTzOffset(rawDatetime).orElse("+00:00");

        return TimeZone.getTimeZone(ZoneOffset.of(tzOffset));
    }

    /**
     * Apparently some libraries return media file datetime adjusted to system
     * timezone (Such as drewnoakes metadata-extractor). This method extracts
     * timezone info from raw media and applies it to 'systemTzDate' param.
     *
     * @param systemTzDate Datetime adjusted to system's default timezone
     * @param rawMediaDatetime Raw datetime as stored in media file
     * @return Datetime adjusted to timezone specified in raw datetime
     */
    public static ZonedDateTime fromSystemTzIntoMediaTz(
            Date systemTzDate,
            String rawMediaDatetime) {
        ZonedDateTime dtAtSystemTz = systemTzDate
                .toInstant()
                .atZone(ZoneOffset.systemDefault());

        TimeZone mediaTz = TimeUtils.getTimeZone(rawMediaDatetime);
        return dtAtSystemTz.withZoneSameInstant(mediaTz.toZoneId());
    }
}
