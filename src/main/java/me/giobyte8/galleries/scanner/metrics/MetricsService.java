package me.giobyte8.galleries.scanner.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void increment(Metric metric) {
        meterRegistry.counter(metric.getName()).increment();
    }
}
