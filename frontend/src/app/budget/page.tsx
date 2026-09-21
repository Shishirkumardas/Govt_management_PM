"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { BudgetRow, modulesApi } from "@/lib/api";
import { formatNumber } from "@/lib/format";
import { BiHeading } from "@/components/BiHeading";
import { useLang } from "@/lib/i18n";

export default function BudgetPage() {
  const { t } = useLang();
  const [rows, setRows] = useState<BudgetRow[]>([]);
  const [note, setNote] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    modulesApi
      .budget()
      .then((r) => {
        setRows(r.ministries);
        setNote(r.note);
      })
      .catch((e) => setError(e.message));
  }, []);

  return (
    <Shell>
      <BiHeading titleKey="budgetTitle" subKey="budgetSub" />
      {error && <div className="card text-rose-300 mb-4">{error}</div>}
      {note && <div className="text-xs text-amber-300/90 mb-4">{note}</div>}

      <div className="table-wrap">
        <table className="data">
          <thead>
            <tr>
              <th>{t("ministry")}</th>
              <th>{t("budget")} (cr)</th>
              <th>{t("spent")} (cr)</th>
              <th>{t("remaining")} (cr)</th>
              <th>{t("utilization")}</th>
              <th>AI</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td className="font-medium">{r.name}</td>
                <td>{formatNumber(r.budgetCrore)}</td>
                <td>{formatNumber(r.spentCrore)}</td>
                <td>{formatNumber(r.remainingCrore)}</td>
                <td>
                  <div className="flex items-center gap-2 min-w-[120px]">
                    <div className="flex-1 h-1.5 rounded-full bg-surface-muted">
                      <div
                        className={`h-full rounded-full ${r.unusual ? "bg-amber-500" : "bg-bd-green"}`}
                        style={{ width: `${Math.min(100, r.utilizationPercent)}%` }}
                      />
                    </div>
                    <span className="text-xs">{formatNumber(r.utilizationPercent)}%</span>
                  </div>
                </td>
                <td>
                  {r.unusual ? (
                    <span className="text-xs text-amber-300">{t("unusual")}</span>
                  ) : (
                    <span className="text-xs text-muted">{t("normal")}</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Shell>
  );
}
