package bd.gov.govos.domain.entity;

import bd.gov.govos.domain.enums.UserRole;
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

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount extends BaseEntity {

	@Column(nullable = false, unique = true, length = 80)
	private String username;

	@Column(nullable = false, length = 200)
	private String passwordHash;

	@Column(nullable = false, length = 150)
	private String fullName;

	@Column(length = 150)
	private String email;

	@Column(length = 30)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private UserRole role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ministry_id")
	private Ministry ministry;

	/** e.g. Dhaka, Chattogram */
	@Column(length = 100)
	private String district;

	/** e.g. Gulshan, Mirpur */
	@Column(length = 100)
	private String upazila;

	@Column(length = 100)
	private String designation;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;

	/** TOTP MFA for privileged accounts */
	@Column(nullable = false)
	@Builder.Default
	private Boolean mfaEnabled = false;

	@Column(length = 64)
	private String mfaSecret;
}
