package logia.assistant.gateway.config;

import java.io.IOException;

import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class ElasticsearchConfiguration.
 *
 * @author Dai Mai
 */
@Configuration
public class ElasticsearchConfiguration {

    /**
     * Elasticsearch template.
     *
     * @param client the client
     * @param jackson2ObjectMapperBuilder the jackson 2 object mapper builder
     * @return the elasticsearch template
     */
    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(Client client, Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        return new ElasticsearchTemplate(client, new CustomEntityMapper(jackson2ObjectMapperBuilder.createXmlMapper(false).build()));
    }

    /**
     * The Class CustomEntityMapper.
     *
     * @author Dai Mai
     */
    public class CustomEntityMapper implements EntityMapper {

        /** The object mapper. */
        private ObjectMapper objectMapper;

        /**
         * Instantiates a new custom entity mapper.
         *
         * @param objectMapper the object mapper
         */
        public CustomEntityMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        }

        /* (non-Javadoc)
         * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToString(java.lang.Object)
         */
        @Override
        public String mapToString(Object object) throws IOException {
            return objectMapper.writeValueAsString(object);
        }

        /* (non-Javadoc)
         * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToObject(java.lang.String, java.lang.Class)
         */
        @Override
        public <T> T mapToObject(String source, Class<T> clazz) throws IOException {
            return objectMapper.readValue(source, clazz);
        }
    }
}
