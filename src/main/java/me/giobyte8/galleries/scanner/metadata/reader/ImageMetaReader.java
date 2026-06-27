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
import me.giobyte8.galleries.scanner.metadata.reader.datetime.DateTimeParsersChain;
import org.springframework.util.StringUtils;

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
    private final DateTimeParsersChain dateTimeParsers;
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

        var dateTime = dateTimeParsers
                .parse(
                        rawCaptureDate,
                        tzOffset,
                        coordinates().orElse(null)
                )
                .orElse(null);

        return MediaDateTime.builder()
                .raw(rawCaptureDate)
                .datetime(dateTime)
                .build();
    }

    public MediaFormat mediaFormat() {
        return format;
    }
}
