package logia.assistant.gateway.web.rest.errors;

/**
 * The Class LoginAlreadyUsedException.
 *
 * @author Dai Mai
 */
public class LoginAlreadyUsedException extends BadRequestAlertException {

    /**
     * Instantiates a new login already used exception.
     */
    public LoginAlreadyUsedException() {
        super(ErrorConstants.LOGIN_ALREADY_USED_TYPE, "Login already in use", "userManagement", "userexists");
    }
}
