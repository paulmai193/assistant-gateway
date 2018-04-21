package logia.assistant.gateway.service.impl;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.config.Constants;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.security.SecurityUtils;
import logia.assistant.gateway.service.AccountBusinessService;
import logia.assistant.gateway.service.CredentialService;
import logia.assistant.gateway.service.MailService;
import logia.assistant.gateway.service.UserService;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.service.util.RandomUtil;
import logia.assistant.gateway.service.validator.ValidatorService;
import logia.assistant.gateway.web.rest.errors.BadRequestAlertException;
import logia.assistant.gateway.web.rest.errors.InternalServerErrorException;
import logia.assistant.gateway.web.rest.errors.LoginAlreadyUsedException;
import logia.assistant.share.gateway.securiry.jwt.AuthoritiesConstants;

/**
 * The Class AccountBusinessServiceImpl.
 *
 * @author Dai Mai
 */
@Service
@Transactional
public class AccountBusinessServiceImpl implements AccountBusinessService {

    /** The log. */
    private final Logger            log = LoggerFactory.getLogger(AccountBusinessServiceImpl.class);

    /** The credential service. */
    private final CredentialService credentialService;

    /** The mail service. */
    private final MailService       mailService;

    /** The password encoder. */
    private final PasswordEncoder   passwordEncoder;

    /** The user service. */
    private final UserService       userService;

    /** The validator service. */
    private final ValidatorService  validatorService;

    /**
     * Instantiates a new account business service impl.
     *
     * @param credentialService the credential service
     * @param mailService the mail service
     * @param passwordEncoder the password encoder
     * @param userService the user service
     * @param validatorService the validator service
     */
    public AccountBusinessServiceImpl(CredentialService credentialService, MailService mailService,
            PasswordEncoder passwordEncoder, UserService userService,
            ValidatorService validatorService) {
        super();
        this.credentialService = credentialService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.validatorService = validatorService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * logia.assistant.gateway.service.AccountBusinessService#activateRegistration(java.lang.String)
     */
    @Override
    public Optional<Credential> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return this.credentialService.findOneByActivationKey(key).map(credential -> {
            // activate given credential for the registration key.
            credential.activated(true).activationKey(null);
            this.credentialService.saveEntity(credential, false);
            log.debug("Activated user: {}", credential);
            return credential;
        });
    }

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.AccountBusinessService#changePassword(java.lang.String)
     */
    @Override
    public void changePassword(String password) {
        this.validatorService.validatePassword(password);
        SecurityUtils.getCurrentUserLogin().flatMap(this.credentialService::findOneWithUserByLogin)
                .ifPresent(credential -> {
                    credential.resetKey(null).resetDate(null);
                    this.credentialService.saveEntity(credential, false);
                    User user = credential.getUser();
                    user.setPassword(passwordEncoder.encode(password));
                    user = this.userService.saveOrUpdate(user, false);
                    log.debug("Changed password for User: {}", credential);
                });
    }

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.AccountBusinessService#completePasswordReset(java.lang.String, java.lang.String)
     */
    @Override
    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        this.validatorService.validatePassword(newPassword);
        return this.credentialService.findOneByResetKey(key).filter(
                credential -> credential.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
                .map(credential -> {
                    credential.resetKey(null).resetDate(null).activated(true);
                    this.credentialService.saveEntity(credential, false);
                    User user = credential.getUser();
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user = this.userService.saveOrUpdate(user, false);
                    log.debug("Complete reset password for User: {}", credential);
                    return user;
                });
    }

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.AccountBusinessService#createUser(logia.assistant.gateway.service.dto.UserDTO)
     */
    @Override
    public User createUser(UserDTO userDTO) {
        log.debug("Admin created Information for User: {}", userDTO);
        // Low case login if is email
        if (this.validatorService.isEmail(userDTO.getLogin())) {
            userDTO.setLogin(userDTO.getLogin());
        }

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an UUID",
                    "userManagement", "idexists");
        }
        else if (this.credentialService.findOneWithUserByLogin(userDTO.getLogin()).isPresent()) {
            throw new LoginAlreadyUsedException();
        }
        if (userDTO.getLangKey() == null) {
            userDTO.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        User user = new User().password(encryptedPassword);
        user = this.userService.updateOrCreateUser(user, userDTO, true);
        userDTO.setId(user.getUuid());

        // Create new credential
        Credential credential = new Credential().user(user).login(userDTO.getLogin())
                .activated(true).primary(true).resetKey(RandomUtil.generateResetKey())
                .resetDate(Instant.now());
        credential = this.credentialService.saveEntity(credential, false);

        // Send creation email
        try {
            String email = userDTO.getLogin();
            this.validatorService.validateEmail(email);
            mailService.sendCreationEmail(credential);
        }
        catch (Exception e) {
            log.debug(MessageFormat.format("Cannot send creation email to user {0}", userDTO), e);
        }

        return user;
    }
    
    @Override
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        log.debug("Admin change information of user {}", userDTO);
        Optional<User> optUser = this.userService.findByUuid(userDTO.getId());
        if (optUser.isPresent()) {
            this.credentialService.updateByUserId(optUser.get().getId(), userDTO.getLogin());
            return Optional.of(this.userService.findByUuid(userDTO.getId())).map(user -> {
                return this.userService.updateOrCreateUser(user.get(), userDTO, false);
            }).map(UserDTO::new);
        }
        else {
            return Optional.empty();
        }
    }

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.AccountBusinessService#deleteUser(java.lang.String)
     */
    @Override
    public void deleteUser(String uuid) {
        log.debug("Request to delete User UUID : {}", uuid);
        this.userService.findByUuid(uuid).ifPresent(deleteUser -> {
            List<Credential> credentials = this.credentialService.findByUserId(deleteUser.getId());
            credentials.forEach(credential -> this.credentialService.delete(credential.getId()));
            this.userService.delete(deleteUser);
        });
    }

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.AccountBusinessService#registerUser(logia.assistant.gateway.service.dto.UserDTO, java.lang.String)
     */
    @Override
    public User registerUser(UserDTO userDTO, String password) {
        // Low case login if is email
        if (this.validatorService.isEmail(userDTO.getLogin())) {
            userDTO.setLogin(userDTO.getLogin().toLowerCase());
        }

        log.debug("User create new account: {}", userDTO);
        // Validate register information
        this.validatorService.validateNewCredential(userDTO.getLogin(), password);

        // new user gets initially a generated password
        userDTO.authority(AuthoritiesConstants.USER);
        String encryptedPassword = passwordEncoder.encode(password);
        User newUser = new User().password(encryptedPassword);
        newUser = this.userService.updateOrCreateUser(newUser, userDTO, true);

        // Create new credential, new user is not active, new user gets registration key
        Credential credential = new Credential().user(newUser).login(userDTO.getLogin())
                .activated(false).primary(true).activationKey(RandomUtil.generateActivationKey());
        credential = this.credentialService.saveEntity(credential, false);

        // Send activation email
        try {
            String email = userDTO.getLogin();
            this.validatorService.validateEmail(email);
            mailService.sendActivationEmail(credential);
        }
        catch (Exception e) {
            log.debug(MessageFormat.format("Cannot send activation email to user {0}", userDTO), e);
        }

        return newUser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * logia.assistant.gateway.service.AccountBusinessService#requestPasswordReset(java.lang.String)
     */
    @Override
    public Optional<Credential> requestPasswordReset(String mail) {
        return this.credentialService.findOneByEmail(mail).filter(Credential::isActivated)
                .map(credential -> {
                    credential.resetKey(RandomUtil.generateResetKey()).resetDate(Instant.now());
                    this.credentialService.saveEntity(credential, true);

                    // Send reset password email
                    mailService.sendPasswordResetMail(credential);
                    return credential;
                });
    }    

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.AccountBusinessService#updateUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void updateUser(String firstName, String lastName, String langKey, String imageUrl) {
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(
                () -> new InternalServerErrorException("Current user login not found"));
        Optional<Credential> credential = this.credentialService.findOneWithUserByLogin(userLogin);
        if (!credential.isPresent()) {
            throw new InternalServerErrorException(
                    MessageFormat.format("User {0} could not be found", userLogin));
        }
        User user = credential.get().getUser();
        UserDTO userDTO = new UserDTO().firstName(firstName).lastName(lastName).langKey(langKey)
                .imageUrl(imageUrl);
        log.debug("User {} change information to {}", user.getId(), userDTO);
        this.userService.updateOrCreateUser(user, userDTO, false);
    }

}
