package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.repository.DirectoryRepository;
import me.giobyte8.galleries.scanner.repository.ImageRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LFSScanMediaObserver implements ScanMediaObserver {

    private final DirectoryRepository dirRepository;
    private final ImageRepository imgRepository;

    public LFSScanMediaObserver(
            DirectoryRepository dirRepository,
            ImageRepository imgRepository
    ) {
        this.dirRepository = dirRepository;
        this.imgRepository = imgRepository;
    }

    @Override
    public void onDirectoryFound(Directory parent, Directory directory) {
        dirRepository.save(parent, directory);
        // TODO Emit event for directory found (?)
    }

    @Override
    public void onImageFound(Directory parent, Image image) {
        if (contentChanged(image)) {
            // TODO Publish event for thumbnails creation
        }

        imgRepository.save(parent, image);
    }

    private boolean contentChanged(Image image) {
        Image dbImg = imgRepository.findBy(image.getPath());
        return dbImg != null &&
                !Objects.equals(
                        dbImg.getContentHash(),
                        image.getContentHash()
                );
    }
}
