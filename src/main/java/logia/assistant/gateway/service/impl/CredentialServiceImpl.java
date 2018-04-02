package logia.assistant.gateway.service.impl;

import logia.assistant.gateway.service.CredentialService;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.repository.search.CredentialSearchRepository;
import logia.assistant.gateway.service.dto.CredentialDTO;
import logia.assistant.gateway.service.mapper.CredentialMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Credential.
 */
@Service
@Transactional
public class CredentialServiceImpl implements CredentialService {

    private final Logger log = LoggerFactory.getLogger(CredentialServiceImpl.class);

    private final CredentialRepository credentialRepository;

    private final CredentialMapper credentialMapper;

    private final CredentialSearchRepository credentialSearchRepository;

    public CredentialServiceImpl(CredentialRepository credentialRepository, CredentialMapper credentialMapper, CredentialSearchRepository credentialSearchRepository) {
        this.credentialRepository = credentialRepository;
        this.credentialMapper = credentialMapper;
        this.credentialSearchRepository = credentialSearchRepository;
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
}
