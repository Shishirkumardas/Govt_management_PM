package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.GovNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GovNotificationRepository extends JpaRepository<GovNotification, Long> {
	List<GovNotification> findAllByOrderByCreatedAtDesc();
	List<GovNotification> findByReadFlagFalseOrderByCreatedAtDesc();
}
