package logia.assistant.gateway.service;

import java.util.Optional;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.service.dto.UserDTO;

/**
 * The Interface AccountBusinessService.
 *
 * @author Dai Mai
 */
public interface AccountBusinessService {
    
    /**
     * Activate registration.
     *
     * @param key the key
     * @return the optional
     */
    Optional<Credential> activateRegistration(String key);
    
    /**
     * Change password.
     *
     * @param password the password
     */
    void changePassword(String password);
    
    /**
     * Complete password reset.
     *
     * @param newPassword the new password
     * @param kresetKey the kreset key
     * @return the optional
     */
    Optional<User> completePasswordReset(String newPassword, String kresetKey);
    
    /**
     * Creates the user.
     *
     * @param userDTO the user DTO
     * @return the user
     */
    User createUser(UserDTO userDTO);

    /**
     * Delete user.
     *
     * @param uuid the UUID
     */
    void deleteUser(String uuid);

    /**
     * Register user.
     *
     * @param userDTO the user DTO
     * @param password the password
     * @return the user
     */
    User registerUser(UserDTO userDTO, String password);

    /**
     * Request password reset.
     *
     * @param mail the mail
     * @return the optional
     */
    Optional<Credential> requestPasswordReset(String mail);

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param langKey language key
     * @param imageUrl image URL of user
     */
    void updateUser(String firstName, String lastName, String langKey, String imageUrl);

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    Optional<UserDTO> updateUser(UserDTO userDTO);
    
}
