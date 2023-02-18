package me.giobyte8.galleries.scanner.scanners;

import java.io.File;

public interface MediaScanner {

    void scan(File path, boolean recursive);
}
