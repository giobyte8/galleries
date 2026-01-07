package me.giobyte8.galleries.scanner.config;

import me.giobyte8.galleries.scanner.config.properties.Neo4jProps;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScannerConfig {

    private final Neo4jProps neo4jProps;

    public ScannerConfig(Neo4jProps neo4jProps) {
        this.neo4jProps = neo4jProps;
    }

    @Bean( destroyMethod = "")
    public Driver neo4jDriver() {
        AuthToken auth = AuthTokens.basic(
                neo4jProps.getUsername(),
                neo4jProps.getPassword()
        );

        Driver driver = GraphDatabase.driver(neo4jProps.getUri(), auth);
        driver.verifyConnectivity();

        return driver;
    }
}
