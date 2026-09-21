"use client";

import { useEffect, useMemo } from "react";
import { MapContainer, TileLayer, CircleMarker, Popup, Tooltip, useMap } from "react-leaflet";
import type { DistrictRow } from "@/lib/api";
import { formatNumber } from "@/lib/format";
import { useTheme } from "@/lib/theme";
import "leaflet/dist/leaflet.css";

function FitBangladesh() {
  const map = useMap();
  useEffect(() => {
    map.setView([23.7, 90.3], 7);
  }, [map]);
  return null;
}

/** Risk colors aligned with BD palette (green safe → red critical) */
function riskColor(flood: number, dengue: number): string {
  const score = Math.max(flood || 0, dengue || 0);
  if (score >= 8) return "#DA291C";
  if (score >= 6) return "#d97706";
  if (score >= 4) return "#0e7490";
  return "#006A4E";
}

export function BangladeshMap({
  districts,
  selected,
  onSelect,
}: {
  districts: DistrictRow[];
  selected: DistrictRow | null;
  onSelect: (d: DistrictRow) => void;
}) {
  const { theme } = useTheme();
  const center = useMemo<[number, number]>(() => [23.7, 90.35], []);
  const tileUrl =
    theme === "dark"
      ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
      : "https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png";

  return (
    <div className="h-[420px] md:h-[520px] w-full rounded-xl overflow-hidden border border-border relative z-0 shadow-bd">
      <MapContainer
        center={center}
        zoom={7}
        scrollWheelZoom
        className="h-full w-full"
        style={{ background: "var(--map-bg)" }}
      >
        <FitBangladesh />
        <TileLayer
          key={theme}
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url={tileUrl}
        />
        {districts.map((d) => {
          if (d.latitude == null || d.longitude == null) return null;
          const active = selected?.id === d.id;
          const color = riskColor(d.floodRiskScore, d.dengueRiskScore);
          return (
            <CircleMarker
              key={d.id}
              center={[d.latitude, d.longitude]}
              radius={active ? 14 : 10}
              pathOptions={{
                color: active ? "#ffffff" : color,
                fillColor: color,
                fillOpacity: active ? 0.95 : 0.8,
                weight: active ? 3 : 1.5,
              }}
              eventHandlers={{
                click: () => onSelect(d),
              }}
            >
              <Tooltip direction="top" offset={[0, -6]} opacity={0.95}>
                <span className="font-medium">{d.districtName}</span>
              </Tooltip>
              <Popup>
                <div className="text-sm min-w-[160px] text-slate-900">
                  <div className="font-semibold">{d.districtName}</div>
                  <div className="text-slate-600 text-xs">{d.division} Division</div>
                  <div className="mt-2 space-y-0.5 text-xs text-slate-700">
                    <div>Population: {formatNumber(d.population, 0)}</div>
                    <div>Hospitals: {d.hospitals} · Schools: {d.schools}</div>
                    <div>Projects: {d.activeProjects}</div>
                    <div>
                      Flood {d.floodRiskScore}/10 · Dengue {d.dengueRiskScore}/10
                    </div>
                  </div>
                </div>
              </Popup>
            </CircleMarker>
          );
        })}
      </MapContainer>
    </div>
  );
}
