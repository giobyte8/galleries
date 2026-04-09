package me.giobyte8.galleries.scanner.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void increment(Metric metric) {
        meterRegistry.counter(metric.getName()).increment();
    }

    public void record(Metric metric, Duration duration) {
        record(metric, duration, new String[0]);
    }

    public void record(Metric metric, Duration duration, String... tags) {
        meterRegistry.timer(metric.getName(), tags).record(duration);
    }

    public void record(Metric metric, Long sinceNs, String... tags) {
        record(metric, Duration.ofNanos(System.nanoTime() - sinceNs), tags);
    }
}
