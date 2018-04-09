package logia.assistant.gateway.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Social user.
 *
 * @author Dai Mai
 */
@Entity
@Table(name = "jhi_social_user_connection")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

public class SocialUserConnection implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    /** The user id. */
    @NotNull
    @Column(name = "user_id", length = 255, nullable = false)
    private String userId;

    /** The provider id. */
    @NotNull
    @Column(name = "provider_id", length = 255, nullable = false)
    private String providerId;

    /** The provider user id. */
    @NotNull
    @Column(name = "provider_user_id", length = 255, nullable = false)
    private String providerUserId;

    /** The rank. */
    @NotNull
    @Column(nullable = false)
    private Long rank;

    /** The display name. */
    @Column(name = "display_name", length = 255)
    private String displayName;

    /** The profile URL. */
    @Column(name = "profile_url", length = 255)
    private String profileURL;

    /** The image URL. */
    @Column(name = "image_url", length = 255)
    private String imageURL;

    /** The access token. */
    @NotNull
    @Column(name = "access_token", length = 255, nullable = false)
    private String accessToken;

    /** The secret. */
    @Column(length = 255)
    private String secret;

    /** The refresh token. */
    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    /** The expire time. */
    @Column(name = "expire_time")
    private Long expireTime;

    /**
     * Instantiates a new social user connection.
     */
    public SocialUserConnection() {}
    
    /**
     * Instantiates a new social user connection.
     *
     * @param userId the user id
     * @param providerId the provider id
     * @param providerUserId the provider user id
     * @param rank the rank
     * @param displayName the display name
     * @param profileURL the profile URL
     * @param imageURL the image URL
     * @param accessToken the access token
     * @param secret the secret
     * @param refreshToken the refresh token
     * @param expireTime the expire time
     */
    public SocialUserConnection(String userId,
                                String providerId,
                                String providerUserId,
                                Long rank,
                                String displayName,
                                String profileURL,
                                String imageURL,
                                String accessToken,
                                String secret,
                                String refreshToken,
                                Long expireTime) {
        this.userId = userId;
        this.providerId = providerId;
        this.providerUserId = providerUserId;
        this.rank = rank;
        this.displayName = displayName;
        this.profileURL = profileURL;
        this.imageURL = imageURL;
        this.accessToken = accessToken;
        this.secret = secret;
        this.refreshToken = refreshToken;
        this.expireTime = expireTime;
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
     * Gets the user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the new user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the provider id.
     *
     * @return the provider id
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * Sets the provider id.
     *
     * @param providerId the new provider id
     */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /**
     * Gets the provider user id.
     *
     * @return the provider user id
     */
    public String getProviderUserId() {
        return providerUserId;
    }

    /**
     * Sets the provider user id.
     *
     * @param providerUserId the new provider user id
     */
    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    /**
     * Gets the rank.
     *
     * @return the rank
     */
    public Long getRank() {
        return rank;
    }

    /**
     * Sets the rank.
     *
     * @param rank the new rank
     */
    public void setRank(Long rank) {
        this.rank = rank;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the profile URL.
     *
     * @return the profile URL
     */
    public String getProfileURL() {
        return profileURL;
    }

    /**
     * Sets the profile URL.
     *
     * @param profileURL the new profile URL
     */
    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    /**
     * Gets the image URL.
     *
     * @return the image URL
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Sets the image URL.
     *
     * @param imageURL the new image URL
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /**
     * Gets the access token.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken the new access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets the secret.
     *
     * @return the secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the secret.
     *
     * @param secret the new secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Gets the refresh token.
     *
     * @return the refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the refresh token.
     *
     * @param refreshToken the new refresh token
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Gets the expire time.
     *
     * @return the expire time
     */
    public Long getExpireTime() {
        return expireTime;
    }

    /**
     * Sets the expire time.
     *
     * @param expireTime the new expire time
     */
    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
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

        SocialUserConnection user = (SocialUserConnection) o;

        if (!id.equals(user.id)) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SocialUserConnection{" +
            "id=" + id +
            ", userId=" + userId +
            ", providerId='" + providerId + '\'' +
            ", providerUserId='" + providerUserId + '\'' +
            ", rank=" + rank +
            ", displayName='" + displayName + '\'' +
            ", profileURL='" + profileURL + '\'' +
            ", imageURL='" + imageURL + '\'' +
            ", accessToken='" + accessToken + '\'' +
            ", secret='" + secret + '\'' +
            ", refreshToken='" + refreshToken + '\'' +
            ", expireTime=" + expireTime +
            '}';
    }
}
