package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.dto.MFMetadata;

import java.io.IOException;

public interface MetadataExtractor {

    MFMetadata extract(String absPath) throws IOException;
}
