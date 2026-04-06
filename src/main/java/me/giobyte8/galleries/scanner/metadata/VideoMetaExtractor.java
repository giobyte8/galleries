package me.giobyte8.galleries.scanner.metadata;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import me.giobyte8.galleries.scanner.metadata.reader.MetaReader;
import me.giobyte8.galleries.scanner.metadata.reader.Mp4ExifToolMetaReader;
import me.giobyte8.galleries.scanner.metadata.reader.QuickTimeMetaReader;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("test")
public class VideoMetaExtractor implements ImgMetaExtractor {
    private final ObjectMapper jMapper;

    @Override
    public MFMetadata extract(Path absPath) throws IOException {
        try (var fIs = new BufferedInputStream(Files.newInputStream(absPath))) {
            FileType fileType = FileTypeDetector.detectFileType(fIs);

            MetaReader metaReader;
            switch (fileType) {
                case FileType.Mp4 -> metaReader
                        = Mp4ExifToolMetaReader.forFile(absPath, jMapper);

                case FileType.QuickTime -> {
                    Metadata meta = ImageMetadataReader.readMetadata(fIs);
                    metaReader = new QuickTimeMetaReader(meta);
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

            var mfMeta = metaReader.read();
//            for (var dir : meta.getDirectories()) {
//                for (var tag : dir.getTags()) {
//                    log.debug("Video metadata - {}: {} = {}",
//                            dir.getName(),
//                            tag.getTagName(),
//                            tag.getDescription()
//                    );
//                }
//            }

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
}
