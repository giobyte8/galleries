package me.giobyte8.galleries.scanner.scanners;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.metadata.ImgMetaExtractor;
import me.giobyte8.galleries.scanner.model.DirStatus;
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

@Service
@Slf4j
public class LFSDirMediaScanner implements DirMediaScanner {

    private final ScannerProps scannerProps;
    private final PathService pathSvc;
    private final HashingService hashingSvc;
    private final ImgMetaExtractor imgMetaExtractor;
    private final ScanMediaObserver scanMediaObserver;

    public LFSDirMediaScanner(
            ScannerProps scannerProps,
            PathService pathSvc,
            HashingService hashingSvc,
            ImgMetaExtractor imgMetaExtractor,
            ScanMediaObserver scanMediaObserver
    ) {
        this.scannerProps = scannerProps;
        this.pathSvc = pathSvc;
        this.hashingSvc = hashingSvc;
        this.imgMetaExtractor = imgMetaExtractor;
        this.scanMediaObserver = scanMediaObserver;
    }

    @Override
    public void scan(ScanRequest scanReq, Directory dir) {
        scanMediaObserver.onScanStarted(scanReq);
        Queue<Directory> dirs = new ArrayDeque<>();
        dirs.offer(dir);

        scanNext(scanReq, dirs);
        scanMediaObserver.onScanCompleted(scanReq);
    }

    private void scanNext(ScanRequest scanReq, Queue<Directory> dirs) {
        if (dirs.isEmpty()) return;

        Directory directory = dirs.poll();
        scanMediaObserver.prepareForScanning(directory);

        Path dirAbsPath = pathSvc.toAbsolute(directory.getPath());
        try (Stream<Path> fStream = Files.list(dirAbsPath)) {
            fStream

                    // Handle nested found directories
                    .filter(absPath -> {
                        if (Files.isDirectory(absPath)) {
                            onDirectoryFound(dirs, directory, absPath);
                        }

                        return !Files.isDirectory(absPath);
                    })

                    // Handle found files
                    .filter(this::hasMediaExtension)
                    .forEach(absPath -> onImageFound(scanReq, directory, absPath));
        } catch (IOException e) {
            scanMediaObserver.onScanFailed(directory, e);
            return;
        }

        scanMediaObserver.onScanCompleted(scanReq, directory);
        scanNext(scanReq, dirs);
    }

    private void onDirectoryFound(
            Queue<Directory> dirsToScan,
            Directory parent,
            Path childDirAbsPath
    ) {

        // Only process nested dir if parent dir is 'recursive'
        if (parent.isRecursive()) {
            Directory childDir = Directory.builder()
                    .path(pathSvc.toRelative(childDirAbsPath).toString())
                    .recursive(parent.isRecursive())
                    .status(DirStatus.SCAN_PENDING)
                    .build();
            scanMediaObserver.onDirectoryFound(parent, childDir);

            // Enqueue for scanning
            dirsToScan.offer(childDir);
        }
    }

    private void onImageFound(ScanRequest scanReq, Directory parent, Path absPath) {
        try {
            String contentHash = hashingSvc.hashContent(absPath);
            Image img = Image.builder()
                    .path(pathSvc.toRelative(absPath).toString())
                    .contentHash(contentHash)
                    .build();

            img.setMetadata(imgMetaExtractor.extract(absPath));
            scanMediaObserver.onImageFound(scanReq, parent, img);
        } catch (IOException e) {
            log.error("Error while hashing content: {}", absPath, e);
        }
    }

    private boolean hasMediaExtension(Path absPath) {
        String sPath = absPath.toString();
        String ext = sPath
                .substring(sPath.lastIndexOf(".") + 1)
                .toLowerCase();

        return scannerProps
                .getMediaFilesExtensions()
                .contains(ext);
    }
}
