"use client";

import { changeClass, formatNumber } from "@/lib/format";

export function StatCard({
  label,
  value,
  unit,
  change,
  accent = "emerald",
}: {
  label: string;
  value: string | number;
  unit?: string;
  change?: number;
  accent?: "emerald" | "sky" | "amber" | "rose" | "violet";
}) {
  const accents = {
    emerald: "from-bd-green/20 to-transparent border-bd-green/25",
    sky: "from-sky-500/15 to-transparent border-sky-500/25",
    amber: "from-amber-500/15 to-transparent border-amber-500/25",
    rose: "from-bd-red/15 to-transparent border-bd-red/25",
    violet: "from-violet-500/15 to-transparent border-violet-500/25",
  };

  return (
    <div
      className={`rounded-xl border bg-gradient-to-br ${accents[accent]} bg-surface-elevated p-4 shadow-bd`}
    >
      <div className="text-xs uppercase tracking-wide text-muted mb-2">{label}</div>
      <div className="flex items-baseline gap-2">
        <span className="text-2xl font-semibold tabular-nums text-foreground">
          {typeof value === "number" ? formatNumber(value) : value}
        </span>
        {unit && <span className="text-xs text-muted">{unit}</span>}
      </div>
      {change != null && (
        <div className={`mt-2 text-xs font-medium ${changeClass(change)}`}>
          {change > 0 ? "▲" : change < 0 ? "▼" : "●"} {formatNumber(Math.abs(change))}% vs prior
        </div>
      )}
    </div>
  );
}
