package me.giobyte8.galleries.scanner.metadata.reader;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.metadata.dto.GpsCoordinates;
import me.giobyte8.galleries.scanner.metadata.dto.MediaDateTime;

import java.util.Objects;
import java.util.Optional;

import static com.drew.metadata.mp4.Mp4Directory.TAG_CREATION_TIME;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public class Mp4MetaReader implements MetaReader {
    private final Metadata metadata;

    @Override
    public Optional<String> cameraMaker() {
        return Optional.empty();
    }

    @Override
    public Optional<String> cameraModel() {
        return Optional.empty();
    }

    @Override
    public Optional<GpsCoordinates> coordinates() {
        return metadata.getDirectoriesOfType(Mp4Directory.class)
                .stream()

                // Directory should contain both LAT and LON
                .filter(dir -> dir.containsTag(Mp4Directory.TAG_LATITUDE))
                .filter(dir -> dir.containsTag(Mp4Directory.TAG_LONGITUDE))

                // Parse values into coordinates
                .map(dir -> {
                    try {
                        var lat = dir.getDouble(Mp4Directory.TAG_LATITUDE);
                        var lon = dir.getDouble(Mp4Directory.TAG_LONGITUDE);

                        return new GpsCoordinates(lat, lon);
                    } catch (MetadataException e) {
                        log.error(
                                "Error while reading GPS coordinates " +
                                        "from MP4 metadata - {}",
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
    public Optional<MediaDateTime> captureDateTime() {
        return metadata.getDirectoriesOfType(Mp4Directory.class)
                .stream()
                .filter(dir -> dir.containsTag(TAG_CREATION_TIME))
                .map(dir -> {
                    try {
                        var mDateTimeBuilder = MediaDateTime.builder();

                        var rawCreationTime = dir.getString(TAG_CREATION_TIME);
                        mDateTimeBuilder.raw(rawCreationTime);

                        var captureDatetime = TimeUtils.fromSystemTzIntoMediaTz(
                                dir.getDate(TAG_CREATION_TIME),
                                rawCreationTime
                        );
                        mDateTimeBuilder.datetime(captureDatetime);

                        return mDateTimeBuilder.build();
                    } catch (Exception e) {
                        log.error(
                                "Error while reading creation time " +
                                        "from MP4 metadata - {}",
                                e.getMessage(),
                                e
                        );

                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }
}
