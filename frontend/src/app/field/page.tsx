"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { reportApi } from "@/lib/api";
import { DailyReport } from "@/lib/types";
import { formatBdt } from "@/lib/format";
import { roleLabel, useLang } from "@/lib/i18n";
import { LangToggle } from "@/components/LangToggle";
import { ThemeToggle } from "@/components/ThemeToggle";
import {
  Camera,
  CheckCircle2,
  ClipboardList,
  LogOut,
  MapPin,
  Navigation,
} from "lucide-react";

export default function FieldPage() {
  const { user, loading, logout } = useAuth();
  const { t, mode } = useLang();
  const router = useRouter();
  const [step, setStep] = useState<"home" | "form" | "done">("home");
  const [mine, setMine] = useState<DailyReport[]>([]);
  const [gps, setGps] = useState<{ lat: number; lng: number } | null>(null);
  const [gpsMsg, setGpsMsg] = useState("");
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({
    workCompleted: "",
    visits: "",
    expensesBdt: "",
    problems: "",
    recommendations: "",
    inspectionsCount: "0",
    schoolsVisited: "0",
    projectsApproved: "0",
  });

  useEffect(() => {
    if (!loading && !user) router.replace("/login");
  }, [loading, user, router]);

  useEffect(() => {
    if (user) {
      reportApi.mine().then(setMine).catch(() => {});
    }
  }, [user]);

  function captureGps() {
    setGpsMsg(mode === "bn" ? "অবস্থান খোঁজা হচ্ছে…" : "Locating…");
    if (!navigator.geolocation) {
      setGpsMsg(mode === "bn" ? "এই ডিভাইসে জিপিএস নেই" : "GPS not available");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setGps({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        setGpsMsg(mode === "bn" ? "অবস্থান সংরক্ষিত" : "Location captured");
      },
      () =>
        setGpsMsg(
          mode === "bn"
            ? "জিপিএস পাওয়া যায়নি — ছাড়াই জমা দিতে পারেন"
            : "Could not get GPS — submit without location"
        ),
      { enableHighAccuracy: true, timeout: 12000 }
    );
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      await reportApi.submit({
        reportDate: new Date().toISOString().slice(0, 10),
        workCompleted: form.workCompleted,
        visits: form.visits,
        expensesBdt: form.expensesBdt ? Number(form.expensesBdt) : 0,
        problems: form.problems,
        recommendations: form.recommendations,
        inspectionsCount: Number(form.inspectionsCount) || 0,
        schoolsVisited: Number(form.schoolsVisited) || 0,
        projectsApproved: Number(form.projectsApproved) || 0,
        latitude: gps?.lat,
        longitude: gps?.lng,
      });
      setMsg(
        mode === "bn"
          ? "প্রতিবেদন জমা হয়েছে। তত্ত্বাবধায়ক পর্যালোচনা করবেন।"
          : "Report submitted. Supervisor will review."
      );
      setStep("done");
      setMine(await reportApi.mine());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Submit failed");
    } finally {
      setBusy(false);
    }
  }

  if (loading || !user) {
    return (
      <div className="min-h-screen grid place-items-center bg-surface text-muted">
        {t("loading")}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface text-foreground max-w-lg mx-auto flex flex-col">
      <header className="sticky top-0 z-20 border-b border-border bg-surface/95 backdrop-blur px-4 py-3 safe-top">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-xs text-bd-green font-medium">{t("fieldApp")}</div>
            <div className="font-semibold text-sm truncate max-w-[200px]">{user.fullName}</div>
          </div>
          <div className="flex items-center gap-2">
            <LangToggle compact />
            <ThemeToggle compact />
            <button
              onClick={() => {
                logout();
                router.push("/login");
              }}
              className="p-2 rounded-lg bg-surface-hover border border-border"
              aria-label={t("signOut")}
            >
              <LogOut size={18} />
            </button>
          </div>
        </div>
        <div className="text-[11px] text-muted mt-0.5">
          {roleLabel(user.role, t)}
          {user.ministryName ? ` · ${user.ministryName}` : ""}
        </div>
      </header>

      <main className="flex-1 p-4 pb-28">
        {step === "home" && (
          <div className="space-y-4">
            <div className="rounded-2xl bg-gradient-to-br from-bd-green/25 to-surface-elevated border border-bd-green/25 p-5 shadow-bd">
              <ClipboardList className="text-bd-green mb-2" size={28} />
              <h1 className="text-xl font-semibold">{t("fieldToday")}</h1>
              <p className="text-sm text-muted mt-1">{t("fieldTodaySub")}</p>
              <button
                className="mt-4 w-full btn-primary py-3.5 text-base rounded-xl"
                onClick={() => {
                  setStep("form");
                  captureGps();
                }}
              >
                {t("startReport")}
              </button>
            </div>

            <div>
              <h2 className="text-sm font-medium text-muted mb-2">{t("recentSubmissions")}</h2>
              <div className="space-y-2">
                {mine.slice(0, 5).map((r) => (
                  <div key={r.id} className="rounded-xl border border-border bg-surface-hover p-3">
                    <div className="flex justify-between text-sm">
                      <span className="font-medium">{r.reportDate}</span>
                      <span className="text-xs text-muted">{r.status}</span>
                    </div>
                    <div className="text-xs text-muted mt-1 line-clamp-2">{r.workCompleted}</div>
                    <div className="text-[11px] text-muted mt-1">
                      {formatBdt(r.expensesBdt)} BDT · {r.inspectionsCount || 0}{" "}
                      {mode === "bn" ? "পরিদর্শন" : "inspections"}
                    </div>
                  </div>
                ))}
                {!mine.length && (
                  <div className="text-sm text-muted">
                    {mode === "bn" ? "এখনও কোনো প্রতিবেদন নেই" : "No reports yet"}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {step === "form" && (
          <form onSubmit={submit} className="space-y-4">
            <div className="rounded-xl border border-border bg-surface-hover p-3 flex items-center justify-between gap-3">
              <div className="flex items-center gap-2 text-sm">
                <MapPin size={18} className="text-bd-green" />
                <div>
                  <div className="font-medium">{t("gpsLocation")}</div>
                  <div className="text-xs text-muted">
                    {gps
                      ? `${gps.lat.toFixed(5)}, ${gps.lng.toFixed(5)}`
                      : gpsMsg || "—"}
                  </div>
                </div>
              </div>
              <button type="button" className="btn-ghost py-2 px-3 text-xs" onClick={captureGps}>
                <Navigation size={14} />
              </button>
            </div>

            <Field
              label={`${t("workCompleted")} *`}
              value={form.workCompleted}
              onChange={(v) => setForm({ ...form, workCompleted: v })}
              placeholder={mode === "bn" ? "৫টি পরিদর্শন সম্পন্ন…" : "Completed 5 inspections…"}
              rows={3}
              required
            />
            <Field
              label={t("visits")}
              value={form.visits}
              onChange={(v) => setForm({ ...form, visits: v })}
              placeholder={mode === "bn" ? "৩টি স্কুল পরিদর্শন…" : "Visited 3 schools…"}
              rows={2}
            />

            <div className="grid grid-cols-3 gap-2">
              <Num label={t("inspections")} value={form.inspectionsCount} onChange={(v) => setForm({ ...form, inspectionsCount: v })} />
              <Num label={t("schools")} value={form.schoolsVisited} onChange={(v) => setForm({ ...form, schoolsVisited: v })} />
              <Num label={t("approved")} value={form.projectsApproved} onChange={(v) => setForm({ ...form, projectsApproved: v })} />
            </div>

            <div>
              <label className="text-xs text-muted mb-1 block">{t("expenses")}</label>
              <input
                className="input py-3 text-base"
                inputMode="numeric"
                value={form.expensesBdt}
                onChange={(e) => setForm({ ...form, expensesBdt: e.target.value })}
                placeholder="35000"
              />
            </div>

            <Field
              label={t("problems")}
              value={form.problems}
              onChange={(v) => setForm({ ...form, problems: v })}
              placeholder={mode === "bn" ? "রাস্তা ক্ষতিগ্রস্ত; অতিরিক্ত শ্রমিক দরকার…" : "Road damaged; need extra labour…"}
              rows={2}
            />
            <Field
              label={t("recommendations")}
              value={form.recommendations}
              onChange={(v) => setForm({ ...form, recommendations: v })}
              rows={2}
            />

            <div className="rounded-xl border border-dashed border-border p-4 text-center text-sm text-muted">
              <Camera className="mx-auto mb-1 opacity-50" size={22} />
              {mode === "bn" ? "ছবি আপলোড (পরবর্তী ধাপ) — জিপিএস সংযুক্ত" : "Photo upload (Phase 2) — GPS attached"}
            </div>

            {error && <div className="text-sm text-rose-300 bg-rose-500/10 rounded-lg p-3">{error}</div>}

            <div className="flex gap-2">
              <button type="button" className="btn-ghost flex-1 py-3" onClick={() => setStep("home")}>
                {t("cancel")}
              </button>
              <button type="submit" className="btn-primary flex-1 py-3" disabled={busy}>
                {busy ? "…" : t("submitReport")}
              </button>
            </div>
          </form>
        )}

        {step === "done" && (
          <div className="text-center py-12 space-y-4">
            <CheckCircle2 className="mx-auto text-bd-green" size={56} />
            <h2 className="text-xl font-semibold">{t("reportSent")}</h2>
            <p className="text-sm text-muted px-6">{msg}</p>
            <button
              className="btn-primary w-full py-3.5"
              onClick={() => {
                setStep("home");
                setForm({
                  workCompleted: "",
                  visits: "",
                  expensesBdt: "",
                  problems: "",
                  recommendations: "",
                  inspectionsCount: "0",
                  schoolsVisited: "0",
                  projectsApproved: "0",
                });
              }}
            >
              {t("backHome")}
            </button>
          </div>
        )}
      </main>

      {step === "home" && (
        <nav className="fixed bottom-0 left-0 right-0 max-w-lg mx-auto border-t border-border bg-surface/95 backdrop-blur px-4 py-2 safe-bottom">
          <div className="grid grid-cols-3 gap-1 text-center text-[11px]">
            <button className="py-2 text-bd-green" onClick={() => setStep("home")}>
              {t("home")}
            </button>
            <button className="py-2 text-muted" onClick={() => setStep("form")}>
              {t("report")}
            </button>
            <button className="py-2 text-muted" onClick={() => router.push("/notifications")}>
              {t("alerts")}
            </button>
          </div>
        </nav>
      )}
    </div>
  );
}

function Field({
  label,
  value,
  onChange,
  placeholder,
  rows = 2,
  required,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
  rows?: number;
  required?: boolean;
}) {
  return (
    <div>
      <label className="text-xs text-muted mb-1 block">{label}</label>
      <textarea
        className="input min-h-[72px] text-base py-3"
        rows={rows}
        value={value}
        required={required}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}

function Num({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div>
      <label className="text-[11px] text-muted mb-1 block">{label}</label>
      <input
        className="input py-3 text-center text-base"
        inputMode="numeric"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}
