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

@Entity
@Table(name = "office_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeScore extends BaseEntity {

	@Column(nullable = false, length = 200)
	private String officeName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	@Column(length = 100)
	private String district;

	@Column(precision = 5, scale = 2)
	private BigDecimal efficiencyScore;

	@Column(precision = 5, scale = 2)
	private BigDecimal transparencyScore;

	@Column(precision = 5, scale = 2)
	private BigDecimal budgetScore;

	@Column(precision = 5, scale = 2)
	private BigDecimal citizenSatisfaction;

	@Column(precision = 5, scale = 2)
	private BigDecimal projectCompletion;

	@Column(precision = 5, scale = 2)
	private BigDecimal attendanceScore;

	@Column(precision = 5, scale = 2)
	private BigDecimal overallScore;
}
