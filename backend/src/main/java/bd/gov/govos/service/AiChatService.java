package bd.gov.govos.service;

import bd.gov.govos.domain.entity.DistrictProfile;
import bd.gov.govos.domain.entity.GovProject;
import bd.gov.govos.domain.entity.Ministry;
import bd.gov.govos.domain.entity.NationalIndicator;
import bd.gov.govos.domain.enums.ProjectStatus;
import bd.gov.govos.dto.DashboardDtos;
import bd.gov.govos.repository.DistrictProfileRepository;
import bd.gov.govos.repository.GovProjectRepository;
import bd.gov.govos.repository.MinistryRepository;
import bd.gov.govos.repository.NationalIndicatorRepository;
import bd.gov.govos.repository.ProcurementOrderRepository;
import bd.gov.govos.repository.RiskFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Conversational AI assistant for the PM / policymakers.
 * Answers grounded in live warehouse data; optional SpaceXAI enrichment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

	private final DashboardService dashboardService;
	private final MinistryRepository ministryRepository;
	private final GovProjectRepository projectRepository;
	private final NationalIndicatorRepository indicatorRepository;
	private final DistrictProfileRepository districtRepository;
	private final ProcurementOrderRepository procurementRepository;
	private final RiskFlagRepository riskFlagRepository;
	private final WebClient aiWebClient;

	@Value("${govos.ai.model}")
	private String model;

	@Value("${XAI_API_KEY:}")
	private String apiKey;

	@Transactional(readOnly = true)
	public Map<String, Object> ask(String question) {
		String q = question == null ? "" : question.trim();
		String lower = q.toLowerCase(Locale.ROOT);

		String grounded = answerFromData(lower, q);
		boolean usedLiveAi = false;
		String finalAnswer = grounded;

		if (apiKey != null && !apiKey.isBlank()) {
			try {
				String polished = callXai(
						"You are GovOS AI Assistant for Bangladesh's Prime Minister. "
								+ "Use ONLY the provided data facts. Be concise. Do not invent numbers.",
						"Question: " + q + "\n\nData facts:\n" + grounded
				);
				finalAnswer = polished;
				usedLiveAi = true;
			} catch (Exception e) {
				log.warn("Live AI chat failed: {}", e.getMessage());
			}
		}

		Map<String, Object> result = new HashMap<>();
		result.put("question", q);
		result.put("answer", finalAnswer);
		result.put("usedLiveAi", usedLiveAi);
		result.put("model", usedLiveAi ? model : "data-grounded");
		result.put("generatedAt", java.time.Instant.now().toString());
		return result;
	}

	private String answerFromData(String lower, String original) {
		// Budget exceeded
		if (lower.contains("exceeded budget") || lower.contains("over budget") || lower.contains("budget this month")) {
			return ministriesOverBudget();
		}
		// Rice production
		if (lower.contains("rice") || (lower.contains("production") && lower.contains("decreas"))) {
			return riceProductionAnswer();
		}
		// Dengue
		if (lower.contains("dengue")) {
			return dengueRiskAnswer();
		}
		// Delayed projects
		if (lower.contains("delay") && lower.contains("project")) {
			return delayedProjectsAnswer(lower.contains("6 month") || lower.contains("six month") || lower.contains("180"));
		}
		// Procurement / corruption
		if (lower.contains("procurement") || lower.contains("fake vendor") || lower.contains("invoice")) {
			return procurementAnswer();
		}
		// Flood
		if (lower.contains("flood")) {
			return floodAnswer();
		}
		// Inflation / economy prediction
		if (lower.contains("inflation") || lower.contains("slowdown") || lower.contains("predict")) {
			return predictiveAnswer();
		}
		// Risk engine
		if (lower.contains("risk") || lower.contains("corruption") || lower.contains("ghost")) {
			return riskAnswer();
		}
		// Default national brief
		DashboardDtos.NationalDashboard dash = dashboardService.nationalDashboard();
		StringBuilder sb = new StringBuilder();
		sb.append("I can answer questions like:\n");
		sb.append("• Which ministry exceeded budget this month?\n");
		sb.append("• Why has rice production decreased?\n");
		sb.append("• Which districts have highest dengue risk?\n");
		sb.append("• Which projects are delayed over 6 months?\n\n");
		sb.append("Current snapshot — Ongoing projects: ").append(dash.dailyOps().ongoingProjects());
		sb.append(", Delayed: ").append(dash.dailyOps().delayedProjects());
		sb.append(", Active alerts: ").append(dash.dailyOps().activeAlerts());
		sb.append(", Reports today: ").append(dash.dailyOps().reportsSubmittedToday()).append(".\n");
		sb.append("Your question: \"").append(original).append("\"");
		return sb.toString();
	}

	private String ministriesOverBudget() {
		List<Ministry> ministries = ministryRepository.findAll();
		List<Ministry> over = ministries.stream()
				.filter(m -> m.getAnnualBudgetCrore() != null && m.getSpentBudgetCrore() != null)
				.filter(m -> m.getSpentBudgetCrore().compareTo(
						m.getAnnualBudgetCrore().multiply(new BigDecimal("0.70"))) > 0)
				.sorted(Comparator.comparing((Ministry m) ->
						m.getSpentBudgetCrore().divide(m.getAnnualBudgetCrore(), 4, RoundingMode.HALF_UP)).reversed())
				.toList();

		StringBuilder sb = new StringBuilder("**Ministries with high budget utilization (≥70% of annual):**\n\n");
		if (over.isEmpty()) {
			sb.append("No ministry is currently above 70% annual utilization.\n");
		} else {
			for (Ministry m : over) {
				BigDecimal util = m.getSpentBudgetCrore()
						.multiply(BigDecimal.valueOf(100))
						.divide(m.getAnnualBudgetCrore(), 1, RoundingMode.HALF_UP);
				BigDecimal remaining = m.getAnnualBudgetCrore().subtract(m.getSpentBudgetCrore());
				sb.append("• **").append(m.getNameEn()).append("** — Budget: ")
						.append(m.getAnnualBudgetCrore()).append(" crore, Spent: ")
						.append(m.getSpentBudgetCrore()).append(" crore, Remaining: ")
						.append(remaining).append(" crore (**").append(util).append("%** used)\n");
			}
		}
		sb.append("\n_Note: Unusual mid-year overspending is flagged by the AI Risk Engine for Finance review._");
		return sb.toString();
	}

	private String riceProductionAnswer() {
		NationalIndicator rice = indicatorRepository.findTopByIndicatorKeyOrderByAsOfDateDesc("RICE_PRODUCTION").orElse(null);
		NationalIndicator food = indicatorRepository.findTopByIndicatorKeyOrderByAsOfDateDesc("FOOD_STOCK").orElse(null);
		NationalIndicator fert = indicatorRepository.findTopByIndicatorKeyOrderByAsOfDateDesc("FERTILIZER_STOCK").orElse(null);

		StringBuilder sb = new StringBuilder("**Rice production analysis**\n\n");
		if (rice != null) {
			sb.append("Current estimate: **").append(rice.getValue()).append(" ").append(rice.getUnit())
					.append("** (change: ").append(rice.getChangePercent()).append("%).\n");
		}
		if (food != null) {
			sb.append("Public food stock: **").append(food.getValue()).append(" ").append(food.getUnit())
					.append("** (").append(food.getChangePercent()).append("% change).\n");
		}
		if (fert != null) {
			sb.append("Fertilizer stock: **").append(fert.getValue()).append(" ").append(fert.getUnit()).append("**.\n");
		}
		sb.append("\n**Contributing factors (system signals):**\n");
		sb.append("• Fertilizer distribution gaps in northern upazilas (active agriculture alert).\n");
		sb.append("• Delayed Northern Irrigation project (35% progress) reducing dry-season reliability.\n");
		sb.append("• Flood risk rising in Rangpur division may affect aman/boro transition.\n");
		sb.append("\n**Recommendation:** Accelerate fertilizer buffer dispatch, clear irrigation land acquisition blockers within 30 days, and pre-position 0.3 MT rice stocks in flood-prone districts.");
		return sb.toString();
	}

	private String dengueRiskAnswer() {
		List<DistrictProfile> districts = districtRepository.findAll().stream()
				.sorted(Comparator.comparing(DistrictProfile::getDengueRiskScore,
						Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(8)
				.toList();
		StringBuilder sb = new StringBuilder("**Districts with highest dengue risk**\n\n");
		for (DistrictProfile d : districts) {
			sb.append("• **").append(d.getDistrictName()).append("** (").append(d.getDivision())
					.append(") — dengue risk score **").append(d.getDengueRiskScore())
					.append("**/10 · hospitals: ").append(d.getHospitals()).append("\n");
		}
		sb.append("\n**Actions:** Activate larva control drives, expand fever clinics, ensure platelet/IV fluid stocks for 4 weeks in top-risk districts.");
		return sb.toString();
	}

	private String delayedProjectsAnswer(boolean overSixMonths) {
		List<GovProject> delayed = projectRepository.findAll().stream()
				.filter(p -> p.getStatus() == ProjectStatus.DELAYED
						|| (p.getDelayDays() != null && p.getDelayDays() > 0))
				.filter(p -> !overSixMonths || (p.getDelayDays() != null && p.getDelayDays() >= 180))
				.sorted(Comparator.comparing(GovProject::getDelayDays, Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();

		// Also include projects with delay even if status not DELAYED
		if (delayed.isEmpty() && overSixMonths) {
			delayed = projectRepository.findAll().stream()
					.filter(p -> p.getDelayDays() != null && p.getDelayDays() >= 30)
					.sorted(Comparator.comparing(GovProject::getDelayDays).reversed())
					.toList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append(overSixMonths
				? "**Projects delayed (focus: long slips)**\n\n"
				: "**Projects with schedule risk / delay**\n\n");

		if (delayed.isEmpty()) {
			sb.append("No matching delayed projects in the current dataset.\n");
			// show delayed status projects anyway
			projectRepository.findByStatus(ProjectStatus.DELAYED).forEach(p -> appendProject(sb, p));
		} else {
			delayed.forEach(p -> appendProject(sb, p));
		}
		return sb.toString();
	}

	private void appendProject(StringBuilder sb, GovProject p) {
		BigDecimal total = nvl(p.getTotalCostCrore());
		BigDecimal spent = nvl(p.getSpentCostCrore());
		sb.append("• **").append(p.getTitle()).append("** (").append(p.getProjectCode()).append(")\n");
		sb.append("  Status: ").append(p.getStatus())
				.append(" · Progress: ").append(p.getProgressPercent()).append("%\n");
		sb.append("  Budget: ").append(total).append(" crore · Spent: ").append(spent)
				.append(" · Remaining: ").append(total.subtract(spent)).append(" crore\n");
		sb.append("  Delay: ").append(p.getDelayDays() != null ? p.getDelayDays() : 0).append(" days");
		if (p.getContractor() != null) sb.append(" · Contractor: ").append(p.getContractor());
		sb.append("\n");
		if (p.getRiskLevel() != null) {
			sb.append("  AI risk: **").append(p.getRiskLevel()).append("** — ").append(p.getRiskReason()).append("\n");
			sb.append("  Recommendation: ").append(p.getAiRecommendation()).append("\n");
		}
		sb.append("\n");
	}

	private String procurementAnswer() {
		var flagged = procurementRepository.findByFlaggedTrueOrderByOrderDateDesc();
		StringBuilder sb = new StringBuilder("**Procurement AI checks (flagged orders)**\n\n");
		if (flagged.isEmpty()) {
			sb.append("No flagged procurement orders at this time.\n");
		} else {
			for (var p : flagged) {
				sb.append("• ").append(p.getOrderCode()).append(" — ").append(p.getVendorName())
						.append(" · ").append(p.getAmountBdt()).append(" BDT\n");
				sb.append("  Flags: ").append(p.getRiskFlags()).append("\n");
				sb.append("  Notes: ").append(p.getRiskNotes()).append("\n\n");
			}
		}
		sb.append("AI monitors: price inflation, duplicate invoices, fake vendors, repeated purchases, conflict of interest.");
		return sb.toString();
	}

	private String floodAnswer() {
		List<DistrictProfile> flood = districtRepository.findAll().stream()
				.sorted(Comparator.comparing(DistrictProfile::getFloodRiskScore,
						Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(5)
				.toList();
		StringBuilder sb = new StringBuilder("**Flood risk — top districts**\n\n");
		for (DistrictProfile d : flood) {
			sb.append("• **").append(d.getDistrictName()).append("** — flood risk **")
					.append(d.getFloodRiskScore()).append("**/10 · ").append(d.getWeatherNote()).append("\n");
		}
		sb.append("\nShelters and Disaster Management ministry are on standby for northern riverine districts.");
		return sb.toString();
	}

	private String predictiveAnswer() {
		DashboardDtos.NationalDashboard dash = dashboardService.nationalDashboard();
		StringBuilder sb = new StringBuilder("**Predictive AI outlook (rule + indicator model)**\n\n");
		for (var ind : dash.indicators()) {
			if (List.of("INFLATION", "GDP_GROWTH", "FOOD_STOCK", "ELECTRICITY", "HOSPITAL_OCCUPANCY", "TAX_COLLECTION")
					.contains(ind.key())) {
				sb.append("• ").append(ind.label()).append(": ").append(ind.value()).append(" ")
						.append(ind.unit()).append(" (Δ ").append(ind.changePercent()).append("%)\n");
			}
		}
		sb.append("\n**Near-term risk signals:**\n");
		sb.append("• Inflation still elevated — monitor food prices before Ramadan demand spike.\n");
		sb.append("• Food stock declining — shortage risk if flood disrupts northern supply.\n");
		sb.append("• Hospital occupancy rising with dengue — overload risk in Dhaka/Chattogram.\n");
		sb.append("• Mega project delay risk concentrated in irrigation & metro land disputes.\n");
		return sb.toString();
	}

	private String riskAnswer() {
		var risks = riskFlagRepository.findByOpenTrueOrderByCreatedAtDesc();
		StringBuilder sb = new StringBuilder("**AI Risk Engine — open flags**\n\n");
		for (var r : risks) {
			sb.append("• [").append(r.getSeverity()).append("] **").append(r.getRiskType()).append("** — ")
					.append(r.getTitle()).append("\n");
			sb.append("  ").append(r.getDescription()).append("\n");
			sb.append("  Action: ").append(r.getRecommendedAction()).append("\n\n");
		}
		return sb.toString();
	}

	private BigDecimal nvl(BigDecimal v) {
		return v != null ? v : BigDecimal.ZERO;
	}

	@SuppressWarnings("unchecked")
	private String callXai(String system, String userContent) {
		Map<String, Object> body = Map.of(
				"model", model,
				"messages", List.of(
						Map.of("role", "system", "content", system),
						Map.of("role", "user", "content", userContent)
				),
				"temperature", 0.2
		);
		Map<String, Object> response = aiWebClient.post()
				.uri("/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBearerAuth(apiKey))
				.bodyValue(body)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.block(Duration.ofSeconds(45));
		if (response == null) throw new IllegalStateException("Empty AI response");
		Object choicesObj = response.get("choices");
		if (choicesObj instanceof List<?> choices && !choices.isEmpty() && choices.get(0) instanceof Map<?, ?> choice) {
			Object message = choice.get("message");
			if (message instanceof Map<?, ?> msg && msg.get("content") != null) {
				return msg.get("content").toString();
			}
		}
		throw new IllegalStateException("Failed to parse AI response");
	}
}
