"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { DocRow, modulesApi } from "@/lib/api";

export default function DocumentsPage() {
  const [rows, setRows] = useState<DocRow[]>([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");

  async function search(query?: string) {
    try {
      setRows(await modulesApi.documents(query));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed");
    }
  }

  useEffect(() => {
    search();
  }, []);

  return (
    <Shell>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold">National Document Management</h1>
        <p className="text-sm text-muted mt-1">
          Policies, circulars, contracts, GOs, cabinet decisions, land & audit — AI searchable
        </p>
      </div>

      <div className="flex gap-2 mb-4">
        <input
          className="input"
          placeholder="Search documents (e.g. metro, dengue, audit)…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && search(q)}
        />
        <button className="btn-primary shrink-0" onClick={() => search(q)}>
          Search
        </button>
      </div>
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <div className="space-y-3">
        {rows.map((d) => (
          <div key={d.id} className="card">
            <div className="flex flex-wrap gap-2 items-center">
              <span className="text-[11px] px-2 py-0.5 rounded bg-surface-muted">{d.docType}</span>
              <span className="text-[11px] px-2 py-0.5 rounded bg-amber-500/15 text-amber-200">
                {d.classification}
              </span>
              <span className="text-xs text-muted">{d.issuedDate}</span>
            </div>
            <div className="font-medium mt-2">{d.title}</div>
            <div className="text-sm text-muted mt-1">{d.summary}</div>
            <div className="text-xs text-muted mt-2">
              {d.ministryName} · tags: {d.tags}
            </div>
          </div>
        ))}
      </div>
    </Shell>
  );
}
