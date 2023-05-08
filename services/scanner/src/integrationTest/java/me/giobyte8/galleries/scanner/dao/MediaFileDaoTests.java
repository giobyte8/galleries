package me.giobyte8.galleries.scanner.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ HashingService.class, ContDirDataSvc.class })
class MediaFileDaoTests {

    @Autowired
    private MediaFileDao mFileDao;

    @Autowired
    private ContDirDataSvc contDirDataSvc;

    @Autowired
    private HashingService hashingService;

    @PersistenceContext
    private EntityManager entityMgr;

    @Test
    void saveMFile() {
        String testFilePath = "/pgalleries/portraits/random.jpeg";
        String hashedFPath = hashingService.hashPath(testFilePath);
        MediaFile mFile = new MediaFile();
        mFile.setHashedPath(hashedFPath);
        mFile.setPath(testFilePath);
        mFile.setHash(hashedFPath);
        mFileDao.save(mFile);

        MediaFile dbMFile = mFileDao
                .findById(hashedFPath)
                .orElseThrow();
        assertThat(dbMFile.getPath()).isEqualTo(testFilePath);
        assertThat(dbMFile.getHash()).isEqualTo(hashedFPath);
    }

    @Test
    void deleteInReviewNotAssociatedMediaFiles() {
        String mfPath1 = "path1/";
        String mfPath2 = "path2/";
        String mfPath3 = "path3/";
        String hashedPath1 = hashingService.hashPath(mfPath1);

        String dirPath = "dir/path";
        String hashedDPath = hashingService.hashPath(dirPath);

        MediaFile mFile1 = new MediaFile();
        mFile1.setHashedPath(hashedPath1);
        mFile1.setPath(mfPath1);
        mFile1.setHash(hashedPath1);

        MediaFile mFile2 = new MediaFile();
        mFile2.setHashedPath(hashingService.hashPath(mfPath2));
        mFile2.setPath(mfPath2);
        mFile2.setHash(hashingService.hashPath(mfPath2));
        mFile2.setStatus(MediaFileStatus.IN_REVIEW);

        MediaFile mFile3 = new MediaFile();
        mFile3.setHashedPath(hashingService.hashPath(mfPath3));
        mFile3.setPath(mfPath3);
        mFile3.setHash(hashingService.hashPath(mfPath3));
        mFile3.setStatus(MediaFileStatus.IN_REVIEW);

        // Associate first media file with a content directory
        ContentDir dir = new ContentDir();
        dir.setHashedPath(hashedDPath);
        dir.setPath(dirPath);

        contDirDataSvc.saveWithFiles(dir, mFile1);

        // Save 'In review' media files
        mFileDao.saveAll(Arrays.asList(mFile2, mFile3));

        // Verify all 3 files were saved
        Assertions.assertThat(mFileDao.count()).isEqualTo(3);

        // Delete 'In review' and orphan files
        Assertions
                .assertThat(mFileDao.deleteByStatusAndMediaDirsEmpty(
                        MediaFileStatus.IN_REVIEW.name()
                ))
                .isEqualTo(2);

        Assertions.assertThat(mFileDao.count()).isEqualTo(1);
        Assertions
                .assertThat(mFileDao.countByStatus(
                        MediaFileStatus.READY
                ))
                .isEqualTo(1);
    }

    @Test
    void findAllInReviewAndNotAssociatedDirectory() {
        String mfPath1 = "path1/";
        String mfPath2 = "path2/";
        String mfPath3 = "path3/";
        String hashedPath1 = hashingService.hashPath(mfPath1);

        String dirPath = "dir/path";
        String hashedDPath = hashingService.hashPath(dirPath);

        MediaFile mFile1 = new MediaFile();
        mFile1.setHashedPath(hashedPath1);
        mFile1.setPath(mfPath1);
        mFile1.setHash(hashedPath1);

        MediaFile mFile2 = new MediaFile();
        mFile2.setHashedPath(hashingService.hashPath(mfPath2));
        mFile2.setPath(mfPath2);
        mFile2.setHash(hashingService.hashPath(mfPath2));
        mFile2.setStatus(MediaFileStatus.IN_REVIEW);

        MediaFile mFile3 = new MediaFile();
        mFile3.setHashedPath(hashingService.hashPath(mfPath3));
        mFile3.setPath(mfPath3);
        mFile3.setHash(hashingService.hashPath(mfPath3));
        mFile3.setStatus(MediaFileStatus.IN_REVIEW);

        // Associate first media file with a content directory
        ContentDir dir = new ContentDir();
        dir.setHashedPath(hashedDPath);
        dir.setPath(dirPath);
        contDirDataSvc.saveWithFiles(dir, mFile1);

        // Save 'In review' media files
        mFileDao.saveAll(Arrays.asList(mFile2, mFile3));
        Assertions.assertThat(mFileDao.count()).isEqualTo(3);

        // Run query for "orphan" and 'IN_REVIEW' files
        Stream<MediaFile> mFilesStream = mFileDao.findAllByStatusAndMediaDirsEmpty(
                MediaFileStatus.IN_REVIEW.name()
        );

        // Assert query returned expected files
        List<MediaFile> dbMFiles = mFilesStream
                .filter(mFile -> mFile.equals(mFile2) || mFile.equals(mFile3))
                .toList();
        mFilesStream.close();

        assertThat(dbMFiles)
                .contains(mFile2, mFile3)
                .hasSize(2);
    }

    @Test
    void findAllMediaFilesInReviewAndAssociatedToDir() {
        String mfPath1 = "path1/";
        String mfPath2 = "path2/";
        String mfPath3 = "path3/";

        String dirPath = "dir/path";
        String hashedDPath = hashingService.hashPath(dirPath);

        MediaFile mFile1 = new MediaFile();
        mFile1.setHashedPath(hashingService.hashPath(mfPath1));
        mFile1.setPath(mfPath1);
        mFile1.setHash(hashingService.hashPath(mfPath1));

        MediaFile mFile2 = new MediaFile();
        mFile2.setHashedPath(hashingService.hashPath(mfPath2));
        mFile2.setPath(mfPath2);
        mFile2.setHash(hashingService.hashPath(mfPath2));
        mFile2.setStatus(MediaFileStatus.IN_REVIEW);

        MediaFile mFile3 = new MediaFile();
        mFile3.setHashedPath(hashingService.hashPath(mfPath3));
        mFile3.setPath(mfPath3);
        mFile3.setHash(hashingService.hashPath(mfPath3));
        mFile3.setStatus(MediaFileStatus.IN_REVIEW);

        // Save media files and directory
        ContentDir dir = new ContentDir();
        dir.setHashedPath(hashedDPath);
        dir.setPath(dirPath);
        contDirDataSvc.saveWithFiles(dir, mFile1, mFile2, mFile3);

        // Force test transaction commit to DB before next query
        entityMgr.flush();

        Stream<MediaFile> mFiles = mFileDao.findByDirAndMediaFileStatus(
                dir.getHashedPath(),
                MediaFileStatus.IN_REVIEW.name()
        );
        assertThat(mFiles.count()).isEqualTo(2);
    }

    @Test
    void updateStatusByContentDir() {
        String mfPath1 = "path1/";
        String mfPath2 = "path2/";
        String mfPath3 = "path3/";

        String dirPath = "dir/path";
        String hashedDPath = hashingService.hashPath(dirPath);

        MediaFile mFile1 = new MediaFile();
        mFile1.setHashedPath(hashingService.hashPath(mfPath1));
        mFile1.setPath(mfPath1);
        mFile1.setHash(hashingService.hashPath(mfPath1));

        MediaFile mFile2 = new MediaFile();
        mFile2.setHashedPath(hashingService.hashPath(mfPath2));
        mFile2.setPath(mfPath2);
        mFile2.setHash(hashingService.hashPath(mfPath2));
        mFile2.setStatus(MediaFileStatus.IN_REVIEW);

        MediaFile mFile3 = new MediaFile();
        mFile3.setHashedPath(hashingService.hashPath(mfPath3));
        mFile3.setPath(mfPath3);
        mFile3.setHash(hashingService.hashPath(mfPath3));
        mFile3.setStatus(MediaFileStatus.IN_REVIEW);

        // Save media files and directory
        ContentDir dir = new ContentDir();
        dir.setHashedPath(hashedDPath);
        dir.setPath(dirPath);
        contDirDataSvc.saveWithFiles(dir, mFile1, mFile2, mFile3);

        int updatedCount = mFileDao.updateStatusByMediaDir(
                hashedDPath,
                MediaFileStatus.READY
        );
        assertThat(updatedCount).isEqualTo(2);

        Stream<MediaFile> mFilesReady = mFileDao.findByDirAndMediaFileStatus(
                hashedDPath,
                MediaFileStatus.READY.name()
        );

        assertThat(mFilesReady.count()).isEqualTo(3);
        mFilesReady.close();
    }
}
