package me.giobyte8.galleries.scanner.scanners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.metadata.MediaMetaExtractor;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.Video;
import me.giobyte8.galleries.scanner.scanners.listeners.ScanEventsHub;
import me.giobyte8.galleries.scanner.services.HashingService;
import me.giobyte8.galleries.services.ImageService;
import me.giobyte8.galleries.scanner.services.PathService;
import me.giobyte8.galleries.services.VideoService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Media scanner implementation that works for paths located on
 * local filesystem(s).
 * <br/>
 *
 * It scans directories and their nested subdirectories (if 'recursive' is set).
 * Notice that it doesn't have any side effects per se, instead, it triggers
 * events for each directory and image found so that other components
 * can handle it appropriately.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class LocalMediaScanner implements MediaScanner {

    private final ScannerProps scannerProps;
    private final PathService pathSvc;
    private final HashingService hashingSvc;
    private final MediaMetaExtractor mediaMetaExtractor;
    private final ImageService imageSvc;
    private final VideoService videoSvc;
    private final ScanEventsHub eventsHub;

    private final Queue<Directory> scanPendingQueue = new ArrayDeque<>();

    @Override
    public void scan(Directory dir) {
        eventsHub.onScanStarted();

        scanPendingQueue.offer(dir);
        scanNext();

        eventsHub.onScanCompleted();
    }

    private void scanNext() {
        if (scanPendingQueue.isEmpty()) return;

        Directory dir = scanPendingQueue.poll();
        eventsHub.onScanStarted(dir);

        Path dirAbsPath = pathSvc.toAbsolute(dir.getPath());
        try (Stream<Path> fStream = Files.list(dirAbsPath)) {
            fStream.forEach(absPath -> {
                if (Files.isDirectory(absPath)) {
                    onDirFound(dir, absPath);
                }

                // Handle image files
                else if (hasImageExtension(absPath)) {
                    onImageFound(dir, absPath);
                }

                else if (hasVideoExtension(absPath)) {
                    onVideoFound(dir, absPath);
                }
            });

            eventsHub.onScanCompleted(dir);
        } catch (IOException e) {
            eventsHub.onScanFailed(dir, e);
        } finally {
            scanNext();
        }
    }

    /**
     * Callback invoked when a directory is found during scanning.
     *
     * @param absPath The absolute path of the found directory.
     */
    private void onDirFound(Directory parent, Path absPath) {
        if (!parent.isRecursive()) return;

        var relPath = pathSvc.toRelative(absPath).toString();
        Directory dir = Directory.builder()
                .path(relPath)
                .recursive(parent.isRecursive())
                .build();

        eventsHub.onDirFound(parent, dir);
        scanPendingQueue.offer(dir);
    }

    /**
     * Callback invoked when an image is found during scanning.
     *
     * @param parent Directory where given image was found.
     * @param absPath The absolute path of the found image.
     */
    private void onImageFound(Directory parent, Path absPath) {
        try {
            String path = pathSvc.toRelative(absPath).toString();
            String contentHash = hashingSvc.hashContent(absPath);

            Image foundImage = Image.builder()
                    .path(path)
                    .contentHash(contentHash)
                    .build();

            // Look for same image found during previous scans
            var dbImageOpt = imageSvc.findByPath(path);
            if (dbImageOpt.isPresent()) {
                var dbImage = dbImageOpt.get();

                // Hashes match, image has not changed
                if (dbImage.getContentHash().equals(contentHash)) {
                    eventsHub.onUnchangedImageFound(parent, dbImage);
                }

                // Hashes don't match, image has been updated
                else {
                    foundImage.setMetadata(mediaMetaExtractor.extract(absPath));
                    eventsHub.onUpdatedImageFound(parent, foundImage);
                }
            }

            // No preexistent image. New image has been discovered
            else {
                foundImage.setMetadata(mediaMetaExtractor.extract(absPath));
                eventsHub.onNewImageFound(parent, foundImage);
            }
        } catch (IOException e) {
            log.error("Error while hashing content: {}", absPath, e);
        }
    }

    /**
     * Callback invoked when a video is found during scanning.
     *
     * @param parent Directory where given video was found.
     * @param absPath The absolute path of the found video.
     */
    private void onVideoFound(Directory parent, Path absPath) {
        try {
            String path = pathSvc.toRelative(absPath).toString();
            String contentHash = hashingSvc.hashContent(absPath);

            Video foundVideo = Video.builder()
                    .path(path)
                    .contentHash(contentHash)
                    .build();

            var dbVideoOpt = videoSvc.findByPath(path);
            if (dbVideoOpt.isPresent()) {
                var dbVideo = dbVideoOpt.get();

                if (dbVideo.getContentHash().equals(contentHash)) {
                    eventsHub.onUnchangedVideoFound(parent, dbVideo);
                }

                else {
                    foundVideo.setMetadata(mediaMetaExtractor.extract(absPath));
                    eventsHub.onUpdatedVideoFound(parent, foundVideo);
                }
            }

            else {
                foundVideo.setMetadata(mediaMetaExtractor.extract(absPath));
                eventsHub.onNewVideoFound(parent, foundVideo);
            }
        } catch (IOException e) {
            log.error("Error while hashing content: {}", absPath, e);
        }
    }

    private boolean hasImageExtension(Path absPath) {
        return hasExtension(absPath, scannerProps.getImageFileExtensions());
    }

    private boolean hasVideoExtension(Path absPath) {
        return hasExtension(absPath, scannerProps.getVideoFileExtensions());
    }

    private boolean hasExtension(Path absPath, java.util.Set<String> extensions) {
        String sPath = absPath.toString();
        String ext = sPath
                .substring(sPath.lastIndexOf(".") + 1)
                .toLowerCase();

        return extensions.contains(ext);
    }
}
