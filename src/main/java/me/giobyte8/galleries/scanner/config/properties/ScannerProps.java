package me.giobyte8.galleries.scanner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@ConfigurationProperties(prefix = "galleries.scanner")
@Component
public class ScannerProps {

    private Set<String> mediaFilesExtensions;

    private ContentDirsProps contentDirs;

    public ContentDirsProps getContentDirs() {
        return contentDirs;
    }

    public void setContentDirs(ContentDirsProps contentDirs) {
        this.contentDirs = contentDirs;
    }

    public Set<String> getMediaFilesExtensions() {
        return mediaFilesExtensions;
    }

    public void setMediaFilesExtensions(Set<String> mediaFilesExtensions) {
        this.mediaFilesExtensions = mediaFilesExtensions;
    }
}
