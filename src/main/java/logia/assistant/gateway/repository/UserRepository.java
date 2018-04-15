package logia.assistant.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import logia.assistant.gateway.domain.User;

/**
 * Spring Data JPA repository for the User entity.
 *
 * @author Dai Mai
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** The users by email cache. */
    String USERS_BY_EMAIL_CACHE = "usersByEmail";

//    /**
//     * Find one by email ignore case.
//     *
//     * @param email the email
//     * @return the optional
//     */
//    Optional<User> findOneByEmailIgnoreCase(String email);

    /**
     * Find one with authorities by id.
     *
     * @param id the id
     * @return the optional
     */
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesById(Long id);

    /**
     * Find all by login not.
     *
     * @param pageable the pageable
     * @param login the login
     * @return the page
     */
    Page<User> findAllByLoginNot(Pageable pageable, String login);
    
    /**
     * Find all users not have credential.
     *
     * @return the list
     */
    @Query(value = "select u from User u where u.id not in (select c.user.id from Credential c)")
    List<User> findAllNotHaveCredential();
}
