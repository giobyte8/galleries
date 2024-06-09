package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.ImageStatus;
import me.giobyte8.galleries.scanner.model.Image;

import java.util.stream.Stream;

public interface ImageRepository {

    long countBy(ImageStatus status);

    Image findBy(String path);

    Stream<Image> findBy(Directory parent);

    Stream<Image> findBy(Directory parent, ImageStatus status);

    /**
     * Upserts image to database and associate it to
     * parent directory.
     * <br/>
     * NOTE: Parent directory must already exist in database,
     *   otherwise image won't be saved.
     *
     * @param parent Directory that contains image
     * @param image Image being saved/updated
     */
    void save(Directory parent, Image image);
}
