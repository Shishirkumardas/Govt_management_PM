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
@Table(name = "foreign_investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForeignInvestment extends BaseEntity {

	@Column(nullable = false, length = 200)
	private String investorName;

	@Column(nullable = false, length = 100)
	private String country;

	@Column(nullable = false, length = 300)
	private String sector;

	@Column(precision = 18, scale = 2)
	private BigDecimal amountUsdMillion;

	@Column(length = 40)
	private String status;

	private LocalDate agreementDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	@Column(length = 2000)
	private String notes;
}
