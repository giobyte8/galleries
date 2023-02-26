package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.dao.ContentDirHasMFileDao;
import me.giobyte8.galleries.scanner.dto.UpsertDiscoveredFileResult;
import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.model.ContentDirHasMFile;
import me.giobyte8.galleries.scanner.scanners.MediaScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MediaScannerService {
    Logger log = LoggerFactory.getLogger(MediaScannerService.class);

    private final MediaScanner mediaScanner;
    private final ContentDirHasMFileDao dirHasMFileDao;
    private final ContentDirService dirService;
    private final MediaFileService mFileService;
    private final PathService pathService;

    public MediaScannerService(
            MediaScanner mediaScanner,
            ContentDirHasMFileDao dirHasMFileDao,
            ContentDirService dirService,
            MediaFileService mFileService,
            PathService pathService) {
        this.mediaScanner = mediaScanner;
        this.dirHasMFileDao = dirHasMFileDao;
        this.dirService = dirService;
        this.mFileService = mFileService;
        this.pathService = pathService;
    }

    public void scan(ContentDir dir) throws IOException {
        dirService.preScanHook(dir);

        mediaScanner.scan(
                pathService.toAbsolute(dir.getPath()),
                dir.isRecursive(),
                (absFPath, contentHash) -> {
                    UpsertDiscoveredFileResult upsertMFResult = mFileService
                            .upsertDiscoveredFile(absFPath, contentHash);

                    // Upsert file association with content dir
                    dirHasMFileDao.save(new ContentDirHasMFile(
                            dir.getHashedPath(),
                            upsertMFResult.mFile().getHashedPath()
                    ));

                    // TODO Emit event
                }
        );

        dirService.postScanHook(dir);
    }
}
