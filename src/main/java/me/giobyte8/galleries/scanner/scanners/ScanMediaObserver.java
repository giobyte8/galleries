package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;

import java.io.IOException;

public interface ScanMediaObserver {

    void onScanStarted(ScanRequest scanReq);

    void onScanCompleted(ScanRequest scanReq);

    void onScanFailed(ScanRequest scanReq);

    /**
     * Prepares given directory for scanning.
     * 1. Sets all its children status to 'VERIFYING'
     * 2. Sets dir status to 'SCAN_IN_PROGRESS'
     *
     * @param directory Directory about to being scanned
     */
    void prepareForScanning(Directory directory);

    /**
     * Handles actions required once a directory has been scanned
     * 1. Deletes from DB every image in 'VERIFYING' status (Not found)
     * 2. Deletes from DB every dir in 'VERIFYING' status (Not found) along
     *    with all of its images. (Not found dir means each image inside
     *    doesn't exist anymore)
     * 3. Emits an event for each removed image
     *
     * @param directory Directory which contents scan has been completed
     * @param scanReq Request that initiated this scan
     */
    void onScanCompleted(ScanRequest scanReq, Directory directory);

    /**
     * Handles possible errors during directory scan and takes care of
     * rolling back any necessary actions in DB
     * @param directory Directory that failed its scanning process
     * @param e Exception that caused scanning failure
     */
    void onScanFailed(Directory directory, IOException e);

    void onDirectoryFound(Directory parent, Directory directory);

    void onImageFound(ScanRequest scanReq, Directory parent, Image image);

    // void onVideoFound();
}
