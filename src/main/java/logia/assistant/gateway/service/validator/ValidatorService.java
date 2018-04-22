package logia.assistant.gateway.service.validator;

import javax.inject.Inject;
import javax.validation.ValidationException;
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
    public void validateEmail(@Email String email) {
        this.validator.validate(email);
    }
    
    /**
     * Checks if given string is email.
     *
     * @param string the string
     * @return true, if string is email
     */
    public boolean isEmail(String string) {
        try {
            this.validateEmail(string);
            return true;
        }
        catch (IllegalArgumentException | ValidationException e) {
            return false;
        }
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
        this.credentialRepository.findOneWithUserByLogin(login.toLowerCase()).ifPresent(u -> {
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

}
