package logia.assistant.gateway.service;

import logia.assistant.gateway.service.dto.CredentialDTO;
import java.util.List;

/**
 * Service Interface for managing Credential.
 *
 * @author Dai Mai
 */
public interface CredentialService {

    /**
     * Save a credential.
     *
     * @param credentialDTO the entity to save
     * @return the persisted entity
     */
    CredentialDTO save(CredentialDTO credentialDTO);

    /**
     * Get all the credentials.
     *
     * @return the list of entities
     */
    List<CredentialDTO> findAll();

    /**
     * Get the "id" credential.
     *
     * @param id the id of the entity
     * @return the entity
     */
    CredentialDTO findOne(Long id);

    /**
     * Delete the "id" credential.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the credential corresponding to the query.
     *
     * @param query the query of the search
     * 
     * @return the list of entities
     */
    List<CredentialDTO> search(String query);
}
