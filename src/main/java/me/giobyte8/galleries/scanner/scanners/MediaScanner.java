package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.persistence.models.Directory;

public interface MediaScanner {

    /**
     * Starts scanning of given directory looking for media files
     * and children directories (If given is recursive)
     *
     * @param dir Base directory to scan
     */
    void scan(Directory dir);
}
