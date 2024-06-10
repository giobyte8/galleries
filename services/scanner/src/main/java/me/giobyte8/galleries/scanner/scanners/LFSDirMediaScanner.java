package me.giobyte8.galleries.scanner.scanners;

import lombok.extern.slf4j.Slf4j;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.metadata.ImgMetaExtractor;
import me.giobyte8.galleries.scanner.model.DirStatus;
import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
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
    private final ScanEventsObserver scanEventsObserver;

    private final DirectoryRepository dirRepository;

    public LFSDirMediaScanner(
            ScannerProps scannerProps,
            PathService pathSvc,
            HashingService hashingSvc,
            ImgMetaExtractor imgMetaExtractor,
            ScanEventsObserver scanEventsObserver,
            DirectoryRepository dirRepository
    ) {
        this.scannerProps = scannerProps;
        this.pathSvc = pathSvc;
        this.hashingSvc = hashingSvc;
        this.imgMetaExtractor = imgMetaExtractor;
        this.scanEventsObserver = scanEventsObserver;
        this.dirRepository = dirRepository;
    }

    @Override
    public void scan(Directory dir) {
        Queue<Directory> dirs = new ArrayDeque<>();
        dirs.offer(dir);

        scanNext(dirs);
    }

    private void scanNext(Queue<Directory> dirs) {
        if (dirs.isEmpty()) return;

        Directory directory = dirs.poll();
        log.debug("Scanning directory: {}", directory.getPath());

        // TODO Set all images in 'dir' to 'VERIFYING' status

        // Verify directory path is readable
        Path dirAbsPath = pathSvc.toAbsolute(directory.getPath());
        if (!Files.isDirectory(dirAbsPath)) {
            log.error("Given path is not a directory: {}", dirAbsPath);
            scanNext(dirs);
            return;
        }

        directory.setStatus(DirStatus.SCAN_IN_PROGRESS);
        dirRepository.save(directory);

        try (Stream<Path> fStream = Files.list(dirAbsPath)) {
            fStream

                    // Handle nested found directories
                    .filter(absPath -> {
                        if (Files.isDirectory(absPath)) {
                            handleFoundDir(dirs, directory, absPath);
                        }

                        return !Files.isDirectory(absPath);
                    })

                    // Handle found files
                    .filter(this::hasValidExtension)
                    .forEach(absPath -> handleFoundImage(directory, absPath));

        } catch (IOException e) {
            log.error("Error while scanning directory: {}", dirAbsPath, e);
        }

        // TODO Remove all images in 'dir' and in 'VERIFYING' status
        scanNext(dirs);
    }

    private void handleFoundDir(
            Queue<Directory> dirsToScan,
            Directory parent,
            Path childDirAbsPath
    ) {
        if (Files.isDirectory(childDirAbsPath) && parent.isRecursive()) {
            Directory childDir = Directory.builder()
                    .path(pathSvc.toRelative(childDirAbsPath).toString())
                    .recursive(parent.isRecursive())
                    .status(DirStatus.SCAN_PENDING)
                    .build();
            scanEventsObserver.onDirectoryFound(parent, childDir);

            // Enqueue for scanning
            dirsToScan.offer(childDir);
        }
    }

    private void handleFoundImage(Directory parent, Path absPath) {
        try {
            String contentHash = hashingSvc.hashContent(absPath);
            Image img = Image.builder()
                    .path(pathSvc.toRelative(absPath).toString())
                    .contentHash(contentHash)
                    .build();

            img.setMetadata(imgMetaExtractor.extract(absPath));
            scanEventsObserver.onImageFound(parent, img);
        } catch (IOException e) {
            log.error("Error while hashing content: {}", absPath, e);
        }
    }

    private boolean hasValidExtension(Path absPath) {
        String sPath = absPath.toString();
        String ext = sPath
                .substring(sPath.lastIndexOf(".") + 1)
                .toLowerCase();

        return scannerProps
                .getMediaFilesExtensions()
                .contains(ext);
    }
}
