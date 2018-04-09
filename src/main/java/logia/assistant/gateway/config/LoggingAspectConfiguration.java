package logia.assistant.gateway.config;

import logia.assistant.gateway.aop.logging.LoggingAspect;

import io.github.jhipster.config.JHipsterConstants;

import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

/**
 * The Class LoggingAspectConfiguration.
 *
 * @author Dai Mai
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    /**
     * Logging aspect.
     *
     * @param env the env
     * @return the logging aspect
     */
    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }
}
