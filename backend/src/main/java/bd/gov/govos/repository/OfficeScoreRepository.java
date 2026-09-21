package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.OfficeScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfficeScoreRepository extends JpaRepository<OfficeScore, Long> {
	List<OfficeScore> findAllByOrderByOverallScoreDesc();
}
