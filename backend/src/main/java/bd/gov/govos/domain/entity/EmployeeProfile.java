package bd.gov.govos.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfile extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private UserAccount user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	@Column(nullable = false, unique = true, length = 40)
	private String employeeId;

	@Column(length = 40)
	private String grade;

	private LocalDate joinDate;
	private LocalDate lastPromotionDate;

	@Column(precision = 12, scale = 2)
	private BigDecimal monthlySalaryBdt;

	/** Attendance % last 30 days */
	@Column(precision = 5, scale = 2)
	private BigDecimal attendancePercent;

	/** AI composite performance 0-100 */
	@Column(precision = 5, scale = 2)
	private BigDecimal performanceScore;

	@Column(length = 100)
	private String trainingStatus;

	private Integer leaveBalanceDays;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;
}
