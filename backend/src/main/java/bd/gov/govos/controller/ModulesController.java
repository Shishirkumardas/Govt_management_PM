package bd.gov.govos.controller;

import bd.gov.govos.service.AiChatService;
import bd.gov.govos.service.ExtendedModulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ModulesController {

	private final ExtendedModulesService modules;
	private final AiChatService aiChatService;

	@PostMapping("/ai/ask")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> ask(@RequestBody Map<String, String> body) {
		return ResponseEntity.ok(aiChatService.ask(body.getOrDefault("question", "")));
	}

	@GetMapping("/budget/overview")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> budget() {
		return ResponseEntity.ok(modules.budgetOverview());
	}

	@GetMapping("/procurement")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY','DEPARTMENT_OFFICER')")
	public ResponseEntity<?> procurement(@RequestParam(defaultValue = "false") boolean flaggedOnly) {
		return ResponseEntity.ok(modules.procurement(flaggedOnly));
	}

	@GetMapping("/kpis")
	public ResponseEntity<?> kpis(@RequestParam(required = false) Long ministryId) {
		return ResponseEntity.ok(modules.kpis(ministryId));
	}

	@GetMapping("/risks")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> risks() {
		return ResponseEntity.ok(modules.risks());
	}

	@GetMapping("/notifications")
	public ResponseEntity<?> notifications() {
		return ResponseEntity.ok(modules.notifications());
	}

	@PostMapping("/notifications/{id}/read")
	public ResponseEntity<?> readNotification(@PathVariable Long id) {
		modules.markNotificationRead(id);
		return ResponseEntity.ok(Map.of("status", "read"));
	}

	@GetMapping("/gis/districts")
	public ResponseEntity<?> districts() {
		return ResponseEntity.ok(modules.districts());
	}

	@GetMapping("/gis/districts/{name}")
	public ResponseEntity<?> district(@PathVariable String name) {
		return ResponseEntity.ok(modules.district(name));
	}

	@GetMapping("/documents")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY','DEPARTMENT_OFFICER')")
	public ResponseEntity<?> documents(@RequestParam(required = false) String q) {
		return ResponseEntity.ok(modules.documents(q));
	}

	@GetMapping("/hr/employees")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> employees() {
		return ResponseEntity.ok(modules.employees());
	}

	@GetMapping("/hr/office-scores")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> officeScores() {
		return ResponseEntity.ok(modules.officeScores());
	}

	@GetMapping("/citizen/complaints")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY','DISTRICT_ADMIN')")
	public ResponseEntity<?> complaints() {
		return ResponseEntity.ok(modules.complaints());
	}

	/** Public citizen portal endpoints (still JWT-free via security config) */
	@PostMapping("/public/complaints")
	public ResponseEntity<?> submitComplaint(@RequestBody Map<String, String> body) {
		return ResponseEntity.ok(modules.submitComplaint(body));
	}

	@GetMapping("/public/complaints/{trackingId}")
	public ResponseEntity<?> trackComplaint(@PathVariable String trackingId) {
		return ResponseEntity.ok(modules.trackComplaint(trackingId));
	}
}
