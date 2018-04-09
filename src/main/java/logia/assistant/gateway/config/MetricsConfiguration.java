package logia.assistant.gateway.config;

import io.github.jhipster.config.JHipsterProperties;
import io.github.jhipster.config.metrics.SpectatorLogMetricWriter;

import com.netflix.spectator.api.Registry;
import org.springframework.boot.actuate.autoconfigure.ExportMetricReader;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.metrics.spectator.SpectatorMetricReader;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.*;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

/**
 * The Class MetricsConfiguration.
 *
 * @author Dai Mai
 */
@Configuration
@EnableMetrics(proxyTargetClass = true)
public class MetricsConfiguration extends MetricsConfigurerAdapter {

    /** The Constant PROP_METRIC_REG_JVM_MEMORY. */
    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";
    
    /** The Constant PROP_METRIC_REG_JVM_GARBAGE. */
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";
    
    /** The Constant PROP_METRIC_REG_JVM_THREADS. */
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";
    
    /** The Constant PROP_METRIC_REG_JVM_FILES. */
    private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";
    
    /** The Constant PROP_METRIC_REG_JVM_BUFFERS. */
    private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";
    
    /** The Constant PROP_METRIC_REG_JVM_ATTRIBUTE_SET. */
    private static final String PROP_METRIC_REG_JVM_ATTRIBUTE_SET = "jvm.attributes";

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(MetricsConfiguration.class);

    /** The metric registry. */
    private MetricRegistry metricRegistry = new MetricRegistry();

    /** The health check registry. */
    private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    /** The j hipster properties. */
    private final JHipsterProperties jHipsterProperties;

    /** The hikari data source. */
    private HikariDataSource hikariDataSource;

    /**
     * Instantiates a new metrics configuration.
     *
     * @param jHipsterProperties the j hipster properties
     */
    public MetricsConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    /**
     * Sets the hikari data source.
     *
     * @param hikariDataSource the new hikari data source
     */
    @Autowired(required = false)
    public void setHikariDataSource(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    /* (non-Javadoc)
     * @see com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter#getMetricRegistry()
     */
    @Override
    @Bean
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    /* (non-Javadoc)
     * @see com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter#getHealthCheckRegistry()
     */
    @Override
    @Bean
    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    /**
     * Inits the.
     */
    @PostConstruct
    public void init() {
        log.debug("Registering JVM gauges");
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
        metricRegistry.register(PROP_METRIC_REG_JVM_BUFFERS, new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metricRegistry.register(PROP_METRIC_REG_JVM_ATTRIBUTE_SET, new JvmAttributeGaugeSet());
        if (hikariDataSource != null) {
            log.debug("Monitoring the datasource");
            hikariDataSource.setMetricRegistry(metricRegistry);
        }
        if (jHipsterProperties.getMetrics().getJmx().isEnabled()) {
            log.debug("Initializing Metrics JMX reporting");
            JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
            jmxReporter.start();
        }
        if (jHipsterProperties.getMetrics().getLogs().isEnabled()) {
            log.info("Initializing Metrics Log reporting");
            Marker metricsMarker = MarkerFactory.getMarker("metrics");
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("metrics"))
                .markWith(metricsMarker)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
            reporter.start(jHipsterProperties.getMetrics().getLogs().getReportFrequency(), TimeUnit.SECONDS);
        }
    }

    /**
     * Spectator metric reader.
     *
     * @param registry the registry
     * @return the spectator metric reader
     */
    /* Spectator metrics log reporting */
    @Bean
    @ConditionalOnProperty("jhipster.logging.spectator-metrics.enabled")
    @ExportMetricReader
    public SpectatorMetricReader spectatorMetricReader(Registry registry) {
        log.info("Initializing Spectator Metrics Log reporting");
        return new SpectatorMetricReader(registry);
    }

    /**
     * Metric writer.
     *
     * @return the metric writer
     */
    @Bean
    @ConditionalOnProperty("jhipster.logging.spectator-metrics.enabled")
    @ExportMetricWriter
    MetricWriter metricWriter() {
        return new SpectatorLogMetricWriter();
    }
}
