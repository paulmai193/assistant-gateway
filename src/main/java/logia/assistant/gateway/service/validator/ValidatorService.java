package logia.assistant.gateway.service.validator;

import javax.inject.Inject;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.springframework.stereotype.Component;

import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.web.rest.errors.InvalidPasswordException;
import logia.assistant.gateway.web.rest.errors.LoginAlreadyUsedException;
import logia.assistant.gateway.web.rest.vm.ManagedUserVM;

/**
 * The Class ValidatorService.
 *
 * @author Dai Mai
 */
@Component
public final class ValidatorService {

    /** The validator. */
    @Inject
    private Validator            validator;

    /** The credential repository. */
    @Inject
    private CredentialRepository credentialRepository;

    /**
     * Validate email.
     *
     * @param email the email
     */
    public void validateEmail(String email) {
        this.validator.validate(new EmailValidatorWrapper(email));
    }

    /**
     * Validate new credential.
     *
     * @param login the login
     * @param password the password
     */
    public void validateNewCredential(String login, String password) {
        this.validatePassword(password);
        this.validateNewCredential(login);
    }
    
    /**
     * Validate new credential.
     *
     * @param login the login
     */
    public void validateNewCredential(String login) {
        this.credentialRepository.findOneByLogin(login.toLowerCase()).ifPresent(u -> {
            throw new LoginAlreadyUsedException();
        });
    }
    
    /**
     * Validate password.
     *
     * @param password the password
     */
    public void validatePassword(String password) {
        if (!checkPasswordLength(password)) {
            throw new InvalidPasswordException();
        }
    }

    /**
     * Check password length.
     *
     * @param password the password
     * @return true, if successful
     */
    public static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password)
                && password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH
                && password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }

    /**
     * The Class EmailValidatorWrapper.
     *
     * @author Dai Mai
     */
    public static class EmailValidatorWrapper {

        /** The email. */
        @Email
        private String email;

        /**
         * Instantiates a new email validator wrapper.
         *
         * @param email the email
         */
        public EmailValidatorWrapper(String email) {
            super();
            this.email = email;
        }

        /**
         * Gets the email.
         *
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * Sets the email.
         *
         * @param email the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }

    }
}
