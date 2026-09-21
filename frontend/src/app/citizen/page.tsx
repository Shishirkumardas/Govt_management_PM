"use client";

import { FormEvent, useState } from "react";
import { Shell } from "@/components/Shell";
import { ComplaintRow, modulesApi } from "@/lib/api";
import { BiHeading } from "@/components/BiHeading";
import { useLang } from "@/lib/i18n";

export default function CitizenPage() {
  const { t, mode } = useLang();
  const [form, setForm] = useState({
    citizenName: "",
    phone: "",
    subject: "",
    description: "",
    district: "Dhaka",
  });
  const [tracking, setTracking] = useState("");
  const [result, setResult] = useState<ComplaintRow | null>(null);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setMsg("");
    try {
      const res = await modulesApi.submitComplaint(form);
      setResult(res);
      setMsg(`Complaint filed. Tracking ID: ${res.trackingId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed");
    }
  }

  async function track(e: FormEvent) {
    e.preventDefault();
    setError("");
    try {
      setResult(await modulesApi.trackComplaint(tracking));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Not found");
    }
  }

  return (
    <Shell>
      <BiHeading titleKey="citizenTitle" subKey="citizenSub" />

      <div className="grid lg:grid-cols-2 gap-6">
        <form onSubmit={submit} className="card space-y-3">
          <h2 className="font-medium">{t("submitComplaint")}</h2>
          <input
            className="input"
            placeholder={t("yourName")}
            value={form.citizenName}
            onChange={(e) => setForm({ ...form, citizenName: e.target.value })}
            required
          />
          <input
            className="input"
            placeholder={t("phone")}
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
          />
          <input
            className="input"
            placeholder={t("subject")}
            value={form.subject}
            onChange={(e) => setForm({ ...form, subject: e.target.value })}
            required
          />
          <textarea
            className="input min-h-[100px]"
            placeholder={t("description")}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <input
            className="input"
            placeholder={t("district")}
            value={form.district}
            onChange={(e) => setForm({ ...form, district: e.target.value })}
          />
          <button className="btn-primary" type="submit">
            {t("submitComplaint")}
          </button>
          {msg && <div className="text-sm text-bd-green">{msg}</div>}
        </form>

        <div className="space-y-4">
          <form onSubmit={track} className="card space-y-3">
            <h2 className="font-medium">{t("trackComplaint")}</h2>
            <input
              className="input"
              placeholder="Tracking ID e.g. CMP-DEMO001"
              value={tracking}
              onChange={(e) => setTracking(e.target.value)}
            />
            <button className="btn-ghost" type="submit">
              Track status
            </button>
          </form>

          {error && <div className="card text-rose-300">{error}</div>}

          {result && (
            <div className="card">
              <div className="text-xs text-muted">{result.trackingId}</div>
              <div className="font-medium mt-1">{result.subject}</div>
              <div className="text-sm text-muted mt-1">{result.description}</div>
              <div className="mt-3 text-sm">
                Status: <span className="text-bd-green">{result.status}</span>
              </div>
              <div className="text-xs text-muted mt-1">
                {result.district}
                {result.ministryName ? ` · ${result.ministryName}` : ""}
              </div>
            </div>
          )}

          <div className="card">
            <h2 className="font-medium mb-2">
              {mode === "bn"
                ? "পরিকল্পিত ডিজিটাল সেবা (পর্যায় ৫)"
                : mode === "en"
                ? "Planned digital services (Phase 5)"
                : "পরিকল্পিত ডিজিটাল সেবা · Phase 5"}
            </h2>
            <ul className="text-sm text-muted space-y-1 list-disc list-inside">
              <li>{mode === "en" ? "Licenses & business registration" : "লাইসেন্স ও ব্যবসা নিবন্ধন"}</li>
              <li>{mode === "en" ? "Birth / certificates / land records" : "জন্ম / সনদ / ভূমি রেকর্ড"}</li>
              <li>{mode === "en" ? "Tax & utility payments" : "কর ও ইউটিলিটি পরিশোধ"}</li>
              <li>{mode === "en" ? "Passport & National ID status" : "পাসপোর্ট ও জাতীয় পরিচয়পত্র"}</li>
            </ul>
          </div>
        </div>
      </div>
    </Shell>
  );
}
