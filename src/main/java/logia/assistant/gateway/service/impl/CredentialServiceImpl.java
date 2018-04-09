package logia.assistant.gateway.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.repository.search.CredentialSearchRepository;
import logia.assistant.gateway.security.SecurityUtils;
import logia.assistant.gateway.service.CredentialService;
import logia.assistant.gateway.service.dto.CredentialDTO;
import logia.assistant.gateway.service.mapper.CredentialMapper;
import logia.assistant.gateway.service.util.RandomUtil;
import logia.assistant.gateway.service.validator.ValidatorService;

/**
 * Service Implementation for managing Credential.
 *
 * @author Dai Mai
 */
@Service
@Transactional
public class CredentialServiceImpl implements CredentialService {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(CredentialServiceImpl.class);

    /** The credential repository. */
    private final CredentialRepository credentialRepository;

    /** The credential mapper. */
    private final CredentialMapper credentialMapper;

    /** The credential search repository. */
    private final CredentialSearchRepository credentialSearchRepository;
    
    /** The cache manager. */
    private final CacheManager cacheManager;
    
    /** The password encoder. */
    private final PasswordEncoder passwordEncoder;
    
    /** The validator service. */
    private final ValidatorService validatorService; 

    /**
     * Instantiates a new credential service impl.
     *
     * @param credentialRepository the credential repository
     * @param credentialMapper the credential mapper
     * @param credentialSearchRepository the credential search repository
     * @param cacheManager the cache manager
     * @param passwordEncoder the password encoder
     * @param validatorService the validator service
     */
    public CredentialServiceImpl(CredentialRepository credentialRepository,
            CredentialMapper credentialMapper,
            CredentialSearchRepository credentialSearchRepository, CacheManager cacheManager,
            PasswordEncoder passwordEncoder, ValidatorService validatorService) {
        super();
        this.credentialRepository = credentialRepository;
        this.credentialMapper = credentialMapper;
        this.credentialSearchRepository = credentialSearchRepository;
        this.cacheManager = cacheManager;
        this.passwordEncoder = passwordEncoder;
        this.validatorService = validatorService;
    }

    /**
     * Save a credential.
     *
     * @param credentialDTO the entity to save
     * @return the persisted entity
     */
    @Override
    public CredentialDTO save(CredentialDTO credentialDTO) {
        log.debug("Request to save Credential : {}", credentialDTO);
        Credential credential = credentialMapper.toEntity(credentialDTO);
        credential = credentialRepository.save(credential);
        CredentialDTO result = credentialMapper.toDto(credential);
        credentialSearchRepository.save(credential);
        return result;
    }

    /**
     * Get all the credentials.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<CredentialDTO> findAll() {
        log.debug("Request to get all Credentials");
        return credentialRepository.findAll().stream()
            .map(credentialMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one credential by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public CredentialDTO findOne(Long id) {
        log.debug("Request to get Credential : {}", id);
        Credential credential = credentialRepository.findOne(id);
        return credentialMapper.toDto(credential);
    }

    /**
     * Delete the credential by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Credential : {}", id);
        credentialRepository.delete(id);
        credentialSearchRepository.delete(id);
    }

    /**
     * Search for the credential corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<CredentialDTO> search(String query) {
        log.debug("Request to search Credentials for query {}", query);
        return StreamSupport
            .stream(credentialSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .map(credentialMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Activate registration.
     *
     * @param key the key
     * @return the optional
     */
    public Optional<Credential> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return this.credentialRepository.findOneByActivationKey(key)
            .map(credential -> {
                // activate given credential for the registration key.
                credential.setActivated(true);
                credential.setActivationKey(null);
                this.credentialSearchRepository.save(credential);
                cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE).evict(credential.getLogin());
                log.debug("Activated user: {}", credential);
                return credential;
            });
    }

    /**
     * Complete password reset.
     *
     * @param newPassword the new password
     * @param key the key
     * @return the optional
     */
    public Optional<Credential> completePasswordReset(String newPassword, String key) {
       log.debug("Reset user password for reset key {}", key);

       return this.credentialRepository.findOneByResetKey(key)
           .filter(credential -> credential.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
           .map(credential -> {
                credential.passwordHash(passwordEncoder.encode(newPassword)).resetKey(null).resetDate(null);
                cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE).evict(credential.getLogin());
                return credential;
           });
    }

    /**
     * Request password reset.
     *
     * @param mail the mail
     * @return the optional
     */
    public Optional<Credential> requestPasswordReset(String mail) {
        this.validatorService.validateEmail(mail);
        return this.credentialRepository.findOneByLoginIgnoreCase(mail)
            .filter(Credential::isActivated)
            .map(credential -> {
                credential.setResetKey(RandomUtil.generateResetKey());
                credential.setResetDate(Instant.now());
                cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE).evict(credential.getLogin());
                return credential;
            });
    }
    
    /**
     * Change password.
     *
     * @param password the password
     */
    public void changePassword(String password) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(this.credentialRepository::findOneWithUserBylogin)
            .ifPresent(credential -> {
                String encryptedPassword = passwordEncoder.encode(password);
                credential.setPasswordHash(encryptedPassword);
                cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE).evict(credential.getLogin());
                log.debug("Changed password for User: {}", credential);
            });
    }

    /**
     * Find one by login.
     *
     * @param userLogin the user login
     * @return the optional
     */
    public Optional<Credential> findOneByLogin(String userLogin) {
        return this.credentialRepository.findOneByLogin(userLogin);
    }
    
    /**
     * Not activated credential should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        List<Credential> users = this.credentialRepository.findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
            userSearchRepository.delete(user);
            cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).evict(user.getLogin());
            cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE).evict(user.getEmail());
        }
    }
}
