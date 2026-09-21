"use client";

import { FormEvent, useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { reportApi } from "@/lib/api";
import { DailyReport } from "@/lib/types";
import { formatBdt } from "@/lib/format";
import { BiHeading } from "@/components/BiHeading";
import { useLang } from "@/lib/i18n";

export default function ReportsPage() {
  const { t, mode } = useLang();
  const [mine, setMine] = useState<DailyReport[]>([]);
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    reportDate: new Date().toISOString().slice(0, 10),
    workCompleted: "",
    visits: "",
    approvals: "",
    expensesBdt: "",
    problems: "",
    recommendations: "",
    inspectionsCount: "0",
    schoolsVisited: "0",
    projectsApproved: "0",
  });

  function load() {
    reportApi.mine().then(setMine).catch((e) => setError(e.message));
  }

  useEffect(() => {
    load();
  }, []);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setMsg("");
    setError("");
    try {
      await reportApi.submit({
        reportDate: form.reportDate,
        workCompleted: form.workCompleted,
        visits: form.visits,
        approvals: form.approvals,
        expensesBdt: form.expensesBdt ? Number(form.expensesBdt) : 0,
        problems: form.problems,
        recommendations: form.recommendations,
        inspectionsCount: Number(form.inspectionsCount) || 0,
        schoolsVisited: Number(form.schoolsVisited) || 0,
        projectsApproved: Number(form.projectsApproved) || 0,
      });
      setMsg("Daily report submitted — feeds national statistics after supervisor review.");
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Submit failed");
    }
  }

  return (
    <Shell>
      <BiHeading titleKey="dailyReporting" subKey="dailyReportingSub" />

      <div className="grid lg:grid-cols-2 gap-6">
        <form onSubmit={onSubmit} className="card space-y-3">
          <h2 className="font-medium">{t("submitToday")}</h2>
          <div>
            <label className="text-xs text-muted">{t("date")}</label>
            <input
              type="date"
              className="input"
              value={form.reportDate}
              onChange={(e) => setForm({ ...form, reportDate: e.target.value })}
            />
          </div>
          <div>
            <label className="text-xs text-muted">{t("workCompleted")}</label>
            <textarea
              className="input min-h-[80px]"
              value={form.workCompleted}
              onChange={(e) => setForm({ ...form, workCompleted: e.target.value })}
              placeholder={mode === "bn" ? "৫টি পরিদর্শন সম্পন্ন…" : "Completed 5 inspections…"}
            />
          </div>
          <div>
            <label className="text-xs text-muted">{t("visits")}</label>
            <textarea
              className="input min-h-[60px]"
              value={form.visits}
              onChange={(e) => setForm({ ...form, visits: e.target.value })}
              placeholder={mode === "bn" ? "৩টি স্কুল পরিদর্শন…" : "Visited 3 schools…"}
            />
          </div>
          <div>
            <label className="text-xs text-muted">
              {mode === "bn" ? "অনুমোদন" : mode === "en" ? "Approvals" : "অনুমোদন · Approvals"}
            </label>
            <textarea
              className="input min-h-[60px]"
              value={form.approvals}
              onChange={(e) => setForm({ ...form, approvals: e.target.value })}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-muted">Expenses (BDT)</label>
              <input
                className="input"
                value={form.expensesBdt}
                onChange={(e) => setForm({ ...form, expensesBdt: e.target.value })}
                placeholder="35000"
              />
            </div>
            <div>
              <label className="text-xs text-muted">Inspections</label>
              <input
                className="input"
                value={form.inspectionsCount}
                onChange={(e) => setForm({ ...form, inspectionsCount: e.target.value })}
              />
            </div>
            <div>
              <label className="text-xs text-muted">Schools visited</label>
              <input
                className="input"
                value={form.schoolsVisited}
                onChange={(e) => setForm({ ...form, schoolsVisited: e.target.value })}
              />
            </div>
            <div>
              <label className="text-xs text-muted">Projects approved</label>
              <input
                className="input"
                value={form.projectsApproved}
                onChange={(e) => setForm({ ...form, projectsApproved: e.target.value })}
              />
            </div>
          </div>
          <div>
            <label className="text-xs text-muted">Problems</label>
            <textarea
              className="input min-h-[60px]"
              value={form.problems}
              onChange={(e) => setForm({ ...form, problems: e.target.value })}
              placeholder="Road damaged…"
            />
          </div>
          <div>
            <label className="text-xs text-muted">Recommendations / manpower needed</label>
            <textarea
              className="input min-h-[60px]"
              value={form.recommendations}
              onChange={(e) => setForm({ ...form, recommendations: e.target.value })}
            />
          </div>
          {msg && <div className="text-sm text-bd-green">{msg}</div>}
          {error && <div className="text-sm text-rose-300">{error}</div>}
          <button type="submit" className="btn-primary">
            {t("submitReport")}
          </button>
        </form>

        <div className="card">
          <h2 className="font-medium mb-4">{t("myReports")}</h2>
          <div className="space-y-3">
            {mine.map((r) => (
              <div key={r.id} className="rounded-lg border border-border p-3">
                <div className="flex justify-between text-sm">
                  <span className="font-medium">{r.reportDate}</span>
                  <span className="text-xs text-muted">{r.status}</span>
                </div>
                <div className="text-sm text-muted-foreground mt-1">{r.workCompleted}</div>
                <div className="text-xs text-muted mt-2">
                  Expenses: {formatBdt(r.expensesBdt)} BDT · Inspections: {r.inspectionsCount}
                </div>
                {r.problems && <div className="text-xs text-amber-300 mt-1">Problems: {r.problems}</div>}
              </div>
            ))}
            {!mine.length && <div className="text-sm text-muted">No reports yet</div>}
          </div>
        </div>
      </div>
    </Shell>
  );
}
