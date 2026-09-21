package bd.gov.govos.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "gov_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovDocument extends BaseEntity {

	@Column(nullable = false, length = 300)
	private String title;

	/** POLICY, CIRCULAR, CONTRACT, GO, CABINET, LAND, AUDIT */
	@Column(nullable = false, length = 40)
	private String docType;

	/** PUBLIC, INTERNAL, CONFIDENTIAL, SECRET */
	@Column(nullable = false, length = 30)
	@Builder.Default
	private String classification = "INTERNAL";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	private LocalDate issuedDate;

	@Column(length = 4000)
	private String summary;

	@Column(length = 500)
	private String storagePath;

	@Column(length = 1000)
	private String tags;
}
