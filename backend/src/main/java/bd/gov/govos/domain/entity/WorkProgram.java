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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Operational work item / scheme under a ministry organization.
 */
@Entity
@Table(name = "work_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkProgram extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id", nullable = false)
	private Ministry ministry;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private GovOrganization organization;

	@Column(nullable = false, length = 40)
	private String workCode;

	@Column(nullable = false, length = 350)
	private String titleEn;

	@Column(length = 350)
	private String titleBn;

	@Column(length = 3000)
	private String description;

	/** PLANNED, ONGOING, DELAYED, COMPLETED, SUSPENDED */
	@Column(nullable = false, length = 30)
	private String status;

	@Column(precision = 18, scale = 2)
	private BigDecimal budgetCrore;

	@Column(precision = 18, scale = 2)
	private BigDecimal spentCrore;

	@Column(precision = 5, scale = 2)
	private BigDecimal progressPercent;

	@Column(length = 150)
	private String location;

	private Integer beneficiaries;

	private LocalDate startDate;
	private LocalDate endDate;

	@Column(length = 2000)
	private String latestUpdate;

	@Column(nullable = false)
	@Builder.Default
	private Boolean transparencyIssue = false;

	@Column(length = 1000)
	private String issueNote;
}
