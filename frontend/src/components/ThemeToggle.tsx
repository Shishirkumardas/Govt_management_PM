"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "@/lib/theme";
import { useLang } from "@/lib/i18n";

export function ThemeToggle({ compact = false }: { compact?: boolean }) {
  const { theme, setTheme } = useTheme();
  const { mode } = useLang();

  const label =
    mode === "bn"
      ? theme === "dark"
        ? "অন্ধকার"
        : "উজ্জ্বল"
      : mode === "en"
      ? theme === "dark"
        ? "Dark"
        : "Light"
      : theme === "dark"
      ? "অন্ধকার · Dark"
      : "উজ্জ্বল · Light";

  return (
    <div
      className={`inline-flex items-center rounded-lg border border-border bg-surface-muted p-0.5 ${
        compact ? "text-[10px]" : "text-xs"
      }`}
      role="group"
      aria-label={mode === "bn" ? "থিম" : "Theme"}
    >
      <button
        type="button"
        onClick={() => setTheme("light")}
        className={`inline-flex items-center gap-1 px-2 py-1 rounded-md transition font-medium ${
          theme === "light"
            ? "bg-bd-green text-white shadow-sm"
            : "text-muted hover:text-foreground"
        }`}
        title="Light / উজ্জ্বল"
      >
        <Sun size={compact ? 12 : 14} />
        {!compact && <span>{mode === "en" ? "Light" : "উজ্জ্বল"}</span>}
      </button>
      <button
        type="button"
        onClick={() => setTheme("dark")}
        className={`inline-flex items-center gap-1 px-2 py-1 rounded-md transition font-medium ${
          theme === "dark"
            ? "bg-bd-green text-white shadow-sm"
            : "text-muted hover:text-foreground"
        }`}
        title="Dark / অন্ধকার"
      >
        <Moon size={compact ? 12 : 14} />
        {!compact && <span>{mode === "en" ? "Dark" : "অন্ধকার"}</span>}
      </button>
      {compact && <span className="sr-only">{label}</span>}
    </div>
  );
}
