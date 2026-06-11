package me.giobyte8.galleries.scanner.postprocessors;

import me.giobyte8.galleries.persistence.models.Directory;

public interface DirScanPostProcessor {
    void process(Directory dir);
}
