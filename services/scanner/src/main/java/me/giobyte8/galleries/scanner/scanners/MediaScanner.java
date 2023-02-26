package me.giobyte8.galleries.scanner.scanners;

import java.io.IOException;
import java.nio.file.Path;

public interface MediaScanner {

    void scan(
            Path absDirPath,
            boolean recursive,
            OnFileFoundCB cb
    ) throws IOException;
}
