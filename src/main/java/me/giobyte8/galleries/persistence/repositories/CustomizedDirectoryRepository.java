package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;

import java.util.Optional;
import java.util.Set;

public interface CustomizedDirectoryRepository {

    Set<Directory> findByParentPathAndStatus(Directory parent, DirStatus status);

    /**
     * Upserts directory into database and associate it with its
     * parent directory.
     * <br/>
     * NOTE: Parent directory must already exist in database,
     *   otherwise, child directory won't be saved.
     *
     * @param parent Parent directory
     * @param directory Directory being saved/updated
     */
    Optional<Directory> saveAsChild(Directory parent, Directory directory);

    /**
     * Sets status to all directories contained inside
     * parent directory
     *
     * @param parent Parent directory
     * @param status Status to set on children dirs
     * @return Number of updated directories
     */
    long updateStatusByParent(Directory parent, DirStatus status);

    long deleteWithDescendants(Directory parent);
}
