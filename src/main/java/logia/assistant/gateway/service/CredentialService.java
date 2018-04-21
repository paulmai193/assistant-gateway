package logia.assistant.gateway.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.service.dto.CredentialDTO;
import logia.assistant.share.common.service.EntityService;

/**
 * Service Interface for managing Credential.
 *
 * @author Dai Mai
 */
public interface CredentialService extends EntityService<CredentialDTO, Credential, Long> {

    /**
     * Delete.
     *
     * @param login the login
     * @return the credential
     */
    Credential delete(String login);

    /**
     * Find all by login not.
     *
     * @param pageable the pageable
     * @param login the login
     * @return the page
     */
    Page<Credential> findAllByLoginNot(Pageable pageable, String login);

    /**
     * Find by user id.
     *
     * @param userId the user id
     * @return the list
     */
    List<Credential> findByUserId(Long userId);

    /**
     * Find credential by activation key.
     *
     * @param activationKey the activation key
     * @return the optional
     */
    Optional<Credential> findOneByActivationKey(String activationKey);

    /**
     * Find one by email.
     *
     * @param mail the email
     * @return the optional
     */
    Optional<Credential> findOneByEmail(String mail);

    /**
     * Find one by reset key.
     *
     * @param resetKey the reset key
     * @return the optional
     */
    Optional<Credential> findOneByResetKey(String resetKey);

    /**
     * Find one with user by login.
     *
     * @param login the login
     * @return the optional
     */
    Optional<Credential> findOneWithUserByLogin(String login);

    /**
     * Removes the not activated users.
     */
    void removeNotActivatedUsers();

    /**
     * Update by user id.
     *
     * @param userId the user id
     * @param login the login
     * @return the credential
     */
    Credential updateByUserId(Long userId, String login);

}
