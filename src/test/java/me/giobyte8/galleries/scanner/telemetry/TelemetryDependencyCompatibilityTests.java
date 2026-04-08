package me.giobyte8.galleries.scanner.telemetry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class TelemetryDependencyCompatibilityTests {

    @Test
    void otlpMetricBuilderShouldBeRuntimeCompatible() {
        assertThatCode(() -> {
            var metricClass = Class.forName(
                    "io.opentelemetry.proto.metrics.v1.Metric"
            );
            var builder = metricClass.getMethod("newBuilder").invoke(null);

            builder.getClass().getMethod("getGaugeBuilder").invoke(builder);
        }).doesNotThrowAnyException();
    }
}
