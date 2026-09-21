package bd.gov.govos.dto;

import bd.gov.govos.domain.enums.AlertSeverity;
import bd.gov.govos.domain.enums.ProjectStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class DashboardDtos {
	private DashboardDtos() {}

	@Builder
	public record IndicatorItem(
			String key,
			String label,
			String category,
			BigDecimal value,
			String unit,
			BigDecimal changePercent,
			LocalDate asOfDate
	) {}

	@Builder
	public record ProjectItem(
			Long id,
			String projectCode,
			String title,
			String ministryName,
			ProjectStatus status,
			BigDecimal totalCostCrore,
			BigDecimal spentCostCrore,
			BigDecimal remainingCostCrore,
			BigDecimal progressPercent,
			String location,
			String latestUpdate,
			LocalDate expectedEndDate,
			String contractor,
			Integer delayDays,
			String riskLevel,
			String riskReason,
			String aiRecommendation
	) {}

	@Builder
	public record MinistryOverview(
			Long id,
			String code,
			String nameEn,
			String nameBn,
			BigDecimal annualBudgetCrore,
			BigDecimal spentBudgetCrore,
			BigDecimal utilizationPercent,
			long activeProjects,
			long reportsToday
	) {}

	@Builder
	public record AlertItem(
			Long id,
			String title,
			String message,
			AlertSeverity severity,
			String category,
			String location,
			String ministryName
	) {}

	@Builder
	public record ForeignInvestmentItem(
			Long id,
			String investorName,
			String country,
			String sector,
			BigDecimal amountUsdMillion,
			String status,
			LocalDate agreementDate
	) {}

	@Builder
	public record AiRecommendationItem(
			Long id,
			String title,
			String body,
			String scope,
			String priority
	) {}

	@Builder
	public record NationalDashboard(
			String generatedAt,
			List<IndicatorItem> indicators,
			Map<String, List<IndicatorItem>> indicatorsByCategory,
			List<ProjectItem> megaProjects,
			List<MinistryOverview> ministries,
			List<AlertItem> activeAlerts,
			List<ForeignInvestmentItem> foreignInvestments,
			List<AiRecommendationItem> aiRecommendations,
			BudgetSnapshot budget,
			DailyOpsSnapshot dailyOps
	) {}

	@Builder
	public record BudgetSnapshot(
			BigDecimal totalRevenueBdt,
			BigDecimal totalExpenditureBdt,
			BigDecimal deficitBdt,
			BigDecimal totalMinistryBudgetCrore,
			BigDecimal totalMinistrySpentCrore,
			BigDecimal foreignInvestmentUsdMillion
	) {}

	@Builder
	public record DailyOpsSnapshot(
			long reportsSubmittedToday,
			BigDecimal expensesTodayBdt,
			long activeAlerts,
			long ongoingProjects,
			long delayedProjects
	) {}

	@Builder
	public record MinistryDashboard(
			MinistryOverview ministry,
			List<ProjectItem> projects,
			List<AlertItem> alerts,
			List<ReportDtos.DailyReportResponse> recentReports,
			List<AiRecommendationItem> aiRecommendations,
			Map<String, BigDecimal> budgetByType
	) {}
}
