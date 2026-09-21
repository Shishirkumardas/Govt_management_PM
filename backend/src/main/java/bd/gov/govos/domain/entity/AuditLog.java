package bd.gov.govos.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable-style audit trail (append-only; no update/delete APIs).
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

	@Column(length = 80)
	private String username;

	@Column(length = 40)
	private String role;

	@Column(nullable = false, length = 80)
	private String action;

	@Column(nullable = false, length = 120)
	private String resource;

	@Column(length = 500)
	private String details;

	@Column(length = 60)
	private String ipAddress;

	@Column(length = 20)
	private String httpMethod;

	@Column(length = 300)
	private String path;

	private Integer statusCode;

	/** Optional integrity fingerprint for tamper evidence */
	@Column(length = 128)
	private String integrityHash;
}
