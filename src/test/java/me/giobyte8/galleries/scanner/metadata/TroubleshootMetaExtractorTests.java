package me.giobyte8.galleries.scanner.metadata;

import me.giobyte8.galleries.scanner.exceptions.MediaProcessingException;
import me.giobyte8.galleries.scanner.metadata.format.MediaFormatResolver;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import org.junit.jupiter.api.Disabled;
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

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@ExtendWith(MockitoExtension.class)
public class TroubleshootMetaExtractorTests {

    @Spy
    private ObjectMapper jMapper = new ObjectMapper();

    @Spy
    private MediaFormatResolver formatResolver = new MediaFormatResolver(jMapper);

    @Mock
    private MetricsService metricsSvc;

    @InjectMocks
    private LFSMediaMetaExtractor extractor;

    private final Path contentPath = Paths.get(
            "src/test/resources/galleries",
            "troubleshoot"
    );

    @Test
    void extractMetadata() throws IOException, MediaProcessingException {
        var path = contentPath.resolve("mariedeemelons_vid_205.mp4");
        var meta = extractor.extract(path);

        assertThat(meta).isNotNull();
    }
}
