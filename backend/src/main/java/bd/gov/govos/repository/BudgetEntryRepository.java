package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.BudgetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BudgetEntryRepository extends JpaRepository<BudgetEntry, Long> {
	List<BudgetEntry> findByMinistryIdAndEntryDateBetween(Long ministryId, LocalDate from, LocalDate to);
	List<BudgetEntry> findByEntryDateBetween(LocalDate from, LocalDate to);

	@Query("SELECT COALESCE(SUM(b.amountBdt), 0) FROM BudgetEntry b WHERE b.entryType = :type AND b.entryDate BETWEEN :from AND :to")
	BigDecimal sumByTypeBetween(@Param("type") String type, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
