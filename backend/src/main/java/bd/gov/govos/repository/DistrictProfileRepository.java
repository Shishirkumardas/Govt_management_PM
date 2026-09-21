package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.DistrictProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistrictProfileRepository extends JpaRepository<DistrictProfile, Long> {
	Optional<DistrictProfile> findByDistrictNameIgnoreCase(String districtName);
}
