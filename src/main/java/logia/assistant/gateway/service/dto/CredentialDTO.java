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
 */
public class CredentialDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 5, max = 100)
    private String credential;

    @NotNull
    @Size(min = 60, max = 60)
    private String passwordHash;

    private ZonedDateTime lastLoginDate;

    @Size(max = 20)
    private String activation_key;

    @Size(max = 20)
    private String reset_key;

    private Instant reset_date;

    private Long userId;

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public ZonedDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(ZonedDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getActivation_key() {
        return activation_key;
    }

    public void setActivation_key(String activation_key) {
        this.activation_key = activation_key;
    }

    public String getReset_key() {
        return reset_key;
    }

    public void setReset_key(String reset_key) {
        this.reset_key = reset_key;
    }

    public Instant getReset_date() {
        return reset_date;
    }

    public void setReset_date(Instant reset_date) {
        this.reset_date = reset_date;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CredentialDTO{" +
            "id=" + getId() +
            ", credential='" + getCredential() + "'" +
            ", passwordHash='" + getPasswordHash() + "'" +
            ", lastLoginDate='" + getLastLoginDate() + "'" +
            ", activation_key='" + getActivation_key() + "'" +
            ", reset_key='" + getReset_key() + "'" +
            ", reset_date='" + getReset_date() + "'" +
            "}";
    }
}
