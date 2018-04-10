package logia.assistant.gateway.service;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import logia.assistant.gateway.config.Constants;
import logia.assistant.gateway.domain.Authority;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.AuthorityRepository;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.repository.search.UserSearchRepository;
import logia.assistant.gateway.security.SecurityUtils;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.service.impl.CredentialServiceImpl;
import logia.assistant.gateway.service.util.RandomUtil;
import logia.assistant.gateway.web.rest.errors.EmailAlreadyUsedException;
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
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    /** The user repository. */
    private final UserRepository userRepository;

    /** The password encoder. */
    private final PasswordEncoder passwordEncoder;

    /** The user search repository. */
    private final UserSearchRepository userSearchRepository;
    
    /** The credential service. */
    private final CredentialServiceImpl credentialService;

    /** The authority repository. */
    private final AuthorityRepository authorityRepository;

    /** The cache manager. */
    private final CacheManager cacheManager;

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
        User newUser = new User();
        Authority authority = authorityRepository.findOne(AuthoritiesConstants.USER);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        // new user gets initially a generated password
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
//        newUser.setEmail(userDTO.getEmail());
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        newUser.setLogin(userDTO.getLogin());
        newUser.setPassword(encryptedPassword);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        userSearchRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * Creates the user.
     *
     * @param userDTO the user DTO
     * @return the user
     */
    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
//        user.setEmail(userDTO.getEmail());
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities().stream()
                .map(authorityRepository::findOne)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setLogin(userDTO.getLogin());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        userRepository.save(user);
        userSearchRepository.save(user);
        log.debug("Created Information for User: {}", user);
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
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new InternalServerErrorException("Current user login not found"));
        Optional<Credential> credential = this.credentialService.findOneByLogin(userLogin);
        if (!credential.isPresent()) {
            throw new InternalServerErrorException(MessageFormat.format("User {0} could not be found", userLogin));
        }
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                if (Objects.nonNull(firstName)) {
                    user.setFirstName(firstName);    
                }
                if (Objects.nonNull(lastName)) {
                    user.setLastName(lastName);    
                }
                if (Objects.nonNull(langKey)) {
                    user.setLangKey(langKey);    
                }
                if (Objects.nonNull(imageUrl)) {
                    user.setImageUrl(imageUrl);    
                }                
                userSearchRepository.save(user);
                log.debug("Changed Information for User: {}", user);
            });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findOne(userDTO.getId()))
            .map(user -> {
                if (Objects.nonNull(userDTO.getFirstName())) {
                    user.setFirstName(userDTO.getFirstName());    
                }
                if (Objects.nonNull(userDTO.getLastName())) {
                    user.setLastName(userDTO.getLastName());    
                }
                if (Objects.nonNull(userDTO.getImageUrl())) {
                    user.setImageUrl(userDTO.getImageUrl());    
                }
                if (Objects.nonNull(userDTO.getLangKey())) {
                    user.setLangKey(userDTO.getLangKey());    
                }
                if (Objects.nonNull(userDTO.getAuthorities()) && !userDTO.getAuthorities().isEmpty()) {
                    Set<Authority> managedAuthorities = user.getAuthorities();
                    managedAuthorities.clear();
                    userDTO.getAuthorities().stream()
                        .map(authorityRepository::findOne)
                        .forEach(managedAuthorities::add);    
                }                
                userSearchRepository.save(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    /**
     * Delete user.
     *
     * @param login the login
     */
    public void deleteUser(String login) {
        log.debug("Request to delete User login : {}", login);
        Credential credential = this.credentialService.delete(login);;
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
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    /**
     * Gets the user with authorities by login.
     *
     * @param login the login
     * @return the user with authorities by login
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
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
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
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
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
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
