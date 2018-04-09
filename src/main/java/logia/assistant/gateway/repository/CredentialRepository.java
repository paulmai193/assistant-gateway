package logia.assistant.gateway.repository;

import logia.assistant.gateway.domain.Credential;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import java.util.List;

/**
 * Spring Data JPA repository for the Credential entity.
 *
 * @author Dai Mai
 */
@SuppressWarnings("unused")
@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /**
     * Find by user is current user.
     *
     * @return the list
     */
    @Query("select credential from Credential credential where credential.user.login = ?#{principal.username}")
    List<Credential> findByUserIsCurrentUser();

}
