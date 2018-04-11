package logia.assistant.gateway.service.dto;


import java.time.Instant;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the Credential entity.
 *
 * @author Dai Mai
 */
public class CredentialDTO implements Serializable {

    /** The id. */
    private Long id;

    /** The login. */
    @NotNull
    @Size(min = 1, max = 50)
    private String login;

    /** The password hash. */
    @NotNull
    @Size(min = 60, max = 60)
    private String passwordHash;

    /** The last login date. */
    private ZonedDateTime lastLoginDate;

    /** The activation key. */
    @Size(max = 20)
    private String activationKey;

    /** The reset key. */
    @Size(max = 20)
    private String resetKey;

    /** The reset date. */
    private Instant resetDate;

    /** The activated. */
    @NotNull
    private Boolean activated;

    /** The user id. */
    private Long userId;

    /** The user login. */
    private String userLogin;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the login.
     *
     * @param login the new login
     */
    public void setLogin(String login) {
        this.login = login;
    }
    
    /**
     * Login.
     *
     * @param login the login
     * @return the credential DTO
     */
    public CredentialDTO login(String login) {
        this.setLogin(login);
        return this;
    }

    /**
     * Gets the password hash.
     *
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     *
     * @param passwordHash the new password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    /**
     * Password hash.
     *
     * @param passwordHash the password hash
     * @return the credential DTO
     */
    public CredentialDTO passwordHash(String passwordHash) {
        this.setPasswordHash(passwordHash);
        return this;
    }

    /**
     * Gets the last login date.
     *
     * @return the last login date
     */
    public ZonedDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * Sets the last login date.
     *
     * @param lastLoginDate the new last login date
     */
    public void setLastLoginDate(ZonedDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    
    /**
     * Last login date.
     *
     * @param lastLoginDate the last login date
     * @return the credential DTO
     */
    public CredentialDTO lastLoginDate(ZonedDateTime lastLoginDate) {
        this.setLastLoginDate(lastLoginDate);
        return this;
    }

    /**
     * Gets the activation key.
     *
     * @return the activation key
     */
    public String getActivationKey() {
        return activationKey;
    }

    /**
     * Sets the activation key.
     *
     * @param activationKey the new activation key
     */
    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }
    
    /**
     * Activation key.
     *
     * @param activationKey the activation key
     * @return the credential DTO
     */
    public CredentialDTO activationKey(String activationKey) {
        this.setActivated(activated);
        return this;
    }

    /**
     * Gets the reset key.
     *
     * @return the reset key
     */
    public String getResetKey() {
        return resetKey;
    }

    /**
     * Sets the reset key.
     *
     * @param resetKey the new reset key
     */
    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }
    
    /**
     * Reset key.
     *
     * @param resetKey the reset key
     * @return the credential DTO
     */
    public CredentialDTO resetKey(String resetKey) {
        this.setResetKey(resetKey);
        return this;
    }

    /**
     * Gets the reset date.
     *
     * @return the reset date
     */
    public Instant getResetDate() {
        return resetDate;
    }

    /**
     * Sets the reset date.
     *
     * @param resetDate the new reset date
     */
    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }
    
    /**
     * Reset date.
     *
     * @param resetDate the reset date
     * @return the credential DTO
     */
    public CredentialDTO resetDate(Instant resetDate) {
        this.setResetDate(resetDate);
        return this;
    }

    /**
     * Checks if is activated.
     *
     * @return the boolean
     */
    public Boolean isActivated() {
        return activated;
    }

    /**
     * Sets the activated.
     *
     * @param activated the new activated
     */
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }
    
    /**
     * Activated.
     *
     * @param activated the activated
     * @return the credential DTO
     */
    public CredentialDTO activated(Boolean activated) {
        this.setActivated(activated);
        return this;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the new user id
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    /**
     * User id.
     *
     * @param userId the user id
     * @return the credential DTO
     */
    public CredentialDTO userId(Long userId) {
        this.setUserId(userId);
        return this;
    }

    /**
     * Gets the user login.
     *
     * @return the user login
     */
    public String getUserLogin() {
        return userLogin;
    }

    /**
     * Sets the user login.
     *
     * @param userLogin the new user login
     */
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }
    
    /**
     * User login.
     *
     * @param userLogin the user login
     * @return the credential DTO
     */
    public CredentialDTO userLogin(String userLogin) {
        this.setUserLogin(userLogin);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CredentialDTO credentialDTO = (CredentialDTO) o;
        if(credentialDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), credentialDTO.getId());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CredentialDTO{" +
            "id=" + getId() +
            ", login='" + getLogin() + "'" +
            ", passwordHash='" + getPasswordHash() + "'" +
            ", lastLoginDate='" + getLastLoginDate() + "'" +
            ", activationKey='" + getActivationKey() + "'" +
            ", resetKey='" + getResetKey() + "'" +
            ", resetDate='" + getResetDate() + "'" +
            ", activated='" + isActivated() + "'" +
            "}";
    }
}
