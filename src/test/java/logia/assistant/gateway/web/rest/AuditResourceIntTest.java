package logia.assistant.gateway.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.AssistantGatewayApp;
import logia.assistant.gateway.config.audit.AuditEventConverter;
import logia.assistant.gateway.domain.PersistentAuditEvent;
import logia.assistant.gateway.repository.PersistenceAuditEventRepository;
import logia.assistant.gateway.service.AuditEventService;

/**
 * Test class for the AuditResource REST controller.
 *
 * @see AuditResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
@Transactional
public class AuditResourceIntTest {

    /** The Constant SAMPLE_PRINCIPAL. */
    private static final String SAMPLE_PRINCIPAL = "SAMPLE_PRINCIPAL";
    
    /** The Constant SAMPLE_TYPE. */
    private static final String SAMPLE_TYPE = "SAMPLE_TYPE";
    
    /** The Constant SAMPLE_TIMESTAMP. */
    private static final Instant SAMPLE_TIMESTAMP = Instant.parse("2015-08-04T10:11:30Z");
    
    /** The Constant SECONDS_PER_DAY. */
    private static final long SECONDS_PER_DAY = 60 * 60 * 24;

    /** The audit event repository. */
    @Autowired
    private PersistenceAuditEventRepository auditEventRepository;

    /** The audit event converter. */
    @Autowired
    private AuditEventConverter auditEventConverter;

    /** The jackson message converter. */
    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    /** The formatting conversion service. */
    @Autowired
    private FormattingConversionService formattingConversionService;

    /** The pageable argument resolver. */
    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    /** The audit event. */
    private PersistentAuditEvent auditEvent;

    /** The rest audit mock mvc. */
    private MockMvc restAuditMockMvc;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AuditEventService auditEventService =
            new AuditEventService(auditEventRepository, auditEventConverter);
        AuditResource auditResource = new AuditResource(auditEventService);
        this.restAuditMockMvc = MockMvcBuilders.standaloneSetup(auditResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setConversionService(formattingConversionService)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Inits the test.
     */
    @Before
    public void initTest() {
        auditEventRepository.deleteAll();
        auditEvent = new PersistentAuditEvent();
        auditEvent.setAuditEventType(SAMPLE_TYPE);
        auditEvent.setPrincipal(SAMPLE_PRINCIPAL);
        auditEvent.setAuditEventDate(SAMPLE_TIMESTAMP);
    }

    /**
     * Gets the all audits.
     *
     * @return the all audits
     * @throws Exception the exception
     */
    @Test
    public void getAllAudits() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Get all the audits
        restAuditMockMvc.perform(get("/management/audits"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].principal").value(hasItem(SAMPLE_PRINCIPAL)));
    }

    /**
     * Gets the audit.
     *
     * @return the audit
     * @throws Exception the exception
     */
    @Test
    public void getAudit() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Get the audit
        restAuditMockMvc.perform(get("/management/audits/{id}", auditEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.principal").value(SAMPLE_PRINCIPAL));
    }

    /**
     * Gets the audits by date.
     *
     * @return the audits by date
     * @throws Exception the exception
     */
    @Test
    public void getAuditsByDate() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Generate dates for selecting audits by date, making sure the period will contain the audit
        String fromDate  = SAMPLE_TIMESTAMP.minusSeconds(SECONDS_PER_DAY).toString().substring(0,10);
        String toDate = SAMPLE_TIMESTAMP.plusSeconds(SECONDS_PER_DAY).toString().substring(0,10);

        // Get the audit
        restAuditMockMvc.perform(get("/management/audits?fromDate="+fromDate+"&toDate="+toDate))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].principal").value(hasItem(SAMPLE_PRINCIPAL)));
    }

    /**
     * Gets the non existing audits by date.
     *
     * @return the non existing audits by date
     * @throws Exception the exception
     */
    @Test
    public void getNonExistingAuditsByDate() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Generate dates for selecting audits by date, making sure the period will not contain the sample audit
        String fromDate  = SAMPLE_TIMESTAMP.minusSeconds(2*SECONDS_PER_DAY).toString().substring(0,10);
        String toDate = SAMPLE_TIMESTAMP.minusSeconds(SECONDS_PER_DAY).toString().substring(0,10);

        // Query audits but expect no results
        restAuditMockMvc.perform(get("/management/audits?fromDate=" + fromDate + "&toDate=" + toDate))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(header().string("X-Total-Count", "0"));
    }

    /**
     * Gets the non existing audit.
     *
     * @return the non existing audit
     * @throws Exception the exception
     */
    @Test
    public void getNonExistingAudit() throws Exception {
        // Get the audit
        restAuditMockMvc.perform(get("/management/audits/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }
}
