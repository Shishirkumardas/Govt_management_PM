package bd.gov.govos.service;

import bd.gov.govos.domain.entity.AuditLog;
import bd.gov.govos.repository.AuditLogRepository;
import bd.gov.govos.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository auditLogRepository;

	@Transactional
	public void record(String action, String resource, String details, HttpServletRequest request, Integer status) {
		String username = null;
		String role = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
			username = principal.getUsername();
			role = principal.getRole().name();
		}

		String ip = request != null ? clientIp(request) : null;
		String method = request != null ? request.getMethod() : null;
		String path = request != null ? request.getRequestURI() : null;

		String payload = String.join("|",
				nullSafe(username), nullSafe(action), nullSafe(resource),
				nullSafe(details), nullSafe(ip), nullSafe(path), String.valueOf(System.currentTimeMillis()));

		AuditLog log = AuditLog.builder()
				.username(username)
				.role(role)
				.action(action)
				.resource(resource)
				.details(truncate(details, 500))
				.ipAddress(ip)
				.httpMethod(method)
				.path(truncate(path, 300))
				.statusCode(status)
				.integrityHash(sha256(payload))
				.build();
		auditLogRepository.save(log);
	}

	@Transactional
	public void recordSimple(String action, String resource, String details) {
		record(action, resource, details, null, null);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> recent(int limit) {
		return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, Math.min(limit, 200)))
				.stream()
				.map(a -> {
					Map<String, Object> m = new java.util.LinkedHashMap<>();
					m.put("id", a.getId());
					m.put("username", a.getUsername() != null ? a.getUsername() : "");
					m.put("role", a.getRole() != null ? a.getRole() : "");
					m.put("action", a.getAction());
					m.put("resource", a.getResource());
					m.put("details", a.getDetails() != null ? a.getDetails() : "");
					m.put("ipAddress", a.getIpAddress() != null ? a.getIpAddress() : "");
					m.put("path", a.getPath() != null ? a.getPath() : "");
					m.put("statusCode", a.getStatusCode() != null ? a.getStatusCode() : 0);
					m.put("integrityHash", a.getIntegrityHash() != null ? a.getIntegrityHash() : "");
					m.put("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : "");
					return m;
				})
				.toList();
	}

	private String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private String sha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (Exception e) {
			return null;
		}
	}

	private String nullSafe(String s) {
		return s == null ? "" : s;
	}

	private String truncate(String s, int max) {
		if (s == null) return null;
		return s.length() <= max ? s : s.substring(0, max);
	}
}
