package logia.assistant.gateway.security;

import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 *
 * @author Dai Mai
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    /** The user repository. */
    private final UserRepository userRepository;

    /**
     * Instantiates a new domain user details service.
     *
     * @param userRepository the user repository
     */
    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        Optional<User> userByEmailFromDatabase = userRepository.findOneWithAuthoritiesByEmail(lowercaseLogin);
        return userByEmailFromDatabase.map(user -> createSpringSecurityUser(lowercaseLogin, user)).orElseGet(() -> {
            Optional<User> userByLoginFromDatabase = userRepository.findOneWithAuthoritiesByLogin(lowercaseLogin);
            return userByLoginFromDatabase.map(user -> createSpringSecurityUser(lowercaseLogin, user))
                .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the " +
                    "database"));
        });
    }

    /**
     * Creates the spring security user.
     *
     * @param lowercaseLogin the lowercase login
     * @param user the user
     * @return the org.springframework.security.core.userdetails. user
     */
    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin, User user) {
        if (!user.getActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getLogin(),
            user.getPassword(),
            grantedAuthorities);
    }
}
