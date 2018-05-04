package logia.assistant.gateway.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.User;

import logia.assistant.gateway.domain.Credential;

/**
 * The Class DomainUserDetail.
 *
 * @author Dai Mai
 */
public class DomainUserDetail extends User {

    /** The Constant serialVersionUID. */
    private static final long                         serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    /** The credential. */
    private final Credential credential;

    /**
     * Instantiates a new domain user detail.
     *
     * @param username the username
     * @param password the password
     * @param authorities the authorities
     * @param credential the credential
     */
    public DomainUserDetail(String username, String password,
            Collection<? extends GrantedAuthority> authorities,
            Credential credential) {
        super(username, password, authorities);
        this.credential = credential;
    }

    /**
     * Gets the entity.
     *
     * @return the entity
     */
    public Credential getCredential() {
        return credential;
    }

}
