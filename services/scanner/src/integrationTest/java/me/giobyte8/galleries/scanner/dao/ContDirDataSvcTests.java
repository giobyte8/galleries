package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.assertj.core.api.Assertions;
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
class ContDirDataSvcTests {

    @Autowired
    private ContentDirDao dirDao;

    @Autowired
    private MediaFileDao mFileDao;

    @Autowired
    private ContDirDataSvc contDirDataSvc;

    @Autowired
    private ContentDirHasMFileDao hasMFileDao;

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
    void associateFiles() {
        ContentDir dir = new ContentDir();
        dir.setHashedPath(hashingService.hashPath(testDirPath));
        dir.setPath(testDirPath);
        dirDao.save(dir);

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

        contDirDataSvc.associateFiles(dir, mFile1, mFile2);

        // Verify two files were saved
        assertThat(mFileDao.count()).isEqualTo(2);

        // Verify association
        try (Stream<MediaFile> mFilesStr = mFileDao
                .findByDir(dir.getHashedPath())) {
            List<MediaFile> mFiles = mFilesStr.toList();

            assertThat(mFiles).hasSize(2);
            assertThat(mFiles).contains(mFile1, mFile2);
        }
    }

    @Test
    void saveWithMediaFiles() {
        ContentDir dir = new ContentDir();
        String hashedDPath = hashingService.hashPath(testDirPath);
        dir.setHashedPath(hashedDPath);
        dir.setPath(testDirPath);

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

        contDirDataSvc.saveWithFiles(dir, mFile1, mFile2);

        // Verify dir was saved
        ContentDir dbContDir = dirDao
                .findById(hashedDPath)
                .orElseThrow();
        assertThat(dbContDir.getPath()).isEqualTo(testDirPath);

        // Verify records in media files table
        Assertions.assertThat(mFileDao.count()).isEqualTo(2);

        // Verify association
        try (Stream<MediaFile> mFilesStr = mFileDao
                .findByDir(dir.getHashedPath())) {
            List<MediaFile> mFiles = mFilesStr.toList();

            assertThat(mFiles).hasSize(2);
            assertThat(mFiles).contains(mFile1, mFile2);
        }
    }

    @Test
    void removeAssociation() {
        ContentDir dir = new ContentDir();
        String hashedDPath = hashingService.hashPath(testDirPath);
        dir.setHashedPath(hashedDPath);
        dir.setPath(testDirPath);

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

        contDirDataSvc.saveWithFiles(dir, mFile1, mFile2);

        // Verify files and dir saved
        assertThat(mFileDao.count()).isEqualTo(2);
        assertThat(dirDao.count()).isEqualTo(1);

        contDirDataSvc.removeAssociation(dir, mFile1);

        // Verify files and dir still there
        assertThat(mFileDao.count()).isEqualTo(2);
        assertThat(dirDao.count()).isEqualTo(1);

        // Verify association was removed
        try (Stream<MediaFile> mFilesStr = mFileDao
                .findByDir(dir.getHashedPath())) {
            List<MediaFile> mFiles = mFilesStr.toList();
            assertThat(mFiles).hasSize(1);
            assertThat(mFiles).contains(mFile2);
        }
    }
}
