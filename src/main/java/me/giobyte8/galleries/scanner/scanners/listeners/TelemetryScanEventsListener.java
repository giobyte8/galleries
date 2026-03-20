package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.scanner.metrics.Metric;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelemetryScanEventsListener implements ScanEventsListener {

    private final MetricsService metricsService;

    @Override
    public void onScanStarted() {
        metricsService.increment(Metric.SCAN_STARTED);
    }

    @Override
    public void onScanCompleted() {
        metricsService.increment(Metric.SCAN_COMPLETED);
    }

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
        metricsService.increment(Metric.SCAN_DIR_FOUND);
    }

    @Override
    public void onNewImageFound(Directory parent, Image img) {
        metricsService.increment(Metric.SCAN_IMG_FOUND_NEW);
    }

    @Override
    public void onUpdatedImageFound(Directory parent, Image img) {
        metricsService.increment(Metric.SCAN_IMG_FOUND_UPDATED);
    }

    @Override
    public void onUnchangedImageFound(Directory parent, Image img) {
        metricsService.increment(Metric.SCAN_IMG_FOUND_UNCHANGED);
    }

    @Override
    public void onImageNotFound(Directory parent, Image img) {
        metricsService.increment(Metric.SCAN_IMG_NOT_FOUND);
    }
}
