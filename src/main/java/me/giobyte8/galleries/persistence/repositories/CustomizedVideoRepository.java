package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.models.Video;

import java.util.Optional;
import java.util.Set;

public interface CustomizedVideoRepository {

    /**
     * Upserts video to database and associate it to
     * parent directory.
     * <br/>
     * NOTE: Parent directory must already exist in database,
     *   otherwise video won't be saved.
     *
     * @param parent Directory that contains video
     * @param video Video being saved/updated
     */
    @SuppressWarnings("UnusedReturnValue")
    Optional<Video> saveAsChild(Directory parent, Video video);

    /**
     * Sets given status to all videos that are direct children
     * of a directory.
     *
     * @param parent Directory containing videos to update
     * @param status Status to assign to videos
     * @return Number of updated videos
     */
    long updateStatusByParent(Directory parent, MediaFileStatus status);

    /**
     * Removes all videos that are direct children of a directory
     * and that have a specific status.
     *
     * @param parent Directory containing videos to remove
     * @param status Status of videos to be removed
     * @return Number of removed videos
     */
    long deleteByParentAndStatus(Directory parent, MediaFileStatus status);

    /**
     * Removes all videos that are direct children of a directory
     * and that have a specific status.
     *
     * @param parent Directory containing videos to remove
     * @param status Status of videos to be removed
     * @return Paths of removed videos
     */
    Set<String> deleteAndGetPaths(Directory parent, MediaFileStatus status);

    /**
     * Removes all videos that are descendant of a directory, it means,
     * it will descend into each subdirectory and remove its videos too,
     * not just direct children.
     *
     * @param parent Directory containing videos to remove
     * @return Paths of all removed videos
     */
    Set<String> multilevelDeleteAndGetPaths(Directory parent);
}

