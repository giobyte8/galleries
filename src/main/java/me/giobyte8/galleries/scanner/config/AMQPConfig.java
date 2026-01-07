package me.giobyte8.galleries.scanner.config;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.scanner.config.properties.ScannerProps;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Configuration
public class AMQPConfig {

    private final ScannerProps scannerProps;

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public DirectExchange galleriesX() {
        return new DirectExchange(
                scannerProps.getAmqp().getExchangeGl(),
                true,  // Survive to broker restarts?
                false          // X deleted when last queue is unbound from it
        );
    }

    @Bean
    public Queue qGenThumbRequests() {
        return new Queue(
                scannerProps.getAmqp().getQueueGenThumbRequests(),
                true
        );
    }

    @Bean
    public Queue qDelThumbRequests() {
        return new Queue(
                scannerProps.getAmqp().getQueueDelThumbRequests(),
                true
        );
    }

    @Bean
    public Binding bindScanHooksToGalleriesX(
            Queue qGenThumbRequests,
            DirectExchange galleriesX
    ) {
        return BindingBuilder
                .bind(qGenThumbRequests)
                .to(galleriesX)
                .with(qGenThumbRequests.getName());
    }

    @Bean
    public Binding bindScanDiscoveredFilesToGalleriesX(
            Queue qDelThumbRequests,
            DirectExchange galleriesX
    ) {
        return BindingBuilder
                .bind(qDelThumbRequests)
                .to(galleriesX)
                .with(qDelThumbRequests.getName());
    }
}
