package me.giobyte8.galleries.scanner.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.giobyte8.galleries.scanner.config.properties.Neo4jProps;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScannerConfig {

    @Value("${galleries.scanner.amqp.exchange_gl}")
    private String galleriesX;

    @Value("${galleries.scanner.amqp.queue_scan_hooks}")
    private String qNameScanHooks;

    @Value("${galleries.scanner.amqp.queue_scan_discovered_files}")
    private String qNameScanDiscoveredFiles;

    private final Neo4jProps neo4jProps;

    public ScannerConfig(Neo4jProps neo4jProps) {
        this.neo4jProps = neo4jProps;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connFactory,
            Jackson2JsonMessageConverter jsonMsgConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connFactory);
        factory.setMessageConverter(jsonMsgConverter);

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connFactory,
            Jackson2JsonMessageConverter jsonMsgConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connFactory);
        rabbitTemplate.setMessageConverter(jsonMsgConverter);
        rabbitTemplate.setExchange(galleriesX);

        return rabbitTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMsgConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public DirectExchange galleriesX() {
        return new DirectExchange(
                galleriesX,
                true,  // Survive to broker restarts?
                false          // X deleted when last queue is unbound from it
        );
    }

    @Bean
    public Queue qScanHooks() {
        return new Queue(qNameScanHooks);
    }

    @Bean
    public Queue qScanDiscoveredFiles() {
        return new Queue(qNameScanDiscoveredFiles, true);
    }

    @Bean
    public Binding bindScanHooksToGalleriesX(
            Queue qScanHooks,
            DirectExchange galleriesX
    ) {
        return BindingBuilder
                .bind(qScanHooks)
                .to(galleriesX)
                .with(qNameScanHooks);
    }

    @Bean
    public Binding bindScanDiscoveredFilesToGalleriesX(
            Queue qScanDiscoveredFiles,
            DirectExchange galleriesX
    ) {
        return BindingBuilder
                .bind(qScanDiscoveredFiles)
                .to(galleriesX)
                .with(qNameScanDiscoveredFiles);
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
