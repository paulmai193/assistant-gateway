package logia.assistant.gateway.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.repository.search.CredentialSearchRepository;
import logia.assistant.gateway.service.CredentialService;
import logia.assistant.gateway.service.MailService;
import logia.assistant.gateway.service.dto.CredentialDTO;
import logia.assistant.gateway.service.mapper.CredentialMapper;
import logia.assistant.gateway.service.util.RandomUtil;
import logia.assistant.gateway.service.validator.ValidatorService;
import logia.assistant.gateway.web.rest.errors.LoginAlreadyUsedException;

/**
 * Service Implementation for managing Credential.
 *
 * @author Dai Mai
 */
@Service
@Transactional
public class CredentialServiceImpl implements CredentialService {

    /** The log. */
    private final Logger                     log = LoggerFactory
            .getLogger(CredentialServiceImpl.class);

    /** The credential repository. */
    private final CredentialRepository       credentialRepository;

    /** The credential mapper. */
    private final CredentialMapper           credentialMapper;

    /** The credential search repository. */
    private final CredentialSearchRepository credentialSearchRepository;

    /** The cache manager. */
    private final CacheManager               cacheManager;

    /** The validator service. */
    private final ValidatorService           validatorService;

    private final MailService mailService;

    /**
     * Instantiates a new credential service impl.
     *
     * @param credentialRepository the credential repository
     * @param credentialMapper the credential mapper
     * @param credentialSearchRepository the credential search repository
     * @param cacheManager the cache manager
     * @param validatorService the validator service
     * @param mailService the mail service
     */
    public CredentialServiceImpl(CredentialRepository credentialRepository,
            CredentialMapper credentialMapper,
            CredentialSearchRepository credentialSearchRepository, CacheManager cacheManager,
            ValidatorService validatorService, MailService mailService) {
        super();
        this.credentialRepository = credentialRepository;
        this.credentialMapper = credentialMapper;
        this.credentialSearchRepository = credentialSearchRepository;
        this.cacheManager = cacheManager;
        this.validatorService = validatorService;
        this.mailService = mailService;
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
        credential = this.save(credential);
        CredentialDTO result = credentialMapper.toDto(credential);
        return result;
    }

    /**
     * Save.
     *
     * @param credential the credential
     * @return the credential
     */
    public Credential save(Credential credential) {
        Credential savedCredential = this.credentialRepository.save(credential);
        credentialSearchRepository.save(savedCredential);
        cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                .evict(savedCredential.getLogin());
        return savedCredential;
    }
    
    /**
     * Update by user id.
     *
     * @param userId the user id
     * @param login the login
     * @return the credential
     */
    public Credential updateByUserId(Long userId, String login) {
        List<Credential> currentCredentials = this.findByUserId(userId);
        if (currentCredentials.stream().anyMatch(currentCredential -> currentCredential.getLogin().equals(login))) {
            throw new LoginAlreadyUsedException();
        }
        Credential newCredential = Credential.clone(currentCredentials.get(0));
        newCredential.primary(false).login(login);
        newCredential = this.save(newCredential);
        
        // TODO send validation email
        
        return newCredential;
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
        return credentialRepository.findAll().stream().map(credentialMapper::toDto)
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
        log.debug("Request to delete Credential ID : {}", id);
        this.delete(id, null);
    }

    /**
     * Delete.
     *
     * @param login the login
     * @return the credential
     */
    public Credential delete(String login) {
        log.debug("Request to delete Credential login : {}", login);
        return this.credentialRepository.findOneByLogin(login).map(credential -> {
            this.delete(credential.getId(), credential.getLogin());
            return credential;
        }).get();
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
                .stream(credentialSearchRepository.search(queryStringQuery(query)).spliterator(),
                        false)
                .map(credentialMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Activate registration.
     *
     * @param key the key
     * @return the optional
     */
    public Optional<Credential> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return this.credentialRepository.findOneByActivationKey(key).map(credential -> {
            // activate given credential for the registration key.
            credential.activated(true).activationKey(null);
            this.credentialSearchRepository.save(credential);
            cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                    .evict(credential.getLogin());
            log.debug("Activated user: {}", credential);
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
                .filter(Credential::isActivated).map(credential -> {
                    credential.resetKey(RandomUtil.generateResetKey()).resetDate(Instant.now());
                    this.credentialSearchRepository.save(credential);
                    cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                            .evict(credential.getLogin());
                    
                    // Send reset password email
                    mailService.sendPasswordResetMail(credential.getUser(), mail);   
                    return credential;
                });
    }

    /**
     * Find one by reset key.
     *
     * @param resetKey the reset key
     * @return the optional
     */
    @Transactional(readOnly = true)
    public Optional<Credential> findOneByResetKey(String resetKey) {
        return this.credentialRepository.findOneByResetKey(resetKey);
    }

    /**
     * Not activated credential should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        this.credentialRepository.findAllByActivatedIsFalseAndCreatedDateBefore(
                Instant.now().minus(3, ChronoUnit.DAYS)).forEach(credential -> {
                    log.debug("Deleting not activated user {}", credential.getLogin());
                    this.delete(credential.getId(), credential.getLogin());
                });
    }

    /**
     * Delete.
     *
     * @param credentialId the credential id
     * @param login the login
     */
    private void delete(Long credentialId, String login) {
        this.credentialRepository.delete(credentialId);
        this.credentialSearchRepository.delete(credentialId);
        if (Objects.nonNull(login)) {
            cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE).evict(login);
        }
        log.debug("Deleted Credential: {}", credentialId);
    }

    /**
     * Find one by login.
     *
     * @param userLogin the user login
     * @return the optional
     */
    @Transactional(readOnly = true)
    public Optional<Credential> findOneByLogin(String userLogin) {
        return this.credentialRepository.findOneByLogin(userLogin);
    }
    
    /**
     * Find by user id.
     *
     * @param userId the user id
     * @return the list
     */
    @Transactional(readOnly = true)
    public List<Credential> findByUserId(Long userId) {
        return this.credentialRepository.findWithUserByUserId(userId);
    }

}
