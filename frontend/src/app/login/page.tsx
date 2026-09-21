"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { useLang } from "@/lib/i18n";
import { LangToggle } from "@/components/LangToggle";
import { ThemeToggle } from "@/components/ThemeToggle";
import { Shield } from "lucide-react";

const demos = [
  {
    user: "pm",
    roleEn: "Prime Minister",
    roleBn: "প্রধানমন্ত্রী",
    descEn: "MFA + national dashboard",
    descBn: "এমএফএ + জাতীয় ড্যাশবোর্ড",
  },
  {
    user: "agri.minister",
    roleEn: "Agriculture Minister",
    roleBn: "কৃষি মন্ত্রী",
    descEn: "Ministry scope",
    descBn: "মন্ত্রণালয়ের পরিধি",
  },
  {
    user: "finance.sec",
    roleEn: "Finance Secretary",
    roleBn: "অর্থ সচিব",
    descEn: "Operations",
    descBn: "অপারেশন",
  },
  {
    user: "field.dhaka",
    roleEn: "Field Officer",
    roleBn: "মাঠ কর্মকর্তা",
    descEn: "Mobile field app",
    descBn: "মোবাইল মাঠ অ্যাপ",
  },
];

export default function LoginPage() {
  const { login, verifyMfa, user, isPm, isField } = useAuth();
  const { t, mode, en, bn } = useLang();
  const router = useRouter();
  const [username, setUsername] = useState("pm");
  const [password, setPassword] = useState("password123");
  const [mfaCode, setMfaCode] = useState("");
  const [mfaToken, setMfaToken] = useState<string | null>(null);
  const [mfaHint, setMfaHint] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  if (user && !mfaToken) {
    if (isPm) router.replace("/dashboard");
    else if (isField) router.replace("/field");
    else router.replace("/ministry");
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      if (mfaToken) {
        await verifyMfa(mfaToken, mfaCode);
        router.replace("/");
        return;
      }
      const res = await login(username, password, mfaCode || undefined);
      if (res.mfaRequired && res.mfaToken) {
        setMfaToken(res.mfaToken);
        setMfaHint(res.demoMfaHint || "");
        setMfaCode(res.demoMfaHint || "");
        return;
      }
      router.replace("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="min-h-screen bg-surface flex">
      <div className="hidden lg:flex flex-1 relative overflow-hidden border-r border-border">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,_var(--tw-gradient-stops))] from-bd-green/30 via-surface to-surface" />
        <div className="relative z-10 p-12 flex flex-col justify-between">
          <div>
            <div className="inline-flex items-center gap-2 text-bd-green text-sm mb-8">
              <Shield size={18} />
              <span>
                {mode === "en" ? en("nation") : bn("nation")}
                {mode === "both" && (
                  <span className="block text-xs text-bd-green/70 mt-0.5">{en("nation")}</span>
                )}
              </span>
            </div>
            <h1 className="text-4xl font-semibold tracking-tight text-foreground max-w-lg leading-tight">
              {mode === "en" ? (
                "AI-Powered Government Operating System"
              ) : (
                <>
                  <span className="block">এআই-চালিত সরকারি অপারেটিং সিস্টেম</span>
                  {mode === "both" && (
                    <span className="block text-2xl font-normal text-muted mt-2">
                      AI-Powered Government Operating System
                    </span>
                  )}
                </>
              )}
            </h1>
            <p className="mt-4 text-muted max-w-md leading-relaxed text-sm">
              {mode === "en"
                ? "MFA-hardened access · live national dashboard · field mobile reports · GIS · automatic PDF/PPT briefings."
                : mode === "bn"
                ? "এমএফএ সুরক্ষা · লাইভ জাতীয় ড্যাশবোর্ড · মাঠ মোবাইল প্রতিবেদন · জিআইএস · স্বয়ংক্রিয় পিডিএফ/পিপিটি ব্রিফিং।"
                : "এমএফএ সুরক্ষা · লাইভ জাতীয় ড্যাশবোর্ড · মাঠ মোবাইল প্রতিবেদন। MFA · live dashboard · field reports · GIS · PDF/PPT."}
            </p>
          </div>
          <div className="grid grid-cols-2 gap-3 max-w-lg">
            {(mode === "en"
              ? ["MFA + Audit logs", "Leaflet GIS", "PDF / PPT export", "Field mobile app"]
              : mode === "bn"
              ? ["এমএফএ + অডিট লগ", "লিফলেট জিআইএস", "পিডিএফ / পিপিটি", "মাঠ মোবাইল অ্যাপ"]
              : [
                  "এমএফএ + অডিট · MFA + Audit",
                  "জিআইএস · GIS",
                  "পিডিএফ/পিপিটি · PDF/PPT",
                  "মাঠ অ্যাপ · Field app",
                ]
            ).map((label) => (
              <div
                key={label}
                className="rounded-lg border border-border bg-surface-hover px-3 py-2 text-sm text-muted-foreground"
              >
                {label}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="flex-1 grid place-items-center p-6">
        <div className="w-full max-w-md">
          <div className="flex justify-end gap-2 mb-4 flex-wrap">
            <LangToggle />
            <ThemeToggle />
          </div>
          <div className="mb-8">
            <div className="bd-mark h-12 w-12 text-base mb-4">BD</div>
            <h2 className="text-2xl font-semibold text-foreground">
              {mfaToken ? t("mfaTitle") : t("signInTo")}
            </h2>
            <p className="text-sm text-muted mt-1">
              {mfaToken ? t("mfaHint") : t("secureAccess")}
            </p>
          </div>

          <form onSubmit={onSubmit} className="space-y-4">
            {!mfaToken && (
              <>
                <div>
                  <label className="text-xs text-muted mb-1.5 block">{t("username")}</label>
                  <input
                    className="input"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    autoComplete="username"
                  />
                </div>
                <div>
                  <label className="text-xs text-muted mb-1.5 block">{t("password")}</label>
                  <input
                    type="password"
                    className="input"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="current-password"
                  />
                </div>
              </>
            )}

            {mfaToken && (
              <div>
                <label className="text-xs text-muted mb-1.5 block">
                  {mode === "bn" ? "অথেন্টিকেটর কোড" : mode === "en" ? "Authenticator code" : "অথেন্টিকেটর কোড · Authenticator"}
                </label>
                <input
                  className="input text-center text-2xl tracking-[0.4em] font-mono"
                  value={mfaCode}
                  onChange={(e) => setMfaCode(e.target.value)}
                  inputMode="numeric"
                  maxLength={6}
                  autoFocus
                  placeholder="000000"
                />
                {mfaHint && (
                  <p className="text-xs text-amber-300/90 mt-2">
                    {mode === "en"
                      ? "Demo TOTP (auto-filled):"
                      : mode === "bn"
                      ? "ডেমো টিওটিপি (স্বয়ংক্রিয়):"
                      : "ডেমো টিওটিপি · Demo TOTP:"}{" "}
                    <strong>{mfaHint}</strong>
                  </p>
                )}
                <button
                  type="button"
                  className="text-xs text-muted mt-2 underline"
                  onClick={() => {
                    setMfaToken(null);
                    setMfaCode("");
                    setMfaHint("");
                  }}
                >
                  {mode === "bn" ? "পাসওয়ার্ডে ফিরুন" : "Back to password"}
                </button>
              </div>
            )}

            {error && (
              <div className="text-sm text-rose-300 bg-rose-500/10 border border-rose-500/30 rounded-lg px-3 py-2">
                {error}
              </div>
            )}
            <button type="submit" className="btn-primary w-full" disabled={busy}>
              {busy ? "…" : mfaToken ? t("verifyMfa") : t("signIn")}
            </button>
          </form>

          {!mfaToken && (
            <div className="mt-8">
              <div className="text-xs uppercase tracking-wide text-muted mb-3">
                {t("demoAccounts")}
              </div>
              <div className="space-y-2">
                {demos.map((d) => (
                  <button
                    key={d.user}
                    type="button"
                    onClick={() => {
                      setUsername(d.user);
                      setPassword("password123");
                    }}
                    className="w-full text-left rounded-lg border border-border bg-surface-hover hover:bg-surface-hover px-3 py-2.5 transition"
                  >
                    <div className="text-sm text-foreground">
                      {mode === "en" ? d.roleEn : mode === "bn" ? d.roleBn : `${d.roleBn} · ${d.roleEn}`}
                    </div>
                    <div className="text-xs text-muted">
                      {d.user} / password123 ·{" "}
                      {mode === "en" ? d.descEn : mode === "bn" ? d.descBn : d.descBn}
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
