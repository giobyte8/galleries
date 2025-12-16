package me.giobyte8.galleries.scanner.amqp;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.model.DirStatus;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
import me.giobyte8.galleries.scanner.scanners.DirMediaScanner;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ScanRequestsListener {

    private final DirMediaScanner mScanner;
    private final DirectoryRepository dirRepository;

    public ScanRequestsListener(
            DirMediaScanner mScanner,
            DirectoryRepository dirRepository
    ) {
        this.mScanner = mScanner;
        this.dirRepository = dirRepository;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${galleries.scanner.amqp.queue_scan_requests}"),
            exchange = @Exchange(value = "${galleries.scanner.amqp.exchange_gl}"),
            key = "${galleries.scanner.amqp.queue_scan_requests}"
    ))
    public void onScanRequest(ScanRequest request) {
        log.info("AMQP Scan request received: {}", request);

        // Verify directory exist
        Directory directory = dirRepository.findBy(request.dirPath());
        if (directory == null) {
            log.error(
                    "Provided directory wasn't not found in DB: {}",
                    request.dirPath()
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

        mScanner.scan(request, directory);
    }
}
