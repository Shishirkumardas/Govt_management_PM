package bd.gov.govos.controller;

import bd.gov.govos.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

	@GetMapping
	public ResponseEntity<?> me(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(Map.of(
				"id", principal.getId(),
				"username", principal.getUsername(),
				"fullName", principal.getFullName(),
				"role", principal.getRole().name(),
				"roleLevel", principal.getRole().getLevel(),
				"roleDescription", principal.getRole().getDescription(),
				"ministryId", principal.getMinistryId() != null ? principal.getMinistryId() : 0
		));
	}
}
