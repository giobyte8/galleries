package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(HashingService.class)
class MediaFileDaoTests {

    @Autowired
    private MediaFileDao mFileDao;

    @Autowired
    private HashingService hashingService;

    private String testFilePath = "/pgalleries/portraits/random.jpeg";

    @Test
    void basicCRUD() {
        String hashedFPath = hashingService.hashPath(testFilePath);
        MediaFile mFile = new MediaFile();
        mFile.setHashedPath(hashedFPath);
        mFile.setPath(testFilePath);
        mFile.setHash(hashedFPath);
        mFileDao.saveAndFlush(mFile);

        MediaFile dbMFile = mFileDao
                .findById(hashedFPath)
                .orElseThrow();
        assertThat(dbMFile.getPath()).isEqualTo(testFilePath);
        assertThat(dbMFile.getHash()).isEqualTo(hashedFPath);
    }
}
