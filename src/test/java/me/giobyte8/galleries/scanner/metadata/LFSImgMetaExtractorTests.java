package me.giobyte8.galleries.scanner.metadata;

import me.giobyte8.galleries.scanner.dto.MFMetadata;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LFSImgMetaExtractorTests {

    private final LFSImgMetaExtractor mExtractor =
            new LFSImgMetaExtractor();

    private final Path testContentsRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

    @Test
    void extractInvalidFile() {
        Path mfAbsPath = Paths.get(
                testContentsRoot.toAbsolutePath().toString(),
                "cameras",
                "invalidFile.jpg"
        );

        assertThatThrownBy(() -> mExtractor.extract(mfAbsPath))
                .isInstanceOf(IOException.class);
    }

    @Test
    void fullMetadata() throws IOException {
        Path mfAbsPath = Paths.get(
                testContentsRoot.toAbsolutePath().toString(),
                "cameras",
                "20211029_094356_2.jpg"
        );

        MFMetadata meta = mExtractor.extract(mfAbsPath);
        assertThat(meta).isNotNull();

        // Verify all metadata was extracted
        assertThat(meta.getDatetimeOriginal()).isNotNull();
        assertThat(meta.getGpsLatitude()).isNotNull();
        assertThat(meta.getGpsLongitude()).isNotNull();
        assertThat(meta.getCamMaker()).isNotNull();
        assertThat(meta.getCamModel()).isNotNull();
    }

    @Test
    void heicMetadata() throws IOException {
        Path mfAbsPath = Paths.get(
                testContentsRoot.toAbsolutePath().toString(),
                "cameras",
                "iPhone",
                "2025-03-01T204437_IMG_0121.heic"
        );

        MFMetadata meta = mExtractor.extract(mfAbsPath);
        assertThat(meta).isNotNull();

        // Verify all metadata was extracted
        assertThat(meta.dateTimeOriginal()).isNotNull();

        assertThat(meta.getCamMaker()).isEqualTo("Apple");
        assertThat(meta.getCamModel()).isEqualTo("iPhone 16 Pro Max");

        assertThat(meta.getGpsLatitude()).isNotNull();
        assertThat(meta.getGpsLongitude()).isNotNull();
    }

    @Test
    void dateTimeLocalized() throws IOException {

        // Load image taken in a different timezone
        Path mfAbsPath = Paths.get(
                testContentsRoot.toAbsolutePath().toString(),
                "cameras",
                "iPhone",
                "2025-11-13T194401_IMG_2988_edit.jpg"
        );

        MFMetadata meta = mExtractor.extract(mfAbsPath);
        assertThat(meta).isNotNull();


        assertThat(meta.dateTimeOriginal()).isNotNull();
        var dTime = meta.dateTimeOriginal();

        // Verify date
        assertThat(dTime.get(Calendar.YEAR)).isEqualTo(2025);
        assertThat(dTime.get(Calendar.MONTH))
                .isEqualTo(10); // November (0-based)
        assertThat(dTime.get(Calendar.DATE)).isEqualTo(13);

        // Verify time (Local system timezone applied (CST))
        assertThat(dTime.get(Calendar.HOUR_OF_DAY)).isEqualTo(19);
        assertThat(dTime.get(Calendar.MINUTE)).isEqualTo(44);
        assertThat(dTime.get(Calendar.SECOND)).isEqualTo(1);

        // Verify raw date time string
        assertThat(meta.getDatetimeOriginalRaw())
                .isEqualTo("2025:11:14 08:44:01");

        // Verify time zone offset
        assertThat(meta.getTzOffset())
                .isEqualTo("+07:00");
    }
}
