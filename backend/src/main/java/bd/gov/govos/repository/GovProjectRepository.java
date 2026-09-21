package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.GovProject;
import bd.gov.govos.domain.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GovProjectRepository extends JpaRepository<GovProject, Long> {
	Optional<GovProject> findByProjectCode(String projectCode);
	List<GovProject> findByMinistryId(Long ministryId);
	List<GovProject> findByStatus(ProjectStatus status);
	List<GovProject> findByMinistryIdAndStatus(Long ministryId, ProjectStatus status);

	@Query("SELECT p FROM GovProject p WHERE p.status IN ('ONGOING','DELAYED','APPROVED') ORDER BY p.totalCostCrore DESC")
	List<GovProject> findActiveMegaProjects();
}
