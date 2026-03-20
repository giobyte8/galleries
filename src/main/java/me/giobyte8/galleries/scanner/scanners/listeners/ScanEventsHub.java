package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class ScanEventsHub implements ScanEventsListener {
    private final Set<ScanEventsListener> listeners = new HashSet<>();

    public ScanEventsHub(
            GalleriesScanEventsListener galleriesEventsListener,
            TelemetryScanEventsListener telemetryEventsListener,
            StatsScanEventsListener statsEventsListener
    ) {

        // Subscribe built-in listeners
        subscribe(galleriesEventsListener);
        subscribe(telemetryEventsListener);
        subscribe(statsEventsListener);
    }

    public void subscribe(ScanEventsListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onScanStarted() {
        log.debug("Scan request started");
        listeners.forEach(ScanEventsListener::onScanStarted);
    }

    @Override
    public void onScanCompleted() {
        log.debug("Scan request completed");
        listeners.forEach(ScanEventsListener::onScanCompleted);
    }

    @Override
    public void onScanStarted(Directory dir) {
        log.debug(
                "Scan for directory started: {}, recursive: {}",
                dir.getPath(),
                dir.isRecursive()
        );

        listeners.forEach(l -> l.onScanStarted(dir));
    }

    @Override
    public void onScanCompleted(Directory dir) {
        log.debug(
                "Scan for directory completed: {}, recursive: {}",
                dir.getPath(),
                dir.isRecursive()
        );

        listeners.forEach(l -> l.onScanCompleted(dir));
    }

    @Override
    public void onScanFailed(Directory dir, Exception e) {
        log.error("Error while scanning directory: {}", dir.getPath(), e);
        listeners.forEach(l -> l.onScanFailed(dir, e));
    }

    @Override
    public void onDirFound(Directory parent, Directory dir) {
        log.debug("Directory found: {}", dir.getPath());
        listeners.forEach(l -> l.onDirFound(parent, dir));
    }

    @Override
    public void onNewImageFound(Directory parent, Image img) {
        log.debug("New image found: {}", img.getPath());
        listeners.forEach(l -> l.onNewImageFound(parent, img));
    }

    @Override
    public void onUpdatedImageFound(Directory parent, Image img) {
        log.debug("Updated image found: {}", img.getPath());
        listeners.forEach(l -> l.onUpdatedImageFound(parent, img));
    }

    @Override
    public void onUnchangedImageFound(Directory parent, Image img) {
        log.debug("Unchanged image found: {}", img.getPath());
        listeners.forEach(l -> l.onUnchangedImageFound(parent, img));
    }

    @Override
    public void onImageNotFound(Directory parent, Image img) {
        log.debug("Image not found (deleted): {}", img.getPath());
        //listeners.forEach(l -> l.onImageNotFound(parent, img));
    }
}
