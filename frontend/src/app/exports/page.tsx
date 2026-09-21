"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { downloadExport, modulesApi } from "@/lib/api";
import { FileDown, Presentation, FileText, ScrollText } from "lucide-react";
import { BiHeading } from "@/components/BiHeading";
import { useLang } from "@/lib/i18n";

export default function ExportsPage() {
  const { t } = useLang();
  const [period, setPeriod] = useState("monthly");
  const [busy, setBusy] = useState("");
  const [error, setError] = useState("");
  const [logs, setLogs] = useState<Record<string, unknown>[]>([]);

  function loadLogs() {
    modulesApi
      .auditLogs(40)
      .then(setLogs)
      .catch(() => {
        /* optional for non-PM */
      });
  }

  useEffect(() => {
    loadLogs();
  }, []);

  async function download(kind: "pdf" | "pptx") {
    setBusy(kind);
    setError("");
    try {
      await downloadExport(kind, period);
      loadLogs();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Export failed");
    } finally {
      setBusy("");
    }
  }

  return (
    <Shell>
      <BiHeading titleKey="exportsTitle" subKey="exportsSub" />

      <div className="grid md:grid-cols-2 gap-4 mb-8">
        <div className="card space-y-4">
          <div className="flex items-center gap-2">
            <FileText className="text-bd-green" size={20} />
            <h2 className="font-medium">{t("reportPeriod")}</h2>
          </div>
          <select
            className="input"
            value={period}
            onChange={(e) => setPeriod(e.target.value)}
          >
            <option value="daily">Daily</option>
            <option value="weekly">Weekly</option>
            <option value="monthly">Monthly</option>
            <option value="quarterly">Quarterly</option>
            <option value="yearly">Yearly</option>
          </select>

          <div className="flex flex-wrap gap-2">
            <button
              className="btn-primary"
              disabled={!!busy}
              onClick={() => download("pdf")}
            >
              <FileDown size={16} />
              {busy === "pdf" ? "…" : t("downloadPdf")}
            </button>
            <button
              className="btn-ghost"
              disabled={!!busy}
              onClick={() => download("pptx")}
            >
              <Presentation size={16} />
              {busy === "pptx" ? "…" : t("downloadPpt")}
            </button>
          </div>
          {error && <div className="text-sm text-rose-300">{error}</div>}
          <p className="text-xs text-muted">
            Exports are logged in the audit trail. PDF includes indicators, budget, projects and alerts.
            PPT includes snapshot, indicators, mega projects and AI recommendations.
          </p>
        </div>

        <div className="card">
          <h2 className="font-medium mb-3">Report types (roadmap)</h2>
          <ul className="text-sm text-muted space-y-2">
            <li>✓ National daily / monthly / yearly PDF</li>
            <li>✓ Cabinet PowerPoint briefing</li>
            <li>○ AI narrative summary in document (set XAI_API_KEY)</li>
            <li>○ Ministry-scoped packs</li>
            <li>○ 5-year vision reports</li>
          </ul>
        </div>
      </div>

      <div className="card">
        <div className="flex items-center gap-2 mb-4">
          <ScrollText size={18} className="text-amber-300" />
          <h2 className="font-medium">{t("auditLog")}</h2>
        </div>
        <div className="table-wrap max-h-80 overflow-y-auto">
          <table className="data">
            <thead>
              <tr>
                <th>Time</th>
                <th>User</th>
                <th>Action</th>
                <th>Resource</th>
                <th>IP</th>
                <th>Hash</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((l) => (
                <tr key={String(l.id)}>
                  <td className="text-xs whitespace-nowrap">
                    {String(l.createdAt).slice(0, 19).replace("T", " ")}
                  </td>
                  <td className="text-xs">{String(l.username || "—")}</td>
                  <td className="text-xs">{String(l.action)}</td>
                  <td className="text-xs max-w-[180px] truncate">{String(l.resource)}</td>
                  <td className="text-xs">{String(l.ipAddress || "—")}</td>
                  <td className="text-[10px] font-mono text-muted">
                    {String(l.integrityHash || "").slice(0, 12)}…
                  </td>
                </tr>
              ))}
              {!logs.length && (
                <tr>
                  <td colSpan={6} className="text-muted text-sm">
                    No audit entries yet (or insufficient role)
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Shell>
  );
}
