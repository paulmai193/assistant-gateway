package logia.assistant.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import logia.assistant.gateway.domain.Credential;

/**
 * Spring Data JPA repository for the Credential entity.
 *
 * @author Dai Mai
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /** The credentials by login cache. */
    String CREDENTIALS_BY_LOGIN_CACHE = "credentialsByLogin";
    
    /**
     * Find by user is current user.
     *
     * @return the list
     */
    @Query("select credential from Credential credential where credential.login = ?#{principal.username}")
    List<Credential> findByUserIsCurrentUser();
    
    /**
     * Find one by reset key.
     *
     * @param resetKey the reset key
     * @return the optional
     */
    Optional<Credential> findOneByResetKey(String resetKey);
    
    /**
     * Find one by login ignore case.
     *
     * @param login the login
     * @return the optional
     */
    Optional<Credential> findOneByLoginIgnoreCase(String login);

    /**
     * Find one by login.
     *
     * @param login the login
     * @return the optional
     */
    @Cacheable(cacheNames = CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
    @EntityGraph(attributePaths = {"user", "user.authorities"})
    Optional<Credential> findOneWithUserByLogin(String login);
    
    /**
     * Find one with user by user id.
     *
     * @param userId the user id
     * @return the optional
     */
    @EntityGraph(attributePaths = "user")
    List<Credential> findWithUserByUserId(Long userId);
    
    /**
     * Find all by login not.
     *
     * @param pageable the pageable
     * @param login the login
     * @return the page
     */
    Page<Credential> findAllByLoginNot(Pageable pageable, String login);
    
}
