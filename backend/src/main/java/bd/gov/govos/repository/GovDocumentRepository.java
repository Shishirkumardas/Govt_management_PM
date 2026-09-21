package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.GovDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GovDocumentRepository extends JpaRepository<GovDocument, Long> {
	List<GovDocument> findByDocTypeOrderByIssuedDateDesc(String docType);

	@Query("SELECT d FROM GovDocument d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.summary) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.tags) LIKE LOWER(CONCAT('%', :q, '%'))")
	List<GovDocument> search(@Param("q") String q);
}
