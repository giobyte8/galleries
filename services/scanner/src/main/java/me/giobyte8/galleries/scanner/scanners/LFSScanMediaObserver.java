package me.giobyte8.galleries.scanner.scanners;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.amqp.ScanEvents;
import me.giobyte8.galleries.scanner.dto.FDiscoveryEventType;
import me.giobyte8.galleries.scanner.dto.ScanEventType;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.model.DirStatus;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
import me.giobyte8.galleries.scanner.repository.ImageRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

import static me.giobyte8.galleries.scanner.dto.FDiscoveryEventType.FILE_NOT_FOUND;

@Service
@Slf4j
public class LFSScanMediaObserver implements ScanMediaObserver {

    private final ScanEvents scanEvents;
    private final DirectoryRepository dirRepository;
    private final ImageRepository imgRepository;

    public LFSScanMediaObserver(
            ScanEvents scanEvents,
            DirectoryRepository dirRepository,
            ImageRepository imgRepository
    ) {
        this.scanEvents = scanEvents;
        this.dirRepository = dirRepository;
        this.imgRepository = imgRepository;
    }

    @Override
    public void onScanStarted(ScanRequest scanReq) {
        scanEvents.onScanProcessEvent(scanReq, ScanEventType.SCAN_START);
    }

    @Override
    public void onScanCompleted(ScanRequest scanReq) {
        scanEvents.onScanProcessEvent(scanReq, ScanEventType.SCAN_END);
    }

    @Override
    public void onScanFailed(ScanRequest scanReq) {
        log.error("Scan request failed: {}", scanReq.id());
    }

    @Override
    public void prepareForScanning(Directory directory) {
        log.debug("Scanning directory: {}", directory.getPath());

        // Set all images in 'directory' to 'VERIFYING' status
        imgRepository.update(directory, ImageStatus.VERIFYING);

        // Set all children directories from 'directory' to 'VERIFYING' status
        dirRepository.updateByParent(directory, DirStatus.VERIFYING);

        directory.setStatus(DirStatus.SCAN_IN_PROGRESS);
        dirRepository.save(directory);
    }

    @Override
    public void onScanCompleted(ScanRequest scanReq, Directory directory) {

        // If an image is still in 'VERIFYING' status, that means it
        // wasn't found during scanning.
        // Remove every image that remains in 'VERIFYING' status.
        imgRepository
                .deleteAndGetPaths(directory, ImageStatus.VERIFYING)
                .forEach(path -> scanEvents.onFileDiscoveryEvent(
                        scanReq,
                        FILE_NOT_FOUND,
                        path
                ));

        // Process children directories that remains in 'VERIFYING' status
        dirRepository
                .findBy(directory, DirStatus.VERIFYING)
                .forEach(notFoundDir -> {

                    // Remove all descendant images from not found dir and
                    // from subdirectories and emit event for each deleted image
                    imgRepository
                            .multilevelDeleteAndGetPaths(notFoundDir)
                            .forEach(imgPath -> scanEvents.onFileDiscoveryEvent(
                                    scanReq,
                                    FILE_NOT_FOUND,
                                    imgPath
                            ));

                    // Delete directory and its subdirectories
                    dirRepository.deleteWithDescendants(notFoundDir);
                });

        directory.setStatus(DirStatus.SCAN_COMPLETE);
        dirRepository.save(directory);
    }

    @Override
    public void onScanFailed(Directory directory, IOException e) {
        log.error("Error while scanning directory: {}", directory.getPath(), e);

        directory.setStatus(DirStatus.SCAN_FAILED);
        dirRepository.save(directory);
    }

    @Override
    public void onDirectoryFound(Directory parent, Directory directory) {
        dirRepository.save(parent, directory);
    }

    @Override
    public void onImageFound(ScanRequest scanReq, Directory parent, Image image) {
        Image dbImg = imgRepository.findBy(image.getPath());
        if (dbImg == null) {
            scanEvents.onFileDiscoveryEvent(
                    scanReq,
                    FDiscoveryEventType.NEW_FILE_FOUND,
                    image.getPath()
            );
        }

        else if (Objects.equals(dbImg.getContentHash(), image.getContentHash())) {
            scanEvents.onFileDiscoveryEvent(
                    scanReq,
                    FDiscoveryEventType.FILE_CHANGED,
                    image.getPath()
            );
        }

        imgRepository.save(parent, image);
    }
}
