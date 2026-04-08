package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import me.giobyte8.galleries.scanner.thumbnails.ThumbnailsService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * Handles the core logic to keep the galleries updated during
 * scans
 */
@RequiredArgsConstructor
@Service
public class GalleriesScanEventsListener implements ScanEventsListener {

    private final DirectoryRepository dirRepository;
    private final ImageRepository imgRepository;
    private final ThumbnailsService thumbnailsSvc;

    @Override
    public void onScanStarted() { }

    @Override
    public void onScanCompleted() { }

    @Override
    public void onScanStarted(Directory dir) {

        // Set all images under dir to 'VERIFYING' status
        imgRepository.updateStatusByParent(dir, MediaFileStatus.VERIFYING);

        // Set all children directories to 'VERIFYING' status
        dirRepository.updateStatusByParent(dir, DirStatus.VERIFYING);

        dir.setStatus(DirStatus.SCAN_IN_PROGRESS);
        dirRepository.save(dir);
    }

    /**
     * Handles actions required once a directory has been scanned
     * 1. Deletes from DB every image in 'VERIFYING' status (Not found)
     * 2. Deletes from DB every dir in 'VERIFYING' status (Not found) along
     *    with all of its images. (Not found dir means each image inside
     *    doesn't exist anymore)
     * 3. Deletes thumbnails for each removed image
     * 4. Updates directory status to 'SCAN_COMPLETE'
     *
     * @param dir Directory for which contents scan has been completed
     */
    @Override
    public void onScanCompleted(Directory dir) {

        // If an image is still in 'VERIFYING' status, that means it
        // wasn't found during scanning.
        // Remove every image that remains in 'VERIFYING' status.
        imgRepository

                // TODO Mark as NOT_FOUND instead of deleting?
                // TODO Schedule thumbs to be deleted later instead of now?
                // TODO Schedule image record to be deleted later instead of now?
                .deleteAndGetPaths(dir, MediaFileStatus.VERIFYING)
                .forEach(path ->
                        thumbnailsSvc.deleteThumbnails(
                                Path.of(path)
                        )
                );

        // Process children directories that remains in 'VERIFYING' status
        dirRepository
                .findByParentPathAndStatus(dir, DirStatus.VERIFYING)
                .forEach(notFoundDir -> {

                    // Remove all descendant images from not found dir and
                    // from subdirectories and emit event for each deleted image
                    imgRepository
                            .multilevelDeleteAndGetPaths(notFoundDir)
                            .forEach(imgPath ->
                                    thumbnailsSvc.deleteThumbnails(
                                            Path.of(imgPath)
                                    )
                            );

                    // Delete directory and its subdirectories
                    dirRepository.deleteWithDescendants(notFoundDir);
                });

        dir.setStatus(DirStatus.SCAN_COMPLETE);
        dirRepository.save(dir);
    }

    @Override
    public void onScanFailed(Directory dir, Exception e) {
        dir.setStatus(DirStatus.SCAN_FAILED);
        dirRepository.save(dir);
    }

    @Override
    public void onDirFound(Directory parent, Directory dir) {
        dirRepository.saveAsChild(parent, dir);
    }

    @Override
    public void onNewImageFound(Directory parent, Image img) {
        imgRepository.saveAsChild(parent, img);
        thumbnailsSvc.generateThumbnails(Path.of(img.getPath()));
    }

    @Override
    public void onUpdatedImageFound(Directory parent, Image img) {
        imgRepository.saveAsChild(parent, img);
        thumbnailsSvc.refreshThumbnails(Path.of(img.getPath()));
    }

    @Override
    public void onUnchangedImageFound(Directory parent, Image img) {
        img.setStatus(MediaFileStatus.AVAILABLE);
        imgRepository.saveAsChild(parent, img);
    }

    @Override
    public void onImageNotFound(Directory parent, Image img) {

    }
}
