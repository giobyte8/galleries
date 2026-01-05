package me.giobyte8.galleries.scanner.scanners;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.scanners.listeners.ScanEventsListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class ScanEventsHub {
    private final Set<ScanEventsListener> listeners = new HashSet<>();

    public void subscribe(ScanEventsListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(ScanEventsListener listener) {
        listeners.remove(listener);
    }

    public void scanStarted(Directory dir) {
        log.debug(
                "Scan for directory started: {}, recursive: {}",
                dir.getPath(),
                dir.isRecursive()
        );

        listeners.forEach(l -> l.onScanStarted(dir));
    }

    public void scanCompleted(Directory dir) {
        log.debug(
                "Scan for directory completed: {}, recursive: {}",
                dir.getPath(),
                dir.isRecursive()
        );

        listeners.forEach(l -> l.onScanCompleted(dir));
    }

    public void dirFound(Directory parent, Directory dir) {
        log.debug("Directory found: {}", dir.getPath());
        listeners.forEach(l -> l.onDirFound(parent, dir));
    }

    public void imgFound(Directory parent, Image img) {
        log.debug("Image found: {}", img.getPath());
        listeners.forEach(l -> l.onImageFound(parent, img));
    }
}
