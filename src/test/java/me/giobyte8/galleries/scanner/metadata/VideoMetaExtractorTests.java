package me.giobyte8.galleries.scanner.metadata;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class VideoMetaExtractorTests {

    private final VideoMetaExtractor metaExtractor =
            new VideoMetaExtractor(new ObjectMapper());

    private final Path testContentsRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

    @Test
    void nonVideoFile() throws IOException {
        var path = pathFor("cameras", "20220612_133112.jpg");
        var meta = metaExtractor.extract(path);

        assertThat(meta).isNull();
    }

    @Test
    void movMediaFile() throws IOException {
        var path = pathFor("10 lake_hdr.mov");
        var meta = metaExtractor.extract(path);

        assertThat(meta).isNotNull();

        // Verify extracted datetime matches
        assertThat(meta.getRawCaptureDateTime())
                .isEqualTo("2025-11-06T13:44:30+0700");
        assertThat(meta.getCaptureDateTime().getHour()).isEqualTo(13);
        assertThat(meta.getCaptureDateTime().getMinute()).isEqualTo(44);
    }

    @Test
    void mp4MediaFile_NoMetadata() throws IOException {
        var path = pathFor("11 whatsapp.mp4");
        var meta = metaExtractor.extract(path);

        assertThat(meta).isNotNull();

        assertThat(meta.getGpsLatitude()).isNull();
        assertThat(meta.getGpsLongitude()).isNull();

        assertThat(meta.getCamMaker()).isNull();
        assertThat(meta.getCamModel()).isNull();

        assertThat(meta.getRawCaptureDateTime()).isNull();
        assertThat(meta.getCaptureDateTime()).isNull();
    }

    @Test
    void mp4MediaFile() throws IOException {
        var path = pathFor("pour_edit_20230619_122200_1.mp4");
        var meta = metaExtractor.extract(path);

        assertThat(meta).isNotNull();

        assertThat(meta.getCamMaker()).isEqualTo("Samsung");
        assertThat(meta.getCamModel()).isEqualTo("SM-S918B");

        assertThat(meta.getGpsLatitude()).isEqualTo("19.4023");
        assertThat(meta.getGpsLongitude()).isEqualTo("-99.1806");
    }

    private Path pathFor(String filename) {
        return pathFor("videos", filename);
    }

    private Path pathFor(String dir, String filename) {
        return Paths.get(
                testContentsRoot.toAbsolutePath().toString(),
                dir,
                filename
        );
    }
}
