package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.CitizenComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CitizenComplaintRepository extends JpaRepository<CitizenComplaint, Long> {
	Optional<CitizenComplaint> findByTrackingId(String trackingId);
	List<CitizenComplaint> findAllByOrderByCreatedAtDesc();
}
