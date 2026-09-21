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

/**
 * Detailed budget line under a ministry (and optionally an organization/project).
 */
@Entity
@Table(name = "budget_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetLine extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id", nullable = false)
	private Ministry ministry;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private GovOrganization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private GovProject project;

	/** ADP, NON_DEVELOPMENT, SALARY, ALLOWANCE, SUBSIDY, PROCUREMENT, MAINTENANCE, CONTINGENCY, FOREIGN_AID, LOAN */
	@Column(nullable = false, length = 40)
	private String category;

	@Column(nullable = false, length = 300)
	private String titleEn;

	@Column(length = 300)
	private String titleBn;

	@Column(precision = 18, scale = 2, nullable = false)
	private BigDecimal allocatedCrore;

	@Column(precision = 18, scale = 2)
	private BigDecimal releasedCrore;

	@Column(precision = 18, scale = 2)
	private BigDecimal spentCrore;

	/** Fiscal year e.g. 2025-26 */
	@Column(length = 20)
	private String fiscalYear;

	@Column(nullable = false)
	@Builder.Default
	private Boolean flagged = false;

	@Column(length = 1000)
	private String flagReason;
}
