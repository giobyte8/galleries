package me.giobyte8.galleries.scanner.scanners;

import me.giobyte8.galleries.scanner.dto.ScanRequest;
import me.giobyte8.galleries.scanner.model.Directory;

public interface DirMediaScanner {

    void scan(ScanRequest scanReq, Directory dir);
}
