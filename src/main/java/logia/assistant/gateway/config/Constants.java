package logia.assistant.gateway.config;

/**
 * Application constants.
 *
 * @author Dai Mai
 */
public final class Constants {

    /** The Constant LOGIN_REGEX. */
    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_'.@A-Za-z0-9-]*$";

    /** The Constant SYSTEM_ACCOUNT. */
    public static final String SYSTEM_ACCOUNT = "system";
    
    /** The Constant ANONYMOUS_USER. */
    public static final String ANONYMOUS_USER = "anonymoususer";
    
    /** The Constant DEFAULT_LANGUAGE. */
    public static final String DEFAULT_LANGUAGE = "vi";
    
    /**
     * Instantiates a new constants.
     */
    private Constants() {
    }
}
