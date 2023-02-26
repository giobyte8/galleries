package me.giobyte8.galleries.scanner.amqp;

import org.springframework.stereotype.Service;

@Service
public class ScanEventsProducer {

    /**
     * Triggered when a file previously scanned was not found
     * in a subsequent scan
     *
     * @param path Relative path to file
     */
    public void onScannedFileNotFound(String path) {

    }
}
