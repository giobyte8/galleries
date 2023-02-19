package me.giobyte8.galleries.scanner.amqp;

import me.giobyte8.galleries.scanner.dto.ScanOrder;
import me.giobyte8.galleries.scanner.services.MediaScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ScanRequestsListener {
    Logger log = LoggerFactory.getLogger(ScanRequestsListener.class);

    private final MediaScannerService mScanner;

    public ScanRequestsListener(MediaScannerService mScanner) {
        this.mScanner = mScanner;
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${galleries.scanner.amqp.queue_scan_orders}"),
            exchange = @Exchange(value = "${galleries.scanner.amqp.exchange_gl}"),
            key = "${galleries.scanner.amqp.queue_scan_orders}"
    ))
    public void onScanRequest(ScanOrder order) {
        log.debug("AMQP Scan request received: {}", order);

        mScanner.scan(order);
    }
}
