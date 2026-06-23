package me.giobyte8.galleries.scanner.metadata.reader;

import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import me.giobyte8.galleries.scanner.metadata.dto.GpsCoordinates;
import me.giobyte8.galleries.scanner.metadata.dto.MediaDateTime;

import java.math.BigDecimal;
import java.util.Optional;

public interface MetaReader {

    Optional<String> cameraMaker();

    Optional<String> cameraModel();

    Optional<GpsCoordinates> coordinates();

    Optional<MediaDateTime> captureDateTime();

    MediaFormat mediaFormat();

    default MFMetadata read() {
        var mfMetaBuilder = MFMetadata.builder();

        this.coordinates().ifPresent(coordinates -> {
            BigDecimal lat = BigDecimal.valueOf(coordinates.latitude());
            BigDecimal lon = BigDecimal.valueOf(coordinates.longitude());

            mfMetaBuilder
                    .gpsLatitude(lat)
                    .gpsLongitude(lon);
        });

        this.cameraMaker().ifPresent(mfMetaBuilder::camMaker);
        this.cameraModel().ifPresent(mfMetaBuilder::camModel);

        this.captureDateTime().ifPresent(mDateTime -> {
            mfMetaBuilder.rawCaptureDateTime(mDateTime.raw());
            mfMetaBuilder.captureDateTime(mDateTime.datetime());
        });

        mfMetaBuilder.format(mediaFormat());

        return mfMetaBuilder.build();
    }
}
