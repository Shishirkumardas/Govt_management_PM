package bd.gov.govos.dto;

import bd.gov.govos.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public final class AuthDtos {
	private AuthDtos() {}

	public record LoginRequest(
			@NotBlank String username,
			@NotBlank String password,
			String mfaCode
	) {}

	public record MfaVerifyRequest(
			@NotBlank String mfaToken,
			@NotBlank String mfaCode
	) {}

	@Builder
	public record AuthResponse(
			String token,
			String username,
			String fullName,
			UserRole role,
			int roleLevel,
			Long ministryId,
			String ministryName,
			boolean mfaRequired,
			String mfaToken,
			boolean mfaEnabled,
			/** Demo only: current TOTP when MFA secret is known server-side */
			String demoMfaHint
	) {}
}
