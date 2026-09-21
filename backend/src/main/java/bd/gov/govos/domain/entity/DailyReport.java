package bd.gov.govos.domain.entity;

import bd.gov.govos.domain.enums.ReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "daily_reports", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"user_id", "report_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReport extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	@Column(nullable = false)
	private LocalDate reportDate;

	@Column(length = 3000)
	private String workCompleted;

	@Column(length = 2000)
	private String visits;

	@Column(length = 2000)
	private String approvals;

	/** Expenses in BDT */
	@Column(precision = 14, scale = 2)
	private BigDecimal expensesBdt;

	@Column(length = 3000)
	private String problems;

	@Column(length = 2000)
	private String recommendations;

	/** Optional GPS of field work */
	private Double latitude;
	private Double longitude;

	@Column(length = 500)
	private String photoUrls;

	@Column(length = 500)
	private String documentUrls;

	private Integer inspectionsCount;
	private Integer schoolsVisited;
	private Integer projectsApproved;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private ReportStatus status = ReportStatus.SUBMITTED;
}
