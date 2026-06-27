package me.giobyte8.galleries.scanner.metadata;

import me.giobyte8.galleries.scanner.exceptions.MediaProcessingException;
import me.giobyte8.galleries.scanner.metadata.format.MediaFormatResolver;
import me.giobyte8.galleries.scanner.metadata.reader.datetime.DateTimeParsersChain;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ImageMetaExtractorTests {

    @Spy
    private ObjectMapper jMapper = new ObjectMapper();

    @Spy
    private MediaFormatResolver formatResolver = new MediaFormatResolver(jMapper);

    @Mock
    private MetricsService metricsSvc;

    @Spy
    private DateTimeParsersChain parsersChain = DateTimeParsersChain.defaultChain();

    @InjectMocks
    private LFSMediaMetaExtractor mExtractor;

    private final Path testContentsRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

    @Test
    void invalidFile() {
        Path mfAbsPath = pathFor("invalidFile.jpg");
        assertThatThrownBy(() -> mExtractor.extract(mfAbsPath))
                .isInstanceOf(IOException.class);
    }

    @Test
    void jpegMetadata()
            throws IOException, MediaProcessingException {
        var absPath = pathFor("20220612_133112.jpg");
        var meta = mExtractor.extract(absPath);

        assertThat(meta).isNotNull();

        // Verify capture datetime is Jun 12th, 2022 at 13:31 in -06 timezone
        assertThat(meta.getCaptureDateTime()).isNotNull();
        assertThat(meta.getCaptureDateTime().getYear()).isEqualTo(2022);
        assertThat(meta.getCaptureDateTime().getMonthValue()).isEqualTo(6);
        assertThat(meta.getCaptureDateTime().getDayOfMonth()).isEqualTo(12);
        assertThat(meta.getCaptureDateTime().getHour()).isEqualTo(13);
        assertThat(meta.getCaptureDateTime().getMinute()).isEqualTo(31);
        assertThat(meta.getCaptureDateTime().getOffset().getId())
                .isEqualTo("-06:00");
    }

    @Test
    void heicMetadata() throws IOException, MediaProcessingException {
        Path absPath = pathFor(
                Path.of("cameras", "iPhone").toString(),
                "2025-03-01T204437_IMG_0121.heic"
        );
        var meta = mExtractor.extract(absPath);

        assertThat(meta).isNotNull();

        assertThat(meta.getCamMaker()).isEqualTo("Apple");
        assertThat(meta.getCamModel()).isEqualTo("iPhone 16 Pro Max");

        assertThat(meta.getGpsLatitude()).isNotNull();
        assertThat(meta.getGpsLongitude()).isNotNull();

        // Verify capture datetime is March 3rd, 2025 at 20:44 in -06 timezone
        assertThat(meta.getCaptureDateTime()).isNotNull();
        assertThat(meta.getCaptureDateTime().getYear()).isEqualTo(2025);
        assertThat(meta.getCaptureDateTime().getMonthValue()).isEqualTo(3);
        assertThat(meta.getCaptureDateTime().getDayOfMonth()).isEqualTo(1);
        assertThat(meta.getCaptureDateTime().getHour()).isEqualTo(20);
        assertThat(meta.getCaptureDateTime().getMinute()).isEqualTo(44);
        assertThat(meta.getCaptureDateTime().getOffset().getId())
                .isEqualTo("-06:00");
    }

    @Test
    void heicInJpgExtension() throws IOException, MediaProcessingException {
        Path absPath = pathFor(
                Path.of("cameras", "iPhone").toString(),
                "2025-03-01T204801_IMG_0122_edit.jpg"
        );
        var meta = mExtractor.extract(absPath);

        assertThat(meta).isNotNull();

        assertThat(meta.getCamMaker()).isEqualTo("Apple");
        assertThat(meta.getCamModel()).isEqualTo("iPhone 16 Pro Max");

        assertThat(meta.getGpsLatitude()).isNotNull();
        assertThat(meta.getGpsLongitude()).isNotNull();

        // Verify capture datetime is March 3rd, 2025 at 20:44 in -06 timezone
        assertThat(meta.getCaptureDateTime()).isNotNull();
        assertThat(meta.getCaptureDateTime().getYear()).isEqualTo(2025);
        assertThat(meta.getCaptureDateTime().getMonthValue()).isEqualTo(3);
        assertThat(meta.getCaptureDateTime().getDayOfMonth()).isEqualTo(1);
        assertThat(meta.getCaptureDateTime().getHour()).isEqualTo(20);
        assertThat(meta.getCaptureDateTime().getMinute()).isEqualTo(48);
        assertThat(meta.getCaptureDateTime().getOffset().getId())
                .isEqualTo("-06:00");
    }

    @Test
    void pngMetadata() throws IOException, MediaProcessingException {
        Path absPath = pathFor("2025-07-31T132949_IMG_1136.png");
        var meta = mExtractor.extract(absPath);

        assertThat(meta).isNotNull();
    }

    @Test
    void webPMetadata() throws IOException, MediaProcessingException {
        Path absPath = pathFor("flower.webp");
        var meta = mExtractor.extract(absPath);

        assertThat(meta).isNotNull();
    }

    private Path pathFor(String filename) {
        return pathFor("cameras", filename);
    }

    private Path pathFor(String dir, String filename) {
        return Paths.get(
                testContentsRoot.toAbsolutePath().toString(),
                dir,
                filename
        );
    }
}


