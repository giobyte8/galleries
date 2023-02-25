package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.dto.MFMetadata;

import java.io.IOException;
import java.nio.file.Path;

public interface MetadataExtractor {

    MFMetadata extract(Path absPath) throws IOException;
}
