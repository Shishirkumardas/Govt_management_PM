package bd.gov.govos.service;

import bd.gov.govos.domain.entity.BudgetLine;
import bd.gov.govos.domain.entity.GovOrganization;
import bd.gov.govos.domain.entity.GovProject;
import bd.gov.govos.domain.entity.Ministry;
import bd.gov.govos.domain.entity.WorkProgram;
import bd.gov.govos.domain.enums.ProjectStatus;
import bd.gov.govos.dto.DashboardDtos;
import bd.gov.govos.dto.MinistryIntelDtos;
import bd.gov.govos.dto.ReportDtos;
import bd.gov.govos.repository.BudgetLineRepository;
import bd.gov.govos.repository.DailyReportRepository;
import bd.gov.govos.repository.EmployeeProfileRepository;
import bd.gov.govos.repository.GovOrganizationRepository;
import bd.gov.govos.repository.GovProjectRepository;
import bd.gov.govos.repository.MinistryKpiRepository;
import bd.gov.govos.repository.MinistryRepository;
import bd.gov.govos.repository.NationalAlertRepository;
import bd.gov.govos.repository.OfficeScoreRepository;
import bd.gov.govos.repository.ProcurementOrderRepository;
import bd.gov.govos.repository.RiskFlagRepository;
import bd.gov.govos.repository.WorkProgramRepository;
import bd.gov.govos.repository.AiRecommendationRepository;
import bd.gov.govos.repository.GovDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MinistryIntelligenceService {

	private final MinistryRepository ministryRepository;
	private final GovOrganizationRepository orgRepository;
	private final BudgetLineRepository budgetLineRepository;
	private final WorkProgramRepository workProgramRepository;
	private final GovProjectRepository projectRepository;
	private final ProcurementOrderRepository procurementRepository;
	private final RiskFlagRepository riskFlagRepository;
	private final MinistryKpiRepository kpiRepository;
	private final EmployeeProfileRepository employeeRepository;
	private final OfficeScoreRepository officeScoreRepository;
	private final DailyReportRepository reportRepository;
	private final NationalAlertRepository alertRepository;
	private final AiRecommendationRepository aiRepository;
	private final GovDocumentRepository documentRepository;
	private final DashboardService dashboardService;

	@Transactional(readOnly = true)
	public MinistryIntelDtos.MinistryIntelligenceReport fullReport(Long ministryId) {
		Ministry ministry = ministryRepository.findById(ministryId)
				.orElseThrow(() -> new IllegalArgumentException("Ministry not found"));
		LocalDate today = LocalDate.now();
		LocalDate from = today.minusDays(30);

		DashboardDtos.MinistryOverview overview = dashboardService.allMinistries().stream()
				.filter(m -> m.id().equals(ministryId))
				.findFirst()
				.orElseGet(() -> DashboardDtos.MinistryOverview.builder()
						.id(ministry.getId())
						.code(ministry.getCode().name())
						.nameEn(ministry.getNameEn())
						.nameBn(ministry.getNameBn())
						.annualBudgetCrore(ministry.getAnnualBudgetCrore())
						.spentBudgetCrore(ministry.getSpentBudgetCrore())
						.utilizationPercent(util(ministry.getSpentBudgetCrore(), ministry.getAnnualBudgetCrore()))
						.build());

		List<GovOrganization> orgs = orgRepository.findByMinistryIdOrderByCorruptionRiskScoreDesc(ministryId);
		List<BudgetLine> lines = budgetLineRepository.findByMinistryIdOrderByAllocatedCroreDesc(ministryId);
		List<WorkProgram> works = workProgramRepository.findByMinistryIdOrderByProgressPercentAsc(ministryId);
		List<GovProject> projects = projectRepository.findByMinistryId(ministryId);

		List<MinistryIntelDtos.OrgItem> orgItems = orgs.stream().map(this::toOrg).toList();
		List<MinistryIntelDtos.BudgetLineItem> lineItems = lines.stream().map(this::toLine).toList();
		List<MinistryIntelDtos.WorkItem> workItems = works.stream().map(this::toWork).toList();

		Map<String, BigDecimal> allocByCat = new LinkedHashMap<>();
		Map<String, BigDecimal> spentByCat = new LinkedHashMap<>();
		for (BudgetLine l : lines) {
			allocByCat.merge(l.getCategory(), nvl(l.getAllocatedCrore()), BigDecimal::add);
			spentByCat.merge(l.getCategory(), nvl(l.getSpentCrore()), BigDecimal::add);
		}

		int sanctioned = orgs.stream().mapToInt(o -> o.getSanctionedPosts() != null ? o.getSanctionedPosts() : 0).sum();
		int filled = orgs.stream().mapToInt(o -> o.getFilledPosts() != null ? o.getFilledPosts() : 0).sum();
		int anomaly = orgs.stream().mapToInt(o -> o.getAnomalyHeadcount() != null ? o.getAnomalyHeadcount() : 0).sum();

		MinistryIntelDtos.HeadcountSummary headcount = MinistryIntelDtos.HeadcountSummary.builder()
				.sanctionedPosts(sanctioned)
				.filledPosts(filled)
				.vacancy(Math.max(0, sanctioned - filled))
				.anomalyHeadcount(anomaly)
				.fillRatePercent(sanctioned == 0 ? BigDecimal.ZERO :
						BigDecimal.valueOf(filled * 100.0 / sanctioned).setScale(1, RoundingMode.HALF_UP))
				.build();

		var procurements = procurementRepository.findByMinistryIdOrderByOrderDateDesc(ministryId);
		long flaggedProc = procurements.stream().filter(p -> Boolean.TRUE.equals(p.getFlagged())).count();
		var risks = riskFlagRepository.findByOpenTrueOrderByCreatedAtDesc().stream()
				.filter(r -> r.getMinistry() != null && ministryId.equals(r.getMinistry().getId()))
				.toList();
		long delayedProjects = projects.stream()
				.filter(p -> p.getStatus() == ProjectStatus.DELAYED
						|| (p.getDelayDays() != null && p.getDelayDays() > 0))
				.count();
		long highRiskOrgs = orgs.stream()
				.filter(o -> o.getCorruptionRiskScore() != null && o.getCorruptionRiskScore().compareTo(new BigDecimal("60")) >= 0)
				.count();
		long flaggedLines = lines.stream().filter(l -> Boolean.TRUE.equals(l.getFlagged())).count();
		long workIssues = works.stream().filter(w -> Boolean.TRUE.equals(w.getTransparencyIssue())).count();

		List<String> redFlags = new ArrayList<>();
		List<String> actions = new ArrayList<>();
		if (flaggedProc > 0) {
			redFlags.add(flaggedProc + " procurement order(s) flagged (price inflation / fake vendor / conflict / duplicate invoice)");
			actions.add("Freeze flagged payments; open internal audit + CAG referral for high-value cases");
		}
		if (anomaly > 0) {
			redFlags.add(anomaly + " headcount anomaly signal(s) (possible ghost employees / biometric mismatch)");
			actions.add("Physical verification + payroll freeze for unmatched IDs within 7 days");
		}
		if (highRiskOrgs > 0) {
			redFlags.add(highRiskOrgs + " organization(s) with corruption risk score ≥ 60");
			actions.add("Secretary-level integrity review of high-risk directorates this week");
		}
		if (flaggedLines > 0) {
			redFlags.add(flaggedLines + " budget line(s) with unusual spend pattern");
			actions.add("Require voucher-level justification before further releases");
		}
		if (delayedProjects > 0) {
			redFlags.add(delayedProjects + " delayed project(s) — cost overrun risk");
			actions.add("Re-baseline delayed packages; publish contractor performance score");
		}
		if (workIssues > 0) {
			redFlags.add(workIssues + " work program(s) with transparency issues");
			actions.add("Field inspection + beneficiary cross-check for flagged schemes");
		}
		if (redFlags.isEmpty()) {
			redFlags.add("No critical red flags in current dataset — continue routine monitoring");
			actions.add("Maintain daily reporting discipline and procurement AI screening");
		}

		int riskScore = (int) Math.min(100,
				flaggedProc * 12 + anomaly * 3 + highRiskOrgs * 15 + flaggedLines * 10
						+ delayedProjects * 8 + workIssues * 8 + risks.size() * 6);
		String riskLevel = riskScore >= 70 ? "CRITICAL" : riskScore >= 45 ? "HIGH" : riskScore >= 25 ? "MEDIUM" : "LOW";

		BigDecimal unusualSpend = lines.stream()
				.filter(l -> Boolean.TRUE.equals(l.getFlagged()))
				.map(l -> nvl(l.getSpentCrore()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		MinistryIntelDtos.AntiCorruptionScorecard scorecard = MinistryIntelDtos.AntiCorruptionScorecard.builder()
				.overallRiskScore(riskScore)
				.overallRiskLevel(riskLevel)
				.flaggedProcurements(flaggedProc)
				.openRiskFlags(risks.size())
				.ghostEmployeeSignals(anomaly)
				.highRiskOrganizations(highRiskOrgs)
				.flaggedBudgetLines(flaggedLines)
				.delayedProjects(delayedProjects)
				.worksWithTransparencyIssues(workIssues)
				.delayedFieldReports(0)
				.unusualSpendCrore(unusualSpend)
				.redFlags(redFlags)
				.recommendedActions(actions)
				.build();

		List<Map<String, Object>> procMaps = procurements.stream().map(p -> {
			Map<String, Object> m = new HashMap<>();
			m.put("orderCode", p.getOrderCode());
			m.put("itemDescription", p.getItemDescription());
			m.put("vendorName", p.getVendorName());
			m.put("amountBdt", p.getAmountBdt());
			m.put("orderDate", p.getOrderDate());
			m.put("status", p.getStatus());
			m.put("flagged", p.getFlagged());
			m.put("riskFlags", p.getRiskFlags());
			m.put("riskNotes", p.getRiskNotes());
			return m;
		}).toList();

		List<Map<String, Object>> riskMaps = risks.stream().map(r -> {
			Map<String, Object> m = new HashMap<>();
			m.put("riskType", r.getRiskType());
			m.put("title", r.getTitle());
			m.put("description", r.getDescription());
			m.put("severity", r.getSeverity().name());
			m.put("recommendedAction", r.getRecommendedAction());
			m.put("location", r.getLocation());
			return m;
		}).toList();

		List<Map<String, Object>> kpis = kpiRepository.findByMinistryIdOrderByLabelAsc(ministryId).stream().map(k -> {
			Map<String, Object> m = new HashMap<>();
			m.put("label", k.getLabel());
			m.put("value", k.getValue());
			m.put("target", k.getTarget());
			m.put("unit", k.getUnit());
			return m;
		}).toList();

		List<Map<String, Object>> employees = employeeRepository.findByMinistryId(ministryId).stream().map(e -> {
			Map<String, Object> m = new HashMap<>();
			m.put("employeeId", e.getEmployeeId());
			m.put("fullName", e.getUser().getFullName());
			m.put("designation", e.getUser().getDesignation());
			m.put("grade", e.getGrade());
			m.put("attendancePercent", e.getAttendancePercent());
			m.put("performanceScore", e.getPerformanceScore());
			m.put("monthlySalaryBdt", e.getMonthlySalaryBdt());
			return m;
		}).toList();

		List<Map<String, Object>> offices = officeScoreRepository.findAllByOrderByOverallScoreDesc().stream()
				.filter(o -> o.getMinistry() != null && ministryId.equals(o.getMinistry().getId()))
				.map(o -> {
					Map<String, Object> m = new HashMap<>();
					m.put("officeName", o.getOfficeName());
					m.put("district", o.getDistrict());
					m.put("efficiencyScore", o.getEfficiencyScore());
					m.put("transparencyScore", o.getTransparencyScore());
					m.put("budgetScore", o.getBudgetScore());
					m.put("overallScore", o.getOverallScore());
					return m;
				}).toList();

		List<ReportDtos.DailyReportResponse> reports = reportRepository
				.findByMinistryIdAndReportDateBetween(ministryId, from, today).stream()
				.sorted((a, b) -> b.getReportDate().compareTo(a.getReportDate()))
				.limit(20)
				.map(r -> ReportDtos.DailyReportResponse.builder()
						.id(r.getId())
						.userId(r.getUser().getId())
						.userName(r.getUser().getFullName())
						.designation(r.getUser().getDesignation())
						.ministryId(ministryId)
						.ministryName(ministry.getNameEn())
						.reportDate(r.getReportDate())
						.workCompleted(r.getWorkCompleted())
						.visits(r.getVisits())
						.approvals(r.getApprovals())
						.expensesBdt(r.getExpensesBdt())
						.problems(r.getProblems())
						.recommendations(r.getRecommendations())
						.inspectionsCount(r.getInspectionsCount())
						.schoolsVisited(r.getSchoolsVisited())
						.projectsApproved(r.getProjectsApproved())
						.status(r.getStatus())
						.build())
				.toList();

		List<DashboardDtos.AlertItem> alerts = alertRepository
				.findByMinistryIdAndActiveTrueOrderByCreatedAtDesc(ministryId).stream()
				.map(a -> DashboardDtos.AlertItem.builder()
						.id(a.getId())
						.title(a.getTitle())
						.message(a.getMessage())
						.severity(a.getSeverity())
						.category(a.getCategory())
						.location(a.getLocation())
						.ministryName(ministry.getNameEn())
						.build())
				.toList();

		List<DashboardDtos.AiRecommendationItem> ai = aiRepository.findAll().stream()
				.filter(a -> a.getMinistry() != null && ministryId.equals(a.getMinistry().getId()))
				.map(a -> DashboardDtos.AiRecommendationItem.builder()
						.id(a.getId()).title(a.getTitle()).body(a.getBody())
						.scope(a.getScope()).priority(a.getPriority()).build())
				.toList();

		List<Map<String, Object>> docs = documentRepository.findAll().stream()
				.filter(d -> d.getMinistry() != null && ministryId.equals(d.getMinistry().getId()))
				.map(d -> {
					Map<String, Object> m = new HashMap<>();
					m.put("title", d.getTitle());
					m.put("docType", d.getDocType());
					m.put("classification", d.getClassification());
					m.put("issuedDate", d.getIssuedDate());
					m.put("summary", d.getSummary());
					return m;
				}).toList();

		List<DashboardDtos.ProjectItem> projectItems = projects.stream().map(p -> {
			BigDecimal total = nvl(p.getTotalCostCrore());
			BigDecimal spent = nvl(p.getSpentCostCrore());
			return DashboardDtos.ProjectItem.builder()
					.id(p.getId())
					.projectCode(p.getProjectCode())
					.title(p.getTitle())
					.ministryName(ministry.getNameEn())
					.status(p.getStatus())
					.totalCostCrore(total)
					.spentCostCrore(spent)
					.remainingCostCrore(total.subtract(spent))
					.progressPercent(p.getProgressPercent())
					.location(p.getLocation())
					.latestUpdate(p.getLatestUpdate())
					.expectedEndDate(p.getExpectedEndDate())
					.contractor(p.getContractor())
					.delayDays(p.getDelayDays())
					.riskLevel(p.getRiskLevel())
					.riskReason(p.getRiskReason())
					.aiRecommendation(p.getAiRecommendation())
					.build();
		}).toList();

		return MinistryIntelDtos.MinistryIntelligenceReport.builder()
				.generatedAt(Instant.now().toString())
				.ministry(overview)
				.headcount(headcount)
				.antiCorruption(scorecard)
				.organizations(orgItems)
				.budgetLines(lineItems)
				.budgetByCategoryAllocated(allocByCat)
				.budgetByCategorySpent(spentByCat)
				.projects(projectItems)
				.workPrograms(workItems)
				.procurement(procMaps)
				.risks(riskMaps)
				.kpis(kpis)
				.employees(employees)
				.officeScores(offices)
				.recentReports(reports)
				.alerts(alerts)
				.aiRecommendations(ai)
				.documents(docs)
				.build();
	}

	private MinistryIntelDtos.OrgItem toOrg(GovOrganization o) {
		int sanc = o.getSanctionedPosts() != null ? o.getSanctionedPosts() : 0;
		int fill = o.getFilledPosts() != null ? o.getFilledPosts() : 0;
		return MinistryIntelDtos.OrgItem.builder()
				.id(o.getId())
				.orgCode(o.getOrgCode())
				.nameEn(o.getNameEn())
				.nameBn(o.getNameBn())
				.orgType(o.getOrgType())
				.headName(o.getHeadName())
				.headDesignation(o.getHeadDesignation())
				.budgetCrore(o.getBudgetCrore())
				.spentCrore(o.getSpentCrore())
				.utilizationPercent(util(o.getSpentCrore(), o.getBudgetCrore()))
				.sanctionedPosts(sanc)
				.filledPosts(fill)
				.vacancy(Math.max(0, sanc - fill))
				.anomalyHeadcount(o.getAnomalyHeadcount())
				.transparencyScore(o.getTransparencyScore())
				.efficiencyScore(o.getEfficiencyScore())
				.corruptionRiskScore(o.getCorruptionRiskScore())
				.district(o.getDistrict())
				.mandate(o.getMandate())
				.build();
	}

	private MinistryIntelDtos.BudgetLineItem toLine(BudgetLine l) {
		BigDecimal alloc = nvl(l.getAllocatedCrore());
		BigDecimal spent = nvl(l.getSpentCrore());
		return MinistryIntelDtos.BudgetLineItem.builder()
				.id(l.getId())
				.category(l.getCategory())
				.titleEn(l.getTitleEn())
				.titleBn(l.getTitleBn())
				.organizationName(l.getOrganization() != null ? l.getOrganization().getNameEn() : null)
				.projectCode(l.getProject() != null ? l.getProject().getProjectCode() : null)
				.allocatedCrore(alloc)
				.releasedCrore(l.getReleasedCrore())
				.spentCrore(spent)
				.remainingCrore(alloc.subtract(spent))
				.utilizationPercent(util(spent, alloc))
				.fiscalYear(l.getFiscalYear())
				.flagged(Boolean.TRUE.equals(l.getFlagged()))
				.flagReason(l.getFlagReason())
				.build();
	}

	private MinistryIntelDtos.WorkItem toWork(WorkProgram w) {
		return MinistryIntelDtos.WorkItem.builder()
				.id(w.getId())
				.workCode(w.getWorkCode())
				.titleEn(w.getTitleEn())
				.titleBn(w.getTitleBn())
				.organizationName(w.getOrganization() != null ? w.getOrganization().getNameEn() : null)
				.status(w.getStatus())
				.budgetCrore(w.getBudgetCrore())
				.spentCrore(w.getSpentCrore())
				.progressPercent(w.getProgressPercent())
				.location(w.getLocation())
				.beneficiaries(w.getBeneficiaries())
				.startDate(w.getStartDate())
				.endDate(w.getEndDate())
				.latestUpdate(w.getLatestUpdate())
				.transparencyIssue(Boolean.TRUE.equals(w.getTransparencyIssue()))
				.issueNote(w.getIssueNote())
				.build();
	}

	private BigDecimal nvl(BigDecimal v) {
		return v != null ? v : BigDecimal.ZERO;
	}

	private BigDecimal util(BigDecimal spent, BigDecimal budget) {
		if (budget == null || budget.signum() == 0) return BigDecimal.ZERO;
		return nvl(spent).multiply(BigDecimal.valueOf(100)).divide(budget, 1, RoundingMode.HALF_UP);
	}
}
