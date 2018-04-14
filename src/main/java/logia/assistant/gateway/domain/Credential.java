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
 */
@Entity
@Table(name = "credential")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "credential")
public class Credential implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "login", length = 50, nullable = false)
    private String login;

    @Column(name = "last_login_date")
    private ZonedDateTime lastLoginDate;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate;

    @NotNull
    @Column(name = "activated", nullable = false)
    private Boolean activated;

    @NotNull
    @Column(name = "jhi_primary", nullable = false)
    private Boolean primary;

    @ManyToOne(optional = false)
    @NotNull
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public Credential login(String login) {
        this.login = login;
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public ZonedDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public Credential lastLoginDate(ZonedDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
        return this;
    }

    public void setLastLoginDate(ZonedDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public Credential activationKey(String activationKey) {
        this.activationKey = activationKey;
        return this;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getResetKey() {
        return resetKey;
    }

    public Credential resetKey(String resetKey) {
        this.resetKey = resetKey;
        return this;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public Instant getResetDate() {
        return resetDate;
    }

    public Credential resetDate(Instant resetDate) {
        this.resetDate = resetDate;
        return this;
    }

    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }

    public Boolean isActivated() {
        return activated;
    }

    public Credential activated(Boolean activated) {
        this.activated = activated;
        return this;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Boolean isPrimary() {
        return primary;
    }

    public Credential primary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public User getUser() {
        return user;
    }

    public Credential user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

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
            "}";
    }
}
