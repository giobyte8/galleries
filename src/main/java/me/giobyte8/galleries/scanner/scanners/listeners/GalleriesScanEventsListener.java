package me.giobyte8.galleries.scanner.scanners.listeners;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.scanner.model.DirStatus;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
import me.giobyte8.galleries.scanner.repository.ImageRepository;
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
    public void onScanStarted(Directory dir) {

        // Set all images under dir to 'VERIFYING' status
        imgRepository.update(dir, ImageStatus.VERIFYING);

        // Set all children directories to 'VERIFYING' status
        dirRepository.updateByParent(dir, DirStatus.VERIFYING);

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
                .deleteAndGetPaths(dir, ImageStatus.VERIFYING)
                .forEach(path ->
                        thumbnailsSvc.deleteThumbnails(
                                Path.of(path)
                        )
                );

        // Process children directories that remains in 'VERIFYING' status
        dirRepository
                .findBy(dir, DirStatus.VERIFYING)
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
        dirRepository.save(parent, dir);
    }

    @Override
    public void onImageFound(Directory parent, Image img) {
        Image dbImg = imgRepository.findBy(img.getPath());

        // If image is new, generate thumbnails
        if (dbImg == null) {
            thumbnailsSvc.generateThumbnails(Path.of(img.getPath()));
        }

        // If image content has changed, refresh thumbnails
        else if (!img.getContentHash().equals(dbImg.getContentHash())) {
            thumbnailsSvc.refreshThumbnails(Path.of(img.getPath()));
        }

        // If image is new or attributes were changed, save to DB
        if (!img.equals(dbImg)) {
            imgRepository.save(parent, img);
        }
    }
}
