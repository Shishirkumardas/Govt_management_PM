package bd.gov.govos.controller;

import bd.gov.govos.dto.AuthDtos;
import bd.gov.govos.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest request,
			HttpServletRequest httpRequest) {
		try {
			return ResponseEntity.ok(authService.login(request, httpRequest));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/mfa/verify")
	public ResponseEntity<?> verifyMfa(@Valid @RequestBody AuthDtos.MfaVerifyRequest request,
			HttpServletRequest httpRequest) {
		try {
			return ResponseEntity.ok(authService.verifyMfa(request, httpRequest));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
		}
	}
}
