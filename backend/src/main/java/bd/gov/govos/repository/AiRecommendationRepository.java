package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {
	List<AiRecommendation> findByAcknowledgedFalseOrderByCreatedAtDesc();
	List<AiRecommendation> findByScopeOrderByCreatedAtDesc(String scope);
}
