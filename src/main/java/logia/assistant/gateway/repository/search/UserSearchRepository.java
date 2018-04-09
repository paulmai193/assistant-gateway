package logia.assistant.gateway.repository.search;

import logia.assistant.gateway.domain.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the User entity.
 *
 * @author Dai Mai
 */
public interface UserSearchRepository extends ElasticsearchRepository<User, Long> {
}
