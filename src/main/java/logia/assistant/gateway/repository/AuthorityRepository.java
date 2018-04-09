package logia.assistant.gateway.repository;

import logia.assistant.gateway.domain.Authority;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Authority entity.
 *
 * @author Dai Mai
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
