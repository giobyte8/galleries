package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

@Service
public class PathService {

    private final ScannerProps scannerProps;

    public PathService(ScannerProps scannerProps) {
        this.scannerProps = scannerProps;
    }

    public Path toAbsolute(String relPath) {
        return Path.of(
                this.scannerProps.getContentDirs().getRootPath(),
                relPath
        );
    }

    /**
     * Takes an absolute file/dir path and removes the content's root
     * part from it.
     *
     * @param absPath File or directory absolute path
     * @return Relative path to file
     */
    public String toRelative(Path absPath) {
        String relFPath = absPath.toString().replace(
                scannerProps.getContentDirs().getRootPath(),
                ""
        );

        if (relFPath.startsWith(File.separator)) {
            relFPath = relFPath.substring(1);
        }

        return relFPath;
    }
}
