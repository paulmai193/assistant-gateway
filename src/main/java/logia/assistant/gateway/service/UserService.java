package logia.assistant.gateway.service;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.config.Constants;
import logia.assistant.gateway.domain.Authority;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.AuthorityRepository;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.repository.search.UserSearchRepository;
import logia.assistant.gateway.security.SecurityUtils;
import logia.assistant.gateway.service.dto.CredentialDTO;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.service.impl.CredentialServiceImpl;
import logia.assistant.gateway.service.util.RandomUtil;
import logia.assistant.gateway.service.validator.ValidatorService;
import logia.assistant.gateway.web.rest.errors.BadRequestAlertException;
import logia.assistant.gateway.web.rest.errors.InternalServerErrorException;
import logia.assistant.gateway.web.rest.errors.LoginAlreadyUsedException;
import logia.assistant.share.common.service.UuidService;
import logia.assistant.share.gateway.securiry.jwt.AuthoritiesConstants;

/**
 * Service class for managing users.
 *
 * @author Dai Mai
 */
@Service
@Transactional
public class UserService implements UuidService<User> {

    /** The log. */
    private final Logger                log = LoggerFactory.getLogger(UserService.class);

    /** The user repository. */
    private final UserRepository        userRepository;

    /** The password encoder. */
    private final PasswordEncoder       passwordEncoder;

    /** The user search repository. */
    private final UserSearchRepository  userSearchRepository;

    /** The credential service. */
    private final CredentialServiceImpl credentialService;

    /** The authority repository. */
    private final AuthorityRepository   authorityRepository;

    /** The cache manager. */
    private final CacheManager          cacheManager;

    /** The validator service. */
    private final ValidatorService      validatorService;

    /** The mail service. */
    private final MailService           mailService;

    /**
     * Instantiates a new user service.
     *
     * @param userRepository the user repository
     * @param passwordEncoder the password encoder
     * @param userSearchRepository the user search repository
     * @param credentialService the credential service
     * @param authorityRepository the authority repository
     * @param cacheManager the cache manager
     * @param validatorService the validator service
     * @param mailService the mail service
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            UserSearchRepository userSearchRepository, CredentialServiceImpl credentialService,
            AuthorityRepository authorityRepository, CacheManager cacheManager,
            ValidatorService validatorService, MailService mailService) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSearchRepository = userSearchRepository;
        this.credentialService = credentialService;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
        this.validatorService = validatorService;
        this.mailService = mailService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see logia.assistant.share.common.service.UuidService#getByUuid(java.lang.String)
     */
    @Override
    public Optional<User> findByUuid(String uuid) {
        List<User> results = StreamSupport.stream(
                this.userSearchRepository.search(queryStringQuery("uuid=" + uuid)).spliterator(),
                false).collect(Collectors.toList());
        if (results.size() > 1) {
            throw new InternalServerErrorException(
                    MessageFormat.format("UUID {0} not unique", uuid));
        }
        if (results.size() == 1) {
            return Optional.of(results.get(0));
        }
        else {
            // Maybe elastic search not persistent, try finding in DB
            Optional<User> optUser = this.userRepository.findOneByUuid(uuid);
            if (optUser.isPresent()) {
                this.userSearchRepository.save(optUser.get());
            }
            return optUser;
        }
    }

    /**
     * Register user.
     *
     * @param userDTO the user DTO
     * @param password the password
     * @return the user
     */
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
        newUser = this.updateOrCreateUser(newUser, userDTO, true);

        // Create new credential, new user is not active, new user gets registration key
        this.credentialService.save(new CredentialDTO().login(userDTO.getLogin()).activated(false)
                .activationKey(RandomUtil.generateActivationKey()));

        // Send activation email
        try {
            String email = userDTO.getLogin();
            this.validatorService.validateEmail(email);
            mailService.sendActivationEmail(newUser, email);
        }
        catch (Exception e) {
            log.debug(MessageFormat.format("Cannot send activation email to user {0}", userDTO), e);
        }

        return newUser;
    }

    /**
     * Creates the user.
     *
     * @param userDTO the user DTO
     * @return the user
     */
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
        else if (this.credentialService.findOneByLogin(userDTO.getLogin()).isPresent()) {
            throw new LoginAlreadyUsedException();
        }
        if (userDTO.getLangKey() == null) {
            userDTO.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        User user = new User().password(encryptedPassword);
        user = this.updateOrCreateUser(user, userDTO, true);
        userDTO.setId(user.getUuid());

        // Create new credential
        this.credentialService.save(new CredentialDTO().userId(user.getId()).login(userDTO.getLogin()).activated(true)
                .primary(true).resetKey(RandomUtil.generateResetKey()).resetDate(Instant.now()));

        // Send creation email
        try {
            String email = userDTO.getLogin();
            this.validatorService.validateEmail(email);
            mailService.sendCreationEmail(user, email);
        }
        catch (Exception e) {
            log.debug(MessageFormat.format("Cannot send creation email to user {0}", userDTO), e);
        }

        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param langKey language key
     * @param imageUrl image URL of user
     */
    public void updateUser(String firstName, String lastName, String langKey, String imageUrl) {
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(
                () -> new InternalServerErrorException("Current user login not found"));
        Optional<Credential> credential = this.credentialService.findOneByLogin(userLogin);
        if (!credential.isPresent()) {
            throw new InternalServerErrorException(
                    MessageFormat.format("User {0} could not be found", userLogin));
        }
        User user = credential.get().getUser();
        UserDTO userDTO = new UserDTO().firstName(firstName).lastName(lastName).langKey(langKey)
                .imageUrl(imageUrl);
        log.debug("User {} change information to {}", user.getId(), userDTO);
        this.updateOrCreateUser(user, userDTO, false);
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        log.debug("Admin change information of user {}", userDTO);
        Optional<User> optUser = this.userRepository.findOneByUuid(userDTO.getId());
        if (optUser.isPresent()) {
            this.credentialService.updateByUserId(optUser.get().getId(), userDTO.getLogin());
            return Optional.of(this.userRepository.findOneByUuid(userDTO.getId())).map(user -> {
                return this.updateOrCreateUser(user.get(), userDTO, false);
            }).map(UserDTO::new);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Update or create user.
     *
     * @param user the user.
     * @param updateInformation the update information
     * @return the user
     */
    private User updateOrCreateUser(User user, UserDTO updateInformation, boolean persistent) {
        if (Objects.nonNull(updateInformation.getFirstName())) {
            user.setFirstName(updateInformation.getFirstName());
        }
        if (Objects.nonNull(updateInformation.getLastName())) {
            user.setLastName(updateInformation.getLastName());
        }
        if (Objects.nonNull(updateInformation.getImageUrl())) {
            user.setImageUrl(updateInformation.getImageUrl());
        }
        if (Objects.nonNull(updateInformation.getLangKey())) {
            user.setLangKey(updateInformation.getLangKey());
        }
        if (Objects.nonNull(updateInformation.getAuthorities())
                && !updateInformation.getAuthorities().isEmpty()) {
            Set<Authority> managedAuthorities = user.getAuthorities();
            managedAuthorities.clear();
            updateInformation.getAuthorities().stream().map(authorityRepository::findOne)
                    .forEach(managedAuthorities::add);
        }
        if (Objects.isNull(user.getId())) {
            if (persistent) {
                user = this.userRepository.saveAndFlush(user);
            } else {
                user = this.userRepository.save(user);
            }
        }
        userSearchRepository.save(user);
        this.cacheManager.getCache(UserRepository.USERS_BY_UUID_CACHE).evict(user.getUuid());
        this.cacheManager.getCache(UserRepository.USERS_BY_FIRST_NAME_CACHE)
                .evict(user.getFirstName());
        this.cacheManager.getCache(UserRepository.USERS_BY_LAST_NAME_CACHE)
                .evict(user.getLastName());
        log.debug("Create user or change information for User: {}", user);
        return user;
    }

    /**
     * Delete user.
     *
     * @param uuid the id
     */
    public void deleteUser(String uuid) {
        log.debug("Request to delete User UUID : {}", uuid);
        this.findByUuid(uuid).ifPresent(deleteUser -> {
            List<Credential> credentials = this.credentialService.findByUserId(deleteUser.getId());
            credentials.forEach(credential -> this.credentialService.delete(credential.getId()));
            this.delete(deleteUser);
        });
    }

    /**
     * Gets the all managed users.
     *
     * @param pageable the pageable
     * @return the all managed users
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return this.credentialService.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER)
                .map(UserDTO::new);
    }

    /**
     * Gets the user with authorities by login.
     *
     * @param login the login
     * @return the user with authorities by login
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return this.credentialService.findOneByLogin(login)
                .flatMap(credential -> Optional.of(credential.getUser()));
    }

    /**
     * Gets the user with authorities.
     *
     * @param id the id
     * @return the user with authorities
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(Long id) {
        return userRepository.findOneWithAuthoritiesById(id);
    }

    /**
     * Gets the user with authorities.
     *
     * @return the user with authorities
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(login -> this.credentialService
                .findOneByLogin(login).flatMap(credential -> Optional.of(credential.getUser())));
    }

    /**
     * Non credential users should be automatically deleted.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNonCredentialUsers() {
        List<User> users = userRepository.findAllNotHaveCredential();
        for (User user : users) {
            log.debug("Deleting user {} not have credential", user);
            this.delete(user);
        }
    }

    /**
     * Gets the authorities.
     *
     * @return a list of all the authorities
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName)
                .collect(Collectors.toList());
    }

    /**
     * Delete.
     *
     * @param user the user
     */
    private void delete(User user) {
        userRepository.delete(user.getId());
        userSearchRepository.delete(user.getId());
        this.cacheManager.getCache(UserRepository.USERS_BY_UUID_CACHE).evict(user.getUuid());
        this.cacheManager.getCache(UserRepository.USERS_BY_FIRST_NAME_CACHE)
                .evict(user.getFirstName());
        this.cacheManager.getCache(UserRepository.USERS_BY_LAST_NAME_CACHE)
                .evict(user.getLastName());
        log.debug("Deleted User: {}", user);
    }

    /**
     * Complete password reset.
     *
     * @param newPassword the new password
     * @param key the key
     * @return the optional
     */
    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        this.validatorService.validatePassword(newPassword);
        return this.credentialService.findOneByResetKey(key).filter(
                credential -> credential.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
                .map(credential -> {
                    credential.resetKey(null).resetDate(null).activated(true);
                    this.credentialService.save(credential);
                    User user = credential.getUser();
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user = this.userSearchRepository.save(user);
                    log.debug("Complete reset password for User: {}", credential);
                    return user;
                });
    }

    /**
     * Change password.
     *
     * @param password the password
     */
    public void changePassword(String password) {
        this.validatorService.validatePassword(password);
        SecurityUtils.getCurrentUserLogin().flatMap(this.credentialService::findOneByLogin)
                .ifPresent(credential -> {
                    credential.resetKey(null).resetDate(null);
                    this.credentialService.save(credential);
                    User user = credential.getUser();
                    user.setPassword(passwordEncoder.encode(passwordEncoder.encode(password)));
                    user = this.userSearchRepository.save(user);
                    log.debug("Changed password for User: {}", credential);
                });
    }

}
