package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.ScanStatus;
import me.giobyte8.galleries.persistence.repositories.ScanStatsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Tracks counters for scan events; lifecycle and persistence trigger are
 * managed by ScanService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StatsScanEventsListener implements ScanEventsListener {

    private final ScanStatsRepository scanStatsRepository;

    @Override
    public void onScanStarted() {
        ScanStatsContext.current().startedAt(LocalDateTime.now());
    }

    @Override
    public void onScanCompleted() {
        var ctx = ScanStatsContext.current();
        ctx.completedAt(LocalDateTime.now());
        ctx.status(ScanStatus.COMPLETED);

        var scanStats = ctx.buildStats();
        scanStatsRepository.save(scanStats);
    }

    @Override
    public void onScanStarted(Directory dir) { }

    @Override
    public void onScanCompleted(Directory dir) { }

    @Override
    public void onScanFailed(Directory dir, Exception e) { }

    @Override
    public void onDirFound(Directory parent, Directory dir) {
        ScanStatsContext.current().incrementFoundDirectories();
    }

    @Override
    public void onNewImageFound(Directory parent, Image img) {
        ScanStatsContext.current().incrementNewImages();
    }

    @Override
    public void onUpdatedImageFound(Directory parent, Image img) {
        ScanStatsContext.current().incrementUpdatedImages();
    }

    @Override
    public void onUnchangedImageFound(Directory parent, Image img) {
        ScanStatsContext.current().incrementUnchangedImages();
    }

    @Override
    public void onImageNotFound(Directory parent, Image img) {
        ScanStatsContext.current().incrementNotFoundImages();
    }
}
