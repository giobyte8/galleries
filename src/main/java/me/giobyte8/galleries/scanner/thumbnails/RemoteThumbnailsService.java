package me.giobyte8.galleries.scanner.thumbnails;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@RequiredArgsConstructor
@Service
public class RemoteThumbnailsService implements ThumbnailsService {

    @Override
    public void generateThumbnails(Path path) {
        // TODO Implement calls to external thumbnails service
    }

    @Override
    public void refreshThumbnails(Path path) {
        // TODO Implement calls to external thumbnails service
    }

    @Override
    public void deleteThumbnails(Path path) {
        // TODO Implement calls to external thumbnails service
    }
}
