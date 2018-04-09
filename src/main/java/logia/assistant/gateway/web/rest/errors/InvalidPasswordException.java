package logia.assistant.gateway.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * The Class InvalidPasswordException.
 *
 * @author Dai Mai
 */
public class InvalidPasswordException extends AbstractThrowableProblem {

    /**
     * Instantiates a new invalid password exception.
     */
    public InvalidPasswordException() {
        super(ErrorConstants.INVALID_PASSWORD_TYPE, "Incorrect password", Status.BAD_REQUEST);
    }
}
