package me.giobyte8.galleries.scanner.metadata;

import me.giobyte8.galleries.scanner.dto.MFMetadata;

import java.io.IOException;
import java.nio.file.Path;

public interface ImgMetaExtractor {

    MFMetadata extract(Path absPath) throws IOException;
}
