package logia.assistant.gateway.repository.search;

import logia.assistant.gateway.domain.User;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the User entity.
 *
 * @author Dai Mai
 */
public interface UserSearchRepository extends ElasticsearchRepository<User, Long> {
    
    /**
     * Find user by uuid.
     *
     * @param uuid the uuid
     * @return the optional
     */
    Optional<User> findOneByUuid(String uuid);
}
