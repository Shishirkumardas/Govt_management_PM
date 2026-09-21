"use client";

import { LangMode, useLang } from "@/lib/i18n";

const options: { mode: LangMode; label: string }[] = [
  { mode: "both", label: "বাংলা · EN" },
  { mode: "bn", label: "বাংলা" },
  { mode: "en", label: "EN" },
];

export function LangToggle({ compact = false }: { compact?: boolean }) {
  const { mode, setMode, t } = useLang();

  return (
    <div
      className={`inline-flex items-center rounded-lg border border-border bg-surface-muted p-0.5 ${
        compact ? "text-[10px]" : "text-xs"
      }`}
      role="group"
      aria-label={t("language")}
    >
      {options.map((o) => (
        <button
          key={o.mode}
          type="button"
          onClick={() => setMode(o.mode)}
          className={`px-2 py-1 rounded-md transition font-medium ${
            mode === o.mode
              ? "bg-bd-green text-white shadow-sm"
              : "text-muted hover:text-foreground"
          }`}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}
