package me.giobyte8.galleries.scanner.scanners;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.Video;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.Fingerprint;
import me.giobyte8.galleries.scanner.exceptions.MediaProcessingException;
import me.giobyte8.galleries.scanner.metadata.MediaMetaExtractor;
import me.giobyte8.galleries.scanner.metrics.Metric;
import me.giobyte8.galleries.scanner.metrics.MetricStr;
import me.giobyte8.galleries.scanner.metrics.MetricTag;
import me.giobyte8.galleries.scanner.metrics.MetricsService;
import me.giobyte8.galleries.scanner.scanners.listeners.ScanEventsHub;
import me.giobyte8.galleries.scanner.services.FingerprintService;
import me.giobyte8.galleries.scanner.services.PathService;
import me.giobyte8.galleries.services.ImageService;
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
    private final FingerprintService fingerprintSvc;
    private final MediaMetaExtractor mediaMetaExtractor;
    private final ImageService imageSvc;
    private final VideoService videoSvc;
    private final ScanEventsHub eventsHub;
    private final MetricsService metricsSvc;

    private final Queue<Directory> scanPendingQueue = new ArrayDeque<>();

    @Override
    @Timed(
            value = MetricStr.SCAN_REQUEST,
            description = MetricStr.SCAN_REQUEST_DESC
    )
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
        long startNs = System.nanoTime();
        String foundType = null;

        try {
            String path = pathSvc.toRelative(absPath).toString();
            var fingerprint = fingerprintSvc.forPath(absPath);

            Image foundImage = Image.builder()
                    .path(path)
                    .fileSize(fingerprint.fileSize())
                    .lastModified(fingerprint.lastModified())
                    .build();

            // Look for same image found during previous scans
            var dbImageOpt = imageSvc.findByPath(path);
            if (dbImageOpt.isPresent()) {
                var dbImage = dbImageOpt.get();

                // Fingerprints match, image has not changed
                if (Fingerprint.from(dbImage).equals(fingerprint)) {
                    foundType = "unchanged";
                    eventsHub.onUnchangedImageFound(parent, dbImage);
                }

                // Fingerprints differ, image has been updated
                else {
                    foundType = "updated";
                    foundImage.setMetadata(mediaMetaExtractor.extract(absPath));
                    eventsHub.onUpdatedImageFound(parent, foundImage);
                }
            }

            // No preexistent image. New image has been discovered
            else {
                foundType = "new";
                foundImage.setMetadata(mediaMetaExtractor.extract(absPath));
                eventsHub.onNewImageFound(parent, foundImage);
            }
        } catch (IOException e) {
            log.error("Error while hashing content: {}", absPath, e);
        } catch (MediaProcessingException e) {
            log.error("Error while processing media file: {}", absPath, e);
        } finally {
            if (foundType != null) {
                metricsSvc.record(
                        Metric.SCAN_MEDIA_FOUND,
                        startNs,
                        MetricTag.MEDIA_TYPE.getName(), "image",
                        MetricTag.FOUND_TYPE.getName(), foundType
                );
            }
        }
    }

    /**
     * Callback invoked when a video is found during scanning.
     *
     * @param parent Directory where given video was found.
     * @param absPath The absolute path of the found video.
     */
    private void onVideoFound(Directory parent, Path absPath) {
        long startNs = System.nanoTime();
        String foundType = null;

        try {
            String path = pathSvc.toRelative(absPath).toString();
            var fingerprint = fingerprintSvc.forPath(absPath);

            Video foundVideo = Video.builder()
                    .path(path)
                    .fileSize(fingerprint.fileSize())
                    .lastModified(fingerprint.lastModified())
                    .build();

            var dbVideoOpt = videoSvc.findByPath(path);
            if (dbVideoOpt.isPresent()) {
                var dbVideo = dbVideoOpt.get();

                if (Fingerprint.from(dbVideo).equals(fingerprint)) {
                    foundType = "unchanged";
                    eventsHub.onUnchangedVideoFound(parent, dbVideo);
                }

                else {
                    foundType = "updated";
                    foundVideo.setMetadata(mediaMetaExtractor.extract(absPath));
                    eventsHub.onUpdatedVideoFound(parent, foundVideo);
                }
            }

            else {
                foundType = "new";
                foundVideo.setMetadata(mediaMetaExtractor.extract(absPath));
                eventsHub.onNewVideoFound(parent, foundVideo);
            }
        } catch (IOException e) {
            log.error("Error while hashing content: {}", absPath, e);
        } catch (MediaProcessingException e) {
            log.error("Error while processing media file: {}", absPath, e);
        } finally {
            if (foundType != null) {
                metricsSvc.record(
                        Metric.SCAN_MEDIA_FOUND,
                        startNs,
                        MetricTag.MEDIA_TYPE.getName(), "video",
                        MetricTag.FOUND_TYPE.getName(), foundType
                );
            }
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
