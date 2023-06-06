package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.amqp.ScanEventsProducer;
import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.dao.ContentDirHasMFileDao;
import me.giobyte8.galleries.scanner.dao.MediaFileDao;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.ContentDirStatus;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ContentDirService {
    private static final Logger log = LoggerFactory
            .getLogger(ContentDirService.class);

    private final ContentDirDao dirDao;
    private final MediaFileDao mFileDao;
    private final ContentDirHasMFileDao hasMFileDao;
    private final ScanEventsProducer eventsProducer;

    public ContentDirService(
            ContentDirDao dirDao,
            MediaFileDao mFileDao,
            ContentDirHasMFileDao hasMFileDao,
            ScanEventsProducer eventsProducer) {
        this.dirDao = dirDao;
        this.mFileDao = mFileDao;
        this.hasMFileDao = hasMFileDao;
        this.eventsProducer = eventsProducer;
    }

    /**
     * Prepares directory and its contained files for scanning
     * @param dirHashedPath Content directory about to being scanned
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void preScanHook(String dirHashedPath) {
        ContentDir contentDir = dirDao
                .findById(dirHashedPath)
                .orElseThrow();
        contentDir.setLastScanCompletion(null);
        contentDir.setLastScanStart(new Date());
        contentDir.setStatus(ContentDirStatus.SCAN_IN_PROGRESS);
        dirDao.save(contentDir);

        int updatedMFilesCount = mFileDao.updateStatusByMediaDir(
                contentDir.getHashedPath(),
                MediaFileStatus.IN_REVIEW
        );
        log.info(
                "{} Media files where updated to 'IN_REVIEW' for dir: {}",
                updatedMFilesCount,
                contentDir.getPath()
        );

        // TODO: Send monitoring notification (?)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postScanHook(String dirHashedPath) {
        ContentDir contentDir = dirDao
                .findById(dirHashedPath)
                .orElseThrow();
        contentDir.setLastScanCompletion(new Date());
        contentDir.setStatus(ContentDirStatus.SCAN_COMPLETE);
        dirDao.save(contentDir);

        // Remove association with all files that remain as 'IN_REVIEW'
        int deletedAssociationsCount = hasMFileDao.deleteAllByDirHashedPathAndMFileStatus(
                contentDir.getHashedPath(),
                MediaFileStatus.IN_REVIEW
        );
        log.debug(
                "{} records removed from 'dir_contains_mfile' in preparation for " +
                        "files deletion",
                deletedAssociationsCount
        );

        // Publish amqp message for each missing media file
        mFileDao
                .findAllByStatusAndMediaDirsEmpty(MediaFileStatus.IN_REVIEW.name())
                .forEach(mFile ->
                        eventsProducer.onScannedFileNotFound(mFile.getPath())
                );

        // Delete from db every file with no owners
        int deletedCount = mFileDao.deleteByStatusAndMediaDirsEmpty(
                MediaFileStatus.IN_REVIEW.name()
        );
        log.info(
                "{} media files removed from DB since were not found during last scan of dir: {}",
                deletedCount,
                contentDir.getPath()
        );

        // TODO: Send monitoring notification (?)
    }
}
