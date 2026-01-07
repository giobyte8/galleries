package me.giobyte8.galleries.scanner.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.StringValue;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.drew.metadata.exif.ExifDirectoryBase.*;

/**
 * Image metadata extractor using the
 * <a href="https://github.com/drewnoakes/metadata-extractor/"/>
 * metadata-extractor</a> library.
 * <br/>
 *
 * See
 * <a href="https://github.com/drewnoakes/metadata-extractor/wiki/Getting-Started-(Java)">
 * Getting started reference
 * </a>
 * for more details about exif tag reading
 */
@Component
@Slf4j
public class LFSImgMetaExtractor implements ImgMetaExtractor {

    @Override
    public MFMetadata extract(Path absPath) throws IOException {
        InputStream fIs = Files.newInputStream(absPath);
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(fIs);
            MFMetadata mfMeta = new MFMetadata();

            loadDatetimeOriginal(metadata, mfMeta);
            loadDatetimeOriginalRaw(metadata, mfMeta);
            loadTzOffsetOriginal(metadata, mfMeta);
            loadGpsCoordinates(metadata, mfMeta);
            loadCameraMaker(metadata, mfMeta);
            loadCameraModel(metadata, mfMeta);

            return mfMeta;
        } catch (ImageProcessingException e) {
            log.error(
                    "Error while retrieving metadata for: {} - {}",
                    absPath,
                    e.getMessage()
            );
            return null;
        }
    }

    /**
     * Extracts the original datetime the photo was taken.
     * <br/>
     *
     * NOTE: The library converts/parses the raw string into a localized Date
     * using local system timezone.
     */
    private void loadDatetimeOriginal(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean dateFound = new AtomicBoolean(false);

        meta.getDirectoriesOfType(ExifSubIFDDirectory.class)
                .stream()
                .takeWhile(_ -> !dateFound.get())
                .forEach(exifDir -> {
                    if (exifDir.hasTagName(TAG_DATETIME_ORIGINAL)) {
                        Date dateOriginal = exifDir.getDateOriginal();
                        mfMeta.setDatetimeOriginal(dateOriginal);

                        dateFound.set(true);
                    }
                });
    }

    /**
     * Extracts the raw datetime original string as stored in image metadata.
     * No conversion/parsing is applied.
     */
    private void loadDatetimeOriginalRaw(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean dateFound = new AtomicBoolean(false);

        meta.getDirectoriesOfType(ExifSubIFDDirectory.class)
                .stream()
                .takeWhile(_ -> !dateFound.get())
                .forEach(exifDir -> {
                    if (exifDir.hasTagName(TAG_DATETIME_ORIGINAL)) {
                        var dTimeRaw = exifDir.getStringValue(TAG_DATETIME_ORIGINAL);

                        if (Objects.nonNull(dTimeRaw)) {
                            mfMeta.setDatetimeOriginalRaw(dTimeRaw.toString());
                            dateFound.set(true);
                        }
                    }
                });
    }

    private void loadTzOffsetOriginal(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean tzFound = new AtomicBoolean(false);

        meta.getDirectoriesOfType(ExifSubIFDDirectory.class)
                .stream()
                .takeWhile(_ -> !tzFound.get())
                .forEach(exifDir -> {
                    if (exifDir.hasTagName(TAG_TIME_ZONE_ORIGINAL)) {
                        var tzOffset = exifDir.getStringValue(TAG_TIME_ZONE_ORIGINAL);

                        if (Objects.nonNull(tzOffset)) {
                            mfMeta.setTzOffset(tzOffset.toString());
                            tzFound.set(true);
                        }
                    }
                });
    }

    private void loadGpsCoordinates(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean gpsFound = new AtomicBoolean(false);

        meta.getDirectoriesOfType(GpsDirectory.class)
                .stream()
                .takeWhile(_ -> !gpsFound.get())
                .forEach(gpsDir -> {
                    GeoLocation geoLoc = gpsDir.getGeoLocation();
                    if (geoLoc != null) {
                        double lat = geoLoc.getLatitude();
                        double lon = geoLoc.getLongitude();
                        mfMeta.setGpsLatitude(BigDecimal.valueOf(lat));
                        mfMeta.setGpsLongitude(BigDecimal.valueOf(lon));
                        gpsFound.set(true);
                    }
                });
    }

    private void loadCameraMaker(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean camMakerFound = new AtomicBoolean();

        meta.getDirectoriesOfType(ExifIFD0Directory.class)
                .stream()
                .takeWhile(_ -> !camMakerFound.get())
                .forEach(exifDir -> {
                    if (exifDir.hasTagName(TAG_MAKE)) {
                        StringValue camModel = exifDir.getStringValue(TAG_MAKE);
                        if (camModel != null) {
                            mfMeta.setCamMaker(camModel.toString());
                            camMakerFound.set(true);
                        }
                    }
                });
    }

    private void loadCameraModel(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean camModelFound = new AtomicBoolean();

        meta.getDirectoriesOfType(ExifIFD0Directory.class)
                .stream()
                .takeWhile(_ -> !camModelFound.get())
                .forEach(exifDir -> {
                    if (exifDir.hasTagName(TAG_MODEL)) {
                        StringValue camModel = exifDir.getStringValue(TAG_MODEL);
                        if (camModel != null) {
                            mfMeta.setCamModel(camModel.toString());
                            camModelFound.set(true);
                        }
                    }
                });
    }
}
