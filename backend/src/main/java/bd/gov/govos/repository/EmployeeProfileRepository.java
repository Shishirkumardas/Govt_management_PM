package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
	List<EmployeeProfile> findByMinistryId(Long ministryId);
}
