"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { modulesApi, RiskRow } from "@/lib/api";
import { severityClass } from "@/lib/format";
import { BiHeading } from "@/components/BiHeading";
import { useLang } from "@/lib/i18n";

export default function RisksPage() {
  const { t } = useLang();
  const [rows, setRows] = useState<RiskRow[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    modulesApi.risks().then(setRows).catch((e) => setError(e.message));
  }, []);

  return (
    <Shell>
      <BiHeading titleKey="riskTitle" subKey="riskSub" />
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <div className="grid md:grid-cols-2 gap-3">
        {rows.map((r) => (
          <div key={r.id} className={`card border ${severityClass(r.severity)}`}>
            <div className="text-[11px] uppercase tracking-wide opacity-80">{r.riskType}</div>
            <div className="font-medium mt-1">{r.title}</div>
            <div className="text-sm mt-2 opacity-90">{r.description}</div>
            <div className="text-xs mt-3 opacity-70">
              {r.ministryName}
              {r.location ? ` · ${r.location}` : ""}
            </div>
            <div className="text-sm mt-3 text-bd-green/90">
              <strong>{t("action")}:</strong> {r.recommendedAction}
            </div>
          </div>
        ))}
      </div>
    </Shell>
  );
}
