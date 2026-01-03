package me.giobyte8.galleries.scanner.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@ConfigurationProperties(prefix = "galleries.scanner")
@Component
@Data
public class ScannerProps {
    private ContentDirsProps contentDirs;
    private Set<String> imageFileExtensions;
}
