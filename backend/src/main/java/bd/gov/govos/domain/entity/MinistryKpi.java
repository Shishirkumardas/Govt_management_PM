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

@Entity
@Table(name = "ministry_kpis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinistryKpi extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id", nullable = false)
	private Ministry ministry;

	@Column(nullable = false, length = 80)
	private String kpiKey;

	@Column(nullable = false, length = 200)
	private String label;

	@Column(name = "kpi_value", precision = 18, scale = 4)
	private BigDecimal value;

	@Column(name = "kpi_target", precision = 18, scale = 4)
	private BigDecimal target;

	@Column(length = 30)
	private String unit;

	private LocalDate asOfDate;
}
