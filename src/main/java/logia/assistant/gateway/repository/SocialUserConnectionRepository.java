package logia.assistant.gateway.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import logia.assistant.gateway.domain.SocialUserConnection;

/**
 * Spring Data JPA repository for the Social User Connection entity.
 *
 * @author Dai Mai
 */
public interface SocialUserConnectionRepository extends JpaRepository<SocialUserConnection, Long> {

    /**
     * Find all by provider id and provider user id.
     *
     * @param providerId the provider id
     * @param providerUserId the provider user id
     * @return the list
     */
    List<SocialUserConnection> findAllByProviderIdAndProviderUserId(String providerId, String providerUserId);

    /**
     * Find all by provider id and provider user id in.
     *
     * @param providerId the provider id
     * @param providerUserIds the provider user ids
     * @return the list
     */
    List<SocialUserConnection> findAllByProviderIdAndProviderUserIdIn(String providerId, Set<String> providerUserIds);

    /**
     * Find all by user id order by provider id asc rank asc.
     *
     * @param userId the user id
     * @return the list
     */
    List<SocialUserConnection> findAllByUserIdOrderByProviderIdAscRankAsc(String userId);

    /**
     * Find all by user id and provider id order by rank asc.
     *
     * @param userId the user id
     * @param providerId the provider id
     * @return the list
     */
    List<SocialUserConnection> findAllByUserIdAndProviderIdOrderByRankAsc(String userId, String providerId);

    /**
     * Find all by user id and provider id and provider user id in.
     *
     * @param userId the user id
     * @param providerId the provider id
     * @param provideUserId the provide user id
     * @return the list
     */
    List<SocialUserConnection> findAllByUserIdAndProviderIdAndProviderUserIdIn(String userId, String providerId, List<String> provideUserId);

    /**
     * Find one by user id and provider id and provider user id.
     *
     * @param userId the user id
     * @param providerId the provider id
     * @param providerUserId the provider user id
     * @return the social user connection
     */
    SocialUserConnection findOneByUserIdAndProviderIdAndProviderUserId(String userId, String providerId, String providerUserId);

    /**
     * Delete by user id and provider id.
     *
     * @param userId the user id
     * @param providerId the provider id
     */
    void deleteByUserIdAndProviderId(String userId, String providerId);

    /**
     * Delete by user id and provider id and provider user id.
     *
     * @param userId the user id
     * @param providerId the provider id
     * @param providerUserId the provider user id
     */
    void deleteByUserIdAndProviderIdAndProviderUserId(String userId, String providerId, String providerUserId);
}
