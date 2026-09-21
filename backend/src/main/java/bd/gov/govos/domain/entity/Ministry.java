package bd.gov.govos.domain.entity;

import bd.gov.govos.domain.enums.MinistryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ministries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ministry extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 40)
	private MinistryCode code;

	@Column(nullable = false, length = 200)
	private String nameEn;

	@Column(nullable = false, length = 200)
	private String nameBn;

	@Column(length = 1000)
	private String description;

	/** Annual allocated budget in BDT crore */
	@Column(precision = 18, scale = 2)
	private BigDecimal annualBudgetCrore;

	/** Spent so far in BDT crore */
	@Column(precision = 18, scale = 2)
	private BigDecimal spentBudgetCrore;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;
}
