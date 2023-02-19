package me.giobyte8.galleries.scanner.scanners;

import java.nio.file.Path;

public interface OnFileFoundCB {

    void onFileFound(Path fPath, String contentHash);
}
