package logia.assistant.gateway.repository;

import logia.assistant.gateway.domain.User;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

/**
 * Spring Data JPA repository for the User entity.
 *
 * @author Dai Mai
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** The users by login cache. */
    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    /** The users by email cache. */
    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    /**
     * Find one by activation key.
     *
     * @param activationKey the activation key
     * @return the optional
     */
    Optional<User> findOneByActivationKey(String activationKey);

    /**
     * Find all by activated is false and created date before.
     *
     * @param dateTime the date time
     * @return the list
     */
    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(Instant dateTime);

    /**
     * Find one by reset key.
     *
     * @param resetKey the reset key
     * @return the optional
     */
    Optional<User> findOneByResetKey(String resetKey);

    /**
     * Find one by email ignore case.
     *
     * @param email the email
     * @return the optional
     */
    Optional<User> findOneByEmailIgnoreCase(String email);

    /**
     * Find one by login.
     *
     * @param login the login
     * @return the optional
     */
    Optional<User> findOneByLogin(String login);

    /**
     * Find one with authorities by id.
     *
     * @param id the id
     * @return the optional
     */
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesById(Long id);

    /**
     * Find one with authorities by login.
     *
     * @param login the login
     * @return the optional
     */
    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    /**
     * Find one with authorities by email.
     *
     * @param email the email
     * @return the optional
     */
    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<User> findOneWithAuthoritiesByEmail(String email);

    /**
     * Find all by login not.
     *
     * @param pageable the pageable
     * @param login the login
     * @return the page
     */
    Page<User> findAllByLoginNot(Pageable pageable, String login);
}
