package bd.gov.govos.controller;

import bd.gov.govos.security.UserPrincipal;
import bd.gov.govos.service.DashboardService;
import bd.gov.govos.service.MinistryIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;
	private final MinistryIntelligenceService ministryIntelligenceService;

	@GetMapping("/national")
	@PreAuthorize("hasRole('PRIME_MINISTER')")
	public ResponseEntity<?> national() {
		return ResponseEntity.ok(dashboardService.nationalDashboard());
	}

	@GetMapping("/ministry/{id}")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY','DEPARTMENT_OFFICER')")
	public ResponseEntity<?> ministry(
			@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal principal) {
		if (!principal.getRole().canViewNational()
				&& (principal.getMinistryId() == null || !principal.getMinistryId().equals(id))) {
			return ResponseEntity.status(403).body(java.util.Map.of("error", "Access denied for this ministry"));
		}
		return ResponseEntity.ok(dashboardService.ministryDashboard(id));
	}

	@GetMapping("/ministry/mine")
	@PreAuthorize("hasAnyRole('CABINET_MINISTER','SECRETARY','DEPARTMENT_OFFICER','DISTRICT_ADMIN')")
	public ResponseEntity<?> myMinistry(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal.getMinistryId() == null) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", "No ministry assigned"));
		}
		return ResponseEntity.ok(dashboardService.ministryDashboard(principal.getMinistryId()));
	}

	/**
	 * Full PM-grade ministry intelligence: orgs, budget lines, works, procurement,
	 * headcount anomalies and anti-corruption scorecard.
	 */
	@GetMapping("/ministry/{id}/intelligence")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> ministryIntelligence(
			@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal principal) {
		if (!principal.getRole().canViewNational()
				&& (principal.getMinistryId() == null || !principal.getMinistryId().equals(id))) {
			return ResponseEntity.status(403).body(java.util.Map.of("error", "Access denied for this ministry"));
		}
		return ResponseEntity.ok(ministryIntelligenceService.fullReport(id));
	}

	@GetMapping("/ministry/mine/intelligence")
	@PreAuthorize("hasAnyRole('CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> myMinistryIntelligence(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal.getMinistryId() == null) {
			return ResponseEntity.badRequest().body(java.util.Map.of("error", "No ministry assigned"));
		}
		return ResponseEntity.ok(ministryIntelligenceService.fullReport(principal.getMinistryId()));
	}

	@GetMapping("/projects")
	public ResponseEntity<?> projects() {
		return ResponseEntity.ok(dashboardService.allProjects());
	}

	@GetMapping("/ministries")
	public ResponseEntity<?> ministries() {
		return ResponseEntity.ok(dashboardService.allMinistries());
	}
}
