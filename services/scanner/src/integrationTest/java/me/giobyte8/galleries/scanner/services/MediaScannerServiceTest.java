package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.amqp.ScanEventsProducer;
import me.giobyte8.galleries.scanner.amqp.ScanRequestsListener;
import me.giobyte8.galleries.scanner.dao.ContentDirDao;
import me.giobyte8.galleries.scanner.dao.ContentDirHasMFileDao;
import me.giobyte8.galleries.scanner.dao.MediaFileDao;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@MockBeans({ @MockBean(ScanRequestsListener.class), @MockBean(ScanEventsProducer.class) })
@Transactional
class MediaScannerServiceTest {

    @Autowired
    private MediaScannerService scannerSvc;

    @Autowired
    private HashingService hashingService;

    @Autowired
    private ContentDirDao dirDao;

    @Autowired
    private MediaFileDao mFileDao;

    @Autowired
    private ContentDirHasMFileDao hasMFileDao;

    @AfterEach
    void afterEach() {
        hasMFileDao.deleteAll();
        mFileDao.deleteAll();
        dirDao.deleteAll();

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    void scanSimpleDir() {
        String dirPath = "cameras/ca";
        String hashedDPath = hashingService.hashPath(dirPath);

        ContentDir contentDir = new ContentDir();
        contentDir.setHashedPath(hashedDPath);
        contentDir.setPath(dirPath);
        dirDao.save(contentDir);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        scannerSvc.scan(UUID.randomUUID(), hashedDPath);

        TestTransaction.start();
        assertThat(mFileDao.count()).isEqualTo(2);
        ContentDir cDirDb = dirDao
                .findById(hashedDPath)
                .orElseThrow();
        assertThat(cDirDb.getLastScanStart()).isNotNull();
        assertThat(cDirDb.getLastScanCompletion()).isNotNull();
    }

    @Test
    void scanTwoNestedNonRecursiveDirs() {
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

        TestTransaction.flagForCommit();
        TestTransaction.end();

        scannerSvc.scan(UUID.randomUUID(), hashedDPath1);
        scannerSvc.scan(UUID.randomUUID(), hashedDPath2);

        // Verify all files were found and scanned
        TestTransaction.start();
        assertThat(mFileDao.count()).isEqualTo(4);

        // Dir 1 should have scan start and end dates
        ContentDir cDirDb1 = dirDao
                .findById(hashedDPath1)
                .orElseThrow();
        assertThat(cDirDb1.getLastScanStart()).isNotNull();
        assertThat(cDirDb1.getLastScanCompletion()).isNotNull();

        // Dir 2 should have scan start and end dates
        ContentDir cDirDb2= dirDao
                .findById(hashedDPath2)
                .orElseThrow();
        assertThat(cDirDb2.getLastScanStart()).isNotNull();
        assertThat(cDirDb2.getLastScanCompletion()).isNotNull();
    }

    /**
     * Covered scenario:
     * 1. Add 'cameras/' and 'cameras/ca' to DB. Scan each dir and verify
     *    2 files found for each one.
     * 2. Make 'cameras/' recursive and run scan again
     * 3. Number of total files should be the same (4), but cameras/ should
     *    now be associated to files in cameras/ca as well
     *
     * <br/><br/>
     * This verifies that you can add inner folders individually, but also
     * group multiple folders by adding its parent folder, for flexibility
     * when every folder is treated as a gallery, without duplicating media file
     * records neither their thumbnails
     */
    @Test
    void makeParentDirRecursive() {
        String dir1Path = "cameras";
        String hashedPathCameras = hashingService.hashPath(dir1Path);
        ContentDir dirCameras = new ContentDir();
        dirCameras.setHashedPath(hashedPathCameras);
        dirCameras.setPath(dir1Path);
        dirDao.save(dirCameras);

        String dir2Path = "cameras/ca";
        String hashedPathCamerasCa = hashingService.hashPath(dir2Path);
        ContentDir dirCamerasCa = new ContentDir();
        dirCamerasCa.setHashedPath(hashedPathCamerasCa);
        dirCamerasCa.setPath(dir2Path);
        dirDao.save(dirCamerasCa);

        // Scan both dirs
        TestTransaction.flagForCommit();
        TestTransaction.end();
        scannerSvc.scan(UUID.randomUUID(), hashedPathCameras);
        scannerSvc.scan(UUID.randomUUID(), hashedPathCamerasCa);

        // Verify 2 files were found for each dir
        TestTransaction.start();
        assertThat(mFileDao.count()).isEqualTo(4);
        verifyReadyFilesCount(hashedPathCameras, 2);
        verifyReadyFilesCount(hashedPathCamerasCa, 2);


        // Make 'cameras/' recursive
        dirCameras.setRecursive(true);
        dirDao.save(dirCameras);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        scannerSvc.scan(UUID.randomUUID(), hashedPathCameras);


        // Verify files count after recursive scan
        TestTransaction.start();
        assertThat(mFileDao.count()).isEqualTo(4);
        verifyReadyFilesCount(hashedPathCameras, 4);
    }

    /**
     * 1. Add 'cameras/' dir as recursive to DB and initiate scan
     * 2. Verify for files were found
     * 3. Update 'cameras/' dir as non-recursive in DB and scan again
     * 4. Verify only two media files were found
     */
    @Test
    void makeParentDirNonRecursive() {
        String dir1Path = "cameras";
        String hashedPathCameras = hashingService.hashPath(dir1Path);
        ContentDir dirCameras = new ContentDir();
        dirCameras.setHashedPath(hashedPathCameras);
        dirCameras.setRecursive(true);
        dirCameras.setPath(dir1Path);

        dirDao.save(dirCameras);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        scannerSvc.scan(UUID.randomUUID(), hashedPathCameras);

        // Verify recursive scan results
        TestTransaction.start();
        verifyReadyFilesCount(hashedPathCameras, 4);

        // Disable recursion for 'cameras/'
        dirCameras.setRecursive(false);
        dirDao.save(dirCameras);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Scan and verify number of found files
        scannerSvc.scan(UUID.randomUUID(), hashedPathCameras);
        TestTransaction.start();
        verifyReadyFilesCount(hashedPathCameras, 2);
    }

    private void verifyReadyFilesCount(String hashedDPath, int expectedCount) {
        try (Stream<MediaFile> mFilesStream = mFileDao
                .findByDirAndMediaFileStatus(
                        hashedDPath,
                        MediaFileStatus.READY.name()
                )) {

            assertThat(mFilesStream.count()).isEqualTo(expectedCount);
        }
    }
}