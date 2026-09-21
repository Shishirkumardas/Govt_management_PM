package bd.gov.govos.service;

import bd.gov.govos.domain.entity.AiRecommendation;
import bd.gov.govos.dto.DashboardDtos;
import bd.gov.govos.repository.AiRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Decision Engine — uses SpaceXAI (xAI) for policy recommendations.
 * Falls back to rule-based insights when API key is missing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiDecisionService {

	private final WebClient aiWebClient;
	private final DashboardService dashboardService;
	private final AiRecommendationRepository aiRecommendationRepository;

	@Value("${govos.ai.model}")
	private String model;

	@Value("${govos.ai.enabled:true}")
	private boolean aiEnabled;

	@Value("${XAI_API_KEY:}")
	private String apiKey;

	@Transactional(readOnly = true)
	public Map<String, Object> nationalBriefing() {
		DashboardDtos.NationalDashboard dash = dashboardService.nationalDashboard();
		String context = buildNationalContext(dash);

		String analysis;
		boolean usedLiveAi = false;

		if (aiEnabled && apiKey != null && !apiKey.isBlank()) {
			try {
				analysis = callXai(
						"You are the AI Decision Engine for GovOS Bangladesh, advising the Prime Minister. "
								+ "Be concise, actionable, and data-driven. Focus on risks, budget, food security, "
								+ "mega projects, and immediate actions. Use bullet points.",
						context
				);
				usedLiveAi = true;
			} catch (Exception e) {
				log.warn("Live AI unavailable, using rule-based briefing: {}", e.getMessage());
				analysis = ruleBasedNationalBriefing(dash);
			}
		} else {
			analysis = ruleBasedNationalBriefing(dash);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("briefing", analysis);
		result.put("usedLiveAi", usedLiveAi);
		result.put("model", usedLiveAi ? model : "rule-based");
		result.put("generatedAt", dash.generatedAt());
		result.put("storedRecommendations", dash.aiRecommendations());
		return result;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> ministryBriefing(Long ministryId) {
		DashboardDtos.MinistryDashboard dash = dashboardService.ministryDashboard(ministryId);
		String context = "Ministry: " + dash.ministry().nameEn()
				+ "\nBudget utilization: " + dash.ministry().utilizationPercent() + "%"
				+ "\nActive projects: " + dash.ministry().activeProjects()
				+ "\nReports today: " + dash.ministry().reportsToday()
				+ "\nProjects: " + dash.projects().size()
				+ "\nAlerts: " + dash.alerts().size();

		String analysis;
		boolean usedLiveAi = false;
		if (aiEnabled && apiKey != null && !apiKey.isBlank()) {
			try {
				analysis = callXai(
						"You advise a Bangladesh Cabinet Minister. Give 5 concrete actions based on the data.",
						context
				);
				usedLiveAi = true;
			} catch (Exception e) {
				analysis = "Budget utilization at " + dash.ministry().utilizationPercent()
						+ "%. Review delayed projects and field problems flagged in daily reports.";
			}
		} else {
			analysis = "Budget utilization at " + dash.ministry().utilizationPercent()
					+ "%. Active projects: " + dash.ministry().activeProjects()
					+ ". Review alerts and daily field reports for operational risks.";
		}

		Map<String, Object> result = new HashMap<>();
		result.put("briefing", analysis);
		result.put("usedLiveAi", usedLiveAi);
		result.put("ministry", dash.ministry().nameEn());
		return result;
	}

	@Transactional(readOnly = true)
	public List<DashboardDtos.AiRecommendationItem> listRecommendations() {
		return aiRecommendationRepository.findByAcknowledgedFalseOrderByCreatedAtDesc().stream()
				.map(a -> DashboardDtos.AiRecommendationItem.builder()
						.id(a.getId())
						.title(a.getTitle())
						.body(a.getBody())
						.scope(a.getScope())
						.priority(a.getPriority())
						.build())
				.toList();
	}

	@Transactional
	public void acknowledge(Long id) {
		AiRecommendation rec = aiRecommendationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
		rec.setAcknowledged(true);
		aiRecommendationRepository.save(rec);
	}

	@SuppressWarnings("unchecked")
	private String callXai(String system, String userContent) {
		Map<String, Object> body = Map.of(
				"model", model,
				"messages", List.of(
						Map.of("role", "system", "content", system),
						Map.of("role", "user", "content", userContent)
				),
				"temperature", 0.3
		);

		Map<String, Object> response = aiWebClient.post()
				.uri("/chat/completions")
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBearerAuth(apiKey))
				.bodyValue(body)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.block(Duration.ofSeconds(45));

		if (response == null) {
			throw new IllegalStateException("Empty AI response");
		}
		Object choicesObj = response.get("choices");
		if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
			Object first = choices.get(0);
			if (first instanceof Map<?, ?> choice) {
				Object message = choice.get("message");
				if (message instanceof Map<?, ?> msg) {
					Object content = msg.get("content");
					if (content != null) {
						return content.toString();
					}
				}
			}
		}
		throw new IllegalStateException("Failed to parse AI response");
	}

	private String buildNationalContext(DashboardDtos.NationalDashboard dash) {
		StringBuilder sb = new StringBuilder("National indicators:\n");
		dash.indicators().forEach(i ->
				sb.append("- ").append(i.label()).append(": ").append(i.value())
						.append(" ").append(i.unit()).append("\n"));
		sb.append("\nBudget deficit (month): ").append(dash.budget().deficitBdt()).append(" BDT\n");
		sb.append("Ongoing projects: ").append(dash.dailyOps().ongoingProjects())
				.append(", Delayed: ").append(dash.dailyOps().delayedProjects()).append("\n");
		sb.append("Active alerts: ").append(dash.activeAlerts().size()).append("\n");
		dash.activeAlerts().stream().limit(5).forEach(a ->
				sb.append("- [").append(a.severity()).append("] ").append(a.title()).append("\n"));
		sb.append("Daily reports today: ").append(dash.dailyOps().reportsSubmittedToday()).append("\n");
		return sb.toString();
	}

	private String ruleBasedNationalBriefing(DashboardDtos.NationalDashboard dash) {
		StringBuilder sb = new StringBuilder();
		sb.append("## AI National Briefing (Rule Engine)\n\n");
		sb.append("### Priority Actions\n");

		if (dash.dailyOps().delayedProjects() > 0) {
			sb.append("- **").append(dash.dailyOps().delayedProjects())
					.append(" delayed mega projects** require Cabinet review and cost re-baselining.\n");
		}
		if (dash.budget().deficitBdt() != null && dash.budget().deficitBdt().signum() > 0) {
			sb.append("- Monthly expenditure exceeds revenue by **")
					.append(dash.budget().deficitBdt())
					.append(" BDT**. Consider tighter expenditure control in non-priority ministries.\n");
		}
		long critical = dash.activeAlerts().stream()
				.filter(a -> "CRITICAL".equals(a.severity().name()) || "HIGH".equals(a.severity().name()))
				.count();
		if (critical > 0) {
			sb.append("- **").append(critical)
					.append(" high/critical alerts** active — Disaster Management & Home Affairs should brief immediately.\n");
		}
		if (dash.dailyOps().reportsSubmittedToday() < 5) {
			sb.append("- Field daily reporting compliance is low today (")
					.append(dash.dailyOps().reportsSubmittedToday())
					.append("). Instruct Secretaries to enforce digital submission.\n");
		}

		sb.append("\n### Snapshot\n");
		dash.indicators().stream().limit(8).forEach(i ->
				sb.append("- ").append(i.label()).append(": **").append(i.value())
						.append("** ").append(i.unit() != null ? i.unit() : "").append("\n"));

		sb.append("\n### Foreign Investment\n");
		sb.append("- Active/committed FDI: **")
				.append(dash.budget().foreignInvestmentUsdMillion())
				.append(" USD million**.\n");

		sb.append("\n_Set `XAI_API_KEY` to enable live SpaceXAI (Grok) analysis._");
		return sb.toString();
	}
}
