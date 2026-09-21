package bd.gov.govos.dto;

import bd.gov.govos.domain.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ReportDtos {
	private ReportDtos() {}

	public record DailyReportRequest(
			@NotNull LocalDate reportDate,
			String workCompleted,
			String visits,
			String approvals,
			BigDecimal expensesBdt,
			String problems,
			String recommendations,
			Double latitude,
			Double longitude,
			String photoUrls,
			String documentUrls,
			Integer inspectionsCount,
			Integer schoolsVisited,
			Integer projectsApproved
	) {}

	@Builder
	public record DailyReportResponse(
			Long id,
			Long userId,
			String userName,
			String designation,
			Long ministryId,
			String ministryName,
			LocalDate reportDate,
			String workCompleted,
			String visits,
			String approvals,
			BigDecimal expensesBdt,
			String problems,
			String recommendations,
			Double latitude,
			Double longitude,
			Integer inspectionsCount,
			Integer schoolsVisited,
			Integer projectsApproved,
			ReportStatus status
	) {}

	@Builder
	public record ReportSummary(
			LocalDate from,
			LocalDate to,
			long totalReports,
			BigDecimal totalExpensesBdt,
			long flaggedProblems,
			List<DailyReportResponse> recent
	) {}
}
