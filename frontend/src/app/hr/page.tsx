"use client";

import { useEffect, useState } from "react";
import { Shell } from "@/components/Shell";
import { EmpRow, modulesApi, OfficeRow } from "@/lib/api";
import { formatNumber } from "@/lib/format";

export default function HrPage() {
  const [emps, setEmps] = useState<EmpRow[]>([]);
  const [offices, setOffices] = useState<OfficeRow[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([modulesApi.employees(), modulesApi.officeScores()])
      .then(([e, o]) => {
        setEmps(e);
        setOffices(o);
      })
      .catch((e) => setError(e.message));
  }, []);

  return (
    <Shell>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold">HR & AI Performance Scores</h1>
        <p className="text-sm text-muted mt-1">
          Employees, attendance, performance — and office efficiency / transparency scores
        </p>
      </div>
      {error && <div className="card text-rose-300 mb-4">{error}</div>}

      <h2 className="font-medium mb-3">Office AI scores</h2>
      <div className="grid md:grid-cols-2 gap-3 mb-8">
        {offices.map((o) => (
          <div key={o.id} className="card">
            <div className="flex justify-between">
              <div>
                <div className="font-medium">{o.officeName}</div>
                <div className="text-xs text-muted">
                  {o.ministryName} · {o.district}
                </div>
              </div>
              <div className="text-2xl font-semibold text-bd-green">
                {formatNumber(o.overallScore)}
              </div>
            </div>
            <div className="grid grid-cols-3 gap-2 mt-4 text-xs">
              <Score label="Efficiency" v={o.efficiencyScore} />
              <Score label="Transparency" v={o.transparencyScore} />
              <Score label="Budget" v={o.budgetScore} />
              <Score label="Citizen sat." v={o.citizenSatisfaction} />
              <Score label="Projects" v={o.projectCompletion} />
              <Score label="Attendance" v={o.attendanceScore} />
            </div>
          </div>
        ))}
      </div>

      <h2 className="font-medium mb-3">Employees</h2>
      <div className="table-wrap">
        <table className="data">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Ministry</th>
              <th>Attendance</th>
              <th>Performance</th>
              <th>Leave</th>
              <th>Training</th>
            </tr>
          </thead>
          <tbody>
            {emps.map((e) => (
              <tr key={e.id}>
                <td className="text-xs">{e.employeeId}</td>
                <td>
                  <div className="font-medium">{e.fullName}</div>
                  <div className="text-[11px] text-muted">{e.designation}</div>
                </td>
                <td className="text-xs">{e.ministryName}</td>
                <td>{formatNumber(e.attendancePercent)}%</td>
                <td className="text-bd-green">{formatNumber(e.performanceScore)}</td>
                <td>{e.leaveBalanceDays}d</td>
                <td className="text-xs">{e.trainingStatus}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Shell>
  );
}

function Score({ label, v }: { label: string; v: number }) {
  return (
    <div className="rounded bg-surface-hover border border-border p-2">
      <div className="text-muted">{label}</div>
      <div className="font-medium text-sm mt-0.5">{formatNumber(v)}</div>
    </div>
  );
}
