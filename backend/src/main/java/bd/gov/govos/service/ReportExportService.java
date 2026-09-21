package bd.gov.govos.service;

import bd.gov.govos.dto.DashboardDtos;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportService {

	private final DashboardService dashboardService;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	public byte[] nationalPdf(String period) throws Exception {
		DashboardDtos.NationalDashboard dash = dashboardService.nationalDashboard();
		auditService.recordSimple("EXPORT_PDF", "REPORT", "National " + period);

		try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);

			PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
			PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				float y = 800;
				cs.beginText();
				cs.setFont(titleFont, 16);
				cs.newLineAtOffset(50, y);
				cs.showText("GovOS Bangladesh — National " + capitalize(period) + " Report");
				cs.endText();

				y -= 24;
				cs.beginText();
				cs.setFont(bodyFont, 10);
				cs.newLineAtOffset(50, y);
				cs.showText("Generated: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE)
						+ "  |  People's Republic of Bangladesh");
				cs.endText();

				y -= 30;
				List<String> lines = buildReportLines(dash, period);
				for (String line : lines) {
					if (y < 50) break;
					cs.beginText();
					cs.setFont(bodyFont, 10);
					cs.newLineAtOffset(50, y);
					cs.showText(sanitize(line));
					cs.endText();
					y -= 14;
				}
			}

			// Summary page 2 if needed
			PDPage page2 = new PDPage(PDRectangle.A4);
			doc.addPage(page2);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
				float y = 800;
				cs.beginText();
				cs.setFont(titleFont, 14);
				cs.newLineAtOffset(50, y);
				cs.showText("Mega Projects & Alerts");
				cs.endText();
				y -= 28;
				for (var p : dash.megaProjects()) {
					if (y < 60) break;
					String line = p.projectCode() + " | " + truncate(p.title(), 40) + " | "
							+ p.progressPercent() + "% | " + p.status()
							+ (p.delayDays() != null && p.delayDays() > 0 ? " delay " + p.delayDays() + "d" : "");
					cs.beginText();
					cs.setFont(bodyFont, 9);
					cs.newLineAtOffset(50, y);
					cs.showText(sanitize(line));
					cs.endText();
					y -= 13;
				}
				y -= 16;
				cs.beginText();
				cs.setFont(titleFont, 12);
				cs.newLineAtOffset(50, y);
				cs.showText("Active Alerts");
				cs.endText();
				y -= 18;
				for (var a : dash.activeAlerts()) {
					if (y < 50) break;
					cs.beginText();
					cs.setFont(bodyFont, 9);
					cs.newLineAtOffset(50, y);
					cs.showText(sanitize("[" + a.severity() + "] " + a.title()));
					cs.endText();
					y -= 13;
				}
			}

			doc.save(out);
			return out.toByteArray();
		}
	}

	@Transactional(readOnly = true)
	public byte[] nationalPpt(String period) throws Exception {
		DashboardDtos.NationalDashboard dash = dashboardService.nationalDashboard();
		auditService.recordSimple("EXPORT_PPT", "REPORT", "National " + period);

		try (XMLSlideShow ppt = new XMLSlideShow(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			// Title slide
			XSLFSlide title = ppt.createSlide();
			addBox(title, 40, 80, 600, 60, "GovOS Bangladesh", 28, true);
			addBox(title, 40, 150, 600, 40, capitalize(period) + " National Briefing", 20, false);
			addBox(title, 40, 220, 600, 30,
					LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
							+ " · Office of the Prime Minister",
					14, false);

			// Snapshot slide
			XSLFSlide snap = ppt.createSlide();
			addBox(snap, 40, 30, 600, 36, "National Snapshot", 22, true);
			StringBuilder sb = new StringBuilder();
			sb.append("Reports today: ").append(dash.dailyOps().reportsSubmittedToday()).append("\n");
			sb.append("Active alerts: ").append(dash.dailyOps().activeAlerts()).append("\n");
			sb.append("Ongoing projects: ").append(dash.dailyOps().ongoingProjects()).append("\n");
			sb.append("Delayed projects: ").append(dash.dailyOps().delayedProjects()).append("\n");
			sb.append("FDI (USD m): ").append(dash.budget().foreignInvestmentUsdMillion()).append("\n");
			addBox(snap, 40, 80, 640, 280, sb.toString(), 16, false);

			// Indicators
			XSLFSlide ind = ppt.createSlide();
			addBox(ind, 40, 30, 600, 36, "Key Indicators", 22, true);
			StringBuilder ib = new StringBuilder();
			dash.indicators().stream().limit(12).forEach(i ->
					ib.append(i.label()).append(": ").append(i.value()).append(" ")
							.append(i.unit() != null ? i.unit() : "").append("\n"));
			addBox(ind, 40, 80, 640, 360, ib.toString(), 14, false);

			// Projects
			XSLFSlide proj = ppt.createSlide();
			addBox(proj, 40, 30, 600, 36, "Mega Projects", 22, true);
			StringBuilder pb = new StringBuilder();
			dash.megaProjects().stream().limit(8).forEach(p ->
					pb.append(p.title()).append(" — ").append(p.progressPercent())
							.append("% (").append(p.status()).append(")\n"));
			addBox(proj, 40, 80, 640, 360, pb.toString(), 13, false);

			// AI slide
			XSLFSlide ai = ppt.createSlide();
			addBox(ai, 40, 30, 600, 36, "AI Recommendations", 22, true);
			StringBuilder ab = new StringBuilder();
			dash.aiRecommendations().forEach(r ->
					ab.append("• ").append(r.title()).append("\n  ").append(truncate(r.body(), 120)).append("\n\n"));
			addBox(ai, 40, 80, 640, 380, ab.toString(), 12, false);

			ppt.write(out);
			return out.toByteArray();
		}
	}

	private void addBox(XSLFSlide slide, double x, double y, double w, double h,
			String text, double fontSize, boolean bold) {
		XSLFTextBox box = slide.createTextBox();
		box.setAnchor(new Rectangle2D.Double(x, y, w, h));
		XSLFTextParagraph p = box.addNewTextParagraph();
		XSLFTextRun run = p.addNewTextRun();
		run.setText(text);
		run.setFontSize(fontSize);
		run.setBold(bold);
		run.setFontColor(Color.DARK_GRAY);
	}

	private List<String> buildReportLines(DashboardDtos.NationalDashboard dash, String period) {
		List<String> lines = new ArrayList<>();
		lines.add("Period: " + period.toUpperCase());
		lines.add("");
		lines.add("--- Operations ---");
		lines.add("Daily reports submitted: " + dash.dailyOps().reportsSubmittedToday());
		lines.add("Field expenses today (BDT): " + dash.dailyOps().expensesTodayBdt());
		lines.add("Active alerts: " + dash.dailyOps().activeAlerts());
		lines.add("Ongoing projects: " + dash.dailyOps().ongoingProjects());
		lines.add("Delayed projects: " + dash.dailyOps().delayedProjects());
		lines.add("");
		lines.add("--- Budget snapshot ---");
		lines.add("Revenue (month BDT): " + dash.budget().totalRevenueBdt());
		lines.add("Expenditure (month BDT): " + dash.budget().totalExpenditureBdt());
		lines.add("Deficit (BDT): " + dash.budget().deficitBdt());
		lines.add("Ministry budget (crore): " + dash.budget().totalMinistryBudgetCrore());
		lines.add("Ministry spent (crore): " + dash.budget().totalMinistrySpentCrore());
		lines.add("FDI (USD million): " + dash.budget().foreignInvestmentUsdMillion());
		lines.add("");
		lines.add("--- Indicators ---");
		dash.indicators().forEach(i ->
				lines.add(i.label() + ": " + i.value() + " " + (i.unit() != null ? i.unit() : "")
						+ " (chg " + i.changePercent() + "%)"));
		return lines;
	}

	private String capitalize(String s) {
		if (s == null || s.isBlank()) return "Report";
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

	private String sanitize(String s) {
		if (s == null) return "";
		// WinAnsi-safe for Helvetica
		return s.replaceAll("[^\\x20-\\x7E]", "?");
	}

	private String truncate(String s, int n) {
		if (s == null) return "";
		return s.length() <= n ? s : s.substring(0, n) + "...";
	}
}
