package me.giobyte8.galleries.scanner.scanners.listeners;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;

public interface ScanEventsListener {

    void onScanStarted(Directory dir);

    void onScanCompleted(Directory dir);

    void onScanFailed(Directory dir, Exception e);

    void onDirFound(Directory parent, Directory dir);

    /**
     * Called when scan detects a new image
     * @param parent Directory that contains the image
     * @param img Image that was found
     */
    void onNewImageFound(Directory parent, Image img);

    /**
     * Called when scan detects an updated image, that is, an image that
     * was already found in previous scans, but now it has a different
     * content hash (It has been edited/updated).
     *
     * @param parent Directory that contains the image
     * @param img Image that was found
     */
    void onUpdatedImageFound(Directory parent, Image img);

    /**
     * Called when scan detects an unchanged image, that is, an image
     * that was already found in previous scans and its content hash
     * remains the same (Image hasn't changed).
     *
     * @param parent Directory that contains the image
     * @param img Image that was found
     */
    void onUnchangedImageFound(Directory parent, Image img);

    /**
     * Called when an image previously known is not found during scanning.
     * This usually means the image has been deleted.
     *
     * @param parent Directory that contained the image
     * @param img Image that was not found
     */
    void onImageNotFound(Directory parent, Image img);

    // void onVideoFound(Video video);
}
