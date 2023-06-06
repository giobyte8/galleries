package me.giobyte8.galleries.scanner.scanners;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.StringValue;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.drew.metadata.exif.ExifDirectoryBase.*;

@Component
public class LocalFSMetadataExtractor implements MetadataExtractor {
    Logger log = LoggerFactory.getLogger(LocalFSMetadataExtractor.class);

    @Override
    public MFMetadata extract(Path absPath) throws IOException {
        InputStream fIs = Files.newInputStream(absPath);
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(fIs);
            MFMetadata mfMeta = new MFMetadata();

            loadDatetimeOriginal(metadata, mfMeta);
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

    private void loadDatetimeOriginal(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean dateFound = new AtomicBoolean(false);

        meta.getDirectoriesOfType(ExifSubIFDDirectory.class)
                .stream()
                .takeWhile(exifDir -> !dateFound.get())
                .forEach(exifDir -> {
                    if (exifDir.hasTagName(TAG_DATETIME_ORIGINAL)) {
                        Date dateOriginal = exifDir.getDateOriginal();
                        mfMeta.setDatetimeOriginal(dateOriginal);

                        dateFound.set(true);
                    }
                });
    }

    private void loadGpsCoordinates(Metadata meta, MFMetadata mfMeta) {
        AtomicBoolean gpsFound = new AtomicBoolean(false);

        meta.getDirectoriesOfType(GpsDirectory.class)
                .stream()
                .takeWhile(gpsDir -> !gpsFound.get())
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
                .takeWhile(exifDir -> !camMakerFound.get())
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
                .takeWhile(exifDir -> !camModelFound.get())
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
