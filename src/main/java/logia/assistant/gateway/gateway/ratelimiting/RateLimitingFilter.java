package logia.assistant.gateway.gateway.ratelimiting;

import logia.assistant.gateway.security.SecurityUtils;

import java.time.Duration;
import java.util.function.Supplier;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import io.github.bucket4j.*;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;
import io.github.jhipster.config.JHipsterProperties;

/**
 * Zuul filter for limiting the number of HTTP calls per client.
 * 
 * See the Bucket4j documentation at https://github.com/vladimir-bukhtoyarov/bucket4j
 * https://github.com/vladimir-bukhtoyarov/bucket4j/blob/master/doc-pages/jcache-usage
 * .md#example-1---limiting-access-to-http-server-by-ip-address
 *
 * @author Dai Mai
 */
public class RateLimitingFilter extends ZuulFilter {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    /** The Constant GATEWAY_RATE_LIMITING_CACHE_NAME. */
    public final static String GATEWAY_RATE_LIMITING_CACHE_NAME = "gateway-rate-limiting";

    /** The j hipster properties. */
    private final JHipsterProperties jHipsterProperties;

    /** The cache. */
    private javax.cache.Cache<String, GridBucketState> cache;

    /** The buckets. */
    private ProxyManager<String> buckets;

    /**
     * Instantiates a new rate limiting filter.
     *
     * @param jHipsterProperties the j hipster properties
     */
    public RateLimitingFilter(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        CompleteConfiguration<String, GridBucketState> config =
            new MutableConfiguration<String, GridBucketState>()
                .setTypes(String.class, GridBucketState.class);

        this.cache = cacheManager.createCache(GATEWAY_RATE_LIMITING_CACHE_NAME, config);
        this.buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(cache);
    }

    /* (non-Javadoc)
     * @see com.netflix.zuul.ZuulFilter#filterType()
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /* (non-Javadoc)
     * @see com.netflix.zuul.ZuulFilter#filterOrder()
     */
    @Override
    public int filterOrder() {
        return 10;
    }

    /* (non-Javadoc)
     * @see com.netflix.zuul.IZuulFilter#shouldFilter()
     */
    @Override
    public boolean shouldFilter() {
        // specific APIs can be filtered out using
        // if (RequestContext.getCurrentContext().getRequest().getRequestURI().startsWith("/api")) { ... }
        return true;
    }

    /* (non-Javadoc)
     * @see com.netflix.zuul.IZuulFilter#run()
     */
    @Override
    public Object run() {
        String bucketId = getId(RequestContext.getCurrentContext().getRequest());
        Bucket bucket = buckets.getProxy(bucketId, getConfigSupplier());
        if (bucket.tryConsume(1)) {
            // the limit is not exceeded
            log.debug("API rate limit OK for {}", bucketId);
        } else {
            // limit is exceeded
            log.info("API rate limit exceeded for {}", bucketId);
            apiLimitExceeded();
        }
        return null;
    }

    /**
     * Gets the config supplier.
     *
     * @return the config supplier
     */
    private Supplier<BucketConfiguration> getConfigSupplier() {
        return () -> {
            JHipsterProperties.Gateway.RateLimiting rateLimitingProperties =
                jHipsterProperties.getGateway().getRateLimiting();

            return Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.simple(rateLimitingProperties.getLimit(),
                    Duration.ofSeconds(rateLimitingProperties.getDurationInSeconds())))
                .buildConfiguration();
        };
    }

    /**
     * Create a Zuul response error when the API limit is exceeded.
     */
    private void apiLimitExceeded() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody("API rate limit exceeded");
            ctx.setSendZuulResponse(false);
        }
    }

    /**
     * The ID that will identify the limit: the user login or the user IP address.
     *
     * @param httpServletRequest the http servlet request
     * @return the id
     */
    private String getId(HttpServletRequest httpServletRequest) {
        return SecurityUtils.getCurrentUserLogin().orElse(httpServletRequest.getRemoteAddr());
    }
}
