package logia.assistant.gateway.web.rest;

import logia.assistant.gateway.AssistantGatewayApp;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.service.CredentialService;
import logia.assistant.gateway.repository.search.CredentialSearchRepository;
import logia.assistant.gateway.service.dto.CredentialDTO;
import logia.assistant.gateway.service.mapper.CredentialMapper;
import logia.assistant.gateway.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static logia.assistant.gateway.web.rest.TestUtil.sameInstant;
import static logia.assistant.gateway.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CredentialResource REST controller.
 *
 * @see CredentialResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
public class CredentialResourceIntTest {

    private static final String DEFAULT_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_LOGIN = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_LAST_LOGIN_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LAST_LOGIN_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_ACTIVATION_KEY = "AAAAAAAAAA";
    private static final String UPDATED_ACTIVATION_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_RESET_KEY = "AAAAAAAAAA";
    private static final String UPDATED_RESET_KEY = "BBBBBBBBBB";

    private static final Instant DEFAULT_RESET_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_RESET_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_ACTIVATED = false;
    private static final Boolean UPDATED_ACTIVATED = true;

    private static final Boolean DEFAULT_PRIMARY = false;
    private static final Boolean UPDATED_PRIMARY = true;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private CredentialMapper credentialMapper;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private CredentialSearchRepository credentialSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restCredentialMockMvc;

    private Credential credential;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CredentialResource credentialResource = new CredentialResource(credentialService);
        this.restCredentialMockMvc = MockMvcBuilders.standaloneSetup(credentialResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Credential createEntity(EntityManager em) {
        Credential credential = new Credential()
            .login(DEFAULT_LOGIN)
            .lastLoginDate(DEFAULT_LAST_LOGIN_DATE)
            .activationKey(DEFAULT_ACTIVATION_KEY)
            .resetKey(DEFAULT_RESET_KEY)
            .resetDate(DEFAULT_RESET_DATE)
            .activated(DEFAULT_ACTIVATED)
            .primary(DEFAULT_PRIMARY);
        // Add required entity
        User user = UserResourceIntTest.createEntity(em);
        em.persist(user);
        em.flush();
        credential.setUser(user);
        return credential;
    }

    @Before
    public void initTest() {
        credentialSearchRepository.deleteAll();
        credential = createEntity(em);
    }

    @Test
    @Transactional
    public void createCredential() throws Exception {
        int databaseSizeBeforeCreate = credentialRepository.findAll().size();

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);
        restCredentialMockMvc.perform(post("/api/credentials")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(credentialDTO)))
            .andExpect(status().isCreated());

        // Validate the Credential in the database
        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeCreate + 1);
        Credential testCredential = credentialList.get(credentialList.size() - 1);
        assertThat(testCredential.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testCredential.getLastLoginDate()).isEqualTo(DEFAULT_LAST_LOGIN_DATE);
        assertThat(testCredential.getActivationKey()).isEqualTo(DEFAULT_ACTIVATION_KEY);
        assertThat(testCredential.getResetKey()).isEqualTo(DEFAULT_RESET_KEY);
        assertThat(testCredential.getResetDate()).isEqualTo(DEFAULT_RESET_DATE);
        assertThat(testCredential.isActivated()).isEqualTo(DEFAULT_ACTIVATED);
        assertThat(testCredential.isPrimary()).isEqualTo(DEFAULT_PRIMARY);

        // Validate the Credential in Elasticsearch
        Credential credentialEs = credentialSearchRepository.findOne(testCredential.getId());
        assertThat(testCredential.getLastLoginDate()).isEqualTo(testCredential.getLastLoginDate());
        assertThat(credentialEs).isEqualToIgnoringGivenFields(testCredential, "lastLoginDate");
    }

    @Test
    @Transactional
    public void createCredentialWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = credentialRepository.findAll().size();

        // Create the Credential with an existing ID
        credential.setId(1L);
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCredentialMockMvc.perform(post("/api/credentials")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkLoginIsRequired() throws Exception {
        int databaseSizeBeforeTest = credentialRepository.findAll().size();
        // set the field null
        credential.setLogin(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc.perform(post("/api/credentials")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkActivatedIsRequired() throws Exception {
        int databaseSizeBeforeTest = credentialRepository.findAll().size();
        // set the field null
        credential.setActivated(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc.perform(post("/api/credentials")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPrimaryIsRequired() throws Exception {
        int databaseSizeBeforeTest = credentialRepository.findAll().size();
        // set the field null
        credential.setPrimary(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc.perform(post("/api/credentials")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCredentials() throws Exception {
        // Initialize the database
        credentialRepository.saveAndFlush(credential);

        // Get all the credentialList
        restCredentialMockMvc.perform(get("/api/credentials?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(credential.getId().intValue())))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN.toString())))
            .andExpect(jsonPath("$.[*].lastLoginDate").value(hasItem(sameInstant(DEFAULT_LAST_LOGIN_DATE))))
            .andExpect(jsonPath("$.[*].activationKey").value(hasItem(DEFAULT_ACTIVATION_KEY.toString())))
            .andExpect(jsonPath("$.[*].resetKey").value(hasItem(DEFAULT_RESET_KEY.toString())))
            .andExpect(jsonPath("$.[*].resetDate").value(hasItem(DEFAULT_RESET_DATE.toString())))
            .andExpect(jsonPath("$.[*].activated").value(hasItem(DEFAULT_ACTIVATED.booleanValue())))
            .andExpect(jsonPath("$.[*].primary").value(hasItem(DEFAULT_PRIMARY.booleanValue())));
    }

    @Test
    @Transactional
    public void getCredential() throws Exception {
        // Initialize the database
        credentialRepository.saveAndFlush(credential);

        // Get the credential
        restCredentialMockMvc.perform(get("/api/credentials/{id}", credential.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(credential.getId().intValue()))
            .andExpect(jsonPath("$.login").value(DEFAULT_LOGIN.toString()))
            .andExpect(jsonPath("$.lastLoginDate").value(sameInstant(DEFAULT_LAST_LOGIN_DATE)))
            .andExpect(jsonPath("$.activationKey").value(DEFAULT_ACTIVATION_KEY.toString()))
            .andExpect(jsonPath("$.resetKey").value(DEFAULT_RESET_KEY.toString()))
            .andExpect(jsonPath("$.resetDate").value(DEFAULT_RESET_DATE.toString()))
            .andExpect(jsonPath("$.activated").value(DEFAULT_ACTIVATED.booleanValue()))
            .andExpect(jsonPath("$.primary").value(DEFAULT_PRIMARY.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingCredential() throws Exception {
        // Get the credential
        restCredentialMockMvc.perform(get("/api/credentials/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCredential() throws Exception {
        // Initialize the database
        credentialRepository.saveAndFlush(credential);
        credentialSearchRepository.save(credential);
        int databaseSizeBeforeUpdate = credentialRepository.findAll().size();

        // Update the credential
        Credential updatedCredential = credentialRepository.findOne(credential.getId());
        // Disconnect from session so that the updates on updatedCredential are not directly saved in db
        em.detach(updatedCredential);
        updatedCredential
            .login(UPDATED_LOGIN)
            .lastLoginDate(UPDATED_LAST_LOGIN_DATE)
            .activationKey(UPDATED_ACTIVATION_KEY)
            .resetKey(UPDATED_RESET_KEY)
            .resetDate(UPDATED_RESET_DATE)
            .activated(UPDATED_ACTIVATED)
            .primary(UPDATED_PRIMARY);
        CredentialDTO credentialDTO = credentialMapper.toDto(updatedCredential);

        restCredentialMockMvc.perform(put("/api/credentials")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(credentialDTO)))
            .andExpect(status().isOk());

        // Validate the Credential in the database
        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeUpdate);
        Credential testCredential = credentialList.get(credentialList.size() - 1);
        assertThat(testCredential.getLogin()).isEqualTo(UPDATED_LOGIN);
        assertThat(testCredential.getLastLoginDate()).isEqualTo(UPDATED_LAST_LOGIN_DATE);
        assertThat(testCredential.getActivationKey()).isEqualTo(UPDATED_ACTIVATION_KEY);
        assertThat(testCredential.getResetKey()).isEqualTo(UPDATED_RESET_KEY);
        assertThat(testCredential.getResetDate()).isEqualTo(UPDATED_RESET_DATE);
        assertThat(testCredential.isActivated()).isEqualTo(UPDATED_ACTIVATED);
        assertThat(testCredential.isPrimary()).isEqualTo(UPDATED_PRIMARY);

        // Validate the Credential in Elasticsearch
        Credential credentialEs = credentialSearchRepository.findOne(testCredential.getId());
        assertThat(testCredential.getLastLoginDate()).isEqualTo(testCredential.getLastLoginDate());
        assertThat(credentialEs).isEqualToIgnoringGivenFields(testCredential, "lastLoginDate");
    }

    @Test
    @Transactional
    public void updateNonExistingCredential() throws Exception {
        int databaseSizeBeforeUpdate = credentialRepository.findAll().size();

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restCredentialMockMvc.perform(put("/api/credentials")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(credentialDTO)))
            .andExpect(status().isCreated());

        // Validate the Credential in the database
        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteCredential() throws Exception {
        // Initialize the database
        credentialRepository.saveAndFlush(credential);
        credentialSearchRepository.save(credential);
        int databaseSizeBeforeDelete = credentialRepository.findAll().size();

        // Get the credential
        restCredentialMockMvc.perform(delete("/api/credentials/{id}", credential.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean credentialExistsInEs = credentialSearchRepository.exists(credential.getId());
        assertThat(credentialExistsInEs).isFalse();

        // Validate the database is empty
        List<Credential> credentialList = credentialRepository.findAll();
        assertThat(credentialList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchCredential() throws Exception {
        // Initialize the database
        credentialRepository.saveAndFlush(credential);
        credentialSearchRepository.save(credential);

        // Search the credential
        restCredentialMockMvc.perform(get("/api/_search/credentials?query=id:" + credential.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(credential.getId().intValue())))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN.toString())))
            .andExpect(jsonPath("$.[*].lastLoginDate").value(hasItem(sameInstant(DEFAULT_LAST_LOGIN_DATE))))
            .andExpect(jsonPath("$.[*].activationKey").value(hasItem(DEFAULT_ACTIVATION_KEY.toString())))
            .andExpect(jsonPath("$.[*].resetKey").value(hasItem(DEFAULT_RESET_KEY.toString())))
            .andExpect(jsonPath("$.[*].resetDate").value(hasItem(DEFAULT_RESET_DATE.toString())))
            .andExpect(jsonPath("$.[*].activated").value(hasItem(DEFAULT_ACTIVATED.booleanValue())))
            .andExpect(jsonPath("$.[*].primary").value(hasItem(DEFAULT_PRIMARY.booleanValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Credential.class);
        Credential credential1 = new Credential();
        credential1.setId(1L);
        Credential credential2 = new Credential();
        credential2.setId(credential1.getId());
        assertThat(credential1).isEqualTo(credential2);
        credential2.setId(2L);
        assertThat(credential1).isNotEqualTo(credential2);
        credential1.setId(null);
        assertThat(credential1).isNotEqualTo(credential2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CredentialDTO.class);
        CredentialDTO credentialDTO1 = new CredentialDTO();
        credentialDTO1.setId(1L);
        CredentialDTO credentialDTO2 = new CredentialDTO();
        assertThat(credentialDTO1).isNotEqualTo(credentialDTO2);
        credentialDTO2.setId(credentialDTO1.getId());
        assertThat(credentialDTO1).isEqualTo(credentialDTO2);
        credentialDTO2.setId(2L);
        assertThat(credentialDTO1).isNotEqualTo(credentialDTO2);
        credentialDTO1.setId(null);
        assertThat(credentialDTO1).isNotEqualTo(credentialDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(credentialMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(credentialMapper.fromId(null)).isNull();
    }
}
