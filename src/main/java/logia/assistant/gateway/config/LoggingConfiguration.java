package logia.assistant.gateway.config;

import java.net.InetSocketAddress;
import java.util.Iterator;

import io.github.jhipster.config.JHipsterProperties;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * The Class LoggingConfiguration.
 *
 * @author Dai Mai
 */
@Configuration
@RefreshScope
public class LoggingConfiguration {

    /** The Constant LOGSTASH_APPENDER_NAME. */
    private static final String LOGSTASH_APPENDER_NAME = "LOGSTASH";

    /** The Constant ASYNC_LOGSTASH_APPENDER_NAME. */
    private static final String ASYNC_LOGSTASH_APPENDER_NAME = "ASYNC_LOGSTASH";

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(LoggingConfiguration.class);

    /** The context. */
    private LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    /** The app name. */
    private final String appName;

    /** The server port. */
    private final String serverPort;

    /** The eureka instance config bean. */
    private final EurekaInstanceConfigBean eurekaInstanceConfigBean;

    /** The version. */
    private final String version;

    /** The j hipster properties. */
    private final JHipsterProperties jHipsterProperties;

    /**
     * Instantiates a new logging configuration.
     *
     * @param appName the app name
     * @param serverPort the server port
     * @param eurekaInstanceConfigBean the eureka instance config bean
     * @param version the version
     * @param jHipsterProperties the j hipster properties
     */
    public LoggingConfiguration(@Value("${spring.application.name}") String appName, @Value("${server.port}") String serverPort,
        @Autowired(required = false) EurekaInstanceConfigBean eurekaInstanceConfigBean, @Value("${info.project.version}") String version, JHipsterProperties jHipsterProperties) {
        this.appName = appName;
        this.serverPort = serverPort;
        this.eurekaInstanceConfigBean = eurekaInstanceConfigBean;
        this.version = version;
        this.jHipsterProperties = jHipsterProperties;
        if (jHipsterProperties.getLogging().getLogstash().isEnabled()) {
            addLogstashAppender(context);
            addContextListener(context);
        }
        if (jHipsterProperties.getMetrics().getLogs().isEnabled()) {
            setMetricsMarkerLogbackFilter(context);
        }
    }

    /**
     * Adds the context listener.
     *
     * @param context the context
     */
    private void addContextListener(LoggerContext context) {
        LogbackLoggerContextListener loggerContextListener = new LogbackLoggerContextListener();
        loggerContextListener.setContext(context);
        context.addListener(loggerContextListener);
    }

    /**
     * Adds the logstash appender.
     *
     * @param context the context
     */
    private void addLogstashAppender(LoggerContext context) {
        log.info("Initializing Logstash logging");

        LogstashTcpSocketAppender logstashAppender = new LogstashTcpSocketAppender();
        logstashAppender.setName(LOGSTASH_APPENDER_NAME);
        logstashAppender.setContext(context);
        String optionalFields = "";
        if (eurekaInstanceConfigBean != null) {
            optionalFields = "\"instance_id\":\"" + eurekaInstanceConfigBean.getInstanceId() + "\",";
        }
        String customFields = "{\"app_name\":\"" + appName + "\",\"app_port\":\"" + serverPort + "\"," +
            optionalFields + "\"version\":\"" + version + "\"}";

        // More documentation is available at: https://github.com/logstash/logstash-logback-encoder
        LogstashEncoder logstashEncoder=new LogstashEncoder();
        // Set the Logstash appender config from JHipster properties
        logstashEncoder.setCustomFields(customFields);
        // Set the Logstash appender config from JHipster properties
        logstashAppender.addDestinations(new InetSocketAddress(jHipsterProperties.getLogging().getLogstash().getHost(),jHipsterProperties.getLogging().getLogstash().getPort()));

        ShortenedThrowableConverter throwableConverter = new ShortenedThrowableConverter();
        throwableConverter.setRootCauseFirst(true);
        logstashEncoder.setThrowableConverter(throwableConverter);
        logstashEncoder.setCustomFields(customFields);

        logstashAppender.setEncoder(logstashEncoder);
        logstashAppender.start();

        // Wrap the appender in an Async appender for performance
        AsyncAppender asyncLogstashAppender = new AsyncAppender();
        asyncLogstashAppender.setContext(context);
        asyncLogstashAppender.setName(ASYNC_LOGSTASH_APPENDER_NAME);
        asyncLogstashAppender.setQueueSize(jHipsterProperties.getLogging().getLogstash().getQueueSize());
        asyncLogstashAppender.addAppender(logstashAppender);
        asyncLogstashAppender.start();

        context.getLogger("ROOT").addAppender(asyncLogstashAppender);
    }

    /**
     * Sets the metrics marker logback filter.
     *
     * @param context the new metrics marker logback filter
     */
    // Configure a log filter to remove "metrics" logs from all appenders except the "LOGSTASH" appender
    private void setMetricsMarkerLogbackFilter(LoggerContext context) {
        log.info("Filtering metrics logs from all appenders except the {} appender", LOGSTASH_APPENDER_NAME);
        OnMarkerEvaluator onMarkerMetricsEvaluator = new OnMarkerEvaluator();
        onMarkerMetricsEvaluator.setContext(context);
        onMarkerMetricsEvaluator.addMarker("metrics");
        onMarkerMetricsEvaluator.start();
        EvaluatorFilter<ILoggingEvent> metricsFilter = new EvaluatorFilter<>();
        metricsFilter.setContext(context);
        metricsFilter.setEvaluator(onMarkerMetricsEvaluator);
        metricsFilter.setOnMatch(FilterReply.DENY);
        metricsFilter.start();

        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext();) {
                Appender<ILoggingEvent> appender = it.next();
                if (!appender.getName().equals(ASYNC_LOGSTASH_APPENDER_NAME)) {
                    log.debug("Filter metrics logs from the {} appender", appender.getName());
                    appender.setContext(context);
                    appender.addFilter(metricsFilter);
                    appender.start();
                }
            }
        }
    }

    /**
     * Logback configuration is achieved by configuration file and API.
     * When configuration file change is detected, the configuration is reset.
     * This listener ensures that the programmatic configuration is also re-applied after reset.
     *
     * @see LogbackLoggerContextEvent
     */
    class LogbackLoggerContextListener extends ContextAwareBase implements LoggerContextListener {

        /* (non-Javadoc)
         * @see ch.qos.logback.classic.spi.LoggerContextListener#isResetResistant()
         */
        @Override
        public boolean isResetResistant() {
            return true;
        }

        /* (non-Javadoc)
         * @see ch.qos.logback.classic.spi.LoggerContextListener#onStart(ch.qos.logback.classic.LoggerContext)
         */
        @Override
        public void onStart(LoggerContext context) {
            addLogstashAppender(context);
        }

        /* (non-Javadoc)
         * @see ch.qos.logback.classic.spi.LoggerContextListener#onReset(ch.qos.logback.classic.LoggerContext)
         */
        @Override
        public void onReset(LoggerContext context) {
            addLogstashAppender(context);
        }

        /* (non-Javadoc)
         * @see ch.qos.logback.classic.spi.LoggerContextListener#onStop(ch.qos.logback.classic.LoggerContext)
         */
        @Override
        public void onStop(LoggerContext context) {
            // Nothing to do.
        }

        /* (non-Javadoc)
         * @see ch.qos.logback.classic.spi.LoggerContextListener#onLevelChange(ch.qos.logback.classic.Logger, ch.qos.logback.classic.Level)
         */
        @Override
        public void onLevelChange(ch.qos.logback.classic.Logger logger, Level level) {
            // Nothing to do.
        }
    }

}
