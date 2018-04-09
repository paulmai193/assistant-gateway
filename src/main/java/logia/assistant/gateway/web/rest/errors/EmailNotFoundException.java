package logia.assistant.gateway.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * The Class EmailNotFoundException.
 *
 * @author Dai Mai
 */
public class EmailNotFoundException extends AbstractThrowableProblem {

    /**
     * Instantiates a new email not found exception.
     */
    public EmailNotFoundException() {
        super(ErrorConstants.EMAIL_NOT_FOUND_TYPE, "Email address not registered", Status.BAD_REQUEST);
    }
}
