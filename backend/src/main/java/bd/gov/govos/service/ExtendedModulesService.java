package bd.gov.govos.service;

import bd.gov.govos.domain.entity.CitizenComplaint;
import bd.gov.govos.domain.entity.DistrictProfile;
import bd.gov.govos.domain.entity.EmployeeProfile;
import bd.gov.govos.domain.entity.GovDocument;
import bd.gov.govos.domain.entity.GovNotification;
import bd.gov.govos.domain.entity.Ministry;
import bd.gov.govos.domain.entity.MinistryKpi;
import bd.gov.govos.domain.entity.OfficeScore;
import bd.gov.govos.domain.entity.ProcurementOrder;
import bd.gov.govos.domain.entity.RiskFlag;
import bd.gov.govos.repository.CitizenComplaintRepository;
import bd.gov.govos.repository.DistrictProfileRepository;
import bd.gov.govos.repository.EmployeeProfileRepository;
import bd.gov.govos.repository.GovDocumentRepository;
import bd.gov.govos.repository.GovNotificationRepository;
import bd.gov.govos.repository.MinistryKpiRepository;
import bd.gov.govos.repository.MinistryRepository;
import bd.gov.govos.repository.OfficeScoreRepository;
import bd.gov.govos.repository.ProcurementOrderRepository;
import bd.gov.govos.repository.RiskFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExtendedModulesService {

	private final ProcurementOrderRepository procurementRepository;
	private final MinistryKpiRepository kpiRepository;
	private final RiskFlagRepository riskFlagRepository;
	private final GovNotificationRepository notificationRepository;
	private final DistrictProfileRepository districtRepository;
	private final GovDocumentRepository documentRepository;
	private final EmployeeProfileRepository employeeRepository;
	private final OfficeScoreRepository officeScoreRepository;
	private final CitizenComplaintRepository complaintRepository;
	private final MinistryRepository ministryRepository;

	@Transactional(readOnly = true)
	public List<Map<String, Object>> procurement(boolean flaggedOnly) {
		List<ProcurementOrder> list = flaggedOnly
				? procurementRepository.findByFlaggedTrueOrderByOrderDateDesc()
				: procurementRepository.findAll();
		return list.stream().map(this::mapProcurement).toList();
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> kpis(Long ministryId) {
		List<MinistryKpi> list = ministryId != null
				? kpiRepository.findByMinistryIdOrderByLabelAsc(ministryId)
				: kpiRepository.findAllByOrderByMinistryIdAsc();
		return list.stream().map(k -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", k.getId());
			m.put("ministryId", k.getMinistry().getId());
			m.put("ministryName", k.getMinistry().getNameEn());
			m.put("kpiKey", k.getKpiKey());
			m.put("label", k.getLabel());
			m.put("value", k.getValue());
			m.put("target", k.getTarget());
			m.put("unit", k.getUnit());
			m.put("asOfDate", k.getAsOfDate());
			return m;
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> risks() {
		return riskFlagRepository.findByOpenTrueOrderByCreatedAtDesc().stream().map(r -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", r.getId());
			m.put("riskType", r.getRiskType());
			m.put("title", r.getTitle());
			m.put("description", r.getDescription());
			m.put("severity", r.getSeverity().name());
			m.put("ministryName", r.getMinistry() != null ? r.getMinistry().getNameEn() : null);
			m.put("location", r.getLocation());
			m.put("recommendedAction", r.getRecommendedAction());
			return m;
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> notifications() {
		return notificationRepository.findAllByOrderByCreatedAtDesc().stream().map(n -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", n.getId());
			m.put("title", n.getTitle());
			m.put("message", n.getMessage());
			m.put("category", n.getCategory());
			m.put("severity", n.getSeverity().name());
			m.put("audience", n.getAudience());
			m.put("read", n.getReadFlag());
			m.put("createdAt", n.getCreatedAt());
			return m;
		}).toList();
	}

	@Transactional
	public void markNotificationRead(Long id) {
		GovNotification n = notificationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Notification not found"));
		n.setReadFlag(true);
		notificationRepository.save(n);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> districts() {
		return districtRepository.findAll().stream().map(this::mapDistrict).toList();
	}

	@Transactional(readOnly = true)
	public Map<String, Object> district(String name) {
		DistrictProfile d = districtRepository.findByDistrictNameIgnoreCase(name)
				.orElseThrow(() -> new IllegalArgumentException("District not found: " + name));
		return mapDistrict(d);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> documents(String q) {
		List<GovDocument> list = (q != null && !q.isBlank())
				? documentRepository.search(q.trim())
				: documentRepository.findAll();
		return list.stream().map(d -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", d.getId());
			m.put("title", d.getTitle());
			m.put("docType", d.getDocType());
			m.put("classification", d.getClassification());
			m.put("ministryName", d.getMinistry() != null ? d.getMinistry().getNameEn() : null);
			m.put("issuedDate", d.getIssuedDate());
			m.put("summary", d.getSummary());
			m.put("tags", d.getTags());
			return m;
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> employees() {
		return employeeRepository.findAll().stream().map(e -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", e.getId());
			m.put("employeeId", e.getEmployeeId());
			m.put("fullName", e.getUser().getFullName());
			m.put("designation", e.getUser().getDesignation());
			m.put("ministryName", e.getMinistry() != null ? e.getMinistry().getNameEn() : null);
			m.put("grade", e.getGrade());
			m.put("monthlySalaryBdt", e.getMonthlySalaryBdt());
			m.put("attendancePercent", e.getAttendancePercent());
			m.put("performanceScore", e.getPerformanceScore());
			m.put("trainingStatus", e.getTrainingStatus());
			m.put("leaveBalanceDays", e.getLeaveBalanceDays());
			m.put("district", e.getUser().getDistrict());
			return m;
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> officeScores() {
		return officeScoreRepository.findAllByOrderByOverallScoreDesc().stream().map(o -> {
			Map<String, Object> m = new HashMap<>();
			m.put("id", o.getId());
			m.put("officeName", o.getOfficeName());
			m.put("ministryName", o.getMinistry() != null ? o.getMinistry().getNameEn() : null);
			m.put("district", o.getDistrict());
			m.put("efficiencyScore", o.getEfficiencyScore());
			m.put("transparencyScore", o.getTransparencyScore());
			m.put("budgetScore", o.getBudgetScore());
			m.put("citizenSatisfaction", o.getCitizenSatisfaction());
			m.put("projectCompletion", o.getProjectCompletion());
			m.put("attendanceScore", o.getAttendanceScore());
			m.put("overallScore", o.getOverallScore());
			return m;
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> complaints() {
		return complaintRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapComplaint).toList();
	}

	@Transactional
	public Map<String, Object> submitComplaint(Map<String, String> body) {
		Ministry ministry = null;
		if (body.get("ministryId") != null && !body.get("ministryId").isBlank()) {
			ministry = ministryRepository.findById(Long.parseLong(body.get("ministryId"))).orElse(null);
		}
		CitizenComplaint c = CitizenComplaint.builder()
				.trackingId("CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
				.citizenName(body.getOrDefault("citizenName", "Citizen"))
				.phone(body.get("phone"))
				.subject(body.getOrDefault("subject", "General complaint"))
				.description(body.get("description"))
				.district(body.get("district"))
				.ministry(ministry)
				.status("OPEN")
				.build();
		return mapComplaint(complaintRepository.save(c));
	}

	@Transactional(readOnly = true)
	public Map<String, Object> trackComplaint(String trackingId) {
		CitizenComplaint c = complaintRepository.findByTrackingId(trackingId)
				.orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
		return mapComplaint(c);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> budgetOverview() {
		List<Map<String, Object>> ministries = ministryRepository.findAll().stream().map(m -> {
			Map<String, Object> map = new HashMap<>();
			BigDecimal budget = m.getAnnualBudgetCrore() != null ? m.getAnnualBudgetCrore() : BigDecimal.ZERO;
			BigDecimal spent = m.getSpentBudgetCrore() != null ? m.getSpentBudgetCrore() : BigDecimal.ZERO;
			map.put("id", m.getId());
			map.put("name", m.getNameEn());
			map.put("code", m.getCode().name());
			map.put("budgetCrore", budget);
			map.put("spentCrore", spent);
			map.put("remainingCrore", budget.subtract(spent));
			map.put("utilizationPercent", budget.signum() == 0 ? 0 :
					spent.multiply(BigDecimal.valueOf(100)).divide(budget, 1, java.math.RoundingMode.HALF_UP));
			map.put("unusual", spent.compareTo(budget.multiply(new BigDecimal("0.75"))) > 0);
			return map;
		}).collect(Collectors.toList());

		Map<String, Object> result = new HashMap<>();
		result.put("ministries", ministries);
		result.put("note", "AI flags ministries above 75% annual spend mid-cycle as unusual for review.");
		return result;
	}

	private Map<String, Object> mapProcurement(ProcurementOrder p) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", p.getId());
		m.put("orderCode", p.getOrderCode());
		m.put("ministryName", p.getMinistry().getNameEn());
		m.put("itemDescription", p.getItemDescription());
		m.put("vendorName", p.getVendorName());
		m.put("amountBdt", p.getAmountBdt());
		m.put("orderDate", p.getOrderDate());
		m.put("status", p.getStatus());
		m.put("riskFlags", p.getRiskFlags());
		m.put("riskNotes", p.getRiskNotes());
		m.put("flagged", p.getFlagged());
		return m;
	}

	private Map<String, Object> mapDistrict(DistrictProfile d) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", d.getId());
		m.put("districtName", d.getDistrictName());
		m.put("division", d.getDivision());
		m.put("latitude", d.getLatitude());
		m.put("longitude", d.getLongitude());
		m.put("population", d.getPopulation());
		m.put("hospitals", d.getHospitals());
		m.put("schools", d.getSchools());
		m.put("activeProjects", d.getActiveProjects());
		m.put("crimeCasesMonth", d.getCrimeCasesMonth());
		m.put("budgetCrore", d.getBudgetCrore());
		m.put("floodRiskScore", d.getFloodRiskScore());
		m.put("dengueRiskScore", d.getDengueRiskScore());
		m.put("weatherNote", d.getWeatherNote());
		m.put("investmentNote", d.getInvestmentNote());
		return m;
	}

	private Map<String, Object> mapComplaint(CitizenComplaint c) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", c.getId());
		m.put("trackingId", c.getTrackingId());
		m.put("citizenName", c.getCitizenName());
		m.put("phone", c.getPhone());
		m.put("subject", c.getSubject());
		m.put("description", c.getDescription());
		m.put("district", c.getDistrict());
		m.put("ministryName", c.getMinistry() != null ? c.getMinistry().getNameEn() : null);
		m.put("status", c.getStatus());
		m.put("resolutionNote", c.getResolutionNote());
		m.put("createdAt", c.getCreatedAt());
		return m;
	}
}
