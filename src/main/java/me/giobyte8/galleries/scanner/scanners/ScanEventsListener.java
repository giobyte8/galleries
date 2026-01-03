package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;

public interface ScanEventsListener {

    void onScanStarted(Directory dir);

    void onScanCompleted(Directory dir);

    void onDirFound(Directory dir);

    void onImageFound(Image img);

    // void onVideoFound(Video video);
}
