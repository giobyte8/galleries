package me.giobyte8.galleries.persistence;

import org.junit.jupiter.api.AfterEach;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.neo4j.Neo4jContainer;

@DataNeo4jTest
public class Neo4jTest {

    // Started once when the class is loaded, reused for all test classes
    protected static final Neo4jContainer neo4j =
            new Neo4jContainer("neo4j:5.20-community-bullseye")
                    .withoutAuthentication();

    // starts the containers once when the class is loaded by the JVM,
    // this way we avoid starting/stopping containers for each test class
    static {
        neo4j.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("neo4j.uri", neo4j::getBoltUrl);

        // Support for spring data neo4j
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
    }

    @AfterEach
    void cleanup(@Autowired Driver n4jDriver) {
        try (Session session = n4jDriver.session()) {
            String deleteAll = "MATCH (n) DETACH DELETE n";
            session.executeWriteWithoutResult(ctx -> ctx.run(deleteAll));
        }
    }
}
