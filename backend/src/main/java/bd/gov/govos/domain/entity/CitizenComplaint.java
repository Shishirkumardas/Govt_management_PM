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

@Entity
@Table(name = "citizen_complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenComplaint extends BaseEntity {

	@Column(nullable = false, unique = true, length = 40)
	private String trackingId;

	@Column(nullable = false, length = 150)
	private String citizenName;

	@Column(length = 30)
	private String phone;

	@Column(nullable = false, length = 200)
	private String subject;

	@Column(length = 3000)
	private String description;

	@Column(length = 100)
	private String district;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	/** OPEN, IN_PROGRESS, RESOLVED, REJECTED */
	@Column(nullable = false, length = 30)
	@Builder.Default
	private String status = "OPEN";

	@Column(length = 1000)
	private String resolutionNote;
}
