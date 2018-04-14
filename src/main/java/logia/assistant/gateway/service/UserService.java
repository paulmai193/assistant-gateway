package logia.assistant.gateway.service;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
import logia.assistant.gateway.web.rest.errors.InternalServerErrorException;
import logia.assistant.share.gateway.securiry.jwt.AuthoritiesConstants;

/**
 * Service class for managing users.
 *
 * @author Dai Mai
 */
@Service
@Transactional
public class UserService {

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
    @SuppressWarnings("unused")
    private final CacheManager          cacheManager;

    @Inject
    private ValidatorService            validatorService;

    /**
     * Instantiates a new user service.
     *
     * @param userRepository the user repository
     * @param passwordEncoder the password encoder
     * @param userSearchRepository the user search repository
     * @param credentialService the credential service
     * @param authorityRepository the authority repository
     * @param cacheManager the cache manager
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            UserSearchRepository userSearchRepository, CredentialServiceImpl credentialService,
            AuthorityRepository authorityRepository, CacheManager cacheManager) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSearchRepository = userSearchRepository;
        this.credentialService = credentialService;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Register user.
     *
     * @param userDTO the user DTO
     * @param password the password
     * @return the user
     */
    public User registerUser(UserDTO userDTO, String password) {
        log.debug("User create new account: {}", userDTO);
        // Validate register information
        this.validatorService.validateNewCredential(userDTO.getLogin(), password);
        // userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {throw
        // new EmailAlreadyUsedException();});

        // new user gets initially a generated password
        userDTO.authority(AuthoritiesConstants.USER);
        User newUser = this.updateOrCreateUser(null, userDTO);

        // Create new credential, new user is not active, new user gets registration key
        String encryptedPassword = passwordEncoder.encode(password);
        this.credentialService
                .save(new CredentialDTO().login(userDTO.getLogin()).passwordHash(encryptedPassword)
                        .activated(false).activationKey(RandomUtil.generateActivationKey()));

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
        if (userDTO.getLangKey() == null) {
            userDTO.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        }
        User user = this.updateOrCreateUser(null, userDTO);

        // Create new credential
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        this.credentialService.save(new CredentialDTO().login(userDTO.getLogin())
                .passwordHash(encryptedPassword).activated(true)
                .resetKey(RandomUtil.generateResetKey()).resetDate(Instant.now()));

        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
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
        this.updateOrCreateUser(user, userDTO);
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        log.debug("Admin change information of user {}", userDTO);
        return Optional.of(userRepository.findOne(userDTO.getId())).map(user -> {
            return this.updateOrCreateUser(user, userDTO);
        }).map(UserDTO::new);
    }

    /**
     * Update or create user.
     *
     * @param user the user. <code><strong>NULL</strong></code> if create new user
     * @param updateInformation the update information
     * @return the user
     */
    private User updateOrCreateUser(User user, UserDTO updateInformation) {
        if (Objects.isNull(user)) {
            user = new User();
        }
        if (Objects.nonNull(updateInformation.getFirstName())) {
            user.setFirstName(updateInformation.getFirstName());
        }
        if (Objects.nonNull(updateInformation.getLastName())) {
            user.setLastName(updateInformation.getLastName());
        }
        // if (Objects.nonNull(updateInformation.getEmail())) {
        // user.setEmail(updateInformation.setEmail());
        // }
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
            user = this.userRepository.save(user);
        }
        userSearchRepository.save(user);
        log.debug("Create user or change information for User: {}", user);
        return user;
    }

    /**
     * Delete user.
     *
     * @param login the login
     */
    public void deleteUser(String login) {
        log.debug("Request to delete User login : {}", login);
        Credential credential = this.credentialService.delete(login);
        ;
        this.delete(credential.getUser().getId());
    }

    /**
     * Gets the all managed users.
     *
     * @param pageable the pageable
     * @return the all managed users
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER)
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
            log.debug("Deleting user {} not have credential", user.getId());
            this.delete(user.getId());
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
     * @param userId the user id
     */
    private void delete(Long userId) {
        userRepository.delete(userId);
        userSearchRepository.delete(userId);
        log.debug("Deleted User: {}", userId);
    }

}
