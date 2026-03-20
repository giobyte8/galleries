package me.giobyte8.galleries.scanner.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.scanners.MediaScanner;
import me.giobyte8.galleries.scanner.scanners.listeners.ScanStatsContext;
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
        var dirOpt = dirRepository.findByPath(scanRequest.path());
        if (dirOpt.isEmpty()) {
            log.error(
                    "Scan request directory not found in DB: {}",
                    scanRequest.path()
            );
            return;
        }

        // Validate directory status is ok
        var directory = dirOpt.get();
        if (directory.getStatus() == DirStatus.SCAN_IN_PROGRESS) {
            log.error(
                    "Another scan is already in progress for: {}",
                    directory.getPath()
            );
            return;
        }

        ScanStatsContext statsContext = new ScanStatsContext(scanRequest);
        ScanStatsContext.runWith(statsContext, () -> mediaScanner.scan(directory));
    }
}
