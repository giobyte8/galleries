package me.giobyte8.galleries.scanner.thumbnails;

import java.nio.file.Path;

public interface ThumbnailsService {

    void generateThumbnails(Path path);

    void refreshThumbnails(Path path);

    void deleteThumbnails(Path path);
}
