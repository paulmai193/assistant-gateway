package logia.assistant.gateway.web.rest;

import com.codahale.metrics.annotation.Timed;
import logia.assistant.gateway.service.CredentialService;
import logia.assistant.gateway.web.rest.errors.BadRequestAlertException;
import logia.assistant.gateway.web.rest.util.HeaderUtil;
import logia.assistant.gateway.service.dto.CredentialDTO;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Credential.
 *
 * @author Dai Mai
 */
@RestController
@RequestMapping("/api")
public class CredentialResource {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(CredentialResource.class);

    /** The Constant ENTITY_NAME. */
    private static final String ENTITY_NAME = "credential";

    /** The credential service. */
    private final CredentialService credentialService;

    /**
     * Instantiates a new credential resource.
     *
     * @param credentialService the credential service
     */
    public CredentialResource(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    /**
     * POST  /credentials : Create a new credential.
     *
     * @param credentialDTO the credentialDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new credentialDTO, or with status 400 (Bad Request) if the credential has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/credentials")
    @Timed
    public ResponseEntity<CredentialDTO> createCredential(@Valid @RequestBody CredentialDTO credentialDTO) throws URISyntaxException {
        log.debug("REST request to save Credential : {}", credentialDTO);
        if (credentialDTO.getId() != null) {
            throw new BadRequestAlertException("A new credential cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CredentialDTO result = credentialService.save(credentialDTO);
        return ResponseEntity.created(new URI("/api/credentials/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /credentials : Updates an existing credential.
     *
     * @param credentialDTO the credentialDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated credentialDTO,
     * or with status 400 (Bad Request) if the credentialDTO is not valid,
     * or with status 500 (Internal Server Error) if the credentialDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/credentials")
    @Timed
    public ResponseEntity<CredentialDTO> updateCredential(@Valid @RequestBody CredentialDTO credentialDTO) throws URISyntaxException {
        log.debug("REST request to update Credential : {}", credentialDTO);
        if (credentialDTO.getId() == null) {
            return createCredential(credentialDTO);
        }
        CredentialDTO result = credentialService.save(credentialDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, credentialDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /credentials : get all the credentials.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of credentials in body
     */
    @GetMapping("/credentials")
    @Timed
    public List<CredentialDTO> getAllCredentials() {
        log.debug("REST request to get all Credentials");
        return credentialService.findAll();
        }

    /**
     * GET  /credentials/:id : get the "id" credential.
     *
     * @param id the id of the credentialDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the credentialDTO, or with status 404 (Not Found)
     */
    @GetMapping("/credentials/{id}")
    @Timed
    public ResponseEntity<CredentialDTO> getCredential(@PathVariable Long id) {
        log.debug("REST request to get Credential : {}", id);
        CredentialDTO credentialDTO = credentialService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(credentialDTO));
    }

    /**
     * DELETE  /credentials/:id : delete the "id" credential.
     *
     * @param id the id of the credentialDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/credentials/{id}")
    @Timed
    public ResponseEntity<Void> deleteCredential(@PathVariable Long id) {
        log.debug("REST request to delete Credential : {}", id);
        credentialService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/credentials?query=:query : search for the credential corresponding
     * to the query.
     *
     * @param query the query of the credential search
     * @return the result of the search
     */
    @GetMapping("/_search/credentials")
    @Timed
    public List<CredentialDTO> searchCredentials(@RequestParam String query) {
        log.debug("REST request to search Credentials for query {}", query);
        return credentialService.search(query);
    }

}
