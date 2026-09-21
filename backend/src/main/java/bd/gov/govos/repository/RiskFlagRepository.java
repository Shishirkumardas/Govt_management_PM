package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.RiskFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskFlagRepository extends JpaRepository<RiskFlag, Long> {
	List<RiskFlag> findByOpenTrueOrderByCreatedAtDesc();
}
