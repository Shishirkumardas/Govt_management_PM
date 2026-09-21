package bd.gov.govos.controller;

import bd.gov.govos.dto.ReportDtos;
import bd.gov.govos.security.UserPrincipal;
import bd.gov.govos.service.DailyReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DailyReportController {

	private final DailyReportService dailyReportService;

	@PostMapping("/daily")
	public ResponseEntity<?> submit(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody ReportDtos.DailyReportRequest request) {
		return ResponseEntity.ok(dailyReportService.submit(principal, request));
	}

	@GetMapping("/mine")
	public ResponseEntity<?> mine(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(dailyReportService.myReports(principal.getId()));
	}

	@GetMapping("/by-date")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY','DEPARTMENT_OFFICER','DISTRICT_ADMIN')")
	public ResponseEntity<?> byDate(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ResponseEntity.ok(dailyReportService.reportsForDate(date));
	}

	@GetMapping("/summary")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<?> summary(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return ResponseEntity.ok(dailyReportService.summary(from, to));
	}
}
