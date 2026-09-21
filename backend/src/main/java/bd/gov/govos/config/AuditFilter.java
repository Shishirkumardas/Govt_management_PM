package bd.gov.govos.config;

import bd.gov.govos.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Records mutating API calls and sensitive reads into the append-only audit log.
 */
@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

	private final AuditService auditService;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		filterChain.doFilter(request, response);

		String path = request.getRequestURI();
		if (!path.startsWith("/api/")) {
			return;
		}
		// Skip noisy health/public chatter
		if (path.startsWith("/api/auth/") || path.startsWith("/actuator")) {
			return;
		}

		String method = request.getMethod();
		boolean interesting = !"GET".equalsIgnoreCase(method)
				|| path.contains("/dashboard/national")
				|| path.contains("/exports/")
				|| path.contains("/ai/");

		if (interesting && response.getStatus() < 500) {
			try {
				auditService.record(
						method + "_API",
						path,
						"status=" + response.getStatus(),
						request,
						response.getStatus());
			} catch (Exception ignored) {
				// never break the request for audit failures
			}
		}
	}
}
