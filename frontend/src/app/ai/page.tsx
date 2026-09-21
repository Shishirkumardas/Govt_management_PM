"use client";

import { FormEvent, useState } from "react";
import { Shell } from "@/components/Shell";
import { aiApi } from "@/lib/api";
import { BiHeading } from "@/components/BiHeading";
import { useLang } from "@/lib/i18n";

const SUGGESTIONS = [
  { en: "Which ministry exceeded budget this month?", bn: "কোন মন্ত্রণালয় এই মাসে বাজেট অতিক্রম করেছে?" },
  { en: "Why has rice production decreased?", bn: "ধান উৎপাদন কেন কমেছে?" },
  { en: "Which districts have highest dengue risk?", bn: "কোন জেলায় ডেঙ্গু ঝুঁকি সবচেয়ে বেশি?" },
  { en: "Which projects are delayed over 6 months?", bn: "কোন প্রকল্প ৬ মাসের বেশি বিলম্বিত?" },
  { en: "Show procurement risk flags", bn: "ক্রয় ঝুঁকির ফ্ল্যাগ দেখান" },
  { en: "What does predictive AI say about inflation and food?", bn: "মূল্যস্ফীতি ও খাদ্য নিয়ে এআই কী বলছে?" },
];

export default function AiPage() {
  const { t, mode } = useLang();
  const [question, setQuestion] = useState(SUGGESTIONS[0].en);
  const [answer, setAnswer] = useState("");
  const [meta, setMeta] = useState("");
  const [briefing, setBriefing] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function ask(q?: string) {
    const query = (q ?? question).trim();
    if (!query) return;
    setBusy(true);
    setError("");
    try {
      const res = await aiApi.ask(query);
      setQuestion(query);
      setAnswer(res.answer);
      setMeta(`${res.model}${res.usedLiveAi ? " · live SpaceXAI" : " · data-grounded"}`);
    } catch (e) {
      setError(e instanceof Error ? e.message : "AI failed");
    } finally {
      setBusy(false);
    }
  }

  async function loadBriefing() {
    setBusy(true);
    try {
      const res = await aiApi.nationalBriefing();
      setBriefing(res.briefing);
      setMeta(`${res.model}${res.usedLiveAi ? " · live" : " · rule engine"}`);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Briefing failed");
    } finally {
      setBusy(false);
    }
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault();
    ask();
  }

  return (
    <Shell>
      <div className="mb-2 flex flex-wrap items-end justify-between gap-3">
        <BiHeading titleKey="aiTitle" subKey="aiSub" />
        <button className="btn-ghost mb-6" onClick={loadBriefing} disabled={busy}>
          {t("nationalBriefing")}
        </button>
      </div>

      <div className="grid lg:grid-cols-3 gap-4">
        <div className="lg:col-span-2 space-y-4">
          <form onSubmit={onSubmit} className="card space-y-3">
            <label className="text-xs text-muted">{t("yourQuestion")}</label>
            <textarea
              className="input min-h-[100px]"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
            />
            <button className="btn-primary" disabled={busy}>
              {busy ? "…" : t("askGovos")}
            </button>
            {error && <div className="text-sm text-rose-300">{error}</div>}
          </form>

          {answer && (
            <div className="card">
              <div className="text-xs text-muted mb-2">{meta}</div>
              <div className="prose-briefing text-sm text-foreground">{answer}</div>
            </div>
          )}

          {briefing && (
            <div className="card">
              <h2 className="font-medium mb-2">{t("nationalBriefing")}</h2>
              <div className="prose-briefing text-sm text-foreground">{briefing}</div>
            </div>
          )}
        </div>

        <div className="card">
          <h2 className="font-medium mb-3">{t("tryAsking")}</h2>
          <div className="space-y-2">
            {SUGGESTIONS.map((s) => (
              <button
                key={s.en}
                type="button"
                onClick={() => ask(s.en)}
                className="w-full text-left text-sm rounded-lg border border-border bg-surface-hover hover:bg-surface-hover px-3 py-2.5"
              >
                <span className="block">{mode === "en" ? s.en : s.bn}</span>
                {mode === "both" && (
                  <span className="block text-xs text-muted mt-0.5">{s.en}</span>
                )}
              </button>
            ))}
          </div>
          <p className="text-xs text-muted mt-4">
            Set <code className="text-bd-green">XAI_API_KEY</code> on the backend for live Grok
            (SpaceXAI). Without it, answers still run on the data-grounded engine.
          </p>
        </div>
      </div>
    </Shell>
  );
}
