package me.giobyte8.galleries.scanner.repository;

import org.junit.jupiter.api.AfterEach;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Neo4jEphemeralTest {

    @AfterEach
    void cleanup(@Autowired Driver n4jDriver) {
        try (Session session = n4jDriver.session()) {
            String deleteAll = "MATCH (n) DETACH DELETE n";
            session.executeWriteWithoutResult(ctx -> ctx.run(deleteAll));
        }
    }
}
