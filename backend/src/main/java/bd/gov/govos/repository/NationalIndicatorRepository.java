package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.NationalIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NationalIndicatorRepository extends JpaRepository<NationalIndicator, Long> {
	Optional<NationalIndicator> findTopByIndicatorKeyOrderByAsOfDateDesc(String indicatorKey);

	@Query("SELECT n FROM NationalIndicator n WHERE n.asOfDate = (SELECT MAX(n2.asOfDate) FROM NationalIndicator n2 WHERE n2.indicatorKey = n.indicatorKey)")
	List<NationalIndicator> findLatestForAllKeys();

	List<NationalIndicator> findByCategoryOrderByAsOfDateDesc(String category);
}
