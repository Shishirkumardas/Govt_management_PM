package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.NationalAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NationalAlertRepository extends JpaRepository<NationalAlert, Long> {
	List<NationalAlert> findByActiveTrueOrderByCreatedAtDesc();
	List<NationalAlert> findByMinistryIdAndActiveTrueOrderByCreatedAtDesc(Long ministryId);
}
