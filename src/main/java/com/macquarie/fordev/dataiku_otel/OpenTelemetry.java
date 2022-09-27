package com.macquarie.fordev.dataiku_otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;

public class OpenTelemetry {

    private static io.opentelemetry.api.OpenTelemetry openTelemetry;
    private static Meter meter;

    public static void start() {
        Resource resource = Resource.create(Attributes.builder()
                .put("service.name", "sn")
                .put("service.instance.id", "sid")
                .build());

        SdkMeterProvider meterProvider = SdkMeterProvider
                .builder()
                .registerMetricReader(PrometheusHttpServer.create())
                .setResource(resource)
                .build();

        openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .buildAndRegisterGlobal();
    }

    public static Meter meter() {
        if (meter == null) meter = openTelemetry.meterBuilder("dataiku").build();
        return meter;
    }

}
