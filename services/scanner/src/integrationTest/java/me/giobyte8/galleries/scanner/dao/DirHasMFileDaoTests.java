package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.*;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ HashingService.class, ContDirDataSvc.class })
class DirHasMFileDaoTests {

    @Autowired
    private ContentDirDao dirDao;

    @Autowired
    private MediaFileDao mFileDao;

    @Autowired
    private ContentDirHasMFileDao hasMFileDao;

    @Autowired
    private ContDirDataSvc contDirDataSvc;

    @Autowired
    private HashingService hashingService;

    private final String testDirPath = "/pgalleries/portraits";
    private final String testFilePath1 = "/pgalleries/portraits/random.jpeg";
    private final String testFilePath2 = "/pgalleries/portraits/random2.jpeg";

    @AfterEach
    void cleanupDb() {
        hasMFileDao.deleteAll();
        mFileDao.deleteAll();
        dirDao.deleteAll();
    }

    @Test
    void saveContentDirQueryJoinTable() {
        ContentDir contentDir = new ContentDir();
        String hashedDPath = hashingService.hashPath(testDirPath);
        contentDir.setHashedPath(hashedDPath);
        contentDir.setPath(testDirPath);

        MediaFile mFile1 = new MediaFile();
        String hashedFPath1 = hashingService.hashPath(testFilePath1);
        mFile1.setHashedPath(hashedFPath1);
        mFile1.setPath(testFilePath1);
        mFile1.setHash(hashedFPath1);

        MediaFile mFile2 = new MediaFile();
        String hashedFPath2 = hashingService.hashPath(testFilePath2);
        mFile2.setHashedPath(hashedFPath2);
        mFile2.setPath(testFilePath2);
        mFile2.setHash(hashedFPath2);

        contDirDataSvc.saveWithFiles(contentDir, mFile1, mFile2);

        // Count of join table should be equals 2
        assertThat(hasMFileDao.count()).isEqualTo(2);

        // Find association with file 1
        DirHasMFileId hasMFileId = new DirHasMFileId();
        hasMFileId.setDirHashedPath(hashedDPath);
        hasMFileId.setFileHashedPath(hashedFPath1);
        ContentDirHasMFile hasMFile1 = hasMFileDao
                .findById(hasMFileId)
                .orElseThrow();
        assertThat(hasMFileId.getDirHashedPath())
                .isEqualTo(hasMFile1.getDirHashedPath());
        assertThat(hasMFileId.getFileHashedPath())
                .isEqualTo(hasMFile1.getFileHashedPath());

        // Find association with file 2
        DirHasMFileId hasMFileId2 = new DirHasMFileId();
        hasMFileId2.setDirHashedPath(hashedDPath);
        hasMFileId2.setFileHashedPath(hashedFPath2);
        ContentDirHasMFile hasMFile2 = hasMFileDao
                .findById(hasMFileId2)
                .orElseThrow();
        assertThat(hasMFileId2.getDirHashedPath())
                .isEqualTo(hasMFile2.getDirHashedPath());
        assertThat(hasMFileId2.getFileHashedPath())
                .isEqualTo(hasMFile2.getFileHashedPath());
    }

    @Test
    void saveJoinTableQueryAssociatedFiles() {
        ContentDir contentDir = new ContentDir();
        String hashedDPath = hashingService.hashPath(testDirPath);
        contentDir.setHashedPath(hashedDPath);
        contentDir.setPath(testDirPath);

        MediaFile mFile1 = new MediaFile();
        String hashedFPath1 = hashingService.hashPath(testFilePath1);
        mFile1.setHashedPath(hashedFPath1);
        mFile1.setPath(testFilePath1);
        mFile1.setHash(hashedFPath1);

        // Save content dir with one file only
        contDirDataSvc.saveWithFiles(contentDir, mFile1);

        // Save media file and associate manually
        MediaFile mFile2 = new MediaFile();
        String hashedFPath2 = hashingService.hashPath(testFilePath2);
        mFile2.setHashedPath(hashedFPath2);
        mFile2.setPath(testFilePath2);
        mFile2.setHash(hashedFPath2);
        mFileDao.save(mFile2);

        ContentDirHasMFile hasMFile2 = new ContentDirHasMFile();
        hasMFile2.setDirHashedPath(hashedDPath);
        hasMFile2.setFileHashedPath(hashedFPath2);
        hasMFileDao.save(hasMFile2);

        assertThat(hasMFileDao.count()).isEqualTo(2);

        // Read content dir again and verify associations
        try (Stream<MediaFile> mFilesStr = mFileDao
                .findByDir(hashedDPath)) {
            List<MediaFile> mFiles = mFilesStr.toList();

            assertThat(mFiles).hasSize(2);
            assertThat(mFiles).contains(mFile1);
            assertThat(mFiles).contains(mFile2);
        }
    }

    @Test
    void deleteByContentDirAndMFileStatus() {
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

        // Associate first media file with a content directory
        ContentDir dir = new ContentDir();
        dir.setHashedPath(hashedDPath);
        dir.setPath(dirPath);
        contDirDataSvc.saveWithFiles(dir, mFile1, mFile2, mFile3);

        // Remove association with files 'IN_REVIEW'
        int deletedCount = hasMFileDao.deleteAllByDirHashedPathAndMFileStatus(
                dir.getHashedPath(),
                MediaFileStatus.IN_REVIEW
        );

        assertThat(deletedCount).isEqualTo(2);
        assertThat(hasMFileDao.count()).isEqualTo(1);
    }
}