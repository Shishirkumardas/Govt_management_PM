package bd.gov.govos.service;

import bd.gov.govos.domain.entity.DailyReport;
import bd.gov.govos.domain.entity.UserAccount;
import bd.gov.govos.domain.enums.ReportStatus;
import bd.gov.govos.dto.ReportDtos;
import bd.gov.govos.repository.DailyReportRepository;
import bd.gov.govos.repository.UserAccountRepository;
import bd.gov.govos.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyReportService {

	private final DailyReportRepository dailyReportRepository;
	private final UserAccountRepository userAccountRepository;

	@Transactional
	public ReportDtos.DailyReportResponse submit(UserPrincipal principal, ReportDtos.DailyReportRequest req) {
		UserAccount user = userAccountRepository.findById(principal.getId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		LocalDate date = req.reportDate() != null ? req.reportDate() : LocalDate.now();

		DailyReport report = dailyReportRepository.findByUserIdAndReportDate(user.getId(), date)
				.orElse(DailyReport.builder()
						.user(user)
						.ministry(user.getMinistry())
						.reportDate(date)
						.status(ReportStatus.SUBMITTED)
						.build());

		report.setWorkCompleted(req.workCompleted());
		report.setVisits(req.visits());
		report.setApprovals(req.approvals());
		report.setExpensesBdt(req.expensesBdt() != null ? req.expensesBdt() : BigDecimal.ZERO);
		report.setProblems(req.problems());
		report.setRecommendations(req.recommendations());
		report.setLatitude(req.latitude());
		report.setLongitude(req.longitude());
		report.setPhotoUrls(req.photoUrls());
		report.setDocumentUrls(req.documentUrls());
		report.setInspectionsCount(req.inspectionsCount());
		report.setSchoolsVisited(req.schoolsVisited());
		report.setProjectsApproved(req.projectsApproved());
		report.setStatus(ReportStatus.SUBMITTED);
		report.setMinistry(user.getMinistry());

		return toResponse(dailyReportRepository.save(report));
	}

	@Transactional(readOnly = true)
	public List<ReportDtos.DailyReportResponse> myReports(Long userId) {
		return dailyReportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ReportDtos.DailyReportResponse> reportsForDate(LocalDate date) {
		return dailyReportRepository.findByReportDate(date).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ReportDtos.ReportSummary summary(LocalDate from, LocalDate to) {
		List<DailyReport> reports = dailyReportRepository.findByReportDateBetween(from, to);
		BigDecimal expenses = dailyReportRepository.sumExpensesBetween(from, to);
		long flagged = reports.stream()
				.filter(r -> r.getProblems() != null && !r.getProblems().isBlank())
				.count();
		List<ReportDtos.DailyReportResponse> recent = reports.stream()
				.sorted((a, b) -> b.getReportDate().compareTo(a.getReportDate()))
				.limit(20)
				.map(this::toResponse)
				.toList();

		return ReportDtos.ReportSummary.builder()
				.from(from)
				.to(to)
				.totalReports(reports.size())
				.totalExpensesBdt(expenses)
				.flaggedProblems(flagged)
				.recent(recent)
				.build();
	}

	private ReportDtos.DailyReportResponse toResponse(DailyReport r) {
		return ReportDtos.DailyReportResponse.builder()
				.id(r.getId())
				.userId(r.getUser().getId())
				.userName(r.getUser().getFullName())
				.designation(r.getUser().getDesignation())
				.ministryId(r.getMinistry() != null ? r.getMinistry().getId() : null)
				.ministryName(r.getMinistry() != null ? r.getMinistry().getNameEn() : null)
				.reportDate(r.getReportDate())
				.workCompleted(r.getWorkCompleted())
				.visits(r.getVisits())
				.approvals(r.getApprovals())
				.expensesBdt(r.getExpensesBdt())
				.problems(r.getProblems())
				.recommendations(r.getRecommendations())
				.latitude(r.getLatitude())
				.longitude(r.getLongitude())
				.inspectionsCount(r.getInspectionsCount())
				.schoolsVisited(r.getSchoolsVisited())
				.projectsApproved(r.getProjectsApproved())
				.status(r.getStatus())
				.build();
	}
}
