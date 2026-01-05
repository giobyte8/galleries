package me.giobyte8.galleries.scanner.scanners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.metadata.ImgMetaExtractor;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.services.HashingService;
import me.giobyte8.galleries.scanner.services.PathService;
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
    private final ImgMetaExtractor imgMetaExtractor;
    private final ScanEventsHub eventsHub;

    private final Queue<Directory> scanPendingQueue = new ArrayDeque<>();

    @Override
    public void scan(Directory dir) {

        // TODO Refactor into events hub 'scanStarted/beforeScan(ScanRequest)
        //scanMediaObserver.onScanStarted(scanReq);

        scanPendingQueue.offer(dir);
        scanNext();

        // TODO Refactor into events hub 'scanCompleted/afterScan(ScanRequest)
        //scanMediaObserver.onScanCompleted(scanReq);
    }

    private void scanNext() {
        if (scanPendingQueue.isEmpty()) return;

        Directory dir = scanPendingQueue.poll();
        eventsHub.scanStarted(dir);

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

                // Handle video files...
                // else if (hasVideoExtension(absPath)) {
            });
        } catch (IOException e) {
            // TODO scanMediaObserver.onScanFailed(dir, e);
            return;
        }

        eventsHub.scanCompleted(dir);
        scanNext();
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

        eventsHub.dirFound(parent, dir);
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
            String contentHash = hashingSvc.hashContent(absPath);

            Image img = Image.builder()
                    .path(pathSvc.toRelative(absPath).toString())
                    .contentHash(contentHash)
                    .build();
            img.setMetadata(imgMetaExtractor.extract(absPath));

            eventsHub.imgFound(parent, img);
        } catch (IOException e) {
            log.error("Error while hashing content: {}", absPath, e);
        }
    }

    private boolean hasImageExtension(Path absPath) {
        String sPath = absPath.toString();
        String ext = sPath
                .substring(sPath.lastIndexOf(".") + 1)
                .toLowerCase();

        return scannerProps
                .getImageFileExtensions()
                .contains(ext);
    }
}
