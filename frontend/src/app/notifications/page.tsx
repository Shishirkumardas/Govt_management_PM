"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { modulesApi, NotifRow } from "@/lib/api";
import { severityClass } from "@/lib/format";
import { BiHeading } from "@/components/BiHeading";
import { useLang } from "@/lib/i18n";

export default function NotificationsPage() {
  const { t } = useLang();
  const [rows, setRows] = useState<NotifRow[]>([]);
  const [error, setError] = useState("");

  function load() {
    modulesApi.notifications().then(setRows).catch((e) => setError(e.message));
  }

  useEffect(() => {
    load();
  }, []);

  async function markRead(id: number) {
    await modulesApi.markRead(id);
    load();
  }

  return (
    <Shell>
      <BiHeading titleKey="notifTitle" subKey="notifSub" />
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <div className="space-y-3">
        {rows.map((n) => (
          <div
            key={n.id}
            className={`card flex flex-wrap justify-between gap-3 ${n.read ? "opacity-60" : ""}`}
          >
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className={`text-[11px] px-2 py-0.5 rounded border ${severityClass(n.severity)}`}>
                  {n.severity}
                </span>
                <span className="text-[11px] text-muted">{n.category}</span>
              </div>
              <div className="font-medium">{n.title}</div>
              <div className="text-sm text-muted mt-1">{n.message}</div>
            </div>
            {!n.read && (
              <button className="btn-ghost text-xs h-fit" onClick={() => markRead(n.id)}>
                {t("markRead")}
              </button>
            )}
          </div>
        ))}
      </div>
    </Shell>
  );
}
