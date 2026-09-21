"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { modulesApi, ProcurementRow } from "@/lib/api";
import { formatNumber } from "@/lib/format";

export default function ProcurementPage() {
  const [rows, setRows] = useState<ProcurementRow[]>([]);
  const [flaggedOnly, setFlaggedOnly] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    modulesApi
      .procurement(flaggedOnly)
      .then(setRows)
      .catch((e) => setError(e.message));
  }, [flaggedOnly]);

  return (
    <Shell>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">Procurement Intelligence</h1>
          <p className="text-sm text-muted mt-1">
            AI checks price inflation, duplicate invoices, fake vendors, repeated purchases, conflicts
          </p>
        </div>
        <label className="flex items-center gap-2 text-sm text-muted-foreground">
          <input
            type="checkbox"
            checked={flaggedOnly}
            onChange={(e) => setFlaggedOnly(e.target.checked)}
          />
          Flagged only
        </label>
      </div>
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <div className="space-y-3">
        {rows.map((p) => (
          <div
            key={p.id}
            className={`card ${p.flagged ? "border-rose-500/30" : ""}`}
          >
            <div className="flex flex-wrap justify-between gap-2">
              <div>
                <div className="text-xs text-muted">{p.orderCode}</div>
                <div className="font-medium">{p.itemDescription}</div>
                <div className="text-sm text-muted mt-1">
                  {p.vendorName} · {p.ministryName}
                </div>
              </div>
              <div className="text-right">
                <div className="text-bd-green font-medium">
                  {formatNumber(p.amountBdt, 0)} BDT
                </div>
                <div className="text-xs text-muted">{p.status}</div>
              </div>
            </div>
            {p.flagged && (
              <div className="mt-3 rounded-lg bg-rose-500/10 border border-rose-500/20 p-3 text-sm">
                <div className="text-rose-300 font-medium">Flags: {p.riskFlags}</div>
                <div className="text-rose-100/80 mt-1">{p.riskNotes}</div>
              </div>
            )}
          </div>
        ))}
      </div>
    </Shell>
  );
}
