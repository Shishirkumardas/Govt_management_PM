package bd.gov.govos.domain.entity;

import bd.gov.govos.domain.enums.AlertSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "national_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NationalAlert extends BaseEntity {

	@Column(nullable = false, length = 200)
	private String title;

	@Column(length = 3000)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AlertSeverity severity;

	/** DISASTER, HEALTH, SECURITY, BUDGET, PROJECT, AGRICULTURE */
	@Column(nullable = false, length = 40)
	private String category;

	@Column(length = 150)
	private String location;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;

	private Instant resolvedAt;
}
