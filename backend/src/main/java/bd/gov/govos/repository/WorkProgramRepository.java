package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.WorkProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkProgramRepository extends JpaRepository<WorkProgram, Long> {
	List<WorkProgram> findByMinistryIdOrderByProgressPercentAsc(Long ministryId);
	List<WorkProgram> findByMinistryIdAndTransparencyIssueTrue(Long ministryId);
}
