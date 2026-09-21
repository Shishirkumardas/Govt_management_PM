"use client";

import { useEffect, useMemo, useState } from "react";
import { Shell } from "@/components/Shell";
import { KpiRow, modulesApi } from "@/lib/api";
import { formatNumber } from "@/lib/format";

export default function KpisPage() {
  const [rows, setRows] = useState<KpiRow[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    modulesApi.kpis().then(setRows).catch((e) => setError(e.message));
  }, []);

  const byMinistry = useMemo(() => {
    const map = new Map<string, KpiRow[]>();
    for (const r of rows) {
      const list = map.get(r.ministryName) || [];
      list.push(r);
      map.set(r.ministryName, list);
    }
    return map;
  }, [rows]);

  return (
    <Shell>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold">National KPIs</h1>
        <p className="text-sm text-muted mt-1">
          Education, agriculture, health and ministry performance indicators vs targets
        </p>
      </div>
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <div className="space-y-6">
        {[...byMinistry.entries()].map(([name, kpis]) => (
          <div key={name} className="card">
            <h2 className="font-medium mb-4">{name}</h2>
            <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-3">
              {kpis.map((k) => {
                const behind = k.target > 0 && k.value < k.target;
                return (
                  <div key={k.id} className="rounded-lg border border-border bg-surface-hover p-3">
                    <div className="text-xs text-muted">{k.label}</div>
                    <div className="mt-1 flex items-baseline gap-2">
                      <span className="text-xl font-semibold">{formatNumber(k.value)}</span>
                      <span className="text-xs text-muted">{k.unit}</span>
                    </div>
                    <div className={`text-xs mt-1 ${behind ? "text-amber-300" : "text-bd-green"}`}>
                      Target: {formatNumber(k.target)} {k.unit}
                      {behind ? " · below target" : " · on track"}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </Shell>
  );
}
