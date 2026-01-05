package me.giobyte8.galleries.scanner.scanners.listeners;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import org.springframework.stereotype.Service;

@Service
public class TelemetryScanEventsListener implements ScanEventsListener {

    @Override
    public void onScanStarted(Directory dir) {

    }

    @Override
    public void onScanCompleted(Directory dir) {

    }

    @Override
    public void onScanFailed(Directory dir, Exception e) {

    }

    @Override
    public void onDirFound(Directory parent, Directory dir) {

    }

    @Override
    public void onImageFound(Directory parent, Image img) {

    }
}
