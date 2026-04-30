package me.giobyte8.galleries.scanner;

import org.junit.jupiter.api.AfterEach;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.neo4j.Neo4jContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest
public abstract class BaseIntegrationTest {

    // Started once when the class is loaded, reused for all test classes
    protected static final Neo4jContainer neo4j =
            new Neo4jContainer("neo4j:2025")
                    .withoutAuthentication();

    @ServiceConnection
    protected static final RabbitMQContainer rabbitMQ =
            new RabbitMQContainer("rabbitmq:3.9.15-alpine");

    // Instead of using '@TestContainers' annotation, this static block
    // starts the containers once when the class is loaded by the JVM,
    // this way we avoid starting/stopping containers for each test class
    static {
        neo4j.start();
        rabbitMQ.start();
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
