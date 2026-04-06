package me.giobyte8.galleries.scanner.metadata.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.metadata.dto.ExifToolMetadata;
import me.giobyte8.galleries.scanner.metadata.dto.GpsCoordinates;
import me.giobyte8.galleries.scanner.metadata.dto.MediaDateTime;
import org.springframework.util.CollectionUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class Mp4ExifToolMetaReader implements MetaReader {
    private final ExifToolMetadata metadata;

    public static Mp4ExifToolMetaReader forFile(
            Path absPath,
            ObjectMapper jMapper
    ) throws IOException {

        // --- ExifTool Command Assembly ---
        // -j:       Output in JSON format (essential for Jackson parsing).
        // -n:       Format coordinates as decimals (e.g., 19.4326) instead
        //           of DMS (19 deg 25'...).
        //           Makes the data ready for map APIs or database storage.
        // -fast:    Optimization flag. Only reads file headers and skips the
        //           video bitstream entirely. Reduces I/O and CPU usage.
        // -Make:    Extracts the manufacturer (e.g., Apple, Samsung).
        // -*Model*: Wildcard to catch "Model", "ModelName", or "SamsungModel"
        //           tags used differently by Android and iOS.
        // -GPS...:  Requesting specific Latitude/Longitude fields.
        // -...Date: Requesting both Apple-specific (CreationDate) and
        //           standard (CreateDate) tags to ensure a timestamp is always
        //           found regardless of the device.
        //
        // NOTE FOR DEV/DEBUGGING:
        // 1. To debug group origins (e.g., [Keys] vs [UserData]), add "-G" in the terminal.
        // 2. To see human-readable output in the terminal, remove "-j".
        //
        // Full Command:
        // exiftool -j -n -fast -Make -*Model* -GPSLatitude -GPSLongitude \
        //   -CreationDate -CreateDate <filepath>
        //
        ProcessBuilder pb = new ProcessBuilder(
                "exiftool",
                "-j", "-n", "-fast",
                "-Make", "-*Model*",
                "-GPSLatitude", "-GPSLongitude",
                "-CreationDate", "-CreateDate",
                absPath.toString()
        );

        Process process = pb.start();

        // Read process output
        try (InputStream is = process.getInputStream()) {
            List<ExifToolMetadata> results = jMapper.readValue(
                    is,
                    new TypeReference<>() { }
            );

            // Usually process will finish almost immediately, but for edge
            // cases we set up a timeout of 5s max, if process doesn't finish
            // in that time window, we throw an exception
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {

                // Read error stream for debugging
                String error = new String(process.getErrorStream().readAllBytes());
                throw new IOException("ExifTool failed: " + error);
            }

            // If no results were retrieved by exiftool
            if (CollectionUtils.isEmpty(results)) {
                throw new IOException(
                        "ExifTool returned empty metadata for file: " + absPath
                );
            }

            ExifToolMetadata metadata = results.getFirst();
            return new Mp4ExifToolMetaReader(metadata);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> cameraMaker() {
        return metadata.resolvedCameraMaker();
    }

    @Override
    public Optional<String> cameraModel() {
        return metadata.resolvedCameraModel();
    }

    @Override
    public Optional<GpsCoordinates> coordinates() {
        if (Objects.nonNull(metadata.getLatitude())) {
            var coordinates = new GpsCoordinates(
                    metadata.getLatitude(),
                    metadata.getLongitude()
            );

            return Optional.of(coordinates);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MediaDateTime> datetime() {
        // TODO Implement parsing/processing logic
        return Optional.empty();
    }
}
