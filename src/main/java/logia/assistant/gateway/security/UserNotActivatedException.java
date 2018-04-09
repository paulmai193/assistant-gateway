package logia.assistant.gateway.security;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is thrown in case of a not activated user trying to authenticate.
 *
 * @author Dai Mai
 */
public class UserNotActivatedException extends AuthenticationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new user not activated exception.
     *
     * @param message the message
     */
    public UserNotActivatedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new user not activated exception.
     *
     * @param message the message
     * @param t the t
     */
    public UserNotActivatedException(String message, Throwable t) {
        super(message, t);
    }
}
