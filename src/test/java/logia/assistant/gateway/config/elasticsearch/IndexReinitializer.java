package logia.assistant.gateway.config.elasticsearch;

import static java.lang.System.currentTimeMillis;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

/**
 * The Class IndexReinitializer.
 *
 * @author Dai Mai
 */
@Component
public class IndexReinitializer {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(getClass());

    /** The elasticsearch template. */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * Reset index.
     */
    @PostConstruct
    public void resetIndex() {
        long t = currentTimeMillis();
        elasticsearchTemplate.deleteIndex("_all");
        t = currentTimeMillis() - t;
        logger.debug("Elasticsearch indexes reset in {} ms", t);
    }
}
