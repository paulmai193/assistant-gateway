package logia.assistant.gateway.service;

import logia.assistant.gateway.config.audit.AuditEventConverter;
import logia.assistant.gateway.repository.PersistenceAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator AuditEventRepository
 *
 * @author Dai Mai
 */
@Service
@Transactional
public class AuditEventService {

    /** The persistence audit event repository. */
    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    /** The audit event converter. */
    private final AuditEventConverter auditEventConverter;

    /**
     * Instantiates a new audit event service.
     *
     * @param persistenceAuditEventRepository the persistence audit event repository
     * @param auditEventConverter the audit event converter
     */
    public AuditEventService(
        PersistenceAuditEventRepository persistenceAuditEventRepository,
        AuditEventConverter auditEventConverter) {

        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
    }

    /**
     * Find all.
     *
     * @param pageable the pageable
     * @return the page
     */
    public Page<AuditEvent> findAll(Pageable pageable) {
        return persistenceAuditEventRepository.findAll(pageable)
            .map(auditEventConverter::convertToAuditEvent);
    }

    /**
     * Find by dates.
     *
     * @param fromDate the from date
     * @param toDate the to date
     * @param pageable the pageable
     * @return the page
     */
    public Page<AuditEvent> findByDates(Instant fromDate, Instant toDate, Pageable pageable) {
        return persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate, toDate, pageable)
            .map(auditEventConverter::convertToAuditEvent);
    }

    /**
     * Find.
     *
     * @param id the id
     * @return the optional
     */
    public Optional<AuditEvent> find(Long id) {
        return Optional.ofNullable(persistenceAuditEventRepository.findOne(id)).map
            (auditEventConverter::convertToAuditEvent);
    }
}
