"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { dashboardApi } from "@/lib/api";
import {
  MinistryIntelligence,
  MinistryOverview,
} from "@/lib/types";
import { useAuth } from "@/lib/auth";
import { formatNumber, severityClass, statusClass } from "@/lib/format";
import { StatCard } from "@/components/StatCard";
import { useLang } from "@/lib/i18n";
import {
  Building2,
  ShieldAlert,
  Wallet,
  Users,
  Briefcase,
  ShoppingCart,
  ClipboardList,
  FileWarning,
  Landmark,
} from "lucide-react";

type Tab =
  | "overview"
  | "orgs"
  | "budget"
  | "works"
  | "projects"
  | "procurement"
  | "people"
  | "integrity"
  | "ops";

export default function MinistryPage() {
  const { user, isPm } = useAuth();
  const { mode } = useLang();
  const [list, setList] = useState<MinistryOverview[]>([]);
  const [intel, setIntel] = useState<MinistryIntelligence | null>(null);
  const [error, setError] = useState("");
  const [tab, setTab] = useState<Tab>("overview");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) return;
    if (isPm) {
      dashboardApi.ministries().then(setList).catch((e) => setError(e.message));
    } else {
      setLoading(true);
      dashboardApi
        .myMinistryIntelligence()
        .then(setIntel)
        .catch((e) => setError(e.message))
        .finally(() => setLoading(false));
    }
  }, [user, isPm]);

  async function openMinistry(id: number) {
    setLoading(true);
    setError("");
    setTab("overview");
    try {
      setIntel(await dashboardApi.ministryIntelligence(id));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed");
    } finally {
      setLoading(false);
    }
  }

  const riskColor = (level: string) => {
    if (level === "CRITICAL") return "text-bd-red bg-bd-red-soft border-bd-red/30";
    if (level === "HIGH") return "text-orange-700 dark:text-orange-300 bg-orange-500/15 border-orange-500/30";
    if (level === "MEDIUM") return "text-amber-800 dark:text-amber-300 bg-amber-500/15 border-amber-500/30";
    return "text-bd-green bg-bd-green-muted border-bd-green/30";
  };

  const tabs: { id: Tab; en: string; bn: string; icon: React.ReactNode }[] = [
    { id: "overview", en: "Overview", bn: "সারসংক্ষেপ", icon: <Landmark size={14} /> },
    { id: "integrity", en: "Anti-corruption", bn: "দুর্নীতি প্রতিরোধ", icon: <ShieldAlert size={14} /> },
    { id: "orgs", en: "Organizations", bn: "প্রতিষ্ঠানসমূহ", icon: <Building2 size={14} /> },
    { id: "budget", en: "Budget lines", bn: "বাজেট লাইন", icon: <Wallet size={14} /> },
    { id: "works", en: "Work programs", bn: "কর্মসূচি", icon: <Briefcase size={14} /> },
    { id: "projects", en: "Projects", bn: "প্রকল্প", icon: <ClipboardList size={14} /> },
    { id: "procurement", en: "Procurement", bn: "ক্রয়", icon: <ShoppingCart size={14} /> },
    { id: "people", en: "HR / Payroll", bn: "জনবল", icon: <Users size={14} /> },
    { id: "ops", en: "Field & docs", bn: "মাঠ ও নথি", icon: <FileWarning size={14} /> },
  ];

  return (
    <Shell>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-foreground">
          {mode === "en" ? (
            "Ministry Intelligence Report"
          ) : (
            <>
              <span className="block">মন্ত্রণালয় গোয়েন্দা প্রতিবেদন</span>
              {mode === "both" && (
                <span className="block text-base font-normal text-muted mt-0.5">
                  Ministry Intelligence Report
                </span>
              )}
            </>
          )}
        </h1>
        <p className="text-sm text-muted mt-2">
          {mode === "en"
            ? "PM-grade detail: every organization, budget line, work program, procurement flag and headcount anomaly — to reduce corruption and improve oversight."
            : mode === "bn"
            ? "প্রধানমন্ত্রী-স্তরের বিস্তারিত: প্রতিটি প্রতিষ্ঠান, বাজেট লাইন, কর্মসূচি, ক্রয় ঝুঁকি ও জনবল অসঙ্গতি — দুর্নীতি কমাতে ও নজরদারি বাড়াতে।"
            : "প্রতিষ্ঠান · বাজেট লাইন · কর্মসূচি · ক্রয় ফ্ল্যাগ · জনবল অসঙ্গতি — full integrity oversight for PMO."}
        </p>
      </div>

      {error && <div className="card text-bd-red mb-4">{error}</div>}
      {loading && <div className="text-muted mb-4">Loading intelligence…</div>}

      {isPm && !intel && (
        <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-3">
          {list.map((m) => (
            <button
              key={m.id}
              onClick={() => openMinistry(m.id)}
              className="card text-left hover:border-bd-green/40 transition group"
            >
              <div className="font-medium text-foreground group-hover:text-bd-green">
                {m.nameEn}
              </div>
              <div className="text-xs text-muted mt-1">{m.nameBn}</div>
              <div className="mt-3 grid grid-cols-3 gap-2 text-xs">
                <div className="rounded-lg bg-surface-muted p-2">
                  <div className="text-muted">Budget</div>
                  <div className="font-semibold">{formatNumber(m.annualBudgetCrore)} cr</div>
                </div>
                <div className="rounded-lg bg-surface-muted p-2">
                  <div className="text-muted">Used</div>
                  <div className="font-semibold">{formatNumber(m.utilizationPercent)}%</div>
                </div>
                <div className="rounded-lg bg-surface-muted p-2">
                  <div className="text-muted">Projects</div>
                  <div className="font-semibold">{m.activeProjects}</div>
                </div>
              </div>
              <div className="mt-3 text-[11px] text-bd-green font-medium">
                {mode === "bn" ? "পূর্ণ প্রতিবেদন খুলুন →" : "Open full intelligence →"}
              </div>
            </button>
          ))}
        </div>
      )}

      {intel && (
        <div className="space-y-5">
          {isPm && (
            <button className="btn-ghost text-xs" onClick={() => setIntel(null)}>
              ← {mode === "bn" ? "সব মন্ত্রণালয়" : "All ministries"}
            </button>
          )}

          {/* Header */}
          <div className="card border-bd-green/20">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <div className="text-xs text-bd-green font-medium tracking-wide uppercase">
                  {intel.ministry.code}
                </div>
                <h2 className="text-xl font-semibold mt-0.5">{intel.ministry.nameEn}</h2>
                <p className="text-sm text-muted">{intel.ministry.nameBn}</p>
              </div>
              <div
                className={`px-3 py-2 rounded-xl border text-center min-w-[110px] ${riskColor(
                  intel.antiCorruption.overallRiskLevel
                )}`}
              >
                <div className="text-[10px] uppercase opacity-80">Integrity risk</div>
                <div className="text-2xl font-bold">{intel.antiCorruption.overallRiskScore}</div>
                <div className="text-xs font-semibold">{intel.antiCorruption.overallRiskLevel}</div>
              </div>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-5 gap-2 mt-4">
              <StatCard label="Budget" value={intel.ministry.annualBudgetCrore} unit="crore" />
              <StatCard label="Spent" value={intel.ministry.spentBudgetCrore} unit="crore" accent="amber" />
              <StatCard label="Utilization" value={intel.ministry.utilizationPercent} unit="%" accent="sky" />
              <StatCard label="Organizations" value={intel.organizations.length} accent="violet" />
              <StatCard
                label="Ghost signals"
                value={intel.headcount.anomalyHeadcount}
                accent="rose"
              />
            </div>
          </div>

          {/* Tabs */}
          <div className="flex flex-wrap gap-1.5">
            {tabs.map((t) => (
              <button
                key={t.id}
                onClick={() => setTab(t.id)}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium border transition ${
                  tab === t.id
                    ? "bg-bd-green text-white border-bd-green"
                    : "bg-surface-elevated text-muted border-border hover:border-bd-green/40"
                }`}
              >
                {t.icon}
                {mode === "en" ? t.en : mode === "bn" ? t.bn : `${t.bn}`}
              </button>
            ))}
          </div>

          {tab === "overview" && <OverviewTab intel={intel} />}
          {tab === "integrity" && <IntegrityTab intel={intel} riskColor={riskColor} />}
          {tab === "orgs" && <OrgsTab intel={intel} />}
          {tab === "budget" && <BudgetTab intel={intel} />}
          {tab === "works" && <WorksTab intel={intel} />}
          {tab === "projects" && <ProjectsTab intel={intel} />}
          {tab === "procurement" && <ProcurementTab intel={intel} />}
          {tab === "people" && <PeopleTab intel={intel} />}
          {tab === "ops" && <OpsTab intel={intel} />}
        </div>
      )}
    </Shell>
  );
}

function OverviewTab({ intel }: { intel: MinistryIntelligence }) {
  const ac = intel.antiCorruption;
  return (
    <div className="grid lg:grid-cols-2 gap-4">
      <div className="card">
        <h3 className="font-medium mb-3">Budget by category (crore)</h3>
        <div className="space-y-2">
          {Object.keys(intel.budgetByCategoryAllocated || {}).map((cat) => {
            const alloc = intel.budgetByCategoryAllocated[cat] || 0;
            const spent = intel.budgetByCategorySpent[cat] || 0;
            const pct = alloc > 0 ? (spent / alloc) * 100 : 0;
            return (
              <div key={cat}>
                <div className="flex justify-between text-xs mb-1">
                  <span className="font-medium">{cat.replaceAll("_", " ")}</span>
                  <span className="text-muted">
                    {formatNumber(spent)} / {formatNumber(alloc)} ({formatNumber(pct)}%)
                  </span>
                </div>
                <div className="h-2 rounded-full bg-surface-muted overflow-hidden">
                  <div
                    className="h-full bg-bd-green rounded-full"
                    style={{ width: `${Math.min(100, pct)}%` }}
                  />
                </div>
              </div>
            );
          })}
          {!Object.keys(intel.budgetByCategoryAllocated || {}).length && (
            <div className="text-sm text-muted">No budget lines seeded for this ministry yet.</div>
          )}
        </div>
      </div>

      <div className="card">
        <h3 className="font-medium mb-3">Headcount & integrity snapshot</h3>
        <div className="grid grid-cols-2 gap-2 text-sm">
          <KV label="Sanctioned posts" value={String(intel.headcount.sanctionedPosts)} />
          <KV label="Filled posts" value={String(intel.headcount.filledPosts)} />
          <KV label="Vacancy" value={String(intel.headcount.vacancy)} />
          <KV label="Fill rate" value={`${intel.headcount.fillRatePercent}%`} />
          <KV label="Anomaly / ghost signals" value={String(intel.headcount.anomalyHeadcount)} danger />
          <KV label="Flagged procurements" value={String(ac.flaggedProcurements)} danger={ac.flaggedProcurements > 0} />
          <KV label="Open risk flags" value={String(ac.openRiskFlags)} danger={ac.openRiskFlags > 0} />
          <KV label="High-risk orgs" value={String(ac.highRiskOrganizations)} danger={ac.highRiskOrganizations > 0} />
        </div>
      </div>

      <div className="card lg:col-span-2">
        <h3 className="font-medium mb-2">KPIs</h3>
        <div className="grid md:grid-cols-3 xl:grid-cols-4 gap-2">
          {intel.kpis.map((k, i) => (
            <div key={i} className="rounded-lg border border-border bg-surface-muted p-3">
              <div className="text-xs text-muted">{String(k.label)}</div>
              <div className="text-lg font-semibold mt-0.5">
                {formatNumber(Number(k.value))}{" "}
                <span className="text-xs font-normal text-muted">{String(k.unit || "")}</span>
              </div>
              <div className="text-[11px] text-muted">Target: {formatNumber(Number(k.target))}</div>
            </div>
          ))}
          {!intel.kpis.length && <div className="text-sm text-muted">No KPIs for this ministry</div>}
        </div>
      </div>

      {!!intel.aiRecommendations.length && (
        <div className="card lg:col-span-2">
          <h3 className="font-medium mb-2">AI recommendations</h3>
          <div className="space-y-2">
            {intel.aiRecommendations.map((r) => (
              <div key={r.id} className="border-l-2 border-bd-green pl-3">
                <div className="text-sm font-medium">{r.title}</div>
                <div className="text-xs text-muted mt-0.5">{r.body}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function IntegrityTab({
  intel,
  riskColor,
}: {
  intel: MinistryIntelligence;
  riskColor: (l: string) => string;
}) {
  const ac = intel.antiCorruption;
  return (
    <div className="space-y-4">
      <div className={`card border ${riskColor(ac.overallRiskLevel)}`}>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h3 className="font-semibold text-lg">Anti-corruption scorecard</h3>
            <p className="text-xs opacity-80 mt-1">
              Composite of procurement flags, ghost signals, budget anomalies, delayed projects & risk engine
            </p>
          </div>
          <div className="text-right">
            <div className="text-3xl font-bold">{ac.overallRiskScore}</div>
            <div className="text-sm font-semibold">{ac.overallRiskLevel}</div>
          </div>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-2 mt-4 text-sm">
          <Metric label="Flagged procurement" value={ac.flaggedProcurements} />
          <Metric label="Ghost employee signals" value={ac.ghostEmployeeSignals} />
          <Metric label="High-risk orgs" value={ac.highRiskOrganizations} />
          <Metric label="Flagged budget lines" value={ac.flaggedBudgetLines} />
          <Metric label="Delayed projects" value={ac.delayedProjects} />
          <Metric label="Work transparency issues" value={ac.worksWithTransparencyIssues} />
          <Metric label="Open risk flags" value={ac.openRiskFlags} />
          <Metric label="Unusual spend (cr)" value={formatNumber(ac.unusualSpendCrore)} />
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-4">
        <div className="card">
          <h3 className="font-medium text-bd-red mb-2">Red flags</h3>
          <ul className="space-y-2 text-sm">
            {ac.redFlags.map((f, i) => (
              <li key={i} className="flex gap-2">
                <span className="text-bd-red shrink-0">●</span>
                <span>{f}</span>
              </li>
            ))}
          </ul>
        </div>
        <div className="card">
          <h3 className="font-medium text-bd-green mb-2">Recommended actions</h3>
          <ul className="space-y-2 text-sm">
            {ac.recommendedActions.map((f, i) => (
              <li key={i} className="flex gap-2">
                <span className="text-bd-green shrink-0">→</span>
                <span>{f}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="card">
        <h3 className="font-medium mb-3">Risk engine entries</h3>
        <div className="space-y-2">
          {intel.risks.map((r, i) => (
            <div key={i} className={`rounded-lg border p-3 text-sm ${severityClass(String(r.severity))}`}>
              <div className="text-[11px] uppercase opacity-80">{String(r.riskType)}</div>
              <div className="font-medium">{String(r.title)}</div>
              <div className="text-xs mt-1 opacity-90">{String(r.description)}</div>
              <div className="text-xs mt-2 font-medium">Action: {String(r.recommendedAction)}</div>
            </div>
          ))}
          {!intel.risks.length && <div className="text-sm text-muted">No open risk flags</div>}
        </div>
      </div>
    </div>
  );
}

function OrgsTab({ intel }: { intel: MinistryIntelligence }) {
  return (
    <div className="space-y-3">
      <p className="text-xs text-muted">
        All directorates, departments, corporations and agencies under this ministry — with own budget, headcount and corruption risk score.
      </p>
      <div className="table-wrap">
        <table className="data">
          <thead>
            <tr>
              <th>Organization</th>
              <th>Type</th>
              <th>Head</th>
              <th>Budget / Spent</th>
              <th>Posts</th>
              <th>Ghost?</th>
              <th>Risk</th>
              <th>Transparency</th>
            </tr>
          </thead>
          <tbody>
            {intel.organizations.map((o) => (
              <tr key={o.id}>
                <td>
                  <div className="font-medium text-sm">{o.nameEn}</div>
                  <div className="text-[11px] text-muted">{o.nameBn} · {o.orgCode}</div>
                  <div className="text-[10px] text-muted mt-0.5 line-clamp-1">{o.mandate}</div>
                </td>
                <td className="text-xs">{o.orgType}</td>
                <td className="text-xs">
                  <div>{o.headName}</div>
                  <div className="text-muted">{o.headDesignation}</div>
                </td>
                <td className="text-xs whitespace-nowrap">
                  {formatNumber(o.spentCrore)} / {formatNumber(o.budgetCrore)} cr
                  <div className="text-muted">{formatNumber(o.utilizationPercent)}%</div>
                </td>
                <td className="text-xs">
                  {o.filledPosts}/{o.sanctionedPosts}
                  <div className="text-muted">vac {o.vacancy}</div>
                </td>
                <td className={`text-xs font-semibold ${o.anomalyHeadcount > 0 ? "text-bd-red" : "text-muted"}`}>
                  {o.anomalyHeadcount}
                </td>
                <td>
                  <RiskBadge score={o.corruptionRiskScore} />
                </td>
                <td className="text-xs">{formatNumber(o.transparencyScore)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {!intel.organizations.length && (
        <div className="card text-sm text-muted">No organizations registered under this ministry.</div>
      )}
    </div>
  );
}

function BudgetTab({ intel }: { intel: MinistryIntelligence }) {
  return (
    <div className="space-y-3">
      <p className="text-xs text-muted">
        Line-level budget: ADP, salary, subsidy, procurement, contingency — flagged rows need PM/Finance attention.
      </p>
      <div className="table-wrap">
        <table className="data">
          <thead>
            <tr>
              <th>Line</th>
              <th>Category</th>
              <th>Organization</th>
              <th>Allocated</th>
              <th>Released</th>
              <th>Spent</th>
              <th>Remaining</th>
              <th>Use %</th>
              <th>Flag</th>
            </tr>
          </thead>
          <tbody>
            {intel.budgetLines.map((l) => (
              <tr key={l.id} className={l.flagged ? "bg-bd-red-soft/40" : undefined}>
                <td>
                  <div className="text-sm font-medium">{l.titleEn}</div>
                  <div className="text-[11px] text-muted">{l.titleBn}</div>
                  {l.projectCode && <div className="text-[10px] text-bd-green">{l.projectCode}</div>}
                </td>
                <td className="text-xs whitespace-nowrap">{l.category}</td>
                <td className="text-xs">{l.organizationName || "—"}</td>
                <td className="text-xs">{formatNumber(l.allocatedCrore)}</td>
                <td className="text-xs">{formatNumber(l.releasedCrore)}</td>
                <td className="text-xs">{formatNumber(l.spentCrore)}</td>
                <td className="text-xs">{formatNumber(l.remainingCrore)}</td>
                <td className="text-xs">{formatNumber(l.utilizationPercent)}%</td>
                <td className="text-xs">
                  {l.flagged ? (
                    <span className="text-bd-red font-medium" title={l.flagReason}>
                      ⚠ Flagged
                    </span>
                  ) : (
                    <span className="text-muted">OK</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {intel.budgetLines.filter((l) => l.flagged).map((l) => (
        <div key={`f-${l.id}`} className="rounded-lg border border-bd-red/30 bg-bd-red-soft p-3 text-sm">
          <div className="font-medium text-bd-red">{l.titleEn}</div>
          <div className="text-xs mt-1">{l.flagReason}</div>
        </div>
      ))}
    </div>
  );
}

function WorksTab({ intel }: { intel: MinistryIntelligence }) {
  return (
    <div className="space-y-3">
      <p className="text-xs text-muted">
        Operational schemes and work programs — beneficiary counts, progress, transparency issues.
      </p>
      {intel.workPrograms.map((w) => (
        <div
          key={w.id}
          className={`card ${w.transparencyIssue ? "border-bd-red/40" : ""}`}
        >
          <div className="flex flex-wrap justify-between gap-2">
            <div>
              <div className="text-[11px] text-muted">{w.workCode} · {w.organizationName || "Ministry"}</div>
              <div className="font-medium">{w.titleEn}</div>
              <div className="text-xs text-muted">{w.titleBn}</div>
            </div>
            <span className={`text-[11px] px-2 py-0.5 rounded h-fit ${statusClass(w.status)}`}>
              {w.status}
            </span>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-2 mt-3 text-xs">
            <KV label="Budget" value={`${formatNumber(w.budgetCrore)} cr`} />
            <KV label="Spent" value={`${formatNumber(w.spentCrore)} cr`} />
            <KV label="Progress" value={`${formatNumber(w.progressPercent)}%`} />
            <KV label="Location" value={w.location || "—"} />
            <KV label="Beneficiaries" value={w.beneficiaries ? formatNumber(w.beneficiaries, 0) : "—"} />
          </div>
          <div className="mt-2 h-1.5 rounded-full bg-surface-muted overflow-hidden">
            <div
              className="h-full bg-bd-green rounded-full"
              style={{ width: `${Math.min(100, w.progressPercent || 0)}%` }}
            />
          </div>
          <div className="text-xs text-muted mt-2">{w.latestUpdate}</div>
          {w.transparencyIssue && (
            <div className="mt-2 text-xs text-bd-red font-medium">⚠ {w.issueNote}</div>
          )}
        </div>
      ))}
      {!intel.workPrograms.length && (
        <div className="card text-sm text-muted">No work programs registered.</div>
      )}
    </div>
  );
}

function ProjectsTab({ intel }: { intel: MinistryIntelligence }) {
  return (
    <div className="space-y-3">
      {intel.projects.map((p) => (
        <div key={p.id} className="card">
          <div className="flex flex-wrap justify-between gap-2">
            <div>
              <div className="text-[11px] text-muted">{p.projectCode}</div>
              <div className="font-medium">{p.title}</div>
              <div className="text-xs text-muted mt-0.5">
                {p.location} · {p.contractor || "—"}
              </div>
            </div>
            <span className={`text-[11px] px-2 py-0.5 rounded h-fit ${statusClass(p.status)}`}>
              {p.status}
            </span>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2 mt-3 text-xs">
            <KV label="Budget" value={`${formatNumber(p.totalCostCrore)} cr`} />
            <KV label="Spent" value={`${formatNumber(p.spentCostCrore)} cr`} />
            <KV label="Remaining" value={`${formatNumber(p.remainingCostCrore)} cr`} />
            <KV label="Delay" value={`${p.delayDays ?? 0} days`} danger={(p.delayDays ?? 0) > 0} />
          </div>
          <div className="mt-2 flex items-center gap-2 text-xs">
            <span>{formatNumber(p.progressPercent)}%</span>
            <div className="flex-1 h-1.5 rounded-full bg-surface-muted overflow-hidden">
              <div
                className="h-full bg-bd-green rounded-full"
                style={{ width: `${Math.min(100, p.progressPercent || 0)}%` }}
              />
            </div>
          </div>
          {p.riskLevel && p.riskLevel !== "LOW" && (
            <div className="mt-2 text-xs text-amber-700 dark:text-amber-300">
              AI risk {p.riskLevel}: {p.riskReason}
              {p.aiRecommendation && (
                <div className="text-bd-green mt-0.5">→ {p.aiRecommendation}</div>
              )}
            </div>
          )}
          <div className="text-xs text-muted mt-2">{p.latestUpdate}</div>
        </div>
      ))}
      {!intel.projects.length && <div className="card text-sm text-muted">No projects</div>}
    </div>
  );
}

function ProcurementTab({ intel }: { intel: MinistryIntelligence }) {
  return (
    <div className="space-y-3">
      <p className="text-xs text-muted">
        AI screens price inflation, duplicate invoices, fake vendors, repeated purchases, conflict of interest.
      </p>
      {intel.procurement.map((p, i) => (
        <div
          key={i}
          className={`card ${p.flagged ? "border-bd-red/40" : ""}`}
        >
          <div className="flex flex-wrap justify-between gap-2">
            <div>
              <div className="text-[11px] text-muted">{String(p.orderCode)}</div>
              <div className="font-medium text-sm">{String(p.itemDescription)}</div>
              <div className="text-xs text-muted mt-0.5">{String(p.vendorName)}</div>
            </div>
            <div className="text-right">
              <div className="font-semibold text-bd-green">
                {formatNumber(Number(p.amountBdt), 0)} BDT
              </div>
              <div className="text-[11px] text-muted">{String(p.status)}</div>
            </div>
          </div>
          {p.flagged && (
            <div className="mt-2 rounded-lg bg-bd-red-soft border border-bd-red/20 p-2 text-xs">
              <div className="font-semibold text-bd-red">Flags: {String(p.riskFlags)}</div>
              <div className="mt-1">{String(p.riskNotes)}</div>
            </div>
          )}
        </div>
      ))}
      {!intel.procurement.length && (
        <div className="card text-sm text-muted">No procurement records for this ministry</div>
      )}
    </div>
  );
}

function PeopleTab({ intel }: { intel: MinistryIntelligence }) {
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
        <StatCard label="Sanctioned" value={intel.headcount.sanctionedPosts} />
        <StatCard label="Filled" value={intel.headcount.filledPosts} accent="sky" />
        <StatCard label="Vacancy" value={intel.headcount.vacancy} accent="amber" />
        <StatCard label="Ghost signals" value={intel.headcount.anomalyHeadcount} accent="rose" />
      </div>

      <div className="card">
        <h3 className="font-medium mb-2">Office performance scores</h3>
        <div className="grid md:grid-cols-2 gap-2">
          {intel.officeScores.map((o, i) => (
            <div key={i} className="rounded-lg border border-border p-3 text-sm">
              <div className="font-medium">{String(o.officeName)}</div>
              <div className="text-xs text-muted">{String(o.district)}</div>
              <div className="mt-2 grid grid-cols-4 gap-1 text-[11px]">
                <span>Eff {formatNumber(Number(o.efficiencyScore))}</span>
                <span>Transp {formatNumber(Number(o.transparencyScore))}</span>
                <span>Budget {formatNumber(Number(o.budgetScore))}</span>
                <span className="font-semibold text-bd-green">
                  Overall {formatNumber(Number(o.overallScore))}
                </span>
              </div>
            </div>
          ))}
          {!intel.officeScores.length && (
            <div className="text-sm text-muted">No office scores</div>
          )}
        </div>
      </div>

      <div className="table-wrap">
        <table className="data">
          <thead>
            <tr>
              <th>Employee</th>
              <th>Designation</th>
              <th>Grade</th>
              <th>Attendance</th>
              <th>Performance</th>
              <th>Salary (BDT)</th>
            </tr>
          </thead>
          <tbody>
            {intel.employees.map((e, i) => (
              <tr key={i}>
                <td>
                  <div className="text-sm font-medium">{String(e.fullName)}</div>
                  <div className="text-[11px] text-muted">{String(e.employeeId)}</div>
                </td>
                <td className="text-xs">{String(e.designation)}</td>
                <td className="text-xs">{String(e.grade)}</td>
                <td className="text-xs">{formatNumber(Number(e.attendancePercent))}%</td>
                <td className="text-xs text-bd-green font-medium">
                  {formatNumber(Number(e.performanceScore))}
                </td>
                <td className="text-xs">{formatNumber(Number(e.monthlySalaryBdt), 0)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function OpsTab({ intel }: { intel: MinistryIntelligence }) {
  return (
    <div className="grid lg:grid-cols-2 gap-4">
      <div className="card">
        <h3 className="font-medium mb-3">Field reports (30 days)</h3>
        <div className="space-y-2 max-h-96 overflow-y-auto">
          {intel.recentReports.map((r) => (
            <div key={r.id} className="border-b border-border pb-2 text-sm">
              <div className="flex justify-between">
                <span className="font-medium">{r.userName}</span>
                <span className="text-[11px] text-muted">{r.reportDate}</span>
              </div>
              <div className="text-xs text-muted">{r.designation}</div>
              <div className="text-xs mt-1 line-clamp-2">{r.workCompleted}</div>
              {r.expensesBdt != null && (
                <div className="text-[11px] text-muted mt-0.5">
                  Expenses: {formatNumber(r.expensesBdt, 0)} BDT
                </div>
              )}
              {r.problems && (
                <div className="text-xs text-amber-700 dark:text-amber-300 mt-1">⚠ {r.problems}</div>
              )}
            </div>
          ))}
          {!intel.recentReports.length && (
            <div className="text-sm text-muted">No field reports</div>
          )}
        </div>
      </div>

      <div className="space-y-4">
        <div className="card">
          <h3 className="font-medium mb-2">Active alerts</h3>
          {intel.alerts.map((a) => (
            <div key={a.id} className={`p-2 rounded-lg border text-sm mb-2 ${severityClass(a.severity)}`}>
              <div className="font-medium">{a.title}</div>
              <div className="text-xs opacity-90 mt-0.5">{a.message}</div>
            </div>
          ))}
          {!intel.alerts.length && <div className="text-sm text-muted">No alerts</div>}
        </div>
        <div className="card">
          <h3 className="font-medium mb-2">Documents</h3>
          {intel.documents.map((d, i) => (
            <div key={i} className="border-b border-border py-2 text-sm">
              <div className="flex gap-2 items-center">
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-surface-muted">{String(d.docType)}</span>
                <span className="text-[10px] text-amber-700 dark:text-amber-300">
                  {String(d.classification)}
                </span>
              </div>
              <div className="font-medium mt-0.5">{String(d.title)}</div>
              <div className="text-xs text-muted line-clamp-2">{String(d.summary)}</div>
            </div>
          ))}
          {!intel.documents.length && <div className="text-sm text-muted">No documents</div>}
        </div>
      </div>
    </div>
  );
}

function KV({
  label,
  value,
  danger,
}: {
  label: string;
  value: string;
  danger?: boolean;
}) {
  return (
    <div className="rounded-lg bg-surface-muted border border-border p-2">
      <div className="text-[10px] text-muted uppercase tracking-wide">{label}</div>
      <div className={`text-sm font-semibold mt-0.5 ${danger ? "text-bd-red" : ""}`}>{value}</div>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-lg bg-black/5 dark:bg-white/5 p-2">
      <div className="text-[10px] opacity-70">{label}</div>
      <div className="font-bold text-lg">{value}</div>
    </div>
  );
}

function RiskBadge({ score }: { score: number }) {
  const s = score || 0;
  const cls =
    s >= 60
      ? "bg-bd-red-soft text-bd-red"
      : s >= 40
      ? "bg-amber-500/15 text-amber-800 dark:text-amber-300"
      : "bg-bd-green-muted text-bd-green-deep dark:text-bd-green-mid";
  return (
    <span className={`text-xs font-semibold px-2 py-0.5 rounded ${cls}`}>
      {formatNumber(s)}
    </span>
  );
}
