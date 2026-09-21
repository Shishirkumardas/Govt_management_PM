package bd.gov.govos.domain.entity;

import bd.gov.govos.domain.enums.ProjectStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "gov_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovProject extends BaseEntity {

	@Column(nullable = false, unique = true, length = 40)
	private String projectCode;

	@Column(nullable = false, length = 300)
	private String title;

	@Column(length = 3000)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id", nullable = false)
	private Ministry ministry;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ProjectStatus status;

	/** Total approved cost in BDT crore */
	@Column(precision = 18, scale = 2)
	private BigDecimal totalCostCrore;

	/** Spent to date in BDT crore */
	@Column(precision = 18, scale = 2)
	private BigDecimal spentCostCrore;

	/** Physical progress 0-100 */
	@Column(precision = 5, scale = 2)
	private BigDecimal progressPercent;

	private LocalDate startDate;
	private LocalDate expectedEndDate;
	private LocalDate actualEndDate;

	@Column(length = 150)
	private String location;

	@Column(length = 100)
	private String fundingSource;

	/** Foreign investment linked (BDT crore or USD million note) */
	@Column(precision = 18, scale = 2)
	private BigDecimal foreignInvestmentAmount;

	@Column(length = 50)
	private String foreignInvestmentCurrency;

	@Column(length = 2000)
	private String latestUpdate;

	private LocalDate latestUpdateDate;

	@Column(length = 200)
	private String contractor;

	/** Schedule slip in days (positive = behind) */
	private Integer delayDays;

	/** LOW, MEDIUM, HIGH, CRITICAL */
	@Column(length = 20)
	private String riskLevel;

	@Column(length = 1000)
	private String riskReason;

	@Column(length = 1000)
	private String aiRecommendation;
}
