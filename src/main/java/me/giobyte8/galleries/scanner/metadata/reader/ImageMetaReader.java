package me.giobyte8.galleries.scanner.metadata.reader;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.scanner.metadata.dto.GpsCoordinates;
import me.giobyte8.galleries.scanner.metadata.dto.MediaDateTime;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

import static com.drew.metadata.exif.ExifDirectoryBase.*;

/**
 * Image metadata reader based on metadata-extractor. It exposes the same
 * fields contract as video readers: camera data, coordinates and capture
 * datetime (raw + zoned).
 */
@Slf4j
@RequiredArgsConstructor
public class ImageMetaReader implements MetaReader {
    private static final DateTimeFormatter EXIF_DT_BASE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy:MM:dd HH:mm:ss")
                    .optionalStart()
                    .appendLiteral("a.m.")
                    .optionalEnd()
                    .optionalStart()
                    .appendLiteral("p.m.")
                    .optionalEnd()
                    .toFormatter();

    private static final DateTimeFormatter EXIF_DT_FORMATTER =
            EXIF_DT_BASE_FORMATTER;

    private static final DateTimeFormatter EXIF_DT_TZ_FORMATTER =
            new DateTimeFormatterBuilder()
                    .append(EXIF_DT_BASE_FORMATTER)
                    .optionalStart()
                    .appendOffset("+HH:MM", "+00:00")
                    .optionalEnd()
                    .optionalStart()
                    .appendOffset("+HHMM", "+0000")
                    .optionalEnd()
                    .toFormatter();

    private final MediaFormat format;
    private final Metadata metadata;

    @Override
    public Optional<String> cameraMaker() {
        return metadata.getDirectoriesOfType(ExifIFD0Directory.class)
                .stream()
                .map(dir -> dir.getString(TAG_MAKE))
                .filter(StringUtils::hasText)
                .findFirst();
    }

    @Override
    public Optional<String> cameraModel() {
        return metadata.getDirectoriesOfType(ExifIFD0Directory.class)
                .stream()
                .map(dir -> dir.getString(TAG_MODEL))
                .filter(StringUtils::hasText)
                .findFirst();
    }

    @Override
    public Optional<GpsCoordinates> coordinates() {
        return metadata.getDirectoriesOfType(GpsDirectory.class)
                .stream()
                .map(GpsDirectory::getGeoLocation)
                .filter(Objects::nonNull)
                .map(geo -> new GpsCoordinates(
                        geo.getLatitude(),
                        geo.getLongitude()
                ))
                .findFirst();
    }

    @Override
    public Optional<MediaDateTime> captureDateTime() {
        return captureDateTimeFromExifOriginal()
                .or(this::captureDateTimeFromExifDigitized)
                .or(this::captureDateTimeFromExifDateTime);
    }

    private Optional<MediaDateTime> captureDateTimeFromExifOriginal() {
        return metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)
                .stream()
                .filter(dir -> dir.containsTag(TAG_DATETIME_ORIGINAL))
                .map(dir -> parseCaptureDateTime(
                        dir.getString(TAG_DATETIME_ORIGINAL),
                        dir.getString(TAG_TIME_ZONE_ORIGINAL)
                ))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private Optional<MediaDateTime> captureDateTimeFromExifDigitized() {
        return metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)
                .stream()
                .filter(dir -> dir.containsTag(TAG_DATETIME_DIGITIZED))
                .map(dir -> parseCaptureDateTime(
                        dir.getString(TAG_DATETIME_DIGITIZED),
                        dir.getString(TAG_TIME_ZONE_ORIGINAL)
                ))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private Optional<MediaDateTime> captureDateTimeFromExifDateTime() {
        return metadata.getDirectoriesOfType(ExifIFD0Directory.class)
                .stream()
                .filter(dir -> dir.containsTag(TAG_DATETIME))
                .map(dir -> parseCaptureDateTime(
                        dir.getString(TAG_DATETIME),
                        dir.getString(TAG_TIME_ZONE_ORIGINAL)
                ))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private MediaDateTime parseCaptureDateTime(
            String rawCaptureDate,
            String tzOffset
    ) {
        if (!StringUtils.hasText(rawCaptureDate)) return null;

        var mediaDtBuilder = MediaDateTime.builder()
                .raw(rawCaptureDate);

        try {
            ZonedDateTime captureDatetime;

            // Check if raw value already provides tz info
            if (TimeUtils.containsTz(rawCaptureDate)) {
                captureDatetime = OffsetDateTime
                        .parse(rawCaptureDate, EXIF_DT_TZ_FORMATTER)
                        .toZonedDateTime();
            }

            // Fallback to using tzOffset from dedicated Exif tag
            else if (StringUtils.hasText(tzOffset)) {
                captureDatetime = OffsetDateTime
                        .parse(
                                rawCaptureDate + tzOffset,
                                EXIF_DT_TZ_FORMATTER
                        )
                        .toZonedDateTime();
            }

            // Infer timezone from coordinates if available,
            // otherwise parse it and assume its timezone is UTC
            else {
                var mediaTz = coordinates()
                        .flatMap(TimeUtils::timezoneFor)
                        .orElse(ZoneOffset.UTC);

                captureDatetime = LocalDateTime
                        .parse(rawCaptureDate, EXIF_DT_FORMATTER)
                        .atZone(mediaTz);
            }

            mediaDtBuilder.datetime(captureDatetime);
        }
        catch (DateTimeParseException e) {
            log.warn(
                    "Error parsing image capture datetime: {}",
                    e.getMessage()
            );
        }
        catch (Exception e) {
            log.error(
                    "Error parsing image capture datetime: {}",
                    e.getMessage(),
                    e
            );
        }

        return mediaDtBuilder.build();
    }

    public MediaFormat mediaFormat() {
        return format;
    }
}
