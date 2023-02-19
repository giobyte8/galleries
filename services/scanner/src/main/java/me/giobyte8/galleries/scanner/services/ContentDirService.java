package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import org.springframework.stereotype.Service;

@Service
public class ContentDirService {

    private final ContentDirDao dirDao;

    public ContentDirService(ContentDirDao dirDao) {
        this.dirDao = dirDao;
    }

    /**
     * Updates status of each media file associated to given directory
     * @param dir Directory that contains media files to be updated
     * @param status Status to assign to media files
     */
    public void updateAllFilesStatus(
            ContentDir dir,
            MediaFileStatus status) {
        dir.getFiles().forEach(mFile -> mFile.setStatus(status));
        dirDao.save(dir);
    }
}
