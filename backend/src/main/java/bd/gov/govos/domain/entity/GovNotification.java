package bd.gov.govos.domain.entity;

import bd.gov.govos.domain.enums.AlertSeverity;
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

@Entity
@Table(name = "gov_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovNotification extends BaseEntity {

	@Column(nullable = false, length = 200)
	private String title;

	@Column(length = 2000)
	private String message;

	/** FLOOD, BUDGET, PROJECT, MEDICINE, TAX, FOOD, POWER, HEALTH */
	@Column(nullable = false, length = 40)
	private String category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AlertSeverity severity;

	/** PRIME_MINISTER, ALL, MINISTRY:{id} */
	@Column(nullable = false, length = 80)
	@Builder.Default
	private String audience = "PRIME_MINISTER";

	@Column(nullable = false)
	@Builder.Default
	private Boolean readFlag = false;
}
