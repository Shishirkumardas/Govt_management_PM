package bd.gov.govos.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "national_indicators", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"indicator_key", "as_of_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NationalIndicator extends BaseEntity {

	/** e.g. GDP_GROWTH, INFLATION, FOREX_RESERVE */
	@Column(name = "indicator_key", nullable = false, length = 60)
	private String indicatorKey;

	@Column(nullable = false, length = 150)
	private String label;

	@Column(nullable = false, length = 80)
	private String category;

	@Column(name = "metric_value", precision = 18, scale = 4)
	private BigDecimal value;

	@Column(length = 30)
	private String unit;

	/** Change vs previous period */
	@Column(precision = 10, scale = 4)
	private BigDecimal changePercent;

	@Column(nullable = false)
	private LocalDate asOfDate;

	@Column(length = 500)
	private String notes;
}
