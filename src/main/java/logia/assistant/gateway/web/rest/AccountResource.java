package logia.assistant.gateway.web.rest;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.service.AccountBusinessService;
import logia.assistant.gateway.service.UserService;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.web.rest.errors.EmailAlreadyUsedException;
import logia.assistant.gateway.web.rest.errors.EmailNotFoundException;
import logia.assistant.gateway.web.rest.errors.InternalServerErrorException;
import logia.assistant.gateway.web.rest.errors.InvalidPasswordException;
import logia.assistant.gateway.web.rest.errors.LoginAlreadyUsedException;
import logia.assistant.gateway.web.rest.vm.KeyAndPasswordVM;
import logia.assistant.gateway.web.rest.vm.ManagedUserVM;

/**
 * REST controller for managing the current user's account.
 *
 * @author Dai Mai
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    /** The log. */
    private final Logger                log = LoggerFactory.getLogger(AccountResource.class);
    
    /** The account business service. */
    private final AccountBusinessService accountBusinessService;

    /** The user service. */
    private final UserService           userService;

    /**
     * Instantiates a new account resource.
     *
     * @param accountBusinessService the account business service
     * @param userService the user service
     */
    public AccountResource(AccountBusinessService accountBusinessService, UserService userService) {
        super();
        this.accountBusinessService = accountBusinessService;
        this.userService = userService;
    }

    /**
     * POST /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already used
     */
    @PostMapping("/register")
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        log.info("REST request to register account {}", managedUserVM);
        this.accountBusinessService.registerUser(managedUserVM, managedUserVM.getPassword());
    }

    /**
     * GET /activate : activate the registered user.
     *
     * @param key the activation key
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public void activateAccount(@RequestParam(value = "key") String key) {
        log.info("REST request to activate account by key {}", key);
        Optional<Credential> credential = this.accountBusinessService.activateRegistration(key);
        if (!credential.isPresent()) {
            throw new InternalServerErrorException("No user was found for this reset key");
        }
    }

    /**
     * GET /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.info("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET /account : get the current user.
     *
     * @return the current user
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public UserDTO getAccount() {
        log.info("REST request to get information of current authorized user");
        return userService.getUserWithAuthorities().map(UserDTO::new)
                .orElseThrow(() -> new InternalServerErrorException("User could not be found"));
    }

    /**
     * PUT /account : update the current user information.
     *
     * @param userDTO the current user information
     * @throws RuntimeException 500 (Internal Server Error) if the user login wasn't found
     */
    @PutMapping("/account")
    @Timed
    public void saveAccount(@Valid @RequestBody UserDTO userDTO) {
        log.info("REST request to change information of current authorized account: {}", userDTO);
        this.accountBusinessService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getLangKey(),
                userDTO.getImageUrl());
    }

    /**
     * POST /account/change-password : changes the current user's password.
     *
     * @param password the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the new password is incorrect
     */
    @PostMapping(path = "/account/change-password")
    @Timed
    public void changePassword(@RequestBody String password) {
        log.info("REST request to change current authorized account password {}", password);
        this.accountBusinessService.changePassword(password);
    }

    /**
     * POST /account/reset-password/init : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user
     * @throws EmailNotFoundException 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset-password/init")
    @Timed
    public void requestPasswordReset(@RequestBody String mail) {
        log.info("REST request to reset password of email {}", mail);
        this.accountBusinessService.requestPasswordReset(mail).orElseThrow(EmailNotFoundException::new);
    }

    /**
     * POST /account/reset-password/finish : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws RuntimeException 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset-password/finish")
    @Timed
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        log.info("REST request to finish reset password by key: {}", keyAndPassword.getKey());
        Optional<User> optUser = this.accountBusinessService
                .completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!optUser.isPresent()) {
            throw new InternalServerErrorException("No user was found for this reset key");
        }
    }
}
