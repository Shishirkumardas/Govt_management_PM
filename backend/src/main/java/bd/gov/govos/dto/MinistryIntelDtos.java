package bd.gov.govos.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class MinistryIntelDtos {
	private MinistryIntelDtos() {}

	@Builder
	public record OrgItem(
			Long id,
			String orgCode,
			String nameEn,
			String nameBn,
			String orgType,
			String headName,
			String headDesignation,
			BigDecimal budgetCrore,
			BigDecimal spentCrore,
			BigDecimal utilizationPercent,
			Integer sanctionedPosts,
			Integer filledPosts,
			Integer vacancy,
			Integer anomalyHeadcount,
			BigDecimal transparencyScore,
			BigDecimal efficiencyScore,
			BigDecimal corruptionRiskScore,
			String district,
			String mandate
	) {}

	@Builder
	public record BudgetLineItem(
			Long id,
			String category,
			String titleEn,
			String titleBn,
			String organizationName,
			String projectCode,
			BigDecimal allocatedCrore,
			BigDecimal releasedCrore,
			BigDecimal spentCrore,
			BigDecimal remainingCrore,
			BigDecimal utilizationPercent,
			String fiscalYear,
			boolean flagged,
			String flagReason
	) {}

	@Builder
	public record WorkItem(
			Long id,
			String workCode,
			String titleEn,
			String titleBn,
			String organizationName,
			String status,
			BigDecimal budgetCrore,
			BigDecimal spentCrore,
			BigDecimal progressPercent,
			String location,
			Integer beneficiaries,
			LocalDate startDate,
			LocalDate endDate,
			String latestUpdate,
			boolean transparencyIssue,
			String issueNote
	) {}

	@Builder
	public record AntiCorruptionScorecard(
			int overallRiskScore,
			String overallRiskLevel,
			long flaggedProcurements,
			long openRiskFlags,
			long ghostEmployeeSignals,
			long highRiskOrganizations,
			long flaggedBudgetLines,
			long delayedProjects,
			long worksWithTransparencyIssues,
			long delayedFieldReports,
			BigDecimal unusualSpendCrore,
			List<String> redFlags,
			List<String> recommendedActions
	) {}

	@Builder
	public record HeadcountSummary(
			int sanctionedPosts,
			int filledPosts,
			int vacancy,
			int anomalyHeadcount,
			BigDecimal fillRatePercent
	) {}

	@Builder
	public record MinistryIntelligenceReport(
			String generatedAt,
			DashboardDtos.MinistryOverview ministry,
			HeadcountSummary headcount,
			AntiCorruptionScorecard antiCorruption,
			List<OrgItem> organizations,
			List<BudgetLineItem> budgetLines,
			Map<String, BigDecimal> budgetByCategoryAllocated,
			Map<String, BigDecimal> budgetByCategorySpent,
			List<DashboardDtos.ProjectItem> projects,
			List<WorkItem> workPrograms,
			List<Map<String, Object>> procurement,
			List<Map<String, Object>> risks,
			List<Map<String, Object>> kpis,
			List<Map<String, Object>> employees,
			List<Map<String, Object>> officeScores,
			List<ReportDtos.DailyReportResponse> recentReports,
			List<DashboardDtos.AlertItem> alerts,
			List<DashboardDtos.AiRecommendationItem> aiRecommendations,
			List<Map<String, Object>> documents
	) {}
}
