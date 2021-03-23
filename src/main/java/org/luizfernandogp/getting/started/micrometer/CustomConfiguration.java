package org.luizfernandogp.getting.started.micrometer;

import java.util.Arrays;

import javax.inject.Singleton;
import javax.enterprise.inject.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.micrometer.core.instrument.*;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.quarkus.micrometer.runtime.MeterFilterConstraint;

@Singleton
public class CustomConfiguration {

    @ConfigProperty(name = "deployment.env")
    String deploymentEnv;

    @ConfigProperty(name = "appname")
    String appname;

    /** Define common tags that apply only to a Prometheus Registry */
    @Produces
    @Singleton
    @MeterFilterConstraint(applyTo = PrometheusMeterRegistry.class)
    public MeterFilter configurePrometheusRegistries() {
        return MeterFilter.commonTags(Arrays.asList(
                Tag.of("registry", "prometheus")));
    }

    /** Define common tags that apply globally */
    @Produces
    @Singleton
    public MeterFilter configureAllRegistries() {
        return MeterFilter.commonTags(Arrays.asList(
                Tag.of("env", deploymentEnv),
                Tag.of("application", appname)));
    }

    /** Enable histogram buckets for a specific timer */
    @Produces
    @Singleton
    public MeterFilter enableHistogram() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if(id.getName().startsWith("myservice")) {
                    return DistributionStatisticConfig.builder()
                        .percentiles(0.5, 0.95)     // median and 95th percentile, not aggregable
                        .percentilesHistogram(true) // histogram buckets (e.g. prometheus histogram_quantile)
                        .build()
                        .merge(config);
                }
                return config;
            }
        };
    }
}
