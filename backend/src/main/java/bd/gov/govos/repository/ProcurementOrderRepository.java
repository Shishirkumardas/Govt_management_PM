package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.ProcurementOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, Long> {
	List<ProcurementOrder> findByFlaggedTrueOrderByOrderDateDesc();
	List<ProcurementOrder> findByMinistryIdOrderByOrderDateDesc(Long ministryId);
}
