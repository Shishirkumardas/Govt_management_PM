package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.BudgetLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetLineRepository extends JpaRepository<BudgetLine, Long> {
	List<BudgetLine> findByMinistryIdOrderByAllocatedCroreDesc(Long ministryId);
	List<BudgetLine> findByMinistryIdAndFlaggedTrue(Long ministryId);
}
