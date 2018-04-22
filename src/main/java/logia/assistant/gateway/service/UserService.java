package logia.assistant.gateway.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.config.Constants;
import logia.assistant.gateway.domain.Authority;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.AuthorityRepository;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.repository.search.UserSearchRepository;
import logia.assistant.gateway.security.SecurityUtils;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.service.impl.CredentialServiceImpl;
import logia.assistant.share.common.service.UuidService;

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

    /** The user search repository. */
    private final UserSearchRepository  userSearchRepository;

    /** The credential service. */
    private final CredentialServiceImpl credentialService;

    /** The authority repository. */
    private final AuthorityRepository   authorityRepository;

    /** The cache manager. */
    private final CacheManager          cacheManager;

    /**
     * Instantiates a new user service.
     *
     * @param userRepository the user repository
     * @param userSearchRepository the user search repository
     * @param credentialService the credential service
     * @param authorityRepository the authority repository
     * @param cacheManager the cache manager
     */
    public UserService(UserRepository userRepository, UserSearchRepository userSearchRepository,
            CredentialServiceImpl credentialService, AuthorityRepository authorityRepository,
            CacheManager cacheManager) {
        super();
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.credentialService = credentialService;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see logia.assistant.share.common.service.UuidService#getByUuid(java.lang.String)
     */
    @Override
    public Optional<User> findByUuid(String uuid) {
        Optional<User> optUser = this.userSearchRepository.findOneByUuid(uuid);
        if (optUser.isPresent()) {
            return optUser;
        }
        else {
            // Maybe elastic search not persistent, try finding in DB
            optUser = this.userRepository.findOneWithAuthoritiesByUuid(uuid);
            if (optUser.isPresent()) {
                this.userSearchRepository.save(optUser.get());
            }
            return optUser;
        }
    }

    /**
     * Update or create user.
     *
     * @param user the user.
     * @param updateInformation the update information
     * @param persistent the persistent
     * @return the user
     */
    public User updateOrCreateUser(User user, UserDTO updateInformation, boolean persistent) {
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
        return this.saveOrUpdate(user, persistent);
    }

    /**
     * Save or update.
     *
     * @param user the user
     * @param force the force
     * @return the user
     */
    public User saveOrUpdate(User user, boolean force) {
        if (force) {
            user = this.userRepository.saveAndFlush(user);
        }
        else {
            user = this.userRepository.save(user);
        }
        userSearchRepository.save(user);
        this.cacheManager.getCache(UserRepository.USERS_BY_UUID_CACHE).evict(user.getUuid());
        log.debug("Create user or change information for User: {}", user);
        return user;
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
        return this.credentialService.findOneWithUserByLogin(login).flatMap(credential -> {
            return Optional.ofNullable(credential.getUser());
        });
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
        return SecurityUtils.getCurrentUserLogin()
                .flatMap(login -> this.credentialService.findOneWithUserByLogin(login)
                        .flatMap(credential -> Optional.of(credential.getUser())));
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
    public void delete(User user) {
        userRepository.delete(user.getId());
        userSearchRepository.delete(user.getId());
        this.cacheManager.getCache(UserRepository.USERS_BY_UUID_CACHE).evict(user.getUuid());
        log.debug("Deleted User: {}", user);
    }

}
