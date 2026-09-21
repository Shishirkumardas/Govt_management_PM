package bd.gov.govos.config;

import bd.gov.govos.domain.entity.*;
import bd.gov.govos.domain.enums.*;
import bd.gov.govos.repository.*;
import bd.gov.govos.service.MfaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final MinistryRepository ministryRepository;
	private final UserAccountRepository userAccountRepository;
	private final GovProjectRepository projectRepository;
	private final NationalIndicatorRepository indicatorRepository;
	private final BudgetEntryRepository budgetEntryRepository;
	private final NationalAlertRepository alertRepository;
	private final ForeignInvestmentRepository investmentRepository;
	private final AiRecommendationRepository aiRepository;
	private final DailyReportRepository reportRepository;
	private final ProcurementOrderRepository procurementRepository;
	private final MinistryKpiRepository kpiRepository;
	private final RiskFlagRepository riskFlagRepository;
	private final GovNotificationRepository notificationRepository;
	private final DistrictProfileRepository districtRepository;
	private final GovDocumentRepository documentRepository;
	private final EmployeeProfileRepository employeeRepository;
	private final OfficeScoreRepository officeScoreRepository;
	private final CitizenComplaintRepository complaintRepository;
	private final GovOrganizationRepository orgRepository;
	private final BudgetLineRepository budgetLineRepository;
	private final WorkProgramRepository workProgramRepository;
	private final PasswordEncoder passwordEncoder;
	private final MfaService mfaService;

	@Override
	@Transactional
	public void run(String... args) {
		if (userAccountRepository.count() > 0) {
			log.info("GovOS data already seeded — skipping");
			return;
		}
		log.info("Seeding GovOS Bangladesh demo data...");

		Map<MinistryCode, Ministry> ministries = seedMinistries();
		Map<String, UserAccount> users = seedUsers(ministries);
		seedIndicators();
		seedProjects(ministries);
		seedBudget(ministries, users);
		seedAlerts(ministries);
		seedInvestments(ministries);
		seedAiRecommendations(ministries);
		seedDailyReports(users, ministries);
		seedProcurement(ministries);
		seedKpis(ministries);
		seedRisks(ministries);
		seedNotifications();
		seedDistricts();
		seedDocuments(ministries);
		seedHr(users, ministries);
		seedOfficeScores(ministries);
		seedComplaints(ministries);
		seedMinistryIntelligence(ministries);

		log.info("Seed complete. Demo logins (password: password123): pm, agri.minister, finance.sec, field.dhaka");
	}

	private Map<MinistryCode, Ministry> seedMinistries() {
		record M(MinistryCode code, String en, String bn, String budget, String spent) {}
		List<M> defs = List.of(
				new M(MinistryCode.FINANCE, "Ministry of Finance", "অর্থ মন্ত্রণালয়", "85000", "42000"),
				new M(MinistryCode.AGRICULTURE, "Ministry of Agriculture", "কৃষি মন্ত্রণালয়", "32000", "18500"),
				new M(MinistryCode.EDUCATION, "Ministry of Education", "শিক্ষা মন্ত্রণালয়", "78000", "39000"),
				new M(MinistryCode.HEALTH, "Ministry of Health and Family Welfare", "স্বাস্থ্য ও পরিবার কল্যাণ মন্ত্রণালয়", "10000", "6500"),
				new M(MinistryCode.TRANSPORT, "Ministry of Road Transport and Bridges", "সড়ক পরিবহন ও সেতু মন্ত্রণালয়", "95000", "62000"),
				new M(MinistryCode.POWER, "Ministry of Power, Energy and Mineral Resources", "বিদ্যুৎ, জ্বালানি ও খনিজ সম্পদ মন্ত্রণালয়", "70000", "41000"),
				new M(MinistryCode.FOREIGN_AFFAIRS, "Ministry of Foreign Affairs", "পররাষ্ট্র মন্ত্রণালয়", "8000", "3200"),
				new M(MinistryCode.LOCAL_GOVERNMENT, "Ministry of Local Government", "স্থানীয় সরকার মন্ত্রণালয়", "55000", "30000"),
				new M(MinistryCode.HOME_POLICE, "Ministry of Home Affairs", "স্বরাষ্ট্র মন্ত্রণালয়", "48000", "25000"),
				new M(MinistryCode.DISASTER_MANAGEMENT, "Ministry of Disaster Management and Relief", "দুর্যোগ ব্যবস্থাপনা ও ত্রাণ মন্ত্রণালয়", "12000", "6500"),
				new M(MinistryCode.ICT, "Ministry of ICT", "তথ্য ও যোগাযোগ প্রযুক্তি মন্ত্রণালয়", "15000", "7200"),
				new M(MinistryCode.PLANNING, "Ministry of Planning", "পরিকল্পনা মন্ত্রণালয়", "10000", "4000")
		);
		Map<MinistryCode, Ministry> map = new EnumMap<>(MinistryCode.class);
		for (M d : defs) {
			map.put(d.code(), ministryRepository.save(Ministry.builder()
					.code(d.code()).nameEn(d.en()).nameBn(d.bn())
					.description(d.en() + " — Government of Bangladesh")
					.annualBudgetCrore(new BigDecimal(d.budget()))
					.spentBudgetCrore(new BigDecimal(d.spent()))
					.active(true).build()));
		}
		return map;
	}

	private Map<String, UserAccount> seedUsers(Map<MinistryCode, Ministry> ministries) {
		String hash = passwordEncoder.encode("password123");
		Map<String, UserAccount> users = new HashMap<>();
		String pmMfaSecret = mfaService.generateSecret();
		users.put("pm", userAccountRepository.save(UserAccount.builder()
				.username("pm").passwordHash(hash).fullName("Hon'ble Prime Minister")
				.email("pm@pmo.gov.bd").role(UserRole.PRIME_MINISTER)
				.designation("Prime Minister of Bangladesh").active(true)
				.mfaEnabled(true).mfaSecret(pmMfaSecret).build()));
		log.info("PM MFA enabled (demo TOTP). Use code from login screen hint or authenticator.");
		users.put("agri.minister", userAccountRepository.save(UserAccount.builder()
				.username("agri.minister").passwordHash(hash).fullName("Minister of Agriculture")
				.email("minister@moa.gov.bd").role(UserRole.CABINET_MINISTER)
				.ministry(ministries.get(MinistryCode.AGRICULTURE))
				.designation("Cabinet Minister").active(true).build()));
		users.put("finance.sec", userAccountRepository.save(UserAccount.builder()
				.username("finance.sec").passwordHash(hash).fullName("Secretary, Finance Division")
				.email("secretary@finance.gov.bd").role(UserRole.SECRETARY)
				.ministry(ministries.get(MinistryCode.FINANCE))
				.designation("Senior Secretary").active(true).build()));
		users.put("health.dept", userAccountRepository.save(UserAccount.builder()
				.username("health.dept").passwordHash(hash).fullName("Director, DGHS Operations")
				.email("ops@dghs.gov.bd").role(UserRole.DEPARTMENT_OFFICER)
				.ministry(ministries.get(MinistryCode.HEALTH))
				.designation("Department Officer").active(true).build()));
		users.put("dc.dhaka", userAccountRepository.save(UserAccount.builder()
				.username("dc.dhaka").passwordHash(hash).fullName("Deputy Commissioner, Dhaka")
				.email("dc@dhaka.gov.bd").role(UserRole.DISTRICT_ADMIN)
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT))
				.district("Dhaka").designation("Deputy Commissioner").active(true).build()));
		users.put("field.dhaka", userAccountRepository.save(UserAccount.builder()
				.username("field.dhaka").passwordHash(hash).fullName("Field Officer — Dhaka North")
				.email("field@lgd.gov.bd").role(UserRole.FIELD_OFFICER)
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT))
				.district("Dhaka").upazila("Gulshan")
				.designation("Upazila Field Officer").active(true).build()));
		users.put("field.agri", userAccountRepository.save(UserAccount.builder()
				.username("field.agri").passwordHash(hash).fullName("Agriculture Extension Officer")
				.email("aeo@moa.gov.bd").role(UserRole.FIELD_OFFICER)
				.ministry(ministries.get(MinistryCode.AGRICULTURE))
				.district("Rajshahi").upazila("Paba")
				.designation("AEO").active(true).build()));
		return users;
	}

	private void seedIndicators() {
		LocalDate today = LocalDate.now();
		record Ind(String key, String label, String cat, String val, String unit, String chg) {}
		List.of(
				new Ind("GDP_GROWTH", "GDP Growth", "Economy", "5.80", "%", "0.20"),
				new Ind("INFLATION", "Inflation (CPI)", "Economy", "8.90", "%", "-0.40"),
				new Ind("FOREX_RESERVE", "Foreign Exchange Reserve", "Economy", "26.40", "USD bn", "1.10"),
				new Ind("TAX_COLLECTION", "Tax Collection (FYTD)", "Finance", "285000", "BDT crore", "4.50"),
				new Ind("NATIONAL_DEBT", "National Debt", "Finance", "48.20", "% of GDP", "0.30"),
				new Ind("ELECTRICITY", "Electricity Generation Capacity", "Power", "28.50", "GW", "2.00"),
				new Ind("LOAD_SHEDDING", "Load Shedding (avg)", "Power", "0.40", "hrs/day", "-15.00"),
				new Ind("RICE_PRODUCTION", "Rice Production (est.)", "Agriculture", "39.10", "million MT", "1.80"),
				new Ind("FOOD_STOCK", "Public Food Stock", "Agriculture", "1.65", "million MT", "-3.00"),
				new Ind("FERTILIZER_STOCK", "Fertilizer Stock", "Agriculture", "1.20", "million MT", "5.00"),
				new Ind("SCHOOL_ATTENDANCE", "School Attendance Rate", "Education", "86.50", "%", "0.80"),
				new Ind("DROPOUT_RATE", "Secondary Dropout Rate", "Education", "12.40", "%", "-1.20"),
				new Ind("HOSPITAL_OCCUPANCY", "Hospital Bed Occupancy", "Health", "72.00", "%", "3.00"),
				new Ind("VACCINATION", "Childhood Vaccination Coverage", "Health", "94.20", "%", "0.50"),
				new Ind("CRIME_INDEX", "Reported Crimes (month)", "Security", "18420", "cases", "-2.10"),
				new Ind("EMPLOYMENT", "Employment Rate", "Labour", "96.10", "%", "0.10"),
				new Ind("UNEMPLOYMENT", "Unemployment Rate", "Labour", "3.90", "%", "-0.10"),
				new Ind("POVERTY", "Poverty Rate", "Social", "18.70", "%", "-0.50"),
				new Ind("DIGITAL_SERVICES", "Digital Services Transactions", "ICT", "42.50", "million/mo", "8.00"),
				new Ind("ROAD_PROGRESS", "National Highway Projects Progress", "Transport", "68.00", "%", "2.50")
		).forEach(i -> indicatorRepository.save(NationalIndicator.builder()
				.indicatorKey(i.key()).label(i.label()).category(i.cat())
				.value(new BigDecimal(i.val())).unit(i.unit())
				.changePercent(new BigDecimal(i.chg())).asOfDate(today).build()));
	}

	private void seedProjects(Map<MinistryCode, Ministry> ministries) {
		LocalDate today = LocalDate.now();
		// Padma Bridge style example (user scenario)
		saveProject("PADMA-BRIDGE", "Padma Multipurpose Bridge — Ops & Link Roads",
				ministries.get(MinistryCode.TRANSPORT), ProjectStatus.ONGOING,
				"4200", "3800", "92.00", "Munshiganj / Shariatpur", "GOB / JICA",
				"Main bridge operational; approach road finishing packages ongoing", today.minusDays(2),
				"XYZ Infrastructure Ltd", 15, "HIGH",
				"Monsoon rainfall + material shortage on approach package-3",
				"Increase workforce on critical path; pre-book steel/cement buffer for 45 days");

		saveProject("MRT-LINE1", "Dhaka Metro Rail Line-1", ministries.get(MinistryCode.TRANSPORT),
				ProjectStatus.ONGOING, "52000", "18500", "28.00", "Dhaka", "JICA / GOB",
				"Tunnel boring machines deployed on north corridor", today.minusDays(3),
				"NK Consortium", 45, "HIGH",
				"Land compensation dispute on 1.2 km segment",
				"Cabinet guidance on compensation schedule within 14 days");

		saveProject("PADMA-RAIL", "Padma Bridge Rail Link", ministries.get(MinistryCode.TRANSPORT),
				ProjectStatus.ONGOING, "39246", "35000", "89.00", "Munshiganj–Jashore", "China / GOB",
				"Track laying near completion; trial runs scheduled", today.minusDays(5),
				"CREC", 0, "LOW", "On schedule for trial runs", "Maintain QA on signaling systems");

		saveProject("KARNAPHULI-TUNNEL", "Bangabandhu Sheikh Mujibur Rahman Tunnel",
				ministries.get(MinistryCode.TRANSPORT), ProjectStatus.COMPLETED,
				"10370", "10370", "100.00", "Chattogram", "China / GOB",
				"Operational — maintenance phase", today.minusMonths(6),
				"China Communications", 0, "LOW", null, null);

		saveProject("ROOPPUR-NPP", "Rooppur Nuclear Power Plant", ministries.get(MinistryCode.POWER),
				ProjectStatus.ONGOING, "113000", "78000", "72.00", "Pabna", "Russia / GOB",
				"Unit-1 commissioning activities in progress", today.minusDays(2),
				"Rosatom JV", 30, "MEDIUM", "Specialist workforce ramp-up lag",
				"Expedite visa/clearance for commissioning specialists");

		saveProject("MATARBARI", "Matarbari Ultra Super Critical Coal Power",
				ministries.get(MinistryCode.POWER), ProjectStatus.ONGOING,
				"52000", "41000", "81.00", "Cox's Bazar", "JICA / GOB",
				"Unit operations stabilizing", today.minusDays(7),
				"Sumitomo Consortium", 10, "MEDIUM", "Coal logistics weather risk",
				"Diversify coal stock buffer to 60 days");

		saveProject("FOOD-STORAGE", "National Food Storage Expansion",
				ministries.get(MinistryCode.AGRICULTURE), ProjectStatus.ONGOING,
				"4500", "2100", "48.00", "Nationwide", "GOB",
				"12 silos under construction in northern districts", today.minusDays(1),
				"Local Contractor Pool", 20, "MEDIUM", "Steel delivery delays",
				"Centralize steel procurement for remaining silos");

		saveProject("IRRIGATION-NORTH", "Northern Irrigation Modernization",
				ministries.get(MinistryCode.AGRICULTURE), ProjectStatus.DELAYED,
				"6200", "2800", "35.00", "Rangpur Division", "ADB / GOB",
				"Land acquisition delays in two upazilas", today.minusDays(4),
				"ADB Package Consortium", 210, "CRITICAL",
				"Land acquisition stalled >6 months; monsoon window missed",
				"DC-level task force; re-phase packages or temporary alignment");

		saveProject("DIGITAL-BD", "Digital Bangladesh 2.0 — e-Services",
				ministries.get(MinistryCode.ICT), ProjectStatus.ONGOING,
				"3500", "1900", "55.00", "Nationwide", "GOB",
				"45 new citizen services onboarded this quarter", today.minusDays(2),
				"a2i + Vendors", 0, "LOW", null, "Continue service quality audits");

		saveProject("PRIMARY-SCHOOL", "Primary School Infrastructure Upgrade",
				ministries.get(MinistryCode.EDUCATION), ProjectStatus.ONGOING,
				"12000", "6400", "52.00", "Nationwide", "GOB / WB",
				"2,100 classrooms completed this fiscal year", today.minusDays(6),
				"LGED Contractors", 25, "MEDIUM", "Contractor capacity uneven by district",
				"Blacklist chronic underperformers; reallocate packages");

		saveProject("HOSPITAL-DIST", "District Hospital Capacity Expansion",
				ministries.get(MinistryCode.HEALTH), ProjectStatus.ONGOING,
				"15000", "8200", "54.00", "64 Districts", "GOB",
				"ICU beds added in 18 districts", today.minusDays(3),
				"PWD Health Wing", 40, "MEDIUM", "Medical equipment import lag",
				"Framework contract for ICU equipment");

		saveProject("FLOOD-SHELTER", "Coastal Multipurpose Flood Shelters",
				ministries.get(MinistryCode.DISASTER_MANAGEMENT), ProjectStatus.APPROVED,
				"2800", "200", "8.00", "Coastal Belt", "GOB / JICA",
				"Site selection finalized for 120 new shelters", today.minusDays(10),
				"TBD", 0, "LOW", null, "Fast-track tender before cyclone season");
	}

	private void saveProject(String code, String title, Ministry ministry, ProjectStatus status,
			String total, String spent, String progress, String location, String funding,
			String update, LocalDate updateDate, String contractor, Integer delayDays,
			String riskLevel, String riskReason, String aiRec) {
		projectRepository.save(GovProject.builder()
				.projectCode(code).title(title)
				.description(title + " — Government of Bangladesh development project")
				.ministry(ministry).status(status)
				.totalCostCrore(new BigDecimal(total)).spentCostCrore(new BigDecimal(spent))
				.progressPercent(new BigDecimal(progress)).location(location).fundingSource(funding)
				.startDate(LocalDate.now().minusYears(2)).expectedEndDate(LocalDate.now().plusYears(2))
				.latestUpdate(update).latestUpdateDate(updateDate)
				.contractor(contractor).delayDays(delayDays).riskLevel(riskLevel)
				.riskReason(riskReason).aiRecommendation(aiRec).build());
	}

	private void seedBudget(Map<MinistryCode, Ministry> ministries, Map<String, UserAccount> users) {
		LocalDate today = LocalDate.now();
		budgetEntryRepository.save(BudgetEntry.builder().ministry(ministries.get(MinistryCode.FINANCE))
				.entryType("TAX").title("NBR Tax Collection — Month to date")
				.amountBdt(new BigDecimal("285000000000")).entryDate(today)
				.recordedBy(users.get("finance.sec")).build());
		budgetEntryRepository.save(BudgetEntry.builder().ministry(ministries.get(MinistryCode.FINANCE))
				.entryType("REVENUE").title("Non-tax revenue")
				.amountBdt(new BigDecimal("45000000000")).entryDate(today.minusDays(5))
				.recordedBy(users.get("finance.sec")).build());
		budgetEntryRepository.save(BudgetEntry.builder().ministry(ministries.get(MinistryCode.TRANSPORT))
				.entryType("EXPENDITURE").title("Metro & bridge project disbursements")
				.amountBdt(new BigDecimal("120000000000")).entryDate(today.minusDays(2))
				.recordedBy(users.get("finance.sec")).build());
		budgetEntryRepository.save(BudgetEntry.builder().ministry(ministries.get(MinistryCode.AGRICULTURE))
				.entryType("EXPENDITURE").title("Fertilizer subsidy release")
				.amountBdt(new BigDecimal("18000000000")).entryDate(today.minusDays(1))
				.recordedBy(users.get("finance.sec")).build());
		budgetEntryRepository.save(BudgetEntry.builder().ministry(ministries.get(MinistryCode.HEALTH))
				.entryType("EXPENDITURE").title("Medicine & hospital operations")
				.amountBdt(new BigDecimal("22000000000")).entryDate(today.minusDays(3))
				.recordedBy(users.get("finance.sec")).build());
	}

	private void seedAlerts(Map<MinistryCode, Ministry> ministries) {
		alertRepository.save(NationalAlert.builder().title("Flood Warning — Northern Districts")
				.message("Brahmaputra water level rising above danger mark in Kurigram and Gaibandha.")
				.severity(AlertSeverity.HIGH).category("DISASTER").location("Rangpur Division")
				.ministry(ministries.get(MinistryCode.DISASTER_MANAGEMENT)).active(true).build());
		alertRepository.save(NationalAlert.builder().title("Dengue Case Spike — Dhaka")
				.message("Weekly dengue cases up 22%. DGHS directed hospital readiness.")
				.severity(AlertSeverity.MEDIUM).category("HEALTH").location("Dhaka")
				.ministry(ministries.get(MinistryCode.HEALTH)).active(true).build());
		alertRepository.save(NationalAlert.builder().title("Fertilizer Distribution Gap")
				.message("Three northern upazilas report TSP shortage for Boro season.")
				.severity(AlertSeverity.MEDIUM).category("AGRICULTURE").location("Rangpur")
				.ministry(ministries.get(MinistryCode.AGRICULTURE)).active(true).build());
		alertRepository.save(NationalAlert.builder().title("Metro Line-1 Land Dispute")
				.message("Construction pause risk on 1.2 km segment — needs Cabinet guidance.")
				.severity(AlertSeverity.HIGH).category("PROJECT").location("Dhaka")
				.ministry(ministries.get(MinistryCode.TRANSPORT)).active(true).build());
	}

	private void seedInvestments(Map<MinistryCode, Ministry> ministries) {
		investmentRepository.save(ForeignInvestment.builder().investorName("Samsung Electronics")
				.country("South Korea").sector("Electronics Manufacturing")
				.amountUsdMillion(new BigDecimal("350")).status("ACTIVE")
				.agreementDate(LocalDate.now().minusMonths(8))
				.ministry(ministries.get(MinistryCode.ICT)).notes("BEZA zone expansion").build());
		investmentRepository.save(ForeignInvestment.builder().investorName("Adani Power")
				.country("India").sector("Power Generation")
				.amountUsdMillion(new BigDecimal("1600")).status("ACTIVE")
				.agreementDate(LocalDate.now().minusYears(1))
				.ministry(ministries.get(MinistryCode.POWER)).notes("Cross-border PPA").build());
		investmentRepository.save(ForeignInvestment.builder().investorName("Toyota Tsusho")
				.country("Japan").sector("Automobile & Logistics")
				.amountUsdMillion(new BigDecimal("120")).status("COMMITTED")
				.agreementDate(LocalDate.now().minusMonths(2))
				.ministry(ministries.get(MinistryCode.TRANSPORT)).notes("EV assembly feasibility").build());
		investmentRepository.save(ForeignInvestment.builder().investorName("World Bank / IDA")
				.country("Multilateral").sector("Education Infrastructure")
				.amountUsdMillion(new BigDecimal("500")).status("ACTIVE")
				.agreementDate(LocalDate.now().minusMonths(14))
				.ministry(ministries.get(MinistryCode.EDUCATION)).notes("Learning recovery program").build());
	}

	private void seedAiRecommendations(Map<MinistryCode, Ministry> ministries) {
		aiRepository.save(AiRecommendation.builder()
				.title("Accelerate food stock replenishment before monsoon")
				.body("Public food stock declining (-3%). Procure 0.3 MT rice and pre-position in northern warehouses.")
				.scope("NATIONAL").priority("HIGH").ministry(ministries.get(MinistryCode.AGRICULTURE)).acknowledged(false).build());
		aiRepository.save(AiRecommendation.builder()
				.title("Intervene on delayed irrigation project")
				.body("Northern Irrigation at 35% with 210-day delay. Escalate land acquisition to DC task force.")
				.scope("PROJECT").priority("HIGH").ministry(ministries.get(MinistryCode.AGRICULTURE)).acknowledged(false).build());
		aiRepository.save(AiRecommendation.builder()
				.title("Budget deficit watch — transport heavy disbursements")
				.body("Weekly cash-flow review with Finance Division recommended to avoid Q4 liquidity stress.")
				.scope("NATIONAL").priority("MEDIUM").ministry(ministries.get(MinistryCode.FINANCE)).acknowledged(false).build());
		aiRepository.save(AiRecommendation.builder()
				.title("Dengue surge preparedness")
				.body("Activate larva drives, expand fever clinics, ensure platelet/IV stocks for 4 weeks.")
				.scope("MINISTRY").priority("HIGH").ministry(ministries.get(MinistryCode.HEALTH)).acknowledged(false).build());
	}

	private void seedDailyReports(Map<String, UserAccount> users, Map<MinistryCode, Ministry> ministries) {
		LocalDate today = LocalDate.now();
		reportRepository.save(DailyReport.builder().user(users.get("field.dhaka"))
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT)).reportDate(today)
				.workCompleted("Completed 5 inspections of municipal drainage projects.")
				.visits("Visited 3 schools for infrastructure check.")
				.approvals("Approved 2 small development proposals.")
				.expensesBdt(new BigDecimal("35000"))
				.problems("Road damaged near Gulshan-2; waterlogging risk.")
				.recommendations("Immediate patch repair; coordinate with DWASA.")
				.inspectionsCount(5).schoolsVisited(3).projectsApproved(2)
				.latitude(23.7925).longitude(90.4078).status(ReportStatus.SUBMITTED).build());
		reportRepository.save(DailyReport.builder().user(users.get("field.agri"))
				.ministry(ministries.get(MinistryCode.AGRICULTURE)).reportDate(today)
				.workCompleted("Field survey of Boro seed beds; fertilizer dealer stock audit.")
				.visits("Visited 4 farmer groups.")
				.approvals("Recommended subsidy cards for 28 marginal farmers.")
				.expensesBdt(new BigDecimal("12500"))
				.problems("TSP fertilizer shortage in 2 dealer points.")
				.recommendations("Dispatch buffer stock within 48 hours.")
				.inspectionsCount(6).schoolsVisited(0).projectsApproved(0)
				.latitude(24.3745).longitude(88.6042).status(ReportStatus.SUBMITTED).build());
		reportRepository.save(DailyReport.builder().user(users.get("dc.dhaka"))
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT)).reportDate(today.minusDays(1))
				.workCompleted("Inter-agency meeting on metro corridor safety.")
				.visits("Visited 2 project sites.")
				.approvals("Cleared 1 emergency repair tender.")
				.expensesBdt(new BigDecimal("8000"))
				.problems("Night construction noise complaints.")
				.recommendations("Enforce construction hours policy.")
				.inspectionsCount(2).projectsApproved(1).status(ReportStatus.REVIEWED).build());
	}

	private void seedProcurement(Map<MinistryCode, Ministry> ministries) {
		LocalDate today = LocalDate.now();
		procurementRepository.save(ProcurementOrder.builder()
				.orderCode("PO-HLT-88421").ministry(ministries.get(MinistryCode.HEALTH))
				.itemDescription("Surgical gloves — 2 million units")
				.vendorName("MediSupply BD Ltd").amountBdt(new BigDecimal("48000000"))
				.orderDate(today.minusDays(3)).status("PAID").flagged(true)
				.riskFlags("PRICE_INFLATION,REPEATED_PURCHASE")
				.riskNotes("Unit price 34% above 90-day market median; same vendor 4th order in 45 days.")
				.build());
		procurementRepository.save(ProcurementOrder.builder()
				.orderCode("PO-EDU-55210").ministry(ministries.get(MinistryCode.EDUCATION))
				.itemDescription("School furniture package — 500 schools")
				.vendorName("Green Wood Traders").amountBdt(new BigDecimal("125000000"))
				.orderDate(today.minusDays(12)).status("UNDER_REVIEW").flagged(true)
				.riskFlags("FAKE_VENDOR,DUPLICATE_INVOICE")
				.riskNotes("Vendor TIN mismatch with RJSC; invoice number sequence overlaps prior payment.")
				.build());
		procurementRepository.save(ProcurementOrder.builder()
				.orderCode("PO-AGR-33102").ministry(ministries.get(MinistryCode.AGRICULTURE))
				.itemDescription("TSP fertilizer import tranche")
				.vendorName("BCIC Authorized Agent").amountBdt(new BigDecimal("890000000"))
				.orderDate(today.minusDays(5)).status("APPROVED").flagged(false).build());
		procurementRepository.save(ProcurementOrder.builder()
				.orderCode("PO-TRN-11098").ministry(ministries.get(MinistryCode.TRANSPORT))
				.itemDescription("Bitumen supply — national highways")
				.vendorName("Highway Materials Co").amountBdt(new BigDecimal("210000000"))
				.orderDate(today.minusDays(8)).status("PAID").flagged(true)
				.riskFlags("CONFLICT")
				.riskNotes("Beneficial owner linked to project consultant firm — conflict review required.")
				.build());
	}

	private void seedKpis(Map<MinistryCode, Ministry> ministries) {
		LocalDate today = LocalDate.now();
		// Education
		kpi(ministries.get(MinistryCode.EDUCATION), "TEACHER_ATT", "Teacher attendance", "91.2", "95", "%", today);
		kpi(ministries.get(MinistryCode.EDUCATION), "STUDENT_ATT", "Student attendance", "86.5", "90", "%", today);
		kpi(ministries.get(MinistryCode.EDUCATION), "EXAM_PASS", "SSC exam pass rate", "82.4", "85", "%", today);
		kpi(ministries.get(MinistryCode.EDUCATION), "DROPOUT", "Secondary dropout rate", "12.4", "10", "%", today);
		kpi(ministries.get(MinistryCode.EDUCATION), "BUDGET_UTIL", "Budget utilization", "50.0", "60", "%", today);
		// Agriculture
		kpi(ministries.get(MinistryCode.AGRICULTURE), "RICE", "Rice production", "39.1", "40", "m MT", today);
		kpi(ministries.get(MinistryCode.AGRICULTURE), "WHEAT", "Wheat production", "1.15", "1.2", "m MT", today);
		kpi(ministries.get(MinistryCode.AGRICULTURE), "CORN", "Corn production", "5.4", "5.5", "m MT", today);
		kpi(ministries.get(MinistryCode.AGRICULTURE), "FERT", "Fertilizer distribution", "1.20", "1.30", "m MT", today);
		kpi(ministries.get(MinistryCode.AGRICULTURE), "STORAGE", "Food storage capacity used", "72", "80", "%", today);
		// Health
		kpi(ministries.get(MinistryCode.HEALTH), "DEATHS", "Registered deaths (month)", "8420", "0", "count", today);
		kpi(ministries.get(MinistryCode.HEALTH), "BIRTHS", "Registered births (month)", "22100", "0", "count", today);
		kpi(ministries.get(MinistryCode.HEALTH), "VACC", "Vaccination coverage", "94.2", "95", "%", today);
		kpi(ministries.get(MinistryCode.HEALTH), "MED_STOCK", "Essential medicine stock", "81", "90", "%", today);
		kpi(ministries.get(MinistryCode.HEALTH), "HOSP_OCC", "Hospital occupancy", "72", "75", "%", today);
	}

	private void kpi(Ministry m, String key, String label, String val, String target, String unit, LocalDate d) {
		kpiRepository.save(MinistryKpi.builder().ministry(m).kpiKey(key).label(label)
				.value(new BigDecimal(val)).target(new BigDecimal(target)).unit(unit).asOfDate(d).build());
	}

	private void seedRisks(Map<MinistryCode, Ministry> ministries) {
		riskFlagRepository.save(RiskFlag.builder().riskType("OVERSPENDING")
				.title("Health Ministry approaching high utilization").severity(AlertSeverity.MEDIUM)
				.description("Health spent 6,500 of 10,000 crore (65%). Unusual mid-year spend pattern on medicines.")
				.ministry(ministries.get(MinistryCode.HEALTH)).recommendedAction("Finance Division weekly spend review")
				.open(true).build());
		riskFlagRepository.save(RiskFlag.builder().riskType("DUPLICATE_PAYMENTS")
				.title("Possible duplicate invoice — Education furniture")
				.severity(AlertSeverity.HIGH)
				.description("PO-EDU-55210 shows invoice sequence overlap with prior payment to related vendor.")
				.ministry(ministries.get(MinistryCode.EDUCATION)).recommendedAction("Freeze payment; CAG/internal audit")
				.open(true).build());
		riskFlagRepository.save(RiskFlag.builder().riskType("INACTIVE_PROJECTS")
				.title("Irrigation project inactive > 60 days on site")
				.severity(AlertSeverity.HIGH)
				.description("Northern Irrigation packages show low site activity despite budget release.")
				.ministry(ministries.get(MinistryCode.AGRICULTURE)).location("Rangpur")
				.recommendedAction("Site verification + contractor show-cause")
				.open(true).build());
		riskFlagRepository.save(RiskFlag.builder().riskType("DELAYED_REPORTS")
				.title("Low daily reporting compliance — some upazilas")
				.severity(AlertSeverity.LOW)
				.description("Field reporting below threshold in 14 upazilas yesterday.")
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT))
				.recommendedAction("Secretary circular enforcing digital daily reports")
				.open(true).build());
		riskFlagRepository.save(RiskFlag.builder().riskType("GHOST_EMPLOYEES")
				.title("Attendance anomaly cluster")
				.severity(AlertSeverity.MEDIUM)
				.description("Biometric/login mismatch for 37 IDs across two district offices (demo signal).")
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT)).location("Dhaka")
				.recommendedAction("HR audit + physical verification")
				.open(true).build());
	}

	private void seedNotifications() {
		notificationRepository.save(GovNotification.builder()
				.title("Flood risk rising — northern rivers")
				.message("AI predicts elevated flood risk for Kurigram, Gaibandha, Sirajganj in next 72 hours.")
				.category("FLOOD").severity(AlertSeverity.HIGH).audience("PRIME_MINISTER").readFlag(false).build());
		notificationRepository.save(GovNotification.builder()
				.title("Budget utilization alert — Health")
				.message("Health Ministry spent 65% of annual budget. AI flagged unusual medicine procurement pattern.")
				.category("BUDGET").severity(AlertSeverity.MEDIUM).audience("PRIME_MINISTER").readFlag(false).build());
		notificationRepository.save(GovNotification.builder()
				.title("Project delayed > 6 months")
				.message("Northern Irrigation Modernization delayed 210 days. Requires Cabinet intervention.")
				.category("PROJECT").severity(AlertSeverity.HIGH).audience("PRIME_MINISTER").readFlag(false).build());
		notificationRepository.save(GovNotification.builder()
				.title("Food stock below comfort band")
				.message("Public food stock down 3%. Recommend procurement before monsoon.")
				.category("FOOD").severity(AlertSeverity.MEDIUM).audience("PRIME_MINISTER").readFlag(false).build());
		notificationRepository.save(GovNotification.builder()
				.title("Medicine shortage risk — dengue wards")
				.message("Platelet kit and IV fluid stock projected low in 3 Dhaka hospitals within 10 days.")
				.category("MEDICINE").severity(AlertSeverity.HIGH).audience("PRIME_MINISTER").readFlag(false).build());
	}

	private void seedDistricts() {
		district("Dhaka", "Dhaka", 23.8103, 90.4125, 14_500_000L, 86, 4200, 28, 2100, "8500", "3.2", "8.7",
				"Hot & humid; heavy evening showers likely", "ICT & service sector investments strong");
		district("Chattogram", "Chattogram", 22.3569, 91.7832, 9_200_000L, 54, 3100, 18, 1650, "6200", "4.1", "7.2",
				"Coastal wind advisory", "Port & EPZ manufacturing FDI");
		district("Rajshahi", "Rajshahi", 24.3745, 88.6042, 2_800_000L, 22, 1800, 9, 420, "2100", "5.5", "4.1",
				"Dry heat; irrigation demand high", "Agro-processing potential");
		district("Khulna", "Khulna", 22.8456, 89.5403, 2_400_000L, 18, 1500, 7, 380, "1900", "6.2", "5.0",
				"Tidal flooding risk in south", "Mongla port logistics");
		district("Sylhet", "Sylhet", 24.8949, 91.8687, 3_900_000L, 20, 1600, 6, 510, "1800", "7.1", "5.5",
				"Flash flood watch on haor basin", "Tea & tourism");
		district("Rangpur", "Rangpur", 25.7439, 89.2752, 3_100_000L, 16, 1400, 11, 290, "1600", "8.4", "3.8",
				"River water above danger in tributaries", "Agri value-chain");
		district("Barishal", "Barishal", 22.7010, 90.3535, 2_300_000L, 14, 1200, 5, 310, "1400", "7.8", "4.6",
				"Cyclone shelter readiness advised", "Inland waterways");
		district("Mymensingh", "Mymensingh", 24.7471, 90.4203, 5_800_000L, 24, 2100, 8, 450, "2000", "5.0", "6.1",
				"Seasonal storms", "Education hub growth");
		district("Kurigram", "Rangpur", 25.8072, 89.6295, 2_200_000L, 8, 900, 4, 180, "900", "9.1", "3.2",
				"Flood shelters activated", "Char livelihood programs");
		district("Gaibandha", "Rangpur", 25.3297, 89.5430, 2_500_000L, 9, 950, 3, 200, "850", "8.8", "3.5",
				"Brahmaputra embankment watch", "Flood-resilient crops");
	}

	private void district(String name, String div, double lat, double lon, long pop, int hosp, int schools,
			int projects, int crime, String budget, String flood, String dengue, String weather, String invest) {
		districtRepository.save(DistrictProfile.builder()
				.districtName(name).division(div).latitude(lat).longitude(lon)
				.population(pop).hospitals(hosp).schools(schools).activeProjects(projects)
				.crimeCasesMonth(crime).budgetCrore(new BigDecimal(budget))
				.floodRiskScore(new BigDecimal(flood)).dengueRiskScore(new BigDecimal(dengue))
				.weatherNote(weather).investmentNote(invest).build());
	}

	private void seedDocuments(Map<MinistryCode, Ministry> ministries) {
		documentRepository.save(GovDocument.builder().title("National Budget FY 2025-26 Circular")
				.docType("CIRCULAR").classification("INTERNAL").ministry(ministries.get(MinistryCode.FINANCE))
				.issuedDate(LocalDate.now().minusMonths(2))
				.summary("Budget execution guidelines for all ministries and divisions.")
				.tags("budget,finance,circular").build());
		documentRepository.save(GovDocument.builder().title("Cabinet Decision — Metro Land Compensation")
				.docType("CABINET").classification("CONFIDENTIAL").ministry(ministries.get(MinistryCode.TRANSPORT))
				.issuedDate(LocalDate.now().minusDays(20))
				.summary("Framework for accelerated compensation on MRT Line-1 corridor.")
				.tags("metro,land,cabinet").build());
		documentRepository.save(GovDocument.builder().title("Padma Bridge Rail Link Contract Addendum")
				.docType("CONTRACT").classification("CONFIDENTIAL").ministry(ministries.get(MinistryCode.TRANSPORT))
				.issuedDate(LocalDate.now().minusMonths(6))
				.summary("Contract variation for signaling and trial operations.")
				.tags("padma,rail,contract").build());
		documentRepository.save(GovDocument.builder().title("Dengue Control National Guideline 2026")
				.docType("POLICY").classification("PUBLIC").ministry(ministries.get(MinistryCode.HEALTH))
				.issuedDate(LocalDate.now().minusMonths(1))
				.summary("Clinical and municipal vector-control protocol for dengue season.")
				.tags("health,dengue,policy").build());
		documentRepository.save(GovDocument.builder().title("CAG Interim Audit — Education Procurement")
				.docType("AUDIT").classification("SECRET").ministry(ministries.get(MinistryCode.EDUCATION))
				.issuedDate(LocalDate.now().minusDays(5))
				.summary("Interim findings on furniture package procurement anomalies.")
				.tags("audit,procurement,education").build());
	}

	private void seedHr(Map<String, UserAccount> users, Map<MinistryCode, Ministry> ministries) {
		employeeRepository.save(EmployeeProfile.builder().user(users.get("field.dhaka"))
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT)).employeeId("EMP-LGD-10021")
				.grade("Grade-10").joinDate(LocalDate.now().minusYears(4))
				.lastPromotionDate(LocalDate.now().minusYears(1))
				.monthlySalaryBdt(new BigDecimal("45000")).attendancePercent(new BigDecimal("96.5"))
				.performanceScore(new BigDecimal("82")).trainingStatus("Completed — Digital Reporting")
				.leaveBalanceDays(12).active(true).build());
		employeeRepository.save(EmployeeProfile.builder().user(users.get("field.agri"))
				.ministry(ministries.get(MinistryCode.AGRICULTURE)).employeeId("EMP-MOA-8821")
				.grade("Grade-9").joinDate(LocalDate.now().minusYears(6))
				.lastPromotionDate(LocalDate.now().minusYears(2))
				.monthlySalaryBdt(new BigDecimal("52000")).attendancePercent(new BigDecimal("94.0"))
				.performanceScore(new BigDecimal("88")).trainingStatus("Ongoing — Climate Smart Agri")
				.leaveBalanceDays(8).active(true).build());
		employeeRepository.save(EmployeeProfile.builder().user(users.get("dc.dhaka"))
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT)).employeeId("EMP-LGD-DC-01")
				.grade("Grade-5").joinDate(LocalDate.now().minusYears(15))
				.lastPromotionDate(LocalDate.now().minusMonths(18))
				.monthlySalaryBdt(new BigDecimal("98000")).attendancePercent(new BigDecimal("98.0"))
				.performanceScore(new BigDecimal("91")).trainingStatus("Leadership Cadre")
				.leaveBalanceDays(18).active(true).build());
		employeeRepository.save(EmployeeProfile.builder().user(users.get("finance.sec"))
				.ministry(ministries.get(MinistryCode.FINANCE)).employeeId("EMP-FIN-SEC-01")
				.grade("Grade-1").joinDate(LocalDate.now().minusYears(28))
				.lastPromotionDate(LocalDate.now().minusYears(3))
				.monthlySalaryBdt(new BigDecimal("140000")).attendancePercent(new BigDecimal("99.0"))
				.performanceScore(new BigDecimal("94")).trainingStatus("Complete")
				.leaveBalanceDays(22).active(true).build());
	}

	private void seedOfficeScores(Map<MinistryCode, Ministry> ministries) {
		officeScoreRepository.save(OfficeScore.builder().officeName("DC Office Dhaka")
				.ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT)).district("Dhaka")
				.efficiencyScore(bd("86")).transparencyScore(bd("81")).budgetScore(bd("78"))
				.citizenSatisfaction(bd("74")).projectCompletion(bd("80")).attendanceScore(bd("96"))
				.overallScore(bd("82.5")).build());
		officeScoreRepository.save(OfficeScore.builder().officeName("DAE Rajshahi")
				.ministry(ministries.get(MinistryCode.AGRICULTURE)).district("Rajshahi")
				.efficiencyScore(bd("88")).transparencyScore(bd("84")).budgetScore(bd("79"))
				.citizenSatisfaction(bd("82")).projectCompletion(bd("70")).attendanceScore(bd("94"))
				.overallScore(bd("82.8")).build());
		officeScoreRepository.save(OfficeScore.builder().officeName("DGHS Dhaka Division")
				.ministry(ministries.get(MinistryCode.HEALTH)).district("Dhaka")
				.efficiencyScore(bd("79")).transparencyScore(bd("76")).budgetScore(bd("68"))
				.citizenSatisfaction(bd("71")).projectCompletion(bd("75")).attendanceScore(bd("91"))
				.overallScore(bd("76.7")).build());
		officeScoreRepository.save(OfficeScore.builder().officeName("RHD Dhaka Zone")
				.ministry(ministries.get(MinistryCode.TRANSPORT)).district("Dhaka")
				.efficiencyScore(bd("72")).transparencyScore(bd("70")).budgetScore(bd("65"))
				.citizenSatisfaction(bd("68")).projectCompletion(bd("62")).attendanceScore(bd("89"))
				.overallScore(bd("71.0")).build());
	}

	private void seedComplaints(Map<MinistryCode, Ministry> ministries) {
		complaintRepository.save(CitizenComplaint.builder().trackingId("CMP-DEMO001")
				.citizenName("Rahim Uddin").phone("01700000001")
				.subject("Damaged road near school").description("Main approach road waterlogged after rain.")
				.district("Dhaka").ministry(ministries.get(MinistryCode.LOCAL_GOVERNMENT))
				.status("IN_PROGRESS").build());
		complaintRepository.save(CitizenComplaint.builder().trackingId("CMP-DEMO002")
				.citizenName("Fatema Begum").phone("01800000002")
				.subject("Fertilizer not available").description("Dealer has no TSP for 10 days.")
				.district("Rajshahi").ministry(ministries.get(MinistryCode.AGRICULTURE))
				.status("OPEN").build());
	}

	/** Organizations, budget lines and work programs for PM-grade ministry intelligence */
	private void seedMinistryIntelligence(Map<MinistryCode, Ministry> ministries) {
		String fy = "2025-26";

		// —— Health ——
		Ministry health = ministries.get(MinistryCode.HEALTH);
		GovOrganization dghs = org(health, "DGHS", "Directorate General of Health Services", "স্বাস্থ্য অধিদপ্তর",
				"DIRECTORATE", "Dr. Md. Abu Jafar", "Director General", "4200", "2800", 18500, 16200, 12,
				"78", "74", "42", "Dhaka", "Public hospitals, disease control, supply chain");
		GovOrganization cmsd = org(health, "CMSD", "Central Medical Stores Depot", "কেন্দ্রীয় ঔষধাগার",
				"DEPARTMENT", "Director, CMSD", "Director", "1800", "1450", 920, 880, 8,
				"61", "68", "72", "Dhaka", "National medicine & equipment procurement/distribution");
		GovOrganization dgda = org(health, "DGDA", "Directorate General of Drug Administration", "ঔষধ প্রশাসন অধিদপ্তর",
				"DIRECTORATE", "DG, DGDA", "Director General", "420", "210", 1100, 980, 3,
				"82", "79", "28", "Dhaka", "Drug licensing, quality control");
		line(health, cmsd, null, "PROCUREMENT", "Essential medicines & surgical supplies", "অত্যাবশ্যক ঔষধ ও সার্জিক্যাল সরঞ্জাম",
				"1200", "1100", "980", fy, true, "Mid-year spend 82% — AI flagged unit-price spikes on gloves/IV fluids");
		line(health, dghs, null, "SALARY", "Hospital staff salaries & allowances", "হাসপাতাল কর্মচারী বেতন ও ভাতা",
				"2800", "1400", "1380", fy, false, null);
		line(health, dghs, null, "ADP", "District hospital capacity expansion", "জেলা হাসপাতাল সম্প্রসারণ",
				"2200", "1200", "980", fy, false, null);
		line(health, dghs, null, "MAINTENANCE", "Ambulance & ICU equipment maintenance", "অ্যাম্বুলেন্স ও আইসিইউ রক্ষণাবেক্ষণ",
				"350", "200", "185", fy, false, null);
		line(health, cmsd, null, "CONTINGENCY", "Emergency dengue/outbreak buffer stock", "জরুরি ডেঙ্গু বাফার মজুদ",
				"180", "150", "142", fy, true, "Contingency nearly exhausted before monsoon peak");
		work(health, dghs, "WP-HLT-01", "National dengue larva control drive", "জাতীয় ডেঙ্গু লার্ভা নিধন অভিযান",
				"ONGOING", "85", "52", "61", "Dhaka Division", 12000000, false, null,
				"City corporation drives active in 6 zones");
		work(health, cmsd, "WP-HLT-02", "District cold-chain upgrade for vaccines", "জেলা কোল্ড-চেইন আপগ্রেড",
				"DELAYED", "120", "48", "32", "Nationwide", 0, true,
				"Vendor delivered non-spec freezers in 4 districts — payment held",
				"Independent technical inspection ordered");

		// —— Agriculture ——
		Ministry agri = ministries.get(MinistryCode.AGRICULTURE);
		GovOrganization dae = org(agri, "DAE", "Department of Agricultural Extension", "কৃষি সম্প্রসারণ অধিদপ্তর",
				"DEPARTMENT", "Director General, DAE", "DG", "8500", "5200", 22000, 20500, 18,
				"80", "77", "35", "Dhaka", "Field extension, farmer subsidies, demonstrations");
		GovOrganization badc = org(agri, "BADC", "Bangladesh Agricultural Development Corporation", "বিএডিসি",
				"CORPORATION", "Chairman, BADC", "Chairman", "6200", "4100", 8500, 7900, 22,
				"69", "71", "58", "Dhaka", "Seed, fertilizer, irrigation infrastructure");
		GovOrganization dam = org(agri, "DAM", "Department of Agricultural Marketing", "কৃষি বিপণন অধিদপ্তর",
				"DEPARTMENT", "Director, DAM", "Director", "980", "420", 1400, 1280, 4,
				"75", "70", "31", "Dhaka", "Market price monitoring, storage logistics");
		line(agri, badc, null, "SUBSIDY", "Fertilizer subsidy (urea/TSP/MoP)", "সার ভর্তুকি",
				"12000", "9000", "8800", fy, false, null);
		line(agri, dae, null, "ADP", "Northern irrigation modernization packages", "উত্তরাঞ্চল সেচ আধুনিকীকরণ",
				"3200", "1800", "1400", fy, true, "Land acquisition stall — releases continue without physical progress");
		line(agri, badc, null, "PROCUREMENT", "Seed production & storage bags", "বীজ উৎপাদন ও সংরক্ষণ",
				"1500", "900", "720", fy, false, null);
		line(agri, dae, null, "SALARY", "Extension officer salaries", "সম্প্রসারণ কর্মকর্তা বেতন",
				"4100", "2100", "2050", fy, false, null);
		work(agri, dae, "WP-AGR-01", "Boro season fertilizer dealer audit", "বোরো মৌসুম সার ডিলার অডিট",
				"ONGOING", "45", "18", "55", "Rangpur Division", 850000, false, null,
				"TSP shortage mapped in 14 upazilas");
		work(agri, badc, "WP-AGR-02", "Public food silo construction (north)", "সরকারি খাদ্য সাইলো নির্মাণ",
				"ONGOING", "900", "420", "48", "Rangpur/Rajshahi", 0, false, null,
				"12 silos under construction");

		// —— Education ——
		Ministry edu = ministries.get(MinistryCode.EDUCATION);
		GovOrganization dpe = org(edu, "DPE", "Directorate of Primary Education", "প্রাথমিক শিক্ষা অধিদপ্তর",
				"DIRECTORATE", "DG, DPE", "Director General", "18500", "9200", 45000, 42100, 45,
				"76", "73", "38", "Dhaka", "Primary schools, teacher payroll, infrastructure");
		GovOrganization dshe = org(edu, "DSHE", "Directorate of Secondary & Higher Education", "মাধ্যমিক ও উচ্চ শিক্ষা অধিদপ্তর",
				"DIRECTORATE", "DG, DSHE", "Director General", "22000", "11000", 38000, 35500, 28,
				"74", "72", "33", "Dhaka", "Secondary schools, exams, teacher training");
		GovOrganization nctb = org(edu, "NCTB", "National Curriculum & Textbook Board", "এনসিটিবি",
				"AUTONOMOUS", "Chairman, NCTB", "Chairman", "2800", "1900", 2100, 1980, 6,
				"71", "80", "48", "Dhaka", "Textbooks, curriculum, printing contracts");
		line(edu, dpe, null, "SALARY", "Primary teacher salaries", "প্রাথমিক শিক্ষক বেতন",
				"28000", "14000", "13850", fy, false, null);
		line(edu, dpe, null, "ADP", "Primary school classroom construction", "প্রাথমিক শ্রেণিকক্ষ নির্মাণ",
				"8500", "4200", "3800", fy, false, null);
		line(edu, nctb, null, "PROCUREMENT", "Textbook printing & distribution", "পাঠ্যপুস্তক মুদ্রণ ও বিতরণ",
				"3200", "2800", "2650", fy, true, "Single-vendor concentration; price above market median");
		line(edu, dshe, null, "NON_DEVELOPMENT", "Exam operations & stipends", "পরীক্ষা ও উপবৃত্তি",
				"4500", "2200", "2100", fy, false, null);
		work(edu, dpe, "WP-EDU-01", "Teacher biometric attendance rollout", "শিক্ষক বায়োমেট্রিক উপস্থিতি",
				"ONGOING", "180", "95", "58", "Nationwide", 400000, true,
				"17% devices offline; ghost attendance risk in 3 districts",
				"Network SLA enforcement with vendor");

		// —— Transport ——
		Ministry trn = ministries.get(MinistryCode.TRANSPORT);
		GovOrganization rhd = org(trn, "RHD", "Roads and Highways Department", "সড়ক ও জনপথ অধিদপ্তর",
				"DEPARTMENT", "Chief Engineer, RHD", "Chief Engineer", "28000", "18500", 12000, 11200, 15,
				"68", "65", "55", "Dhaka", "National highways, bridges maintenance");
		GovOrganization brta = org(trn, "BRTA", "Bangladesh Road Transport Authority", "বিআরটিএ",
				"AUTONOMOUS", "Chairman, BRTA", "Chairman", "1200", "620", 6500, 6100, 9,
				"72", "70", "40", "Dhaka", "Vehicle registration, fitness, digital services");
		GovOrganization bba = org(trn, "BBA", "Bangladesh Bridge Authority", "সেতু কর্তৃপক্ষ",
				"AUTONOMOUS", "Executive Director, BBA", "ED", "15000", "9800", 1800, 1650, 2,
				"81", "78", "25", "Dhaka", "Mega bridge projects operations");
		line(trn, rhd, null, "ADP", "National highway packages (FY)", "জাতীয় মহাসড়ক প্যাকেজ",
				"22000", "15000", "14200", fy, false, null);
		line(trn, rhd, null, "PROCUREMENT", "Bitumen & road materials", "বিটুমেন ও সড়ক উপকরণ",
				"4500", "3200", "3100", fy, true, "Conflict-of-interest flag on one major supplier");
		line(trn, bba, null, "ADP", "Metro / bridge linked packages", "মেট্রো/সেতু সংশ্লিষ্ট প্যাকেজ",
				"18000", "9000", "8200", fy, false, null);
		work(trn, rhd, "WP-TRN-01", "Gulshan corridor emergency patch repairs", "গুলশান করিডোর জরুরি মেরামত",
				"ONGOING", "35", "12", "40", "Dhaka", 0, false, null, "Night work restrictions applied");

		// —— Finance ——
		Ministry fin = ministries.get(MinistryCode.FINANCE);
		GovOrganization fdiv = org(fin, "FD", "Finance Division", "অর্থ বিভাগ",
				"DIVISION", "Senior Secretary, FD", "Senior Secretary", "1200", "480", 2200, 2100, 1,
				"88", "90", "15", "Dhaka", "Budget, cash flow, fiscal policy");
		GovOrganization nbr = org(fin, "NBR", "National Board of Revenue", "এনবিআর",
				"AUTONOMOUS", "Chairman, NBR", "Chairman", "3500", "1600", 18500, 17200, 25,
				"70", "75", "52", "Dhaka", "Tax, customs, VAT administration");
		line(fin, nbr, null, "NON_DEVELOPMENT", "Tax administration operations", "কর প্রশাসন পরিচালনা",
				"2800", "1400", "1320", fy, false, null);
		line(fin, fdiv, null, "CONTINGENCY", "Fiscal contingency reserve", "রাজস্ব জরুরি রিজার্ভ",
				"5000", "800", "420", fy, false, null);
		work(fin, nbr, "WP-FIN-01", "e-TIN & VAT online enforcement sweep", "ই-টিআইএন ও ভ্যাট অনলাইন অভিযান",
				"ONGOING", "60", "28", "47", "Nationwide", 0, false, null, "Digital audit flags rising");

		// —— Local Government ——
		Ministry lgd = ministries.get(MinistryCode.LOCAL_GOVERNMENT);
		GovOrganization lgeng = org(lgd, "LGED", "Local Government Engineering Department", "এলজিইডি",
				"DEPARTMENT", "Chief Engineer, LGED", "Chief Engineer", "16000", "9200", 28000, 26100, 35,
				"73", "71", "44", "Dhaka", "Rural roads, markets, upazila infrastructure");
		GovOrganization dphe = org(lgd, "DPHE", "Department of Public Health Engineering", "জনস্বাস্থ্য প্রকৌশল অধিদপ্তর",
				"DEPARTMENT", "Chief Engineer, DPHE", "Chief Engineer", "4800", "2400", 9200, 8600, 11,
				"77", "74", "36", "Dhaka", "Water supply, sanitation");
		line(lgd, lgeng, null, "ADP", "Union/upazila road schemes", "ইউনিয়ন/উপজেলা সড়ক",
				"9500", "5000", "4700", fy, false, null);
		line(lgd, dphe, null, "ADP", "Rural water & sanitation", "গ্রামীণ পানি ও স্যানিটেশন",
				"3200", "1600", "1480", fy, false, null);
		work(lgd, lgeng, "WP-LGD-01", "Municipal drainage inspections — Dhaka North", "পৌর ড্রেনেজ পরিদর্শন",
				"ONGOING", "25", "8", "52", "Dhaka", 250000, false, null, "Waterlogging risk mapped");

		// —— Power ——
		Ministry power = ministries.get(MinistryCode.POWER);
		GovOrganization bpdb = org(power, "BPDB", "Bangladesh Power Development Board", "পিডিবি",
				"CORPORATION", "Chairman, BPDB", "Chairman", "22000", "14000", 16000, 15100, 14,
				"74", "69", "41", "Dhaka", "Generation & distribution utilities coordination");
		org(power, "PGCB", "Power Grid Company of Bangladesh", "পিজিসিবি",
				"CORPORATION", "MD, PGCB", "Managing Director", "8500", "5100", 4200, 4000, 3,
				"79", "76", "27", "Dhaka", "National transmission grid");
		line(power, bpdb, null, "ADP", "Generation capacity expansion support", "উৎপাদন সক্ষমতা সম্প্রসারণ",
				"15000", "9000", "8600", fy, false, null);
		line(power, bpdb, null, "PROCUREMENT", "Fuel & spare parts for plants", "জ্বালানি ও যন্ত্রাংশ",
				"6000", "4000", "3850", fy, false, null);

		// —— Home ——
		Ministry home = ministries.get(MinistryCode.HOME_POLICE);
		org(home, "BP", "Bangladesh Police HQ", "বাংলাদেশ পুলিশ সদর",
				"AGENCY", "IGP", "Inspector General", "18000", "9200", 210000, 198000, 120,
				"71", "68", "47", "Dhaka", "Law & order, investigation, traffic");
		org(home, "RAB", "Rapid Action Battalion", "র‌্যাব",
				"AGENCY", "DG, RAB", "Director General", "2200", "1100", 12000, 11500, 5,
				"75", "72", "30", "Dhaka", "Specialized operations");
		line(home, null, null, "SALARY", "Police force salaries & allowances", "পুলিশ বাহিনী বেতন",
				"22000", "11000", "10850", fy, false, null);
		line(home, null, null, "PROCUREMENT", "Logistics, arms & digital policing", "লজিস্টিকস ও ডিজিটাল পুলিশিং",
				"3500", "1800", "1650", fy, false, null);

		// —— Disaster ——
		Ministry dmr = ministries.get(MinistryCode.DISASTER_MANAGEMENT);
		org(dmr, "DDM", "Department of Disaster Management", "দুর্যোগ ব্যবস্থাপনা অধিদপ্তর",
				"DEPARTMENT", "Director General, DDM", "DG", "2800", "1500", 3200, 3000, 4,
				"80", "78", "22", "Dhaka", "Preparedness, shelters, relief");
		line(dmr, null, null, "CONTINGENCY", "Emergency relief & GR/TR stocks", "জরুরি ত্রাণ মজুদ",
				"1500", "800", "620", fy, false, null);
		work(dmr, null, "WP-DMR-01", "Northern flood shelter readiness", "উত্তরাঞ্চল বন্যা আশ্রয়কেন্দ্র প্রস্তুতি",
				"ONGOING", "90", "35", "48", "Rangpur Division", 500000, false, null,
				"Shelters on standby; stock pre-positioned");

		// —— ICT ——
		Ministry ict = ministries.get(MinistryCode.ICT);
		org(ict, "A2I", "Aspire to Innovate (a2i)", "এটুআই",
				"AGENCY", "Policy Advisor, a2i", "Policy Advisor", "900", "480", 850, 820, 1,
				"86", "88", "12", "Dhaka", "Digital services, e-gov innovation");
		org(ict, "BCC", "Bangladesh Computer Council", "বিসিসি",
				"AUTONOMOUS", "Executive Director, BCC", "ED", "1200", "620", 1100, 1040, 2,
				"83", "81", "18", "Dhaka", "Gov cloud, cyber security capacity");
		line(ict, null, null, "ADP", "Digital Bangladesh 2.0 service rollout", "ডিজিটাল বাংলাদেশ ২.০",
				"2200", "1200", "1100", fy, false, null);

		// —— Planning ——
		Ministry plan = ministries.get(MinistryCode.PLANNING);
		org(plan, "IMED", "Implementation Monitoring & Evaluation Division", "আইএমইডি",
				"DIVISION", "Secretary, IMED", "Secretary", "450", "180", 980, 940, 0,
				"89", "91", "10", "Dhaka", "Project monitoring, evaluation, anti-delay oversight");
		line(plan, null, null, "NON_DEVELOPMENT", "National planning & IMED operations", "পরিকল্পনা ও মনিটরিং",
				"800", "350", "320", fy, false, null);

		// —— Foreign Affairs ——
		Ministry mofa = ministries.get(MinistryCode.FOREIGN_AFFAIRS);
		org(mofa, "MOFA-HQ", "Ministry of Foreign Affairs HQ", "পররাষ্ট্র মন্ত্রণালয় সদর",
				"DEPARTMENT", "Foreign Secretary", "Foreign Secretary", "3200", "1400", 2400, 2300, 2,
				"85", "84", "14", "Dhaka", "Diplomacy, missions, agreements");
		line(mofa, null, null, "NON_DEVELOPMENT", "Embassy operations & representation", "দূতাবাস পরিচালনা",
				"4500", "2200", "2050", fy, false, null);

		log.info("Seeded ministry organizations, budget lines and work programs for intelligence reports");
	}

	private GovOrganization org(Ministry m, String code, String en, String bn, String type,
			String head, String desig, String budget, String spent, int sanc, int fill, int anomaly,
			String transparency, String efficiency, String risk, String district, String mandate) {
		return orgRepository.save(GovOrganization.builder()
				.ministry(m).orgCode(code).nameEn(en).nameBn(bn).orgType(type)
				.headName(head).headDesignation(desig)
				.budgetCrore(bd(budget)).spentCrore(bd(spent))
				.sanctionedPosts(sanc).filledPosts(fill).anomalyHeadcount(anomaly)
				.transparencyScore(bd(transparency)).efficiencyScore(bd(efficiency))
				.corruptionRiskScore(bd(risk)).district(district).mandate(mandate).active(true)
				.build());
	}

	private void line(Ministry m, GovOrganization org, GovProject project, String cat,
			String titleEn, String titleBn, String alloc, String released, String spent,
			String fy, boolean flagged, String reason) {
		budgetLineRepository.save(BudgetLine.builder()
				.ministry(m).organization(org).project(project).category(cat)
				.titleEn(titleEn).titleBn(titleBn)
				.allocatedCrore(bd(alloc)).releasedCrore(bd(released)).spentCrore(bd(spent))
				.fiscalYear(fy).flagged(flagged).flagReason(reason)
				.build());
	}

	private void work(Ministry m, GovOrganization org, String code, String en, String bn,
			String status, String budget, String spent, String progress, String location,
			int beneficiaries, boolean issue, String issueNote, String update) {
		workProgramRepository.save(WorkProgram.builder()
				.ministry(m).organization(org).workCode(code).titleEn(en).titleBn(bn)
				.description(en).status(status)
				.budgetCrore(bd(budget)).spentCrore(bd(spent)).progressPercent(bd(progress))
				.location(location).beneficiaries(beneficiaries)
				.startDate(LocalDate.now().minusMonths(6)).endDate(LocalDate.now().plusMonths(6))
				.latestUpdate(update).transparencyIssue(issue).issueNote(issueNote)
				.build());
	}

	private BigDecimal bd(String v) {
		return new BigDecimal(v);
	}
}

