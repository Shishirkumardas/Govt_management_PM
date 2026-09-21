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
 * Directorate / department / autonomous body under a ministry.
 * Granular unit for budget, headcount and corruption risk monitoring.
 */
@Entity
@Table(name = "gov_organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovOrganization extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id", nullable = false)
	private Ministry ministry;

	@Column(nullable = false, unique = true, length = 40)
	private String orgCode;

	@Column(nullable = false, length = 250)
	private String nameEn;

	@Column(length = 250)
	private String nameBn;

	/** DIRECTORATE, DEPARTMENT, AUTONOMOUS, CORPORATION, AGENCY, DIVISION, FIELD_OFFICE */
	@Column(nullable = false, length = 40)
	private String orgType;

	@Column(length = 150)
	private String headName;

	@Column(length = 100)
	private String headDesignation;

	/** Allocated annual budget (BDT crore) */
	@Column(precision = 18, scale = 2)
	private BigDecimal budgetCrore;

	@Column(precision = 18, scale = 2)
	private BigDecimal spentCrore;

	private Integer sanctionedPosts;
	private Integer filledPosts;
	/** Suspected ghost / biometric mismatch */
	private Integer anomalyHeadcount;

	@Column(precision = 5, scale = 2)
	private BigDecimal transparencyScore;

	@Column(precision = 5, scale = 2)
	private BigDecimal efficiencyScore;

	/** 0-100 higher = more risk */
	@Column(precision = 5, scale = 2)
	private BigDecimal corruptionRiskScore;

	@Column(length = 150)
	private String district;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;

	@Column(length = 2000)
	private String mandate;
}
