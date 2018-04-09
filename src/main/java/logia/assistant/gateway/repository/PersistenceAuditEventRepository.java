package logia.assistant.gateway.repository;

import logia.assistant.gateway.domain.PersistentAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for the PersistentAuditEvent entity.
 *
 * @author Dai Mai
 */
public interface PersistenceAuditEventRepository extends JpaRepository<PersistentAuditEvent, Long> {

    /**
     * Find by principal.
     *
     * @param principal the principal
     * @return the list
     */
    List<PersistentAuditEvent> findByPrincipal(String principal);

    /**
     * Find by audit event date after.
     *
     * @param after the after
     * @return the list
     */
    List<PersistentAuditEvent> findByAuditEventDateAfter(Instant after);

    /**
     * Find by principal and audit event date after.
     *
     * @param principal the principal
     * @param after the after
     * @return the list
     */
    List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfter(String principal, Instant after);

    /**
     * Find by principal and audit event date after and audit event type.
     *
     * @param principle the principle
     * @param after the after
     * @param type the type
     * @return the list
     */
    List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfterAndAuditEventType(String principle, Instant after, String type);

    /**
     * Find all by audit event date between.
     *
     * @param fromDate the from date
     * @param toDate the to date
     * @param pageable the pageable
     * @return the page
     */
    Page<PersistentAuditEvent> findAllByAuditEventDateBetween(Instant fromDate, Instant toDate, Pageable pageable);
}
