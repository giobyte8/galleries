package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.scanner.dto.FDiscoveryEventType;
import me.giobyte8.galleries.scanner.dto.FileDiscoveryEvent;
import me.giobyte8.galleries.scanner.dto.ScanEvent;
import me.giobyte8.galleries.scanner.dto.ScanEventType;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.UUID;

@Service
public class ScanEventsProducer {

    @Value("${galleries.scanner.amqp.queue_scan_hooks}")
    private String qScanHooks;

    @Value("${galleries.scanner.amqp.queue_scan_discovered_files}")
    private String qScanDiscoveredFiles;

    private final AmqpTemplate rbTemplate;

    public ScanEventsProducer(AmqpTemplate rbTemplate) {
        this.rbTemplate = rbTemplate;
    }

    public void onScanEvent(UUID scanReqId, ScanEventType eventType) {
        ScanEvent evt = new ScanEvent(
                scanReqId,
                eventType,
                Calendar.getInstance()
        );

        rbTemplate.convertAndSend(qScanHooks, evt);
    }

    public void onFileDiscoveryEvent(
            UUID scanReqId,
            FDiscoveryEventType eventType,
            String fHashedPath,
            String filePath
    ) {
        FileDiscoveryEvent evt = new FileDiscoveryEvent(
                scanReqId,
                eventType,
                fHashedPath,
                filePath
        );

        rbTemplate.convertAndSend(qScanDiscoveredFiles, evt);
    }
}
