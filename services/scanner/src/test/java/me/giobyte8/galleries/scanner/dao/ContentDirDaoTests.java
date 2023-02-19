package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(HashingService.class)
class ContentDirDaoTests {

    @Autowired
    private ContentDirDao contentDirDao;

    @Autowired
    private MediaFileDao mFilesDao;

    @Autowired
    private HashingService hashingService;

    private String testDirPath = "/pgalleries/portraits";
    private String testFilePath1 = "/pgalleries/portraits/random.jpeg";
    private String testFilePath2 = "/pgalleries/portraits/random2.jpeg";

    @Test
    void injectedComponentsOk() {
        assertNotNull(contentDirDao);
        assertNotNull(hashingService);
    }

    @Test
    void simpleSave() {
        ContentDir contentDir = new ContentDir();
        contentDir.setHashedPath(hashingService.hashPath(testDirPath));
        contentDir.setPath(testDirPath);
        contentDirDao.saveAndFlush(contentDir);

        ContentDir dbContentDir = contentDirDao
                .findById(hashingService.hashPath(testDirPath))
                .orElseThrow();
        assertEquals(dbContentDir.getHashedPath(), contentDir.getHashedPath());
    }

    @Test
    void saveWithMediaFiles() {
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

        contentDir.addFile(mFile1);
        contentDir.addFile(mFile2);
        contentDirDao.saveAndFlush(contentDir);

        ContentDir dbContDir = contentDirDao
                .findById(hashedDPath)
                .orElseThrow();
        assertThat(dbContDir.getPath()).isEqualTo(testDirPath);
        assertThat(dbContDir.getFiles()).hasSize(2);

        // Verify records in media files table
        assertThat(mFilesDao.count()).isEqualTo(2);
    }
}
