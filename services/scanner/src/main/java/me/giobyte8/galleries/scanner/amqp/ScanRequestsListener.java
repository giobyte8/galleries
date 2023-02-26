package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.dto.ScanOrder;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.ContentDirStatus;
import me.giobyte8.galleries.scanner.services.MediaScannerService;
import me.giobyte8.galleries.scanner.services.PathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class ScanRequestsListener {
    private static Logger log = LoggerFactory
            .getLogger(ScanRequestsListener.class);

    private final MediaScannerService mScanner;
    private final PathService pathService;
    private final ContentDirDao dirDao;

    public ScanRequestsListener(
            MediaScannerService mScanner,
            PathService pathService,
            ContentDirDao dirDao) {
        this.mScanner = mScanner;
        this.pathService = pathService;
        this.dirDao = dirDao;
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${galleries.scanner.amqp.queue_scan_orders}"),
            exchange = @Exchange(value = "${galleries.scanner.amqp.exchange_gl}"),
            key = "${galleries.scanner.amqp.queue_scan_orders}"
    ))
    public void onScanRequest(ScanOrder order) {
        log.info("AMQP Scan request received: {}", order);
        String dirHPath = order.dirHPath();

        // Validate directory path hash
        Optional<ContentDir> dirOpt = dirDao.findById(dirHPath);
        if (dirOpt.isEmpty()) {
            log.error(
                    "Provided directory hash not found in DB: {}",
                    dirHPath
            );
            return;
        }

        ContentDir contentDir = dirOpt.get();

        // Validate directory status is ok
        if (contentDir.getStatus() == ContentDirStatus.SCAN_IN_PROGRESS) {
            log.error(
                    "Another scan is already in progress for: {}",
                    contentDir.getPath()
            );
            return;
        }

        // Validate lastScanDate is not greater than scanRequest date
        if (contentDir.getLastScanStart() != null &&
                contentDir.getLastScanStart().after(order.requestedAt())) {
            log.info(
                    "Another scan was started after ScanOrder request time. " +
                            "Scan request time: {}. " +
                            "Last scan start time: {}",
                    order.requestedAt(),
                    contentDir.getLastScanStart()
            );
            return;
        }

        // Verify directory path is accessible
        Path absDPath = pathService.toAbsolute(contentDir.getPath());
        if (!Files.isDirectory(absDPath)) {
            log.error("Directory path is not a valid dir: {}", absDPath);
            return;
        }

        try {
            mScanner.scan(contentDir);
        } catch (IOException e) {
            log.error("Error while scanning directory: {} - {}", absDPath, e);
        }
    }
}
