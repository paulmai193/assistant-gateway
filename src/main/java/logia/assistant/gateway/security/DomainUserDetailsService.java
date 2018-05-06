package logia.assistant.gateway.security;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.repository.CredentialRepository;

/**
 * Authenticate a user from the database.
 *
 * @author Dai Mai
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    /** The credential repository. */
    @Inject
    private CredentialRepository credentialRepository;

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        return this.credentialRepository.findOneWithUserByLogin(login)
                .map(credential -> createSpringSecurityUser(lowercaseLogin, credential))
                .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the " + "database"));
    }

    /**
     * Creates the spring security user.
     *
     * @param lowercaseLogin the lowercase login
     * @param credential the credential
     * @return the domain user detail
     */
    private DomainUserDetail createSpringSecurityUser(String lowercaseLogin, Credential credential) {
        if (!credential.getUser().isActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }
        List<GrantedAuthority> grantedAuthorities = credential.getUser().getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());
        return new DomainUserDetail(credential.getLogin(),
                credential.getUser().getPassword(),
                grantedAuthorities, credential);
    }
}
