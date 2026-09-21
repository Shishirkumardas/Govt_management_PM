package bd.gov.govos.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Lightweight GIS-oriented district snapshot for map clicks. */
@Entity
@Table(name = "district_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictProfile extends BaseEntity {

	@Column(nullable = false, unique = true, length = 80)
	private String districtName;

	@Column(nullable = false, length = 80)
	private String division;

	private Double latitude;
	private Double longitude;

	private Long population;
	private Integer hospitals;
	private Integer schools;
	private Integer activeProjects;
	private Integer crimeCasesMonth;

	@Column(precision = 10, scale = 2)
	private BigDecimal budgetCrore;

	@Column(precision = 10, scale = 2)
	private BigDecimal floodRiskScore;

	@Column(precision = 10, scale = 2)
	private BigDecimal dengueRiskScore;

	@Column(length = 500)
	private String weatherNote;

	@Column(length = 1000)
	private String investmentNote;
}
