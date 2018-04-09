package logia.assistant.gateway.config.social;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
// jhipster-needle-add-social-connection-factory-import-package

import io.github.jhipster.config.JHipsterProperties;
import logia.assistant.gateway.repository.CustomSocialUsersConnectionRepository;
import logia.assistant.gateway.repository.SocialUserConnectionRepository;
import logia.assistant.gateway.security.social.CustomSignInAdapter;
import logia.assistant.share.gateway.securiry.jwt.TokenProvider;

/**
 * Basic Spring Social configuration.
 * 
 * <p>
 * Creates the beans necessary to manage Connections to social services and
 * link accounts from those services to internal Users.
 *
 * @author Dai Mai
 */
@Configuration
@EnableSocial
public class SocialConfiguration implements SocialConfigurer {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(SocialConfiguration.class);

    /** The social user connection repository. */
    private final SocialUserConnectionRepository socialUserConnectionRepository;

    /** The environment. */
    private final Environment environment;

    /**
     * Instantiates a new social configuration.
     *
     * @param socialUserConnectionRepository the social user connection repository
     * @param environment the environment
     */
    public SocialConfiguration(SocialUserConnectionRepository socialUserConnectionRepository,
            Environment environment) {

        this.socialUserConnectionRepository = socialUserConnectionRepository;
        this.environment = environment;
    }

    /**
     * Connect controller.
     *
     * @param connectionFactoryLocator the connection factory locator
     * @param connectionRepository the connection repository
     * @return the connect controller
     */
    @Bean
    public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator,
            ConnectionRepository connectionRepository) {

        ConnectController controller = new ConnectController(connectionFactoryLocator, connectionRepository);
        controller.setApplicationUrl(environment.getProperty("spring.application.url"));
        return controller;
    }

    /* (non-Javadoc)
     * @see org.springframework.social.config.annotation.SocialConfigurer#addConnectionFactories(org.springframework.social.config.annotation.ConnectionFactoryConfigurer, org.springframework.core.env.Environment)
     */
    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer, Environment environment) {
        // Google configuration
        String googleClientId = environment.getProperty("spring.social.google.client-id");
        String googleClientSecret = environment.getProperty("spring.social.google.client-secret");
        if (googleClientId != null && googleClientSecret != null) {
            log.debug("Configuring GoogleConnectionFactory");
            connectionFactoryConfigurer.addConnectionFactory(
                new GoogleConnectionFactory(
                    googleClientId,
                    googleClientSecret
                )
            );
        } else {
            log.error("Cannot configure GoogleConnectionFactory id or secret null");
        }

        // Facebook configuration
        String facebookClientId = environment.getProperty("spring.social.facebook.client-id");
        String facebookClientSecret = environment.getProperty("spring.social.facebook.client-secret");
        if (facebookClientId != null && facebookClientSecret != null) {
            log.debug("Configuring FacebookConnectionFactory");
            connectionFactoryConfigurer.addConnectionFactory(
                new FacebookConnectionFactory(
                    facebookClientId,
                    facebookClientSecret
                )
            );
        } else {
            log.error("Cannot configure FacebookConnectionFactory id or secret null");
        }

        // Twitter configuration
        String twitterClientId = environment.getProperty("spring.social.twitter.client-id");
        String twitterClientSecret = environment.getProperty("spring.social.twitter.client-secret");
        if (twitterClientId != null && twitterClientSecret != null) {
            log.debug("Configuring TwitterConnectionFactory");
            connectionFactoryConfigurer.addConnectionFactory(
                new TwitterConnectionFactory(
                    twitterClientId,
                    twitterClientSecret
                )
            );
        } else {
            log.error("Cannot configure TwitterConnectionFactory id or secret null");
        }

        // jhipster-needle-add-social-connection-factory
    }

    /* (non-Javadoc)
     * @see org.springframework.social.config.annotation.SocialConfigurer#getUserIdSource()
     */
    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    /* (non-Javadoc)
     * @see org.springframework.social.config.annotation.SocialConfigurer#getUsersConnectionRepository(org.springframework.social.connect.ConnectionFactoryLocator)
     */
    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return new CustomSocialUsersConnectionRepository(socialUserConnectionRepository, connectionFactoryLocator);
    }

    /**
     * Sign in adapter.
     *
     * @param userDetailsService the user details service
     * @param jHipsterProperties the j hipster properties
     * @param tokenProvider the token provider
     * @return the sign in adapter
     */
    @Bean
    public SignInAdapter signInAdapter(UserDetailsService userDetailsService, JHipsterProperties jHipsterProperties,
            TokenProvider tokenProvider) {
        return new CustomSignInAdapter(userDetailsService, jHipsterProperties,
            tokenProvider);
    }

    /**
     * Provider sign in controller.
     *
     * @param connectionFactoryLocator the connection factory locator
     * @param usersConnectionRepository the users connection repository
     * @param signInAdapter the sign in adapter
     * @return the provider sign in controller
     */
    @Bean
    public ProviderSignInController providerSignInController(ConnectionFactoryLocator connectionFactoryLocator, UsersConnectionRepository usersConnectionRepository, SignInAdapter signInAdapter) {
        ProviderSignInController providerSignInController = new ProviderSignInController(connectionFactoryLocator, usersConnectionRepository, signInAdapter);
        providerSignInController.setSignUpUrl("/social/signup");
        providerSignInController.setApplicationUrl(environment.getProperty("spring.application.url"));
        return providerSignInController;
    }

    /**
     * Gets the provider sign in utils.
     *
     * @param connectionFactoryLocator the connection factory locator
     * @param usersConnectionRepository the users connection repository
     * @return the provider sign in utils
     */
    @Bean
    public ProviderSignInUtils getProviderSignInUtils(ConnectionFactoryLocator connectionFactoryLocator, UsersConnectionRepository usersConnectionRepository) {
        return new ProviderSignInUtils(connectionFactoryLocator, usersConnectionRepository);
    }
}
