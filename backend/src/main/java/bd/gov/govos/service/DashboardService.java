package bd.gov.govos.service;

import bd.gov.govos.domain.entity.BudgetEntry;
import bd.gov.govos.domain.entity.GovProject;
import bd.gov.govos.domain.entity.Ministry;
import bd.gov.govos.domain.entity.NationalAlert;
import bd.gov.govos.domain.entity.NationalIndicator;
import bd.gov.govos.domain.enums.ProjectStatus;
import bd.gov.govos.dto.DashboardDtos;
import bd.gov.govos.dto.ReportDtos;
import bd.gov.govos.repository.AiRecommendationRepository;
import bd.gov.govos.repository.BudgetEntryRepository;
import bd.gov.govos.repository.DailyReportRepository;
import bd.gov.govos.repository.ForeignInvestmentRepository;
import bd.gov.govos.repository.GovProjectRepository;
import bd.gov.govos.repository.MinistryRepository;
import bd.gov.govos.repository.NationalAlertRepository;
import bd.gov.govos.repository.NationalIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final NationalIndicatorRepository indicatorRepository;
	private final GovProjectRepository projectRepository;
	private final MinistryRepository ministryRepository;
	private final NationalAlertRepository alertRepository;
	private final ForeignInvestmentRepository investmentRepository;
	private final AiRecommendationRepository aiRepository;
	private final BudgetEntryRepository budgetRepository;
	private final DailyReportRepository reportRepository;
	private final DailyReportService dailyReportService;

	@Transactional(readOnly = true)
	public DashboardDtos.NationalDashboard nationalDashboard() {
		LocalDate today = LocalDate.now();
		YearMonth ym = YearMonth.from(today);
		LocalDate monthStart = ym.atDay(1);
		LocalDate monthEnd = ym.atEndOfMonth();

		List<DashboardDtos.IndicatorItem> indicators = indicatorRepository.findLatestForAllKeys().stream()
				.map(this::toIndicator)
				.toList();

		Map<String, List<DashboardDtos.IndicatorItem>> byCategory = indicators.stream()
				.collect(Collectors.groupingBy(DashboardDtos.IndicatorItem::category));

		List<DashboardDtos.ProjectItem> megaProjects = projectRepository.findActiveMegaProjects().stream()
				.limit(15)
				.map(this::toProject)
				.toList();

		List<DashboardDtos.MinistryOverview> ministries = ministryRepository.findAll().stream()
				.map(m -> toMinistryOverview(m, today))
				.toList();

		List<DashboardDtos.AlertItem> alerts = alertRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
				.map(this::toAlert)
				.toList();

		List<DashboardDtos.ForeignInvestmentItem> investments = investmentRepository.findAllByOrderByAgreementDateDesc()
				.stream()
				.map(f -> DashboardDtos.ForeignInvestmentItem.builder()
						.id(f.getId())
						.investorName(f.getInvestorName())
						.country(f.getCountry())
						.sector(f.getSector())
						.amountUsdMillion(f.getAmountUsdMillion())
						.status(f.getStatus())
						.agreementDate(f.getAgreementDate())
						.build())
				.toList();

		List<DashboardDtos.AiRecommendationItem> aiRecs = aiRepository.findByAcknowledgedFalseOrderByCreatedAtDesc()
				.stream()
				.limit(10)
				.map(a -> DashboardDtos.AiRecommendationItem.builder()
						.id(a.getId())
						.title(a.getTitle())
						.body(a.getBody())
						.scope(a.getScope())
						.priority(a.getPriority())
						.build())
				.toList();

		BigDecimal revenue = budgetRepository.sumByTypeBetween("REVENUE", monthStart, monthEnd);
		BigDecimal expenditure = budgetRepository.sumByTypeBetween("EXPENDITURE", monthStart, monthEnd);
		BigDecimal tax = budgetRepository.sumByTypeBetween("TAX", monthStart, monthEnd);
		BigDecimal totalRevenue = revenue.add(tax);

		BigDecimal totalBudget = ministries.stream()
				.map(DashboardDtos.MinistryOverview::annualBudgetCrore)
				.filter(v -> v != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalSpent = ministries.stream()
				.map(DashboardDtos.MinistryOverview::spentBudgetCrore)
				.filter(v -> v != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		DashboardDtos.BudgetSnapshot budget = DashboardDtos.BudgetSnapshot.builder()
				.totalRevenueBdt(totalRevenue)
				.totalExpenditureBdt(expenditure)
				.deficitBdt(expenditure.subtract(totalRevenue))
				.totalMinistryBudgetCrore(totalBudget)
				.totalMinistrySpentCrore(totalSpent)
				.foreignInvestmentUsdMillion(investmentRepository.sumActiveInvestmentUsd())
				.build();

		long ongoing = projectRepository.findByStatus(ProjectStatus.ONGOING).size();
		long delayed = projectRepository.findByStatus(ProjectStatus.DELAYED).size();

		DashboardDtos.DailyOpsSnapshot dailyOps = DashboardDtos.DailyOpsSnapshot.builder()
				.reportsSubmittedToday(reportRepository.countByReportDate(today))
				.expensesTodayBdt(reportRepository.sumExpensesBetween(today, today))
				.activeAlerts(alerts.size())
				.ongoingProjects(ongoing)
				.delayedProjects(delayed)
				.build();

		return DashboardDtos.NationalDashboard.builder()
				.generatedAt(Instant.now().toString())
				.indicators(indicators)
				.indicatorsByCategory(byCategory)
				.megaProjects(megaProjects)
				.ministries(ministries)
				.activeAlerts(alerts)
				.foreignInvestments(investments)
				.aiRecommendations(aiRecs)
				.budget(budget)
				.dailyOps(dailyOps)
				.build();
	}

	@Transactional(readOnly = true)
	public DashboardDtos.MinistryDashboard ministryDashboard(Long ministryId) {
		Ministry ministry = ministryRepository.findById(ministryId)
				.orElseThrow(() -> new IllegalArgumentException("Ministry not found"));
		LocalDate today = LocalDate.now();
		LocalDate from = today.minusDays(30);

		List<DashboardDtos.ProjectItem> projects = projectRepository.findByMinistryId(ministryId).stream()
				.map(this::toProject)
				.toList();

		List<DashboardDtos.AlertItem> alerts = alertRepository
				.findByMinistryIdAndActiveTrueOrderByCreatedAtDesc(ministryId).stream()
				.map(this::toAlert)
				.toList();

		List<ReportDtos.DailyReportResponse> recentReports = reportRepository
				.findByMinistryIdAndReportDateBetween(ministryId, from, today).stream()
				.sorted((a, b) -> b.getReportDate().compareTo(a.getReportDate()))
				.limit(15)
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

		Map<String, BigDecimal> budgetByType = new HashMap<>();
		List<BudgetEntry> entries = budgetRepository.findByMinistryIdAndEntryDateBetween(
				ministryId, YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth());
		for (BudgetEntry e : entries) {
			budgetByType.merge(e.getEntryType(), e.getAmountBdt(), BigDecimal::add);
		}

		List<DashboardDtos.AiRecommendationItem> aiRecs = aiRepository.findAll().stream()
				.filter(a -> a.getMinistry() != null && ministryId.equals(a.getMinistry().getId()))
				.map(a -> DashboardDtos.AiRecommendationItem.builder()
						.id(a.getId())
						.title(a.getTitle())
						.body(a.getBody())
						.scope(a.getScope())
						.priority(a.getPriority())
						.build())
				.toList();

		return DashboardDtos.MinistryDashboard.builder()
				.ministry(toMinistryOverview(ministry, today))
				.projects(projects)
				.alerts(alerts)
				.recentReports(recentReports)
				.aiRecommendations(aiRecs)
				.budgetByType(budgetByType)
				.build();
	}

	@Transactional(readOnly = true)
	public List<DashboardDtos.ProjectItem> allProjects() {
		return projectRepository.findAll().stream().map(this::toProject).toList();
	}

	@Transactional(readOnly = true)
	public List<DashboardDtos.MinistryOverview> allMinistries() {
		LocalDate today = LocalDate.now();
		return ministryRepository.findAll().stream()
				.map(m -> toMinistryOverview(m, today))
				.toList();
	}

	private DashboardDtos.IndicatorItem toIndicator(NationalIndicator n) {
		return DashboardDtos.IndicatorItem.builder()
				.key(n.getIndicatorKey())
				.label(n.getLabel())
				.category(n.getCategory())
				.value(n.getValue())
				.unit(n.getUnit())
				.changePercent(n.getChangePercent())
				.asOfDate(n.getAsOfDate())
				.build();
	}

	private DashboardDtos.ProjectItem toProject(GovProject p) {
		BigDecimal total = p.getTotalCostCrore() != null ? p.getTotalCostCrore() : BigDecimal.ZERO;
		BigDecimal spent = p.getSpentCostCrore() != null ? p.getSpentCostCrore() : BigDecimal.ZERO;
		return DashboardDtos.ProjectItem.builder()
				.id(p.getId())
				.projectCode(p.getProjectCode())
				.title(p.getTitle())
				.ministryName(p.getMinistry().getNameEn())
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
	}

	private DashboardDtos.MinistryOverview toMinistryOverview(Ministry m, LocalDate today) {
		long activeProjects = projectRepository.findByMinistryId(m.getId()).stream()
				.filter(p -> p.getStatus() == ProjectStatus.ONGOING
						|| p.getStatus() == ProjectStatus.DELAYED
						|| p.getStatus() == ProjectStatus.APPROVED)
				.count();
		long reportsToday = reportRepository.findByMinistryIdAndReportDateBetween(m.getId(), today, today).size();
		BigDecimal util = BigDecimal.ZERO;
		if (m.getAnnualBudgetCrore() != null && m.getAnnualBudgetCrore().compareTo(BigDecimal.ZERO) > 0
				&& m.getSpentBudgetCrore() != null) {
			util = m.getSpentBudgetCrore()
					.multiply(BigDecimal.valueOf(100))
					.divide(m.getAnnualBudgetCrore(), 2, RoundingMode.HALF_UP);
		}
		return DashboardDtos.MinistryOverview.builder()
				.id(m.getId())
				.code(m.getCode().name())
				.nameEn(m.getNameEn())
				.nameBn(m.getNameBn())
				.annualBudgetCrore(m.getAnnualBudgetCrore())
				.spentBudgetCrore(m.getSpentBudgetCrore())
				.utilizationPercent(util)
				.activeProjects(activeProjects)
				.reportsToday(reportsToday)
				.build();
	}

	private DashboardDtos.AlertItem toAlert(NationalAlert a) {
		return DashboardDtos.AlertItem.builder()
				.id(a.getId())
				.title(a.getTitle())
				.message(a.getMessage())
				.severity(a.getSeverity())
				.category(a.getCategory())
				.location(a.getLocation())
				.ministryName(a.getMinistry() != null ? a.getMinistry().getNameEn() : null)
				.build();
	}
}
