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
    @Size(min = 5, max = 100)
    @Column(name = "credential", length = 100, unique = true, nullable = false)
    private String credential;

    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String passwordHash;

    @Column(name = "last_login_date")
    private ZonedDateTime lastLoginDate;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    private String activation_key;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    private String reset_key;

    @Column(name = "reset_date")
    private Instant reset_date;

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

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public Credential credential(String credential) {
        this.setCredential(credential);;
        return this;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Credential passwordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public String getActivation_key() {
        return activation_key;
    }

    public Credential activation_key(String activation_key) {
        this.activation_key = activation_key;
        return this;
    }

    public void setActivation_key(String activation_key) {
        this.activation_key = activation_key;
    }

    public String getReset_key() {
        return reset_key;
    }

    public Credential reset_key(String reset_key) {
        this.reset_key = reset_key;
        return this;
    }

    public void setReset_key(String reset_key) {
        this.reset_key = reset_key;
    }

    public Instant getReset_date() {
        return reset_date;
    }

    public Credential reset_date(Instant reset_date) {
        this.reset_date = reset_date;
        return this;
    }

    public void setReset_date(Instant reset_date) {
        this.reset_date = reset_date;
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
            ", login='" + getCredential() + "'" +
            ", passwordHash='" + getPasswordHash() + "'" +
            ", lastLoginDate='" + getLastLoginDate() + "'" +
            ", activation_key='" + getActivation_key() + "'" +
            ", reset_key='" + getReset_key() + "'" +
            ", reset_date='" + getReset_date() + "'" +
            "}";
    }
}
