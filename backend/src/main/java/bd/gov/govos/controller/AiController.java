package bd.gov.govos.controller;

import bd.gov.govos.service.AiDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

	private final AiDecisionService aiDecisionService;

	@GetMapping("/briefing/national")
	@PreAuthorize("hasRole('PRIME_MINISTER')")
	public ResponseEntity<?> nationalBriefing() {
		return ResponseEntity.ok(aiDecisionService.nationalBriefing());
	}

	@GetMapping("/briefing/ministry/{id}")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> ministryBriefing(@PathVariable Long id) {
		return ResponseEntity.ok(aiDecisionService.ministryBriefing(id));
	}

	@GetMapping("/recommendations")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> recommendations() {
		return ResponseEntity.ok(aiDecisionService.listRecommendations());
	}

	@PostMapping("/recommendations/{id}/acknowledge")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER')")
	public ResponseEntity<?> acknowledge(@PathVariable Long id) {
		aiDecisionService.acknowledge(id);
		return ResponseEntity.ok(java.util.Map.of("status", "acknowledged"));
	}
}
