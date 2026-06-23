package me.giobyte8.galleries.scanner.metadata.reader;

import com.drew.metadata.Metadata;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.scanner.metadata.dto.GpsCoordinates;
import me.giobyte8.galleries.scanner.metadata.dto.MediaDateTime;

import java.util.Objects;
import java.util.Optional;

import static com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory.TAG_CREATION_DATE;
import static com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory.TAG_LOCATION_ISO6709;

@Slf4j
@RequiredArgsConstructor
public class QuickTimeMetaReader implements MetaReader {
    private final Metadata metadata;

    @Override
    public Optional<String> cameraMaker() {
        return metadata.getDirectoriesOfType(QuickTimeMetadataDirectory.class)
                .stream()
                .filter(d -> d.containsTag(
                        QuickTimeMetadataDirectory.TAG_MAKE)
                )
                .map(d -> d.getString(
                        QuickTimeMetadataDirectory.TAG_MAKE)
                )
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
    public Optional<String> cameraModel() {
        return metadata.getDirectoriesOfType(QuickTimeMetadataDirectory.class)
                .stream()
                .filter(d -> d.containsTag(
                        QuickTimeMetadataDirectory.TAG_MODEL)
                )
                .map(d -> d.getString(
                        QuickTimeMetadataDirectory.TAG_MODEL)
                )
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
    public Optional<GpsCoordinates> coordinates() {
        return metadata.getDirectoriesOfType(QuickTimeMetadataDirectory.class)
                .stream()
                .filter(d -> d.containsTag(TAG_LOCATION_ISO6709))
                .map(dir -> {
                    var coordinates = dir.getString(TAG_LOCATION_ISO6709);

                    // ISO 6709 Sample value: +19.8242+099.7638+411.633/
                    //   - String end is indicated with "/"
                    //   - Each value is separated by its sign (+ or -)
                    //   - The order of values is: Latitude, Longitude, Altitude

                    // Remove "/" suffix
                    if (coordinates.endsWith("/")) {
                        coordinates = coordinates
                                .substring(0, coordinates.length() - 1);
                    }


                    var latLongAlt = coordinates.split("(?=[+-])");
                    if (latLongAlt.length >= 2) {
                        var lat = latLongAlt[0];
                        var lon = latLongAlt[1];

                        var latD = Double.parseDouble(lat);
                        var lonD = Double.parseDouble(lon);

                        return new GpsCoordinates(latD, lonD);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
    public Optional<MediaDateTime> captureDateTime() {
        return metadata.getDirectoriesOfType(QuickTimeMetadataDirectory.class)
                .stream()
                .filter(dir -> dir.containsTag(TAG_CREATION_DATE))
                .map(dir -> {
                    try {
                        var mDateTimeBuilder = MediaDateTime.builder();

                        // Apple stores datetime and timezone at capture location
                        // in the 'Creation date' tag
                        var rawCreationTime = dir.getString(TAG_CREATION_DATE);
                        mDateTimeBuilder.raw(rawCreationTime);

                        // Even though the file stores datetime local to
                        // capture location, 'drewnoakes' library returns it
                        // in system local timezone, hence, we convert it back
                        // to capture location timezone
                        var captureDatetime = TimeUtils.fromSystemTzIntoMediaTz(
                                dir.getDate(TAG_CREATION_DATE),
                                rawCreationTime
                        );
                        mDateTimeBuilder.datetime(captureDatetime);

                        return mDateTimeBuilder.build();
                    } catch (Exception e) {
                        log.error(
                                "Error while reading creation time " +
                                        "from QuickTime metadata - {}",
                                e.getMessage(),
                                e
                        );

                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
    public MediaFormat mediaFormat() {
        return MediaFormat.QuickTime;
    }
}
