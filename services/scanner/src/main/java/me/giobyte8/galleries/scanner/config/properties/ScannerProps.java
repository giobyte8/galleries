package me.giobyte8.galleries.scanner.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "galleries.scanner")
@Component
public class ScannerProps {

    private ContentDirsProps contentDirs;

    public ContentDirsProps getContentDirs() {
        return contentDirs;
    }

    public void setContentDirs(ContentDirsProps contentDirs) {
        this.contentDirs = contentDirs;
    }
}
