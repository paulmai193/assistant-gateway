package logia.assistant.gateway.web.rest.errors;

import java.net.URI;

/**
 * The Class ErrorConstants.
 *
 * @author Dai Mai
 */
public final class ErrorConstants {

    /** The Constant ERR_CONCURRENCY_FAILURE. */
    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    
    /** The Constant ERR_VALIDATION. */
    public static final String ERR_VALIDATION = "error.validation";
    
    /** The Constant PROBLEM_BASE_URL. */
    public static final String PROBLEM_BASE_URL = "http://www.jhipster.tech/problem";
    
    /** The Constant DEFAULT_TYPE. */
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    
    /** The Constant CONSTRAINT_VIOLATION_TYPE. */
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    
    /** The Constant PARAMETERIZED_TYPE. */
    public static final URI PARAMETERIZED_TYPE = URI.create(PROBLEM_BASE_URL + "/parameterized");
    
    /** The Constant INVALID_PASSWORD_TYPE. */
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    
    /** The Constant EMAIL_ALREADY_USED_TYPE. */
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    
    /** The Constant LOGIN_ALREADY_USED_TYPE. */
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");
    
    /** The Constant EMAIL_NOT_FOUND_TYPE. */
    public static final URI EMAIL_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/email-not-found");

    /**
     * Instantiates a new error constants.
     */
    private ErrorConstants() {
    }
}
