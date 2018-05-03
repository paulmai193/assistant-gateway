package logia.assistant.gateway.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import logia.assistant.share.common.entity.AbstractUuidEntity;

/**
 * A user.
 *
 * @author Dai Mai
 */
@Entity
@Table(name = "jhi_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "user")
public class User extends AbstractUuidEntity implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;
    
    /** The password. */
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60)
    private String password;

    /** The first name. */
    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    /** The last name. */
    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    /** The lang key. */
    @Size(min = 2, max = 6)
    @Column(name = "lang_key", length = 6)
    private String langKey;

    /** The image url. */
    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    /** The activation key. */
    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    private String activationKey;

    /** The activated. */
    @NotNull
    @Column(name = "activated", nullable = false)
    private Boolean activated;

    /** The authorities. */
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "jhi_user_authority",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    /**
     * Instantiates a new user.
     */
    public User() {
        super();
    }

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
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Password.
     *
     * @param password the password
     * @return the user
     */
    public User password(String password) {
        this.setPassword(password);
        return this;
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * First name.
     *
     * @param firstName the first name
     * @return the user
     */
    public User firstName(String firstName) {
        this.setFirstName(firstName);
        return this;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Last name.
     *
     * @param lastName the last name
     * @return the user
     */
    public User lastName(String lastName) {
        this.setLastName(lastName);
        return this;
    }
    
    /**
     * Gets the image url.
     *
     * @return the image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image url.
     *
     * @param imageUrl the new image url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    /**
     * Image url.
     *
     * @param imageUrl the image url
     * @return the user
     */
    public User imageUrl(String imageUrl) {
        this.setImageUrl(imageUrl);
        return this;
    }

    /**
     * Gets the lang key.
     *
     * @return the lang key
     */
    public String getLangKey() {
        return langKey;
    }

    /**
     * Sets the lang key.
     *
     * @param langKey the new lang key
     */
    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }
    
    /**
     * Lang key.
     *
     * @param langKey the lang key
     * @return the user
     */
    public User langKey(String langKey) {
        this.setLangKey(langKey);
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
     * Activation key.
     *
     * @param activationKey the activation key
     * @return the credential
     */
    public User activationKey(String activationKey) {
        this.setActivationKey(activationKey);
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
    public User activated(Boolean activated) {
        this.setActivated(activated);
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
     * Gets the authorities.
     *
     * @return the authorities
     */
    public Set<Authority> getAuthorities() {
        return authorities;
    }

    /**
     * Sets the authorities.
     *
     * @param authorities the new authorities
     */
    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
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

        User user = (User) o;
        return !(user.getId() == null || getId() == null) && Objects.equals(getId(), user.getId());
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
        return "User{" +
            "uuid='" + getUuid() + '\'' +
            ", id='" + id + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", langKey='" + langKey + '\'' +
            ", activationKey='" + getActivationKey() + "'" +
            ", activated='" + isActivated() + "'" +
            "}";
    }
}
