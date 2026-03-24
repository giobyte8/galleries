package me.giobyte8.galleries.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScanRequestsService {

    public enum TriggerResult {
        QUEUED,
        NOT_FOUND,
        ALREADY_IN_PROGRESS,
        PUBLISH_FAILED
    }

    private final DirectoryRepository dirRepository;
    private final AmqpTemplate amqpTemplate;
    private final ScannerProps scannerProps;

    public TriggerResult triggerScan(UUID directoryId) {
        var dirOpt = dirRepository.findById(directoryId);
        if (dirOpt.isEmpty()) {
            log.warn("Trigger scan requested for unknown directory id: {}", directoryId);
            return TriggerResult.NOT_FOUND;
        }

        var directory = dirOpt.get();
        if (directory.getStatus() == DirStatus.SCAN_IN_PROGRESS) {
            log.warn(
                    "Scan already in progress for directory: {}",
                    directory.getPath()
            );
            return TriggerResult.ALREADY_IN_PROGRESS;
        }

        ScanRequest scanRequest = new ScanRequest(
                UUID.randomUUID(),
                directory.getPath(),
                LocalDateTime.now()
        );

        try {
            amqpTemplate.convertAndSend(
                    scannerProps.getAmqp().getQueueScanRequests(),
                    scanRequest
            );
            log.debug(
                    "Scan request published for directory: {}",
                    directory.getPath()
            );
            return TriggerResult.QUEUED;
        } catch (AmqpException e) {
            log.error(
                    "Failed to publish scan request for directory: {}",
                    directory.getPath(),
                    e
            );
            return TriggerResult.PUBLISH_FAILED;
        }
    }
}

