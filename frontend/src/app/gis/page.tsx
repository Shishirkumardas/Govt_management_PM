"use client";

import dynamic from "next/dynamic";
import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { DistrictRow, modulesApi } from "@/lib/api";
import { formatNumber } from "@/lib/format";
import { BiHeading } from "@/components/BiHeading";
import { districtLabel, useLang, DIVISION_BN, dual } from "@/lib/i18n";

const BangladeshMap = dynamic(
  () => import("@/components/BangladeshMap").then((m) => m.BangladeshMap),
  {
    ssr: false,
    loading: () => (
      <div className="h-[420px] md:h-[520px] grid place-items-center rounded-xl border border-border bg-surface-elevated text-muted">
        Loading Bangladesh map…
      </div>
    ),
  }
);

export default function GisPage() {
  const { t, mode } = useLang();
  const [districts, setDistricts] = useState<DistrictRow[]>([]);
  const [selected, setSelected] = useState<DistrictRow | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    modulesApi
      .districts()
      .then((d) => {
        setDistricts(d);
        setSelected(d.find((x) => x.districtName === "Dhaka") || d[0] || null);
      })
      .catch((e) => setError(e.message));
  }, []);

  return (
    <Shell>
      <BiHeading titleKey="gisTitle" subKey="gisSub" />
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <div className="grid lg:grid-cols-5 gap-4">
        <div className="lg:col-span-3 space-y-3">
          <BangladeshMap
            districts={districts}
            selected={selected}
            onSelect={setSelected}
          />
          <div className="flex flex-wrap gap-3 text-xs text-muted">
            <span className="inline-flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-full bg-bd-green" /> {t("lowRisk")}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-full bg-sky-400" /> {t("moderate")}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-full bg-amber-400" /> {t("elevated")}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-full bg-rose-500" /> {t("highRisk")}
            </span>
          </div>
        </div>

        <div className="lg:col-span-2 space-y-3">
          <div className="card max-h-48 overflow-y-auto">
            <div className="text-xs text-muted mb-2">{t("districts")}</div>
            <div className="grid grid-cols-2 gap-1.5">
              {districts.map((d) => (
                <button
                  key={d.id}
                  onClick={() => setSelected(d)}
                  className={`rounded-lg border px-2 py-1.5 text-left text-xs transition ${
                    selected?.id === d.id
                      ? "border-bd-green/40 bg-bd-green-muted"
                      : "border-border hover:bg-surface-hover"
                  }`}
                >
                  {districtLabel(d.districtName, mode)}
                </button>
              ))}
            </div>
          </div>

          {selected && (
            <div className="card space-y-4">
              <div>
                <h2 className="text-xl font-semibold">
                  {districtLabel(selected.districtName, mode)}
                </h2>
                <div className="text-sm text-muted">
                  {dual(
                    `${selected.division} Division`,
                    `${DIVISION_BN[selected.division] || selected.division} বিভাগ`,
                    mode
                  )}{" "}
                  · {selected.latitude?.toFixed(2)}, {selected.longitude?.toFixed(2)}
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <Tile label={t("population")} value={formatNumber(selected.population, 0)} />
                <Tile label={t("hospitals")} value={String(selected.hospitals)} />
                <Tile label={t("schools")} value={String(selected.schools)} />
                <Tile label={t("nav_projects")} value={String(selected.activeProjects)} />
                <Tile label={mode === "bn" ? "অপরাধ / মাস" : "Crime / mo"} value={String(selected.crimeCasesMonth)} />
                <Tile label={t("budget")} value={`${formatNumber(selected.budgetCrore)} cr`} />
                <Tile label={t("floodRisk")} value={`${selected.floodRiskScore}/10`} />
                <Tile label={t("dengueRisk")} value={`${selected.dengueRiskScore}/10`} />
              </div>
              <div className="rounded-lg bg-sky-500/10 border border-sky-500/20 p-3 text-sm">
                <div className="text-xs text-sky-300 mb-1">{t("weather")}</div>
                {selected.weatherNote}
              </div>
              <div className="rounded-lg bg-violet-500/10 border border-violet-500/20 p-3 text-sm">
                <div className="text-xs text-violet-300 mb-1">{t("investments")}</div>
                {selected.investmentNote}
              </div>
            </div>
          )}
        </div>
      </div>
    </Shell>
  );
}

function Tile({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-border bg-surface-hover p-2.5">
      <div className="text-[11px] text-muted">{label}</div>
      <div className="font-medium mt-0.5 text-sm">{value}</div>
    </div>
  );
}
