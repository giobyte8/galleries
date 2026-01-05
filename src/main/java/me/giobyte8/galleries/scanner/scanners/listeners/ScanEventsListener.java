package me.giobyte8.galleries.scanner.scanners.listeners;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;

public interface ScanEventsListener {

    void onScanStarted(Directory dir);

    void onScanCompleted(Directory dir);

    void onScanFailed(Directory dir, Exception e);

    void onDirFound(Directory parent, Directory dir);

    void onImageFound(Directory parent, Image img);

    // void onVideoFound(Video video);
}
