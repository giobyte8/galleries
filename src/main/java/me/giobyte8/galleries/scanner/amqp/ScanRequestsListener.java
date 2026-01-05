package me.giobyte8.galleries.scanner.amqp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.services.ScanService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScanRequestsListener {

    private final ScanService scanService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${galleries.scanner.amqp.queue_scan_requests}"),
            exchange = @Exchange(value = "${galleries.scanner.amqp.exchange_gl}"),
            key = "${galleries.scanner.amqp.queue_scan_requests}"
    ))
    public void onScanRequest(ScanRequest request) {
        log.info("AMQP Scan request received: {}", request.id());
        scanService.scan(request);
    }
}
