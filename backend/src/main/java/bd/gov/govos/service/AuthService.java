package bd.gov.govos.service;

import bd.gov.govos.domain.entity.UserAccount;
import bd.gov.govos.dto.AuthDtos;
import bd.gov.govos.repository.UserAccountRepository;
import bd.gov.govos.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserAccountRepository userAccountRepository;
	private final JwtService jwtService;
	private final MfaService mfaService;
	private final AuditService auditService;

	/** Short-lived MFA challenge tokens (username keyed) */
	private final Map<String, MfaChallenge> mfaChallenges = new ConcurrentHashMap<>();

	private record MfaChallenge(String username, long expiresAt) {}

	@Transactional(readOnly = true)
	public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request, HttpServletRequest httpRequest) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));
		} catch (AuthenticationException ex) {
			auditService.record("LOGIN_FAILED", "AUTH", "Invalid credentials for " + request.username(),
					httpRequest, 401);
			throw new IllegalArgumentException("Invalid username or password");
		}

		UserAccount user = userAccountRepository.findByUsername(request.username())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		if (Boolean.TRUE.equals(user.getMfaEnabled()) && user.getMfaSecret() != null) {
			if (request.mfaCode() == null || request.mfaCode().isBlank()) {
				String mfaToken = java.util.UUID.randomUUID().toString();
				mfaChallenges.put(mfaToken, new MfaChallenge(user.getUsername(),
						System.currentTimeMillis() + 5 * 60_000));
				auditService.record("LOGIN_MFA_CHALLENGE", "AUTH", user.getUsername(), httpRequest, 200);
				return AuthDtos.AuthResponse.builder()
						.mfaRequired(true)
						.mfaToken(mfaToken)
						.mfaEnabled(true)
						.username(user.getUsername())
						.fullName(user.getFullName())
						.role(user.getRole())
						.roleLevel(user.getRole().getLevel())
						.demoMfaHint(mfaService.currentCode(user.getMfaSecret()))
						.build();
			}
			if (!mfaService.verify(user.getMfaSecret(), request.mfaCode())) {
				auditService.record("LOGIN_MFA_FAILED", "AUTH", user.getUsername(), httpRequest, 401);
				throw new IllegalArgumentException("Invalid MFA code");
			}
		}

		return issueSession(user, httpRequest);
	}

	@Transactional(readOnly = true)
	public AuthDtos.AuthResponse verifyMfa(AuthDtos.MfaVerifyRequest request, HttpServletRequest httpRequest) {
		MfaChallenge challenge = mfaChallenges.get(request.mfaToken());
		if (challenge == null || challenge.expiresAt() < System.currentTimeMillis()) {
			throw new IllegalArgumentException("MFA challenge expired — login again");
		}
		UserAccount user = userAccountRepository.findByUsername(challenge.username())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (!mfaService.verify(user.getMfaSecret(), request.mfaCode())) {
			auditService.record("LOGIN_MFA_FAILED", "AUTH", user.getUsername(), httpRequest, 401);
			throw new IllegalArgumentException("Invalid MFA code");
		}
		mfaChallenges.remove(request.mfaToken());
		return issueSession(user, httpRequest);
	}

	private AuthDtos.AuthResponse issueSession(UserAccount user, HttpServletRequest httpRequest) {
		String token = jwtService.generateToken(user);
		auditService.record("LOGIN_SUCCESS", "AUTH", user.getUsername() + " / " + user.getRole(),
				httpRequest, 200);
		return AuthDtos.AuthResponse.builder()
				.token(token)
				.username(user.getUsername())
				.fullName(user.getFullName())
				.role(user.getRole())
				.roleLevel(user.getRole().getLevel())
				.ministryId(user.getMinistry() != null ? user.getMinistry().getId() : null)
				.ministryName(user.getMinistry() != null ? user.getMinistry().getNameEn() : null)
				.mfaRequired(false)
				.mfaEnabled(Boolean.TRUE.equals(user.getMfaEnabled()))
				.build();
	}
}
