package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;

import java.util.List;
import java.util.Set;

public interface DirectoryRepository {

    int count();

    Directory findBy(String path);

    /**
     * Finds root directories, i.e., directories that don't
     * have a parent directory.
     *
     * @return List of root directories sorted by path
     */
    List<Directory> findRoots();

    List<Directory> findByParentPath(String parentPath);

    Set<Directory> findBy(Directory parent, DirStatus status);

    void save(Directory directory);

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
    void save(Directory parent, Directory directory);

    /**
     * Sets status to all directories contained inside
     * parent directory
     *
     * @param parent Parent directory
     * @param status Status to set on children dirs
     * @return Number of updated directories
     */
    long updateByParent(Directory parent, DirStatus status);

    long deleteWithDescendants(Directory parent);

}
