package logia.assistant.gateway.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.springframework.data.elasticsearch.annotations.Document;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Credential.
 *
 * @author Dai Mai
 */
@Entity
@Table(name = "credential")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "credential")
public class Credential implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /** The login. */
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "login", length = 50, nullable = false)
    private String login;

    /** The password hash. */
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String passwordHash;

    /** The last login date. */
    @Column(name = "last_login_date")
    private ZonedDateTime lastLoginDate;

    /** The activation key. */
    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    private String activation_key;

    /** The reset key. */
    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    private String reset_key;

    /** The reset date. */
    @Column(name = "reset_date")
    private Instant reset_date;

    /** The activated. */
    @NotNull
    @Column(name = "activated", nullable = false)
    private Boolean activated;

    /** The user. */
    @ManyToOne(optional = false)
    @NotNull
    private User user;

    /**
     * Gets the id.
     *
     * @return the id
     */
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
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
     * Login.
     *
     * @param login the login
     * @return the credential
     */
    public Credential login(String login) {
        this.login = login;
        return this;
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
     * Password hash.
     *
     * @param passwordHash the password hash
     * @return the credential
     */
    public Credential passwordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
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
     * Last login date.
     *
     * @param lastLoginDate the last login date
     * @return the credential
     */
    public Credential lastLoginDate(ZonedDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
        return this;
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
     * Activation key.
     *
     * @param activation_key the activation key
     * @return the credential
     */
    public Credential activation_key(String activation_key) {
        this.activation_key = activation_key;
        return this;
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
     * Reset key.
     *
     * @param reset_key the reset key
     * @return the credential
     */
    public Credential reset_key(String reset_key) {
        this.reset_key = reset_key;
        return this;
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
     * Reset date.
     *
     * @param reset_date the reset date
     * @return the credential
     */
    public Credential reset_date(Instant reset_date) {
        this.reset_date = reset_date;
        return this;
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
     * Activated.
     *
     * @param activated the activated
     * @return the credential
     */
    public Credential activated(Boolean activated) {
        this.activated = activated;
        return this;
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
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * User.
     *
     * @param user the user
     * @return the credential
     */
    public Credential user(User user) {
        this.user = user;
        return this;
    }

    /**
     * Sets the user.
     *
     * @param user the new user
     */
    public void setUser(User user) {
        this.user = user;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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
        Credential credential = (Credential) o;
        if (credential.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), credential.getId());
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
        return "Credential{" +
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
