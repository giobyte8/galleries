package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.ContentDirHasMFile;
import me.giobyte8.galleries.scanner.model.MediaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class ContDirDataSvc {
    Logger log = LoggerFactory.getLogger(ContDirDataSvc.class);

    private final ContentDirDao dirDao;
    private final MediaFileDao mFileDao;
    private final ContentDirHasMFileDao hasMFileDao;

    public ContDirDataSvc(
            ContentDirDao dirDao,
            MediaFileDao mFileDao,
            ContentDirHasMFileDao hasMFileDao) {
        this.dirDao = dirDao;
        this.mFileDao = mFileDao;
        this.hasMFileDao = hasMFileDao;
    }

    /**
     * Persists given content directory, each media file and
     * creates association between directory and each file
     *
     * @param dir Content directory
     * @param mFiles Media files to persist and associate
     */
    @Transactional
    public void saveWithFiles(ContentDir dir, MediaFile... mFiles) {
        dirDao.save(dir);
        this.associateFiles(dir, mFiles);
    }

    /**
     * Persist each given media file and creates association
     * between directory and each file.
     * <br/><br/>
     *
     * NOTE: Content directory IS NOT persisted, use
     * {@link #saveWithFiles(ContentDir, MediaFile...)} instead if you need to
     * save dir along with media files.
     *
     * @param dir Content directory containing files (NOT PERSISTED)
     * @param mFiles Media files to persist and associate to directory
     */
    @Transactional
    public void associateFiles(ContentDir dir, MediaFile... mFiles) {
        Arrays.stream(mFiles).forEach(mFile -> {
            ContentDirHasMFile hasMFile = new ContentDirHasMFile();
            hasMFile.setFileHashedPath(mFile.getHashedPath());
            hasMFile.setDirHashedPath(dir.getHashedPath());

            mFileDao.save(mFile);
            hasMFileDao.save(hasMFile);
        });
    }

    /**
     * Deletes association between given dir and media file
     * @param dir Content directory
     * @param mFile Media file associated to dir
     */
    @Transactional
    public void removeAssociation(ContentDir dir, MediaFile mFile) {
        int deleteCount = hasMFileDao.deleteByKeys(
                dir.getHashedPath(),
                mFile.getHashedPath()
        );

        log.debug(
                "{} records were deleted from intermediate table " +
                        "for dir: {} and file: {}",
                deleteCount,
                dir.getHashedPath(),
                mFile.getHashedPath()
        );
    }
}
