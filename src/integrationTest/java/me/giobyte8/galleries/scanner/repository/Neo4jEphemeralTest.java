package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Neo4jEphemeralTest extends BaseIntegrationTest {

    @AfterEach
    void cleanup(@Autowired Driver n4jDriver) {
        try (Session session = n4jDriver.session()) {
            String deleteAll = "MATCH (n) DETACH DELETE n";
            session.executeWriteWithoutResult(ctx -> ctx.run(deleteAll));
        }
    }
}
