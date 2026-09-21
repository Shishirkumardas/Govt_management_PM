import type { Config } from "tailwindcss";

/**
 * Bangladesh government digital palette
 * Flag green #006A4E / #006747 · Flag red #DA291C (Pantone 342 C / 485 C approximations)
 */
export default {
  darkMode: "class",
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "var(--surface)",
        foreground: "var(--foreground)",
        surface: {
          DEFAULT: "var(--surface)",
          elevated: "var(--surface-elevated)",
          muted: "var(--surface-muted)",
          hover: "var(--surface-hover)",
          sidebar: "var(--surface-sidebar)",
          header: "var(--surface-header)",
        },
        muted: {
          DEFAULT: "var(--muted)",
          foreground: "var(--foreground-muted)",
        },
        border: {
          DEFAULT: "var(--border)",
          strong: "var(--border-strong)",
        },
        bd: {
          green: "var(--bd-green)",
          "green-deep": "var(--bd-green-deep)",
          "green-mid": "var(--bd-green-mid)",
          "green-soft": "var(--bd-green-soft)",
          "green-muted": "var(--bd-green-muted)",
          red: "var(--bd-red)",
          "red-soft": "var(--bd-red-soft)",
          gold: "var(--bd-gold)",
        },
        success: "var(--success)",
        warning: "var(--warning)",
        danger: "var(--danger)",
        info: "var(--info)",
      },
      boxShadow: {
        bd: "var(--shadow)",
      },
    },
  },
  plugins: [],
} satisfies Config;
