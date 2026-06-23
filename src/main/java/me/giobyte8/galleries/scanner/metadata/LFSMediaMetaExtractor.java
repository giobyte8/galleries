package me.giobyte8.galleries.scanner.metadata;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import me.giobyte8.galleries.scanner.exceptions.MediaProcessingException;
import me.giobyte8.galleries.scanner.exceptions.UnsupportedMediaFileException;
import me.giobyte8.galleries.scanner.metrics.Metric;
import me.giobyte8.galleries.scanner.metrics.MetricTag;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import me.giobyte8.galleries.scanner.metadata.reader.ImageMetaReader;
import me.giobyte8.galleries.scanner.metadata.reader.Mp4ExifToolMetaReader;
import me.giobyte8.galleries.scanner.metadata.reader.QuickTimeMetaReader;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/// Retrieves metadata from image and video media files
@Component
@Slf4j
@RequiredArgsConstructor
public class LFSMediaMetaExtractor implements MediaMetaExtractor {
    private final ObjectMapper jMapper;
    private final MetricsService metricsSvc;

    @Override
    public MFMetadata extract(Path absPath) throws IOException, MediaProcessingException {
        long startNs = System.nanoTime();
        String mediaType = null;

        try (var fIs = new BufferedInputStream(Files.newInputStream(absPath))) {
            FileType fileType = FileTypeDetector.detectFileType(fIs);
            mediaType = mediaTypeFor(fileType);

            switch (fileType) {
                case FileType.Jpeg, FileType.Heif, FileType.Png, FileType.WebP -> {
                    Metadata meta = ImageMetadataReader.readMetadata(fIs);
                    return new ImageMetaReader(fileType, meta).read();
                }

                case FileType.Mp4 -> {
                    return Mp4ExifToolMetaReader
                            .forFile(absPath, jMapper)
                            .read();
                }

                case FileType.QuickTime -> {
                    Metadata meta = ImageMetadataReader.readMetadata(fIs);
                    return new QuickTimeMetaReader(meta).read();
                }

                default -> throw new UnsupportedMediaFileException(
                        fileType.toString()
                );
            }
        } catch (ImageProcessingException | UnsupportedMediaFileException e) {
            throw new MediaProcessingException("Unable to retrieve metadata.", e);
        } finally {
            if (mediaType != null) {
                metricsSvc.record(
                        Metric.SCAN_META_EXTRACTION,
                        startNs,
                        MetricTag.MEDIA_TYPE.getName(),
                        mediaType
                );
            }
        }
    }

    private String mediaTypeFor(FileType fileType) {
        return switch (fileType) {
            case FileType.Jpeg, FileType.Heif, FileType.Png, FileType.WebP ->
                    "image";
            case FileType.Mp4, FileType.QuickTime -> "video";
            default -> null;
        };
    }
}
