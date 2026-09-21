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
@Table(name = "budget_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetEntry extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id", nullable = false)
	private Ministry ministry;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private GovProject project;

	/** REVENUE, EXPENDITURE, LOAN, GRANT, TAX */
	@Column(nullable = false, length = 40)
	private String entryType;

	@Column(nullable = false, length = 300)
	private String title;

	@Column(precision = 18, scale = 2, nullable = false)
	private BigDecimal amountBdt;

	@Column(nullable = false)
	private LocalDate entryDate;

	@Column(length = 1000)
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recorded_by")
	private UserAccount recordedBy;
}
