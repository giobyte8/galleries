package me.giobyte8.galleries.scanner.scanners.listeners;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.Video;

public interface ScanEventsListener {

    /**
     * Called when a scan request starts processing
     * (before any directory is scanned).
     */
    default void onScanStarted() { }

    /**
     * Called when a scan request has fully completed
     * (after all directories are scanned).
     */
    default void onScanCompleted() { }

    default void onScanStarted(Directory dir) { }

    default void onScanCompleted(Directory dir) { }

    default void onScanFailed(Directory dir, Exception e) { }

    default void onDirFound(Directory parent, Directory dir) { }

    /**
     * Called when scan detects a new image
     * @param parent Directory that contains the image
     * @param img Image that was found
     */
    default void onNewImageFound(Directory parent, Image img) { }

    /**
     * Called when scan detects an updated image, that is, an image that
     * was already found in previous scans, but now it has a different
     * content hash (It has been edited/updated).
     *
     * @param parent Directory that contains the image
     * @param img Image that was found
     */
    default void onUpdatedImageFound(Directory parent, Image img) { }

    /**
     * Called when scan detects an unchanged image, that is, an image
     * that was already found in previous scans and its content hash
     * remains the same (Image hasn't changed).
     *
     * @param parent Directory that contains the image
     * @param img Image that was found
     */
    default void onUnchangedImageFound(Directory parent, Image img) { }

    /**
     * Called when an image previously known is not found during scanning.
     * This usually means the image has been deleted.
     *
     * @param parent Directory that contained the image
     * @param img Image that was not found
     */
    default void onImageNotFound(Directory parent, Image img) { }

    /**
     * Called when scan detects a new video.
     *
     * @param parent Directory that contains the video
     * @param video Video that was found
     */
    default void onNewVideoFound(Directory parent, Video video) { }

    /**
     * Called when scan detects an updated video, that is, a video that
     * was already found in previous scans, but now it has a different
     * content hash.
     *
     * @param parent Directory that contains the video
     * @param video Video that was found
     */
    default void onUpdatedVideoFound(Directory parent, Video video) { }

    /**
     * Called when scan detects an unchanged video, that is, a video
     * that was already found in previous scans and its content hash
     * remains the same.
     *
     * @param parent Directory that contains the video
     * @param video Video that was found
     */
    default void onUnchangedVideoFound(Directory parent, Video video) { }

    /**
     * Called when a video previously known is not found during scanning.
     * This usually means the video has been deleted.
     *
     * @param parent Directory that contained the video
     * @param video Video that was not found
     */
    default void onVideoNotFound(Directory parent, Video video) { }
}
