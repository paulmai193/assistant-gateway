package logia.assistant.gateway.web.rest.vm;

import logia.assistant.gateway.service.dto.UserDTO;
import javax.validation.constraints.Size;

/**
 * View Model extending the UserDTO, which is meant to be used in the user management UI.
 *
 * @author Dai Mai
 */
public class ManagedUserVM extends UserDTO {

    /** The Constant PASSWORD_MIN_LENGTH. */
    public static final int PASSWORD_MIN_LENGTH = 4;

    /** The Constant PASSWORD_MAX_LENGTH. */
    public static final int PASSWORD_MAX_LENGTH = 100;

    /** The password. */
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    /**
     * Instantiates a new managed user VM.
     */
    public ManagedUserVM() {
        // Empty constructor needed for Jackson.
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.dto.UserDTO#toString()
     */
    @Override
    public String toString() {
        return "ManagedUserVM{" +
            "} " + super.toString();
    }
}
