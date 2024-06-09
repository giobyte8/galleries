package me.giobyte8.galleries.scanner.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "neo4j")
@Component
@Getter
@Setter
public class Neo4jProps {
    private String uri;
    private String username;
    private String password;
}
