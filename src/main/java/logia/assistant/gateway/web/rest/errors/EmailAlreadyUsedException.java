package logia.assistant.gateway.web.rest.errors;

/**
 * The Class EmailAlreadyUsedException.
 *
 * @author Dai Mai
 */
public class EmailAlreadyUsedException extends BadRequestAlertException {

    /**
     * Instantiates a new email already used exception.
     */
    public EmailAlreadyUsedException() {
        super(ErrorConstants.EMAIL_ALREADY_USED_TYPE, "Email address already in use", "userManagement", "emailexists");
    }
}
