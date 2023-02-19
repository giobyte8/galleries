package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.dto.MFMetadata;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFSMetadataExtractorTests {

    private final LocalFSMetadataExtractor mExtractor =
            new LocalFSMetadataExtractor();

    private final Path testContentsRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

    @Test
    void extractInvalidFile() {
        String mfAbsPath = Paths
                .get(
                        testContentsRoot.toAbsolutePath().toString(),
                        "cameras",
                        "invalidFile.jpg"
                )
                .toString();

        assertThatThrownBy(() -> mExtractor.extract(mfAbsPath))
                .isInstanceOf(IOException.class);
    }

    @Test
    void fullMetadata() throws IOException {
        String mfAbsPath = Paths
                .get(
                        testContentsRoot.toAbsolutePath().toString(),
                        "cameras",
                        "20211029_094356_2.jpg"
                )
                .toString();

        MFMetadata meta = mExtractor.extract(mfAbsPath);
        assertThat(meta).isNotNull();

        // Verify all metadata was extracted
        assertThat(meta.getDatetimeOriginal()).isNotNull();
        assertThat(meta.getGpsLatitude()).isNotNull();
        assertThat(meta.getGpsLongitude()).isNotNull();
    }
}
