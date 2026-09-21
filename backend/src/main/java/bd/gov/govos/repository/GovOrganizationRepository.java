package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.GovOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GovOrganizationRepository extends JpaRepository<GovOrganization, Long> {
	List<GovOrganization> findByMinistryIdOrderByCorruptionRiskScoreDesc(Long ministryId);
	List<GovOrganization> findByMinistryIdAndActiveTrueOrderByNameEnAsc(Long ministryId);
}
