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
@Table(name = "procurement_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementOrder extends BaseEntity {

	@Column(nullable = false, unique = true, length = 40)
	private String orderCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id", nullable = false)
	private Ministry ministry;

	@Column(nullable = false, length = 300)
	private String itemDescription;

	@Column(nullable = false, length = 200)
	private String vendorName;

	@Column(precision = 18, scale = 2, nullable = false)
	private BigDecimal amountBdt;

	private LocalDate orderDate;

	@Column(length = 40)
	private String status;

	/** Flags: PRICE_INFLATION, DUPLICATE_INVOICE, FAKE_VENDOR, REPEATED_PURCHASE, CONFLICT */
	@Column(length = 100)
	private String riskFlags;

	@Column(length = 1000)
	private String riskNotes;

	@Column(nullable = false)
	@Builder.Default
	private Boolean flagged = false;
}
