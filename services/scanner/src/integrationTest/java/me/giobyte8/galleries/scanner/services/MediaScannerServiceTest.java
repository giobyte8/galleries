package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.amqp.ScanRequestsListener;
import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.dao.MediaFileDao;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@MockBeans({ @MockBean(ScanRequestsListener.class) })
class MediaScannerServiceTest {

    @Autowired
    private MediaScannerService scannerSvc;

    @Autowired
    private HashingService hashingService;

    @Autowired
    private ContentDirDao dirDao;

    @Autowired
    private MediaFileDao mFileDao;


    // - Write integration tests for each scenario:
    //   - Add cameras/ca dir to db (Scan it)
    //   - Add cameras/ (Non recursive) dir to db (Scan it)
    //   - * 4 files should be found in total so far
    //   - Make cameras/ recursive in db, run scan again
    //   - * Number of files should be the same, but cameras/ should
    //       now be associated to files in cameras/ca as well
    //   - Reload/refresh dir files set
    //
    // - Scan cameras/ as recursive so that all from cameras/ca/ is included
    // - Mark cameras/ as not recursive
    // - Scan again
    // - everything from cameras/ca/ should be removed and amqp events emitted
    //   for each deleted file
    //
    // ^^^ This verifies that you can add inner folders individually, but also
    //     group multiple folders by adding its parent folder, for flexibility
    //     when every folder is treated as a gallery, without duplicating media file
    //     records neither their thumbnails

    @Test
    void scanSimpleDir() throws IOException {
        String dirPath = "cameras/ca";
        String hashedDPath = hashingService.hashPath(dirPath);

        ContentDir contentDir = new ContentDir();
        contentDir.setHashedPath(hashedDPath);
        contentDir.setPath(dirPath);
        dirDao.save(contentDir);

        scannerSvc.scan(contentDir);
        assertThat(mFileDao.count()).isEqualTo(2);
    }

    @Test
    void scanTwoNestedNonRecursiveDirs() throws IOException {
        String dir1Path = "cameras";
        String hashedDPath1 = hashingService.hashPath(dir1Path);
        ContentDir dir1 = new ContentDir();
        dir1.setHashedPath(hashedDPath1);
        dir1.setPath(dir1Path);
        dirDao.save(dir1);

        String dir2Path = "cameras/ca";
        String hashedDPath2 = hashingService.hashPath(dir2Path);
        ContentDir dir2 = new ContentDir();
        dir2.setHashedPath(hashedDPath2);
        dir2.setPath(dir2Path);
        dirDao.save(dir2);

        scannerSvc.scan(dir1);
        scannerSvc.scan(dir2);
        assertThat(mFileDao.count()).isEqualTo(4);
    }

    @Test
    @Transactional
    void toggleParentDirRecursiveMode() throws IOException {
        String dir1Path = "cameras";
        String hashedDPath1 = hashingService.hashPath(dir1Path);
        ContentDir dir1 = new ContentDir();
        dir1.setHashedPath(hashedDPath1);
        dir1.setPath(dir1Path);
        dirDao.save(dir1);

        String dir2Path = "cameras/ca";
        String hashedDPath2 = hashingService.hashPath(dir2Path);
        ContentDir dir2 = new ContentDir();
        dir2.setHashedPath(hashedDPath2);
        dir2.setPath(dir2Path);
        dirDao.save(dir2);

        scannerSvc.scan(dir1);
        scannerSvc.scan(dir2);
        assertThat(mFileDao.count()).isEqualTo(4);

        try (Stream<MediaFile> dir1MFilesStr = mFileDao
                .findAllByMediaDirsHashedPathAndStatus(
                        hashedDPath1,
                        MediaFileStatus.READY

                )) {
            assertThat(dir1MFilesStr.count()).isEqualTo(2);
        }

        // Make 'cameras/' recursive
        dir1.setRecursive(true);
        dirDao.save(dir1);

        // Scan dir1 again
        scannerSvc.scan(dir1);
        assertThat(mFileDao.count()).isEqualTo(4);

        try (Stream<MediaFile> dir1MFilesStr = mFileDao
                .findAllByMediaDirsHashedPathAndStatus(
                        hashedDPath1,
                        MediaFileStatus.READY
                )) {
            assertThat(dir1MFilesStr.count()).isEqualTo(4);
        }
    }
}