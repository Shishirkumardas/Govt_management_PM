package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.ForeignInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ForeignInvestmentRepository extends JpaRepository<ForeignInvestment, Long> {
	List<ForeignInvestment> findAllByOrderByAgreementDateDesc();

	@Query("SELECT COALESCE(SUM(f.amountUsdMillion), 0) FROM ForeignInvestment f WHERE f.status = 'ACTIVE' OR f.status = 'COMMITTED'")
	BigDecimal sumActiveInvestmentUsd();
}
