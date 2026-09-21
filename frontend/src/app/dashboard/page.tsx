"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { StatCard } from "@/components/StatCard";
import { BiHeading } from "@/components/BiHeading";
import { dashboardApi } from "@/lib/api";
import { NationalDashboard } from "@/lib/types";
import { formatBdt, formatNumber, severityClass, statusClass } from "@/lib/format";
import { useAuth } from "@/lib/auth";
import { indicatorLabel, useLang } from "@/lib/i18n";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function DashboardPage() {
  const { user, isPm } = useAuth();
  const { t, mode } = useLang();
  const router = useRouter();
  const [data, setData] = useState<NationalDashboard | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (user && !isPm) {
      router.replace("/ministry");
      return;
    }
    dashboardApi
      .national()
      .then(setData)
      .catch((e) => setError(e.message));
  }, [user, isPm, router]);

  return (
    <Shell>
      <BiHeading titleKey="dashTitle" subKey="dashSub" />

      {error && (
        <div className="card text-rose-300 mb-4">
          {mode === "bn" ? "ড্যাশবোর্ড লোড ব্যর্থ" : "Failed to load dashboard"}: {error}
        </div>
      )}
      {!data && !error && <div className="text-muted">{t("loading")}</div>}

      {data && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 xl:grid-cols-6 gap-3 mb-6">
            <StatCard label={t("reportsToday")} value={data.dailyOps.reportsSubmittedToday} accent="emerald" />
            <StatCard label={t("activeAlerts")} value={data.dailyOps.activeAlerts} accent="rose" />
            <StatCard label={t("ongoingProjects")} value={data.dailyOps.ongoingProjects} accent="sky" />
            <StatCard label={t("delayedProjects")} value={data.dailyOps.delayedProjects} accent="amber" />
            <StatCard
              label={t("fdi")}
              value={formatNumber(data.budget.foreignInvestmentUsdMillion)}
              unit="USD m"
              accent="violet"
            />
            <StatCard
              label={t("fieldExpenses")}
              value={formatBdt(data.dailyOps.expensesTodayBdt)}
              unit="BDT"
              accent="emerald"
            />
          </div>

          <h2 className="text-sm font-medium text-muted-foreground mb-3">{t("nationalIndicators")}</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-5 gap-3 mb-8">
            {data.indicators.map((ind) => (
              <StatCard
                key={ind.key}
                label={indicatorLabel(ind.label, mode)}
                value={ind.value}
                unit={ind.unit}
                change={ind.changePercent}
                accent={
                  ind.category === "Economy"
                    ? "violet"
                    : ind.category === "Health"
                    ? "rose"
                    : ind.category === "Agriculture"
                    ? "emerald"
                    : "sky"
                }
              />
            ))}
          </div>

          <div className="grid lg:grid-cols-3 gap-4 mb-8">
            <div className="lg:col-span-2 card">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-medium">{t("megaProjects")}</h2>
                <Link href="/projects" className="text-xs text-bd-green hover:underline">
                  {t("viewAll")}
                </Link>
              </div>
              <div className="space-y-3">
                {data.megaProjects.slice(0, 6).map((p) => (
                  <div key={p.id} className="rounded-lg border border-border bg-surface-hover p-3">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="font-medium text-sm">{p.title}</div>
                        <div className="text-xs text-muted mt-0.5">
                          {p.ministryName} · {p.location}
                          {p.contractor ? ` · ${p.contractor}` : ""}
                        </div>
                      </div>
                      <span className={`text-[11px] px-2 py-0.5 rounded ${statusClass(p.status)}`}>
                        {p.status}
                      </span>
                    </div>
                    <div className="mt-2 flex items-center gap-3 text-xs text-muted">
                      <span>{formatNumber(p.progressPercent)}%</span>
                      <div className="flex-1 h-1.5 rounded-full bg-surface-muted overflow-hidden">
                        <div
                          className="h-full bg-bd-green rounded-full"
                          style={{ width: `${Math.min(100, p.progressPercent || 0)}%` }}
                        />
                      </div>
                      <span>
                        {formatNumber(p.spentCostCrore)}/{formatNumber(p.totalCostCrore)} cr
                      </span>
                    </div>
                    {p.riskLevel && p.riskLevel !== "LOW" && (
                      <div className="mt-2 text-xs text-amber-300/90">
                        AI risk {p.riskLevel}: {p.riskReason}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>

            <div className="space-y-4">
              <div className="card">
                <h2 className="font-medium mb-3">{t("activeAlerts")}</h2>
                <div className="space-y-2">
                  {data.activeAlerts.map((a) => (
                    <div key={a.id} className={`rounded-lg border p-3 text-sm ${severityClass(a.severity)}`}>
                      <div className="font-medium">{a.title}</div>
                      <div className="text-xs opacity-80 mt-1">{a.message}</div>
                    </div>
                  ))}
                </div>
              </div>
              <div className="card">
                <div className="flex justify-between mb-3">
                  <h2 className="font-medium">{t("aiRecs")}</h2>
                  <Link href="/ai" className="text-xs text-bd-green">{t("askAi")}</Link>
                </div>
                <div className="space-y-3">
                  {data.aiRecommendations.map((r) => (
                    <div key={r.id} className="border-l-2 border-bd-green pl-3">
                      <div className="text-sm font-medium">{r.title}</div>
                      <div className="text-xs text-muted mt-1 line-clamp-3">{r.body}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          <div className="grid lg:grid-cols-2 gap-4">
            <div className="card">
              <h2 className="font-medium mb-3">{t("ministryBudget")}</h2>
              <div className="table-wrap">
                <table className="data">
                  <thead>
                    <tr>
                      <th>{t("ministry")}</th>
                      <th>{t("budget")}</th>
                      <th>{t("spent")}</th>
                      <th>{t("usePct")}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.ministries.map((m) => (
                      <tr key={m.id}>
                        <td>
                          <div className="font-medium text-sm">{m.nameEn}</div>
                          <div className="text-[11px] text-muted">{m.nameBn}</div>
                        </td>
                        <td>{formatNumber(m.annualBudgetCrore)} cr</td>
                        <td>{formatNumber(m.spentBudgetCrore)} cr</td>
                        <td>
                          <span className={m.utilizationPercent > 70 ? "text-amber-300" : "text-bd-green"}>
                            {formatNumber(m.utilizationPercent)}%
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card">
              <h2 className="font-medium mb-3">{t("foreignInv")}</h2>
              <div className="space-y-3">
                {data.foreignInvestments.map((f) => (
                  <div key={f.id} className="flex justify-between gap-3 border-b border-border pb-3">
                    <div>
                      <div className="text-sm font-medium">{f.investorName}</div>
                      <div className="text-xs text-muted">
                        {f.country} · {f.sector}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm text-bd-green">${formatNumber(f.amountUsdMillion)}m</div>
                      <div className="text-[11px] text-muted">{f.status}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </Shell>
  );
}
