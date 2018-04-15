package logia.assistant.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.repository.UserRepository;

/**
 * The Class AbstractUserServiceInitTest.
 *
 * @author Dai Mai
 */
public abstract class AbstractUserServiceInitTest {
    
    /** The user repository. */
    @Autowired
    protected UserRepository userRepository;
    
    /** The credential repository. */
    @Autowired
    protected CredentialRepository credentialRepository;

    /** The user. */
    protected User user;
    
    /** The credential. */
    protected Credential credential;
}
