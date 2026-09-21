package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
	Optional<DailyReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);
	List<DailyReport> findByReportDate(LocalDate reportDate);
	List<DailyReport> findByMinistryIdAndReportDateBetween(Long ministryId, LocalDate from, LocalDate to);
	List<DailyReport> findByReportDateBetween(LocalDate from, LocalDate to);
	List<DailyReport> findByUserIdOrderByReportDateDesc(Long userId);

	@Query("SELECT COALESCE(SUM(d.expensesBdt), 0) FROM DailyReport d WHERE d.reportDate BETWEEN :from AND :to")
	BigDecimal sumExpensesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

	@Query("SELECT COUNT(d) FROM DailyReport d WHERE d.reportDate = :date")
	long countByReportDate(@Param("date") LocalDate date);
}
