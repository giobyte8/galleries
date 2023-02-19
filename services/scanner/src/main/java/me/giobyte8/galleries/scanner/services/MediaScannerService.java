package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.dto.ScanOrder;
import me.giobyte8.galleries.scanner.exceptions.ContentDirNotFound;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import me.giobyte8.galleries.scanner.scanners.MediaScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class MediaScannerService {
    Logger log = LoggerFactory.getLogger(MediaScannerService.class);

    private final ScannerProps scannerProps;
    private final MediaScanner mediaScanner;
    private final ContentDirDao dirDao;
    private final ContentDirService dirService;

    public MediaScannerService(
            ScannerProps scannerProps,
            MediaScanner mediaScanner,
            ContentDirDao dirDao,
            ContentDirService dirService) {
        this.scannerProps = scannerProps;
        this.mediaScanner = mediaScanner;
        this.dirDao = dirDao;
        this.dirService = dirService;
    }

    public void scan(ScanOrder order) {
        ContentDir dir = dirDao
                .findById(order.dirHPath())
                .orElseThrow(ContentDirNotFound::new);
        dirService.updateAllFilesStatus(dir, MediaFileStatus.IN_REVIEW);

        File physicalDir = new File(
                scannerProps.getContentDirs().getRootPath(),
                dir.getPath()
        );
        if (!physicalDir.isDirectory()) {
            log.error(
                    "Directory path is not a valid dir: {}",
                    physicalDir.getAbsolutePath()
            );
        }

        // TODO Update directory scanning dates

        // Start dir scanning
//        mediaScanner.scan();
        // TODO Implement metadata extractor

        // TODO Delete media files "In review"
    }
}
