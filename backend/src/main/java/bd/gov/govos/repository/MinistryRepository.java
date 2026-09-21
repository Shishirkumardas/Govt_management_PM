package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.Ministry;
import bd.gov.govos.domain.enums.MinistryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MinistryRepository extends JpaRepository<Ministry, Long> {
	Optional<Ministry> findByCode(MinistryCode code);
}
