package logia.assistant.gateway.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import logia.assistant.gateway.domain.Credential;

/**
 * The Interface AccountBusinessService.
 *
 * @author Dai Mai
 */
public interface AccountBusinessService {
    
    /**
     * Update by user id.
     *
     * @param userId the user id
     * @param login the login
     * @return the credential
     */
    Credential updateByUserId(Long userId, String login);
    
    /**
     * Delete.
     *
     * @param login the login
     * @return the credential
     */
    Credential delete(String login);
    
    /**
     * Activate registration.
     *
     * @param key the key
     * @return the optional
     */
    Optional<Credential> activateRegistration(String key);
    
    /**
     * Request password reset.
     *
     * @param mail the mail
     * @return the optional
     */
    Optional<Credential> requestPasswordReset(String mail);
    
    /**
     * Find one by reset key.
     *
     * @param resetKey the reset key
     * @return the optional
     */
    Optional<Credential> findOneByResetKey(String resetKey);
    
    /**
     * Removes the not activated users.
     */
    void removeNotActivatedUsers();
    
    /**
     * Find one with user by login.
     *
     * @param userLogin the user login
     * @return the optional
     */
    Optional<Credential> findOneWithUserByLogin(String userLogin);
    
    /**
     * Find by user id.
     *
     * @param userId the user id
     * @return the list
     */
    List<Credential> findByUserId(Long userId);
    
    /**
     * Find all by login not.
     *
     * @param pageable the pageable
     * @param login the login
     * @return the page
     */
    Page<Credential> findAllByLoginNot(Pageable pageable, String login);
}
