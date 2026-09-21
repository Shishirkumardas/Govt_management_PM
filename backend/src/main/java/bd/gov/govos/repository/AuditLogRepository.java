package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
	List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}
