package bd.gov.govos.controller;

import bd.gov.govos.service.AuditService;
import bd.gov.govos.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ReportExportController {

	private final ReportExportService reportExportService;
	private final AuditService auditService;

	@GetMapping("/national.pdf")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<byte[]> nationalPdf(
			@RequestParam(defaultValue = "monthly") String period) throws Exception {
		byte[] pdf = reportExportService.nationalPdf(period);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"govos-national-" + period + ".pdf\"")
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdf);
	}

	@GetMapping("/national.pptx")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','CABINET_MINISTER','SECRETARY')")
	public ResponseEntity<byte[]> nationalPpt(
			@RequestParam(defaultValue = "monthly") String period) throws Exception {
		byte[] ppt = reportExportService.nationalPpt(period);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"govos-national-" + period + ".pptx\"")
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.presentationml.presentation"))
				.body(ppt);
	}

	@GetMapping("/audit-logs")
	@PreAuthorize("hasAnyRole('PRIME_MINISTER','SECRETARY')")
	public ResponseEntity<?> auditLogs(@RequestParam(defaultValue = "50") int limit) {
		return ResponseEntity.ok(auditService.recent(limit));
	}
}
