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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** The mail service. */
    private final MailService                mailService;

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

    /*
     * (non-Javadoc)
     * 
     * @see logia.assistant.share.common.service.EntityService#saveDto(java.lang.Object)
     */
    @Override
    public CredentialDTO saveDto(CredentialDTO credentialDTO) {
        log.debug("Request to save Credential : {}", credentialDTO);
        Credential credential = credentialMapper.toEntity(credentialDTO);
        credential = this.saveEntity(credential);
        CredentialDTO result = credentialMapper.toDto(credential);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see logia.assistant.share.common.service.EntityService#saveEntity(java.lang.Object)
     */
    @Override
    public Credential saveEntity(Credential credential) {
        Credential savedCredential = this.saveOrUpdate(credential, false);
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
        Credential credential;
        Optional<Credential> existingCredential = this.findOneWithUserByLogin(login);
        if (existingCredential.isPresent()
                && (!existingCredential.get().getUser().getId().equals(userId))) {
            // another user already have this credential
            throw new LoginAlreadyUsedException();
        }
        List<Credential> currentCredentials = this.findByUserId(userId);
        Optional<Credential> optCredential = currentCredentials.stream()
                .filter(currentCredential -> currentCredential.getLogin().equals(login))
                .findFirst();
        if (!optCredential.isPresent()) {
            credential = Credential.clone(currentCredentials.get(0));
            credential.primary(false).login(login);
            credential = this.saveOrUpdate(credential, true);
            this.cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                    .evict(currentCredentials.get(0).getLogin());

            // TODO send validation email
        }
        else {
            credential = optCredential.get();
        }
        return credential;
    }

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#findAllDtos()
     */
    @Override
    @Transactional(readOnly = true)
    public List<CredentialDTO> findAllDtos() {
        log.debug("Request to get all Credentials");
        return credentialRepository.findAll().stream().map(credentialMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#findAllEntities()
     */
    @Override
    public List<Credential> findAllEntities() {
        log.debug("Request to get all Credentials");
        return credentialRepository.findAll().stream().collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#findOneEntity(java.io.Serializable)
     */
    @Override
    @Transactional(readOnly = true)
    public CredentialDTO findOneDto(Long id) {
        log.debug("Request to get Credential : {}", id);
        Credential credential = credentialRepository.findOne(id);
        return credentialMapper.toDto(credential);
    }

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#delete(java.io.Serializable)
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
        return this.credentialRepository.findOneWithUserByLogin(login).map(credential -> {
            this.delete(credential.getId(), credential.getLogin());
            return credential;
        }).get();
    }

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#search(java.lang.String)
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
            this.saveOrUpdate(credential, false);
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
                    this.saveOrUpdate(credential, true);

                    // Send reset password email
                    mailService.sendPasswordResetMail(credential);
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
     * Find one with user by login.
     *
     * @param userLogin the user login
     * @return the optional
     */
    @Transactional(readOnly = true)
    public Optional<Credential> findOneWithUserByLogin(String userLogin) {
        Optional<Credential> credential = this.credentialRepository
                .findOneWithUserByLogin(userLogin);
        return credential;
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

    /**
     * Find all by login not.
     *
     * @param pageable the pageable
     * @param login the login
     * @return the page
     */
    public Page<Credential> findAllByLoginNot(Pageable pageable, String login) {
        return this.credentialRepository.findAllByLoginNot(pageable, login);
    }
    
    

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#findOneEntity(java.io.Serializable)
     */
    @Override
    public Credential findOneEntity(Long id) {
        log.debug("Request to get Credential : {}", id);
        return credentialRepository.findOne(id);
    }

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#mapToDto(java.lang.Object)
     */
    @Override
    public CredentialDTO mapToDto(Credential entity) {
        return this.credentialMapper.toDto(entity);
    }
    
    @Override
    public List<CredentialDTO> mapToDtos(List<Credential> entities) {
        return this.credentialMapper.toDto(entities);
    }

    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#mapToEntity(java.lang.Object)
     */
    @Override
    public Credential mapToEntity(CredentialDTO dto) {
        return this.credentialMapper.toEntity(dto);
    }
    
    /* (non-Javadoc)
     * @see logia.assistant.share.common.service.EntityService#mapToEntities(java.util.List)
     */
    @Override
    public List<Credential> mapToEntities(List<CredentialDTO> dtos) {
        return this.credentialMapper.toEntity(dtos);
    }

    /**
     * Save or update.
     *
     * @param credential the credential
     * @return the credential
     */
    private Credential saveOrUpdate(Credential credential, boolean persistent) {
        if (persistent) {
            credential = this.credentialRepository.saveAndFlush(credential);
        }
        else {
            credential = this.credentialRepository.save(credential);
        }
        this.credentialSearchRepository.save(credential);
        cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                .evict(credential.getLogin());
        return credential;
    }

}
