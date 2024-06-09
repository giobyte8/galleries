package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;

public interface DirectoryRepository {

    int count();

    Directory findBy(String path);

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
}
