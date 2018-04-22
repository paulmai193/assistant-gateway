package logia.assistant.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import logia.assistant.gateway.domain.User;
import logia.assistant.share.common.repository.UuidRepository;

/**
 * Spring Data JPA repository for the User entity.
 *
 * @author Dai Mai
 */
@Repository
public interface UserRepository extends UuidRepository<User, Long> {

    /** The users by email cache. */
    String USERS_BY_UUID_CACHE = "usersByUuid";

    /**
     * Find one with authorities by id.
     *
     * @param id the id
     * @return the optional
     */
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesById(Long id);

    /**
     * Find all users not have credential.
     *
     * @return the list
     */
    @Query(value = "select u from User u where u.id not in (select c.user.id from Credential c)")
    List<User> findAllNotHaveCredential();
    
    /**
     * Find one with authorities by uuid.
     *
     * @param uuid the uuid
     * @return the optional
     */
    @Cacheable(cacheNames = UserRepository.USERS_BY_UUID_CACHE)
    default Optional<User> findOneWithAuthoritiesByUuid(String uuid) {
        Optional<User> optUser = this.findOneByUuid(uuid);
        optUser.ifPresent(user -> user.getAuthorities().size()); // Dummy code to fetch authorities
        return optUser;
    }

}
