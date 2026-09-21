export function formatNumber(n: number | null | undefined, digits = 1): string {
  if (n == null || Number.isNaN(n)) return "—";
  return new Intl.NumberFormat("en-BD", {
    maximumFractionDigits: digits,
    minimumFractionDigits: 0,
  }).format(n);
}

export function formatBdt(n: number | null | undefined): string {
  if (n == null) return "—";
  if (Math.abs(n) >= 1e7) return `${formatNumber(n / 1e7)} Cr`;
  if (Math.abs(n) >= 1e5) return `${formatNumber(n / 1e5)} Lakh`;
  return formatNumber(n, 0);
}

export function changeClass(pct: number | null | undefined): string {
  if (pct == null) return "text-muted";
  if (pct > 0) return "text-bd-green";
  if (pct < 0) return "text-bd-red";
  return "text-muted";
}

export function severityClass(severity: string): string {
  switch (severity) {
    case "CRITICAL":
      return "bg-bd-red-soft text-bd-red border-bd-red/40";
    case "HIGH":
      return "bg-orange-500/15 text-orange-700 dark:text-orange-300 border-orange-500/30";
    case "MEDIUM":
      return "bg-amber-500/15 text-amber-800 dark:text-amber-300 border-amber-500/30";
    case "LOW":
      return "bg-sky-500/15 text-sky-800 dark:text-sky-300 border-sky-500/30";
    default:
      return "bg-surface-muted text-muted border-border";
  }
}

export function statusClass(status: string): string {
  switch (status) {
    case "ONGOING":
      return "bg-bd-green-muted text-bd-green-deep dark:text-bd-green-mid";
    case "DELAYED":
      return "bg-bd-red-soft text-bd-red";
    case "COMPLETED":
      return "bg-sky-500/15 text-sky-800 dark:text-sky-300";
    case "APPROVED":
      return "bg-violet-500/15 text-violet-800 dark:text-violet-300";
    default:
      return "bg-surface-muted text-muted";
  }
}
