package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Implements functionality to look for files in directories that are
 * accessible from the local file system
 */
@Service
public class LocalFSScanner implements MediaScanner {
    Logger log = LoggerFactory.getLogger(LocalFSScanner.class);

    private final ScannerProps scannerProps;
    private final HashingService hashingService;

    public LocalFSScanner(ScannerProps scannerProps,
                          HashingService hashingService) {
        this.scannerProps = scannerProps;
        this.hashingService = hashingService;
    }

    @Override
    public void scan(
            Path absDirPath,
            boolean recursive,
            OnFileFoundCB cb
    ) throws IOException {
        log.info("Scanning for files at: {}", absDirPath);
        if (!Files.isDirectory(absDirPath)) {
            throw new IOException(
                    "Provided media dir is not a valid directory"
            );
        }

        try (Stream<Path> fStream = Files.list(absDirPath)) {
            fStream
                    // Filter nested directories
                    .filter(absPath -> {

                        // Scan recursively is required
                        if (Files.isDirectory(absPath) && recursive) {
                            try {
                                this.scan(absPath, true, cb);
                            } catch (IOException e) {
                                log.error(
                                        "IOException while scanning nested dir: {} - {}",
                                        absPath,
                                        e.getMessage()
                                );
                            }
                        }

                        return !Files.isDirectory(absPath);
                    })

                    // Filter only media files (By extension)
                    .filter(absFPath -> {
                        String sPath = absFPath.toString();
                        String ext = sPath
                                .substring(sPath.lastIndexOf(".") + 1)
                                .toLowerCase();

                        return scannerProps
                                .getMediaFilesExtensions()
                                .contains(ext);
                    })

                    // Get content hash and invoke callback
                    .forEach(absFPath -> {
                        log.debug("File found during scan: {}", absFPath);

                        try {
                            String fHash = hashingService.hashContent(absFPath);
                            cb.onFileFound(absFPath, fHash);
                        } catch (IOException e) {
                            log.error(
                                    "IOException while hashing file: {} - {}",
                                    absFPath,
                                    e.getMessage()
                            );
                        }
                    });
        }
    }
}
