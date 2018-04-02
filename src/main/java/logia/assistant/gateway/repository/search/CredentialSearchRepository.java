package logia.assistant.gateway.repository.search;

import logia.assistant.gateway.domain.Credential;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Credential entity.
 */
public interface CredentialSearchRepository extends ElasticsearchRepository<Credential, Long> {
}
