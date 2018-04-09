package logia.assistant.gateway.config;

import io.github.jhipster.config.JHipsterProperties;

import logia.assistant.gateway.gateway.ratelimiting.RateLimitingFilter;
import logia.assistant.gateway.gateway.accesscontrol.AccessControlFilter;
import logia.assistant.gateway.gateway.responserewriting.SwaggerBasePathRewritingFilter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Class GatewayConfiguration.
 *
 * @author Dai Mai
 */
@Configuration
public class GatewayConfiguration {

    /**
     * The Class SwaggerBasePathRewritingConfiguration.
     *
     * @author Dai Mai
     */
    @Configuration
    public static class SwaggerBasePathRewritingConfiguration {

        /**
         * Swagger base path rewriting filter.
         *
         * @return the swagger base path rewriting filter
         */
        @Bean
        public SwaggerBasePathRewritingFilter swaggerBasePathRewritingFilter(){
            return new SwaggerBasePathRewritingFilter();
        }
    }

    /**
     * The Class AccessControlFilterConfiguration.
     *
     * @author Dai Mai
     */
    @Configuration
    public static class AccessControlFilterConfiguration {

        /**
         * Access control filter.
         *
         * @param routeLocator the route locator
         * @param jHipsterProperties the j hipster properties
         * @return the access control filter
         */
        @Bean
        public AccessControlFilter accessControlFilter(RouteLocator routeLocator, JHipsterProperties jHipsterProperties){
            return new AccessControlFilter(routeLocator, jHipsterProperties);
        }
    }

    /**
     * Configures the Zuul filter that limits the number of API calls per user.
     * <p>
     * This uses Bucket4J to limit the API calls, see {@link logia.assistant.gateway.gateway.ratelimiting.RateLimitingFilter}.
     *
     * @author Dai Mai
     */
    @Configuration
    @ConditionalOnProperty("jhipster.gateway.rate-limiting.enabled")
    public static class RateLimitingConfiguration {

        /** The j hipster properties. */
        private final JHipsterProperties jHipsterProperties;

        /**
         * Instantiates a new rate limiting configuration.
         *
         * @param jHipsterProperties the j hipster properties
         */
        public RateLimitingConfiguration(JHipsterProperties jHipsterProperties) {
            this.jHipsterProperties = jHipsterProperties;
        }

        /**
         * Rate limiting filter.
         *
         * @return the rate limiting filter
         */
        @Bean
        public RateLimitingFilter rateLimitingFilter() {
            return new RateLimitingFilter(jHipsterProperties);
        }
    }
}
