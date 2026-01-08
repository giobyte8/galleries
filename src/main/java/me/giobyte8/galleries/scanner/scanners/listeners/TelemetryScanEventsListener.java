package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.scanner.metrics.Metric;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelemetryScanEventsListener implements ScanEventsListener {

    private final MetricsService metricsService;

    @Override
    public void onScanStarted(Directory dir) {
        metricsService.increment(Metric.SCAN_DIR_STARTED);
    }

    @Override
    public void onScanCompleted(Directory dir) {
        metricsService.increment(Metric.SCAN_DIR_COMPLETED);
    }

    @Override
    public void onScanFailed(Directory dir, Exception e) {
        metricsService.increment(Metric.SCAN_DIR_FAILED);
    }

    @Override
    public void onDirFound(Directory parent, Directory dir) {
        metricsService.increment(Metric.SCAN_FOUND_DIR);
    }

    @Override
    public void onImageFound(Directory parent, Image img) {
        metricsService.increment(Metric.SCAN_FOUND_IMG);
    }
}
