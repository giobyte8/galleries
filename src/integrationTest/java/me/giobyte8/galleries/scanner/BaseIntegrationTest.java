package me.giobyte8.galleries.scanner;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.neo4j.Neo4jContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest
public abstract class BaseIntegrationTest {

    protected static final Neo4jContainer neo4j =
            new Neo4jContainer("neo4j:5.20-community-bullseye")
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
    }
}
