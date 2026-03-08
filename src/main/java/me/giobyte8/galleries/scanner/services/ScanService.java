package me.giobyte8.galleries.scanner.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.scanner.scanners.MediaScanner;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScanService {

    private final DirectoryRepository dirRepository;
    private final MediaScanner mediaScanner;

    public void scan(ScanRequest scanRequest) {
        log.debug("Processing scan request: {}", scanRequest);

        // Verify directory exist
        Directory directory = dirRepository.findByPath(scanRequest.path());
        if (directory == null) {
            log.error(
                    "Scan request directory not found in DB: {}",
                    scanRequest.path()
            );
            return;
        }

        // Validate directory status is ok
        if (directory.getStatus() == DirStatus.SCAN_IN_PROGRESS) {
            log.error(
                    "Another scan is already in progress for: {}",
                    directory.getPath()
            );
            return;
        }

        mediaScanner.scan(directory);
    }
}
