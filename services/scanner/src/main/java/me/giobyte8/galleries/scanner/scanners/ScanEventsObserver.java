package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;

public interface ScanEventsObserver {

    void onDirectoryFound(Directory parent, Directory directory);

    void onImageFound(Directory parent, Image image);

    // void onVideoFound();
}
