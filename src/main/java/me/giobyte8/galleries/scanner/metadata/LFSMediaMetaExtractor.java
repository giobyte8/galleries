package me.giobyte8.galleries.scanner.metadata;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
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

    @Override
    public MFMetadata extract(Path absPath) throws IOException {
        try (var fIs = new BufferedInputStream(Files.newInputStream(absPath))) {
            FileType fileType = FileTypeDetector.detectFileType(fIs);

            switch (fileType) {
                case FileType.Jpeg, FileType.Heif, FileType.Png, FileType.WebP -> {
                    Metadata meta = ImageMetadataReader.readMetadata(fIs);
                    return new ImageMetaReader(meta).read();
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

                default -> {
                    log.error(
                            "Unsupported file type: {} for file: {}",
                            fileType,
                            absPath
                    );
                    return null;
                }
            }
        } catch (ImageProcessingException e) {
            log.error(
                    "Error while retrieving metadata for: {} - {}",
                    absPath,
                    e.getMessage()
            );

            return null;
        }
    }
}
