package me.giobyte8.galleries.scanner.thumbnails;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.ThumbnailsRequest;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@RequiredArgsConstructor
@Service
public class RemoteThumbnailsService implements ThumbnailsService {

    private final ScannerProps scannerProps;
    private final AmqpTemplate rbTemplate;

    @Override
    public void generateThumbnails(Path path) {
        rbTemplate.convertAndSend(
                scannerProps.getAmqp().getQueueGenThumbRequests(),
                mkThumbRequest(path)
        );
    }

    @Override
    public void refreshThumbnails(Path path) {
        this.generateThumbnails(path);
    }

    @Override
    public void deleteThumbnails(Path path) {
        rbTemplate.convertAndSend(
                scannerProps.getAmqp().getQueueDelThumbRequests(),
                mkThumbRequest(path)
        );
    }

    private ThumbnailsRequest mkThumbRequest(Path path) {
        return ThumbnailsRequest
                .builder()
                .filePath(path.toString())
                .build();
    }
}
