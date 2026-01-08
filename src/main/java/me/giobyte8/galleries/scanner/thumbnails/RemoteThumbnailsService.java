package me.giobyte8.galleries.scanner.thumbnails;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.ThumbnailsRequest;
import me.giobyte8.galleries.scanner.metrics.Metric;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@RequiredArgsConstructor
@Service
public class RemoteThumbnailsService implements ThumbnailsService {

    private final ScannerProps scannerProps;
    private final AmqpTemplate rbTemplate;
    private final MetricsService metricsService;

    @Override
    public void generateThumbnails(Path path) {
        sendThumbnailRequest(
                scannerProps.getAmqp().getQueueGenThumbRequests(),
                path
        );
        metricsService.increment(Metric.THUMBS_REQUESTED_GEN);
    }

    @Override
    public void refreshThumbnails(Path path) {
        sendThumbnailRequest(
                scannerProps.getAmqp().getQueueGenThumbRequests(),
                path
        );
        metricsService.increment(Metric.THUMBS_REQUESTED_REF);
    }

    @Override
    public void deleteThumbnails(Path path) {
        sendThumbnailRequest(
                scannerProps.getAmqp().getQueueDelThumbRequests(),
                path
        );
        metricsService.increment(Metric.THUMBS_REQUESTED_DEL);
    }

    private void sendThumbnailRequest(String queueName, Path path) {
        rbTemplate.convertAndSend(queueName, mkThumbRequest(path));
    }

    private ThumbnailsRequest mkThumbRequest(Path path) {
        return ThumbnailsRequest
                .builder()
                .filePath(path.toString())
                .build();
    }
}
