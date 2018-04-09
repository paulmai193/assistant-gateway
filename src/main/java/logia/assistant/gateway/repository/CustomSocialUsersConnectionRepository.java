package logia.assistant.gateway.repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

import logia.assistant.gateway.domain.SocialUserConnection;

/**
 * The Class CustomSocialUsersConnectionRepository.
 *
 * @author Dai Mai
 */
public class CustomSocialUsersConnectionRepository implements UsersConnectionRepository {

    /** The social user connection repository. */
    private SocialUserConnectionRepository socialUserConnectionRepository;

    /** The connection factory locator. */
    private ConnectionFactoryLocator connectionFactoryLocator;

    /**
     * Instantiates a new custom social users connection repository.
     *
     * @param socialUserConnectionRepository the social user connection repository
     * @param connectionFactoryLocator the connection factory locator
     */
    public CustomSocialUsersConnectionRepository(SocialUserConnectionRepository socialUserConnectionRepository, ConnectionFactoryLocator connectionFactoryLocator) {
        this.socialUserConnectionRepository = socialUserConnectionRepository;
        this.connectionFactoryLocator = connectionFactoryLocator;
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.UsersConnectionRepository#findUserIdsWithConnection(org.springframework.social.connect.Connection)
     */
    @Override
    public List<String> findUserIdsWithConnection(Connection<?> connection) {
        ConnectionKey key = connection.getKey();
        List<SocialUserConnection> socialUserConnections =
            socialUserConnectionRepository.findAllByProviderIdAndProviderUserId(key.getProviderId(), key.getProviderUserId());
        return socialUserConnections.stream()
            .map(SocialUserConnection::getUserId)
            .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.UsersConnectionRepository#findUserIdsConnectedTo(java.lang.String, java.util.Set)
     */
    @Override
    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
        List<SocialUserConnection> socialUserConnections =
            socialUserConnectionRepository.findAllByProviderIdAndProviderUserIdIn(providerId, providerUserIds);
        return socialUserConnections.stream()
            .map(SocialUserConnection::getUserId)
            .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.UsersConnectionRepository#createConnectionRepository(java.lang.String)
     */
    @Override
    public ConnectionRepository createConnectionRepository(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return new CustomSocialConnectionRepository(userId, socialUserConnectionRepository, connectionFactoryLocator);
    }
}
