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

    /**
     * Takes a relative path and prepends the content's root
     * to form an absolute path.
     * <br/>
     * Content's root path is taken from application properties
     * 'galleries.scanner.content_dirs.root_path'
     *
     * @param relPath Relative path to file/dir
     * @return Absolute path to file or directory
     */
    public Path toAbsolute(String relPath) {
        return Path.of(
                this.scannerProps.getContentDirs().getRootPath(),
                relPath
        );
    }

    /**
     * Takes an absolute file/dir path and removes the content's root
     * part from it.
     * <br/>
     * Content's root path is taken from application properties
     * 'galleries.scanner.content_dirs.root_path'
     *
     * @param absPath File or directory absolute path
     * @return Relative path to file
     */
    public Path toRelative(Path absPath) {
        String relFPath = absPath.toString().replace(
                scannerProps.getContentDirs().getRootPath(),
                ""
        );

        if (relFPath.startsWith(File.separator)) {
            relFPath = relFPath.substring(1);
        }

        return Path.of(relFPath);
    }
}
