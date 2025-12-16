package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.DirStatus;
import me.giobyte8.galleries.scanner.model.Directory;

import java.util.Set;

public interface DirectoryRepository {

    int count();

    Directory findBy(String path);

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
