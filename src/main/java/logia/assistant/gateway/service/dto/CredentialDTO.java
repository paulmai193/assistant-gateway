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
    private String activation_key;

    /** The reset key. */
    @Size(max = 20)
    private String reset_key;

    /** The reset date. */
    private Instant reset_date;

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
     * Gets the activation key.
     *
     * @return the activation key
     */
    public String getActivation_key() {
        return activation_key;
    }

    /**
     * Sets the activation key.
     *
     * @param activation_key the new activation key
     */
    public void setActivation_key(String activation_key) {
        this.activation_key = activation_key;
    }

    /**
     * Gets the reset key.
     *
     * @return the reset key
     */
    public String getReset_key() {
        return reset_key;
    }

    /**
     * Sets the reset key.
     *
     * @param reset_key the new reset key
     */
    public void setReset_key(String reset_key) {
        this.reset_key = reset_key;
    }

    /**
     * Gets the reset date.
     *
     * @return the reset date
     */
    public Instant getReset_date() {
        return reset_date;
    }

    /**
     * Sets the reset date.
     *
     * @param reset_date the new reset date
     */
    public void setReset_date(Instant reset_date) {
        this.reset_date = reset_date;
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
            ", activation_key='" + getActivation_key() + "'" +
            ", reset_key='" + getReset_key() + "'" +
            ", reset_date='" + getReset_date() + "'" +
            ", activated='" + isActivated() + "'" +
            "}";
    }
}
