package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.MinistryKpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MinistryKpiRepository extends JpaRepository<MinistryKpi, Long> {
	List<MinistryKpi> findByMinistryIdOrderByLabelAsc(Long ministryId);
	List<MinistryKpi> findAllByOrderByMinistryIdAsc();
}
