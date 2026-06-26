package me.giobyte8.galleries.scanner.metadata.format;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.models.MediaFormat;
import me.giobyte8.galleries.scanner.metadata.dto.ExifToolMimeType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaFormatResolver {
    private final ObjectMapper jMapper;

    public MediaFormat forPath(Path absPath) {
        try {

            // drew library uses magic number detection
            MediaFormat mediaFormat = drewResolver(absPath);

            // If magic numbers didn't work. Fallback to exif tool + extension
            if (MediaFormat.Unknown == mediaFormat) {
                mediaFormat = exifToolResolver(absPath);
            }

            return mediaFormat;
        } catch (IOException e) {
            log.error("Error while detecting file type: {}", e.getMessage(), e);
        }

        return MediaFormat.Unknown;
    }

    private MediaFormat drewResolver(Path absPath)
            throws IOException {
        try (var fIs = new BufferedInputStream(Files.newInputStream(absPath))) {
            FileType fileType = FileTypeDetector.detectFileType(fIs);

            return switch (fileType) {
                case FileType.Jpeg -> MediaFormat.Jpeg;
                case FileType.Png -> MediaFormat.Png;
                case FileType.WebP -> MediaFormat.WebP;
                case FileType.Heif -> MediaFormat.Heic;

                case FileType.Mp4 -> MediaFormat.Mp4;
                case FileType.QuickTime -> MediaFormat.QuickTime;

                default -> MediaFormat.Unknown;
            };
        }
    }

    private MediaFormat exifToolResolver(Path absPath) {
        try {
            var mimeType = exifToolMimeType(absPath);
            var extension = fileExtension(absPath);

            if ("video/mpeg".equals(mimeType) && "mp4".equals(extension)) {
                return MediaFormat.Mp4;
            }
        } catch (InterruptedException | IOException e) {
            log.error("Error while running exiftool: {}", e.getMessage(), e);
        }

        return MediaFormat.Unknown;
    }

    private String exifToolMimeType(Path absPath)
            throws IOException, InterruptedException {

        // Assembly command:
        // > exiftool -j -fast -"MIME*" <filepath>
        var pb = new ProcessBuilder(
                "exiftool", "-j", "-fast", "-MIME*",
                absPath.toString()
        );

        var process = pb.start();
        try (var is = process.getInputStream()) {
            List<ExifToolMimeType> results = jMapper.readValue(
                    is,
                    new TypeReference<>() {
                    }
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

            return results.getFirst().getMimeType();
        }
    }

    private String fileExtension(Path absPath) {
        String sPath = absPath.toString();
        return sPath
                .substring(sPath.lastIndexOf(".") + 1)
                .toLowerCase();
    }
}
