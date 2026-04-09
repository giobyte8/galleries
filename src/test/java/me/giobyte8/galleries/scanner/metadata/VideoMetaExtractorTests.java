package me.giobyte8.galleries.scanner.metadata;

import me.giobyte8.galleries.scanner.metrics.MetricsService;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
public class VideoMetaExtractorTests {

    @Spy
    private ObjectMapper jMapper = new ObjectMapper();

    @Mock
    private MetricsService metricsSvc;

    @InjectMocks
    private LFSMediaMetaExtractor metaExtractor;

    private final Path testContentsRoot = Paths.get(
            "src/test/resources",
            "galleries"
    );

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
    void mp4MediaFile_UTCNextDay() throws IOException {
        var path = pathFor("lake_20231006_182745.mp4");
        var meta = metaExtractor.extract(path);

        assertThat(meta).isNotNull();

        assertThat(meta.getCamMaker()).isEqualTo("Samsung");
        assertThat(meta.getCamModel()).isEqualTo("SM-S918B");

        // Verify extracted datetime matches
        // Notice how raw datetime is the next day of "local time" at capture
        // moment (See filename for local time) due to the UTC offset.
        assertThat(meta.getRawCaptureDateTime())
                .isEqualTo("2023:10:07 01:27:52");
        assertThat(meta.getCaptureDateTime().getDayOfMonth()).isEqualTo(6);
        assertThat(meta.getCaptureDateTime().getHour()).isEqualTo(18);
        assertThat(meta.getCaptureDateTime().getMinute()).isEqualTo(27);
        assertThat(meta.getCaptureDateTime().getZone().getId())
                .isEqualTo("America/Vancouver");
        assertThat(meta.getCaptureDateTime().getOffset().getId())
                .isEqualTo("-07:00");
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

        // Verify extracted datetime matches
        assertThat(meta.getRawCaptureDateTime())
                .isEqualTo("2023:06:19 18:22:33");
        assertThat(meta.getCaptureDateTime().getDayOfMonth()).isEqualTo(19);
        assertThat(meta.getCaptureDateTime().getHour()).isEqualTo(12);
        assertThat(meta.getCaptureDateTime().getMinute()).isEqualTo(22);
        assertThat(meta.getCaptureDateTime().getZone().getId())
                .isEqualTo("America/Mexico_City");
        assertThat(meta.getCaptureDateTime().getOffset().getId())
                .isEqualTo("-06:00");
    }


    /**
     * Use this dummy test to try out/troubleshoot particular videos.
     * Set the video file name to `path` and debug.
     */
    @Test
    void m4vMediaFile() throws IOException {
        var path = pathFor("lake_20231006_182745.mp4");
        var meta = metaExtractor.extract(path);

        assertThat(meta).isNotNull();
    }

    private Path pathFor(String filename) {
        return Paths.get(
                testContentsRoot.toAbsolutePath().toString(),
                "videos",
                filename
        );
    }
}
