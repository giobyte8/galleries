package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.dao.MediaFileDao;
import me.giobyte8.galleries.scanner.dto.MFMetadata;
import me.giobyte8.galleries.scanner.dto.UpsertDiscoveredFileResult;
import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import me.giobyte8.galleries.scanner.dto.FDiscoveryEventType;
import me.giobyte8.galleries.scanner.scanners.MetadataExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class MediaFileService {
    private final Logger log = LoggerFactory.getLogger(MediaFileService.class);

    private final MetadataExtractor metaExtractor;
    private final HashingService hashingService;
    private final PathService pathService;
    private final MediaFileDao mFileDao;

    public MediaFileService(
            MetadataExtractor metaExtractor,
            HashingService hashingService,
            PathService pathService,
            MediaFileDao mFileDao) {
        this.metaExtractor = metaExtractor;
        this.hashingService = hashingService;
        this.pathService = pathService;
        this.mFileDao = mFileDao;
    }

    /**
     * <pre>
     *     1. Look for file in database
     *     2. If file exists in DB, then
     *        1. If content's hash changed, update metadata,
     *           hash and status in DB
     *        2. Otherwise, nothing to do, update file's status
     *           to 'ready'
     *     3. If file not found in DB, then insert it
     * </pre>
     *
     * @param relFPath Relative path to file from content's root directory
     * @param contentHash Hash of file's content
     * @return Indicates if discovered file is a new file, an updated file or
     *         an existent file without changes.
     */
    public UpsertDiscoveredFileResult upsertDiscoveredFile(
            Path relFPath,
            String contentHash) {
        UpsertDiscoveredFileResult upsertResult;
        String hashedFPath = hashingService.hashPath(relFPath);

        // Look file in database
        Optional<MediaFile> dbMFileOpt = mFileDao.findById(hashedFPath);

        // New or updated file was found
        if (dbMFileOpt.isEmpty() || !dbMFileOpt.get().getHash().equals(contentHash)) {

            // Load from DB or create a new object
            MediaFile mFile = dbMFileOpt.orElseGet(() -> {
                MediaFile newMFile = new MediaFile();
                newMFile.setHashedPath(hashedFPath);
                newMFile.setPath(relFPath.toString());
                return newMFile;
            });

            try {
                refreshMetadata(mFile);
            } catch (IOException e) {
                log.error(
                        "Error while reading file metadata: {} - {}",
                        pathService.toAbsolute(mFile.getPath()),
                        e.getMessage()
                );
            }
            mFile.setHash(contentHash);
            mFile.setStatus(MediaFileStatus.READY);
            mFileDao.save(mFile);

            upsertResult = new UpsertDiscoveredFileResult(
                    mFile,
                    dbMFileOpt.isEmpty()
                            ? FDiscoveryEventType.NEW_FILE_FOUND
                            : FDiscoveryEventType.FILE_CHANGED
            );
        }

        // File found and content's didn't change
        else {
            MediaFile mFile = dbMFileOpt.orElseThrow();
            mFile.setStatus(MediaFileStatus.READY);
            mFileDao.save(mFile);

            upsertResult = new UpsertDiscoveredFileResult(
                    mFile,
                    FDiscoveryEventType.EXISTENT_FILE_FOUND
            );
        }

        return upsertResult;
    }

    /**
     * Reads metadata from physical file and updates
     * object attributes with read values
     *
     * @param mFile Media file to be updated
     */
    public void refreshMetadata(MediaFile mFile) throws IOException {
        Path absFPath = pathService.toAbsolute(mFile.getPath());
        MFMetadata meta = metaExtractor.extract(absFPath);

        mFile.setMetadata(meta);
    }
}
