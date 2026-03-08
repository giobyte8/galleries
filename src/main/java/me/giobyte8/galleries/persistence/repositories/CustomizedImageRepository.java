package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.ImageStatus;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface CustomizedImageRepository {

    Stream<Image> findByParent(Directory parent);

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
    @SuppressWarnings("UnusedReturnValue")
    Optional<Image> saveAsChild(Directory parent, Image image);

    /**
     * Sets given status to all images that are direct children
     * of a directory.
     *
     * @param parent Directory containing images to update
     * @param status Status to assign to images
     * @return Number of updated images
     */
    long updateStatusByParent(Directory parent, ImageStatus status);

    /**
     * Removes all images that are direct children of a directory
     * and that have a specific status.
     *
     * @param parent Directory containing images to remove
     * @param status Status of images to be removed
     * @return Number of removed images
     */
    long deleteByParentAndStatus(Directory parent, ImageStatus status);

    /**
     * Removes all images that are direct children of a directory
     * and that have a specific status.
     *
     * @param parent Directory containing images to remove
     * @param status Status of images to be removed
     * @return Paths of removed images
     */
    Set<String> deleteAndGetPaths(Directory parent, ImageStatus status);

    /**
     * Removes all images that are descendant of a directory, it means,
     * it will descend into each subdirectory and remove its images too,
     * not just direct children
     *
     * @param parent Directory containing images to remove
     * @return Paths of all removed images
     */
    Set<String> multilevelDeleteAndGetPaths(Directory parent);
}
