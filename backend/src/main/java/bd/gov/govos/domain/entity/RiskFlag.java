package bd.gov.govos.domain.entity;

import bd.gov.govos.domain.enums.AlertSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "risk_flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskFlag extends BaseEntity {

	/** CORRUPTION, BUDGET_LEAKAGE, LOW_PRODUCTIVITY, DELAYED_REPORTS, OVERSPENDING,
	 *  DUPLICATE_PAYMENTS, GHOST_EMPLOYEES, INACTIVE_PROJECTS, PROCUREMENT */
	@Column(nullable = false, length = 40)
	private String riskType;

	@Column(nullable = false, length = 300)
	private String title;

	@Column(length = 3000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AlertSeverity severity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	@Column(length = 150)
	private String location;

	@Column(nullable = false)
	@Builder.Default
	private Boolean open = true;

	@Column(length = 1000)
	private String recommendedAction;
}
