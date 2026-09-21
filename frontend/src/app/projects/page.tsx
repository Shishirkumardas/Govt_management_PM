"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { BiHeading } from "@/components/BiHeading";
import { dashboardApi } from "@/lib/api";
import { ProjectItem } from "@/lib/types";
import { formatNumber, statusClass } from "@/lib/format";
import { useLang } from "@/lib/i18n";

export default function ProjectsPage() {
  const { t } = useLang();
  const [projects, setProjects] = useState<ProjectItem[]>([]);
  const [selected, setSelected] = useState<ProjectItem | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    dashboardApi
      .projects()
      .then((p) => {
        setProjects(p);
        setSelected(p.find((x) => x.projectCode === "PADMA-BRIDGE") || p[0] || null);
      })
      .catch((e) => setError(e.message));
  }, []);

  return (
    <Shell>
      <BiHeading titleKey="projectsTitle" subKey="projectsSub" />
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <div className="grid lg:grid-cols-5 gap-4">
        <div className="lg:col-span-2 space-y-2">
          {projects.map((p) => (
            <button
              key={p.id}
              onClick={() => setSelected(p)}
              className={`w-full text-left card !p-3 transition ${
                selected?.id === p.id ? "border-bd-green/40 bg-bd-green/5" : ""
              }`}
            >
              <div className="flex justify-between gap-2">
                <div className="font-medium text-sm">{p.title}</div>
                <span className={`text-[10px] px-1.5 py-0.5 rounded shrink-0 ${statusClass(p.status)}`}>
                  {p.status}
                </span>
              </div>
              <div className="text-xs text-muted mt-1">
                {p.progressPercent}% · {formatNumber(p.totalCostCrore)} cr
                {p.delayDays ? ` · delay ${p.delayDays}d` : ""}
              </div>
            </button>
          ))}
        </div>

        <div className="lg:col-span-3">
          {selected && (
            <div className="card space-y-5">
              <div>
                <div className="text-xs text-muted">{selected.projectCode}</div>
                <h2 className="text-xl font-semibold mt-1">{selected.title}</h2>
                <div className="text-sm text-muted mt-1">
                  {selected.ministryName} · {selected.location}
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                <Metric label={t("status")} value={selected.status} />
                <Metric label={t("progress")} value={`${formatNumber(selected.progressPercent)}%`} />
                <Metric label={t("budget")} value={`${formatNumber(selected.totalCostCrore)} cr`} />
                <Metric label={t("spent")} value={`${formatNumber(selected.spentCostCrore)} cr`} />
                <Metric label={t("remaining")} value={`${formatNumber(selected.remainingCostCrore)} cr`} />
                <Metric label={t("delay")} value={`${selected.delayDays ?? 0} days`} />
                <Metric label={t("contractor")} value={selected.contractor || "—"} />
                <Metric label={t("aiRisk")} value={selected.riskLevel || "—"} />
              </div>

              <div>
                <div className="text-xs text-muted mb-1">Physical progress</div>
                <div className="h-3 rounded-full bg-surface-muted overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-bd-green-deep to-bd-green-mid"
                    style={{ width: `${Math.min(100, selected.progressPercent || 0)}%` }}
                  />
                </div>
              </div>

              {selected.riskLevel && selected.riskLevel !== "LOW" && (
                <div className="rounded-lg border border-amber-500/30 bg-amber-500/10 p-4">
                  <div className="text-sm font-medium text-amber-200">
                    AI says: {selected.riskLevel} risk of delay
                  </div>
                  <div className="text-sm text-amber-100/80 mt-2">
                    <strong>Reason:</strong> {selected.riskReason}
                  </div>
                  <div className="text-sm text-bd-green/90 mt-2">
                    <strong>Recommendation:</strong> {selected.aiRecommendation}
                  </div>
                </div>
              )}

              <div className="text-sm text-muted-foreground">
                <div className="text-xs text-muted mb-1">{t("latestUpdate")}</div>
                {selected.latestUpdate}
              </div>
            </div>
          )}
        </div>
      </div>
    </Shell>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-surface-hover border border-border p-3">
      <div className="text-[11px] uppercase tracking-wide text-muted">{label}</div>
      <div className="text-sm font-medium mt-1">{value}</div>
    </div>
  );
}
