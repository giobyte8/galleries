package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.scanner.dto.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AMQPScanEvents implements ScanEvents {

    @Value("${galleries.scanner.amqp.queue_scan_hooks}")
    private String qScanHooks;

    @Value("${galleries.scanner.amqp.queue_scan_discovered_files}")
    private String qScanDiscoveredFiles;

    private final AmqpTemplate rbTemplate;

    public AMQPScanEvents(AmqpTemplate rbTemplate) {
        this.rbTemplate = rbTemplate;
    }

    public void onScanProcessEvent(ScanRequest scanReq, ScanEventType eventType) {
        ScanEvent evt = new ScanEvent(
                scanReq.id(),
                eventType,
                LocalDateTime.now()
        );

        rbTemplate.convertAndSend(qScanHooks, evt);
    }

    public void onFileDiscoveryEvent(
            ScanRequest scanReq,
            FDiscoveryEventType eventType,
            String filePath
    ) {
        FileDiscoveryEvent evt = new FileDiscoveryEvent(
                scanReq.id(),
                eventType,
                filePath
        );

        rbTemplate.convertAndSend(qScanDiscoveredFiles, evt);
    }
}
