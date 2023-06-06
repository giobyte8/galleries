package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.dao.ContDirDataSvc;
import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.dto.UpsertDiscoveredFileResult;
import me.giobyte8.galleries.scanner.exceptions.ScannerException;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.scanners.MediaScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

@Service
public class MediaScannerService {
    Logger log = LoggerFactory.getLogger(MediaScannerService.class);

    private final TransactionTemplate txTemplate;
    private final MediaScanner mediaScanner;
    private final ContentDirDao dirDao;
    private final ContDirDataSvc contDirDataSvc;
    private final ContentDirService dirService;
    private final MediaFileService mFileService;
    private final PathService pathService;

    public MediaScannerService(
            TransactionTemplate txTemplate,
            MediaScanner mediaScanner,
            ContentDirDao dirDao,
            ContDirDataSvc contDirDataSvc,
            ContentDirService dirService,
            MediaFileService mFileService,
            PathService pathService) {
        this.txTemplate = txTemplate;
        this.mediaScanner = mediaScanner;
        this.dirDao = dirDao;
        this.contDirDataSvc = contDirDataSvc;
        this.dirService = dirService;
        this.mFileService = mFileService;
        this.pathService = pathService;
    }

    public void scan(String dirHashedPath) {
        dirService.preScanHook(dirHashedPath);

        // Execute scanning and individual files updates into its
        // own transaction
        txTemplate.executeWithoutResult(txStatus -> {
            ContentDir dir = dirDao.findById(dirHashedPath).orElseThrow();

            try {
                mediaScanner.scan(
                        pathService.toAbsolute(dir.getPath()),
                        dir.isRecursive(),
                        (absFPath, contentHash) -> {
                            UpsertDiscoveredFileResult upsertMFResult = mFileService
                                    .upsertDiscoveredFile(
                                            pathService.toRelative(absFPath),
                                            contentHash
                                    );

                            // Upsert file association with content dir
                            contDirDataSvc.associateFiles(dir, upsertMFResult.mFile());

                            // TODO Emit event
                        }
                );
            } catch (IOException e) {
                log.error(
                        "Error during scanning of path: {} - {}",
                        dirHashedPath,
                        e.getMessage()
                );

                throw new ScannerException();
            }
        });

        dirService.postScanHook(dirHashedPath);
    }
}
