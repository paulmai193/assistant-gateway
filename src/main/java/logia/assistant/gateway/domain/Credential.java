package logia.assistant.gateway.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.springframework.data.elasticsearch.annotations.Document;

import logia.assistant.share.common.entity.AbstractAuditingEntity;

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
public class Credential extends AbstractAuditingEntity implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /** The login. */
    @NotNull
    @Size(min = 5, max = 100)
    @Column(name = "login", length = 100, nullable = false)
    private String login;

    /** The last login date. */
    @Column(name = "last_login_date")
    private ZonedDateTime lastLoginDate;

    /** The activation key. */
    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    private String activationKey;

    /** The reset key. */
    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    private String resetKey;

    /** The reset date. */
    @Column(name = "reset_date")
    private Instant resetDate;

    /** The activated. */
    @NotNull
    @Column(name = "activated", nullable = false)
    private Boolean activated;

    /** The primary. */
    @NotNull
    @Column(name = "jhi_primary", nullable = false)
    private Boolean primary;

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
        this.setLogin(login);;
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
    public String getActivationKey() {
        return activationKey;
    }

    /**
     * Activation key.
     *
     * @param activationKey the activation key
     * @return the credential
     */
    public Credential activationKey(String activationKey) {
        this.activationKey = activationKey;
        return this;
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
     * Gets the reset key.
     *
     * @return the reset key
     */
    public String getResetKey() {
        return resetKey;
    }

    /**
     * Reset key.
     *
     * @param resetKey the reset key
     * @return the credential
     */
    public Credential resetKey(String resetKey) {
        this.resetKey = resetKey;
        return this;
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
     * Gets the reset date.
     *
     * @return the reset date
     */
    public Instant getResetDate() {
        return resetDate;
    }

    /**
     * Reset date.
     *
     * @param resetDate the reset date
     * @return the credential
     */
    public Credential resetDate(Instant resetDate) {
        this.resetDate = resetDate;
        return this;
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
     * Checks if is primary.
     *
     * @return the boolean
     */
    public Boolean isPrimary() {
        return primary;
    }

    /**
     * Primary.
     *
     * @param primary the primary
     * @return the credential
     */
    public Credential primary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    /**
     * Sets the primary.
     *
     * @param primary the new primary
     */
    public void setPrimary(Boolean primary) {
        this.primary = primary;
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
            ", lastLoginDate='" + getLastLoginDate() + "'" +
            ", activationKey='" + getActivationKey() + "'" +
            ", resetKey='" + getResetKey() + "'" +
            ", resetDate='" + getResetDate() + "'" +
            ", activated='" + isActivated() + "'" +
            ", primary='" + isPrimary() + "'" +
            ", user='" + getUser() + "'" +
            "}";
    }
    
    /**
     * Clone to another object.
     *
     * @param credential the credential
     * @return the credential
     */
    public static Credential clone(Credential credential) {
        return new Credential().activated(credential.isActivated()).user(credential.getUser());
    }
}
