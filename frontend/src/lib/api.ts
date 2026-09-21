const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("govos_token");
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };
  if (token) {
    (headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (!res.ok) {
    let msg = res.statusText;
    try {
      const body = await res.json();
      msg = body.error || body.message || JSON.stringify(body);
    } catch {
      /* ignore */
    }
    throw new ApiError(res.status, msg);
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

export type LoginResult = {
  token?: string;
  username?: string;
  fullName?: string;
  role?: import("./types").UserRole;
  roleLevel?: number;
  ministryId?: number | null;
  ministryName?: string | null;
  mfaRequired?: boolean;
  mfaToken?: string;
  mfaEnabled?: boolean;
  demoMfaHint?: string;
};

export const authApi = {
  login: (username: string, password: string, mfaCode?: string) =>
    api<LoginResult>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password, mfaCode: mfaCode || null }),
    }),
  verifyMfa: (mfaToken: string, mfaCode: string) =>
    api<LoginResult>("/api/auth/mfa/verify", {
      method: "POST",
      body: JSON.stringify({ mfaToken, mfaCode }),
    }),
};

export async function downloadExport(kind: "pdf" | "pptx", period: string) {
  const token = getToken();
  const path =
    kind === "pdf"
      ? `/api/exports/national.pdf?period=${period}`
      : `/api/exports/national.pptx?period=${period}`;
  const res = await fetch(`${API_BASE}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!res.ok) {
    throw new ApiError(res.status, "Export failed");
  }
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `govos-national-${period}.${kind === "pdf" ? "pdf" : "pptx"}`;
  a.click();
  URL.revokeObjectURL(url);
}

export const dashboardApi = {
  national: () => api<import("./types").NationalDashboard>("/api/dashboard/national"),
  ministry: (id: number) =>
    api<import("./types").MinistryDashboard>(`/api/dashboard/ministry/${id}`),
  myMinistry: () =>
    api<import("./types").MinistryDashboard>("/api/dashboard/ministry/mine"),
  ministryIntelligence: (id: number) =>
    api<import("./types").MinistryIntelligence>(
      `/api/dashboard/ministry/${id}/intelligence`
    ),
  myMinistryIntelligence: () =>
    api<import("./types").MinistryIntelligence>(
      "/api/dashboard/ministry/mine/intelligence"
    ),
  projects: () => api<import("./types").ProjectItem[]>("/api/dashboard/projects"),
  ministries: () =>
    api<import("./types").MinistryOverview[]>("/api/dashboard/ministries"),
};

export const reportApi = {
  submit: (data: Record<string, unknown>) =>
    api<import("./types").DailyReport>("/api/reports/daily", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  mine: () => api<import("./types").DailyReport[]>("/api/reports/mine"),
  byDate: (date: string) =>
    api<import("./types").DailyReport[]>(`/api/reports/by-date?date=${date}`),
  summary: (from: string, to: string) =>
    api<{
      totalReports: number;
      totalExpensesBdt: number;
      flaggedProblems: number;
      recent: import("./types").DailyReport[];
    }>(`/api/reports/summary?from=${from}&to=${to}`),
};

export const aiApi = {
  nationalBriefing: () =>
    api<{
      briefing: string;
      usedLiveAi: boolean;
      model: string;
      storedRecommendations: import("./types").AiRecommendationItem[];
    }>("/api/ai/briefing/national"),
  ask: (question: string) =>
    api<{ question: string; answer: string; usedLiveAi: boolean; model: string }>(
      "/api/ai/ask",
      { method: "POST", body: JSON.stringify({ question }) }
    ),
  recommendations: () =>
    api<import("./types").AiRecommendationItem[]>("/api/ai/recommendations"),
  acknowledge: (id: number) =>
    api<{ status: string }>(`/api/ai/recommendations/${id}/acknowledge`, {
      method: "POST",
    }),
};

export const modulesApi = {
  budget: () => api<{ ministries: BudgetRow[]; note: string }>("/api/budget/overview"),
  procurement: (flaggedOnly = false) =>
    api<ProcurementRow[]>(`/api/procurement?flaggedOnly=${flaggedOnly}`),
  kpis: (ministryId?: number) =>
    api<KpiRow[]>(ministryId ? `/api/kpis?ministryId=${ministryId}` : "/api/kpis"),
  risks: () => api<RiskRow[]>("/api/risks"),
  notifications: () => api<NotifRow[]>("/api/notifications"),
  markRead: (id: number) =>
    api<{ status: string }>(`/api/notifications/${id}/read`, { method: "POST" }),
  districts: () => api<DistrictRow[]>("/api/gis/districts"),
  district: (name: string) => api<DistrictRow>(`/api/gis/districts/${encodeURIComponent(name)}`),
  documents: (q?: string) =>
    api<DocRow[]>(q ? `/api/documents?q=${encodeURIComponent(q)}` : "/api/documents"),
  employees: () => api<EmpRow[]>("/api/hr/employees"),
  officeScores: () => api<OfficeRow[]>("/api/hr/office-scores"),
  complaints: () => api<ComplaintRow[]>("/api/citizen/complaints"),
  submitComplaint: (body: Record<string, string>) =>
    api<ComplaintRow>("/api/public/complaints", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  trackComplaint: (id: string) => api<ComplaintRow>(`/api/public/complaints/${id}`),
  auditLogs: (limit = 50) =>
    api<Record<string, unknown>[]>(`/api/exports/audit-logs?limit=${limit}`),
};

export interface BudgetRow {
  id: number;
  name: string;
  code: string;
  budgetCrore: number;
  spentCrore: number;
  remainingCrore: number;
  utilizationPercent: number;
  unusual: boolean;
}

export interface ProcurementRow {
  id: number;
  orderCode: string;
  ministryName: string;
  itemDescription: string;
  vendorName: string;
  amountBdt: number;
  orderDate: string;
  status: string;
  riskFlags: string;
  riskNotes: string;
  flagged: boolean;
}

export interface KpiRow {
  id: number;
  ministryId: number;
  ministryName: string;
  kpiKey: string;
  label: string;
  value: number;
  target: number;
  unit: string;
  asOfDate: string;
}

export interface RiskRow {
  id: number;
  riskType: string;
  title: string;
  description: string;
  severity: string;
  ministryName: string;
  location: string;
  recommendedAction: string;
}

export interface NotifRow {
  id: number;
  title: string;
  message: string;
  category: string;
  severity: string;
  audience: string;
  read: boolean;
  createdAt: string;
}

export interface DistrictRow {
  id: number;
  districtName: string;
  division: string;
  latitude: number;
  longitude: number;
  population: number;
  hospitals: number;
  schools: number;
  activeProjects: number;
  crimeCasesMonth: number;
  budgetCrore: number;
  floodRiskScore: number;
  dengueRiskScore: number;
  weatherNote: string;
  investmentNote: string;
}

export interface DocRow {
  id: number;
  title: string;
  docType: string;
  classification: string;
  ministryName: string;
  issuedDate: string;
  summary: string;
  tags: string;
}

export interface EmpRow {
  id: number;
  employeeId: string;
  fullName: string;
  designation: string;
  ministryName: string;
  grade: string;
  monthlySalaryBdt: number;
  attendancePercent: number;
  performanceScore: number;
  trainingStatus: string;
  leaveBalanceDays: number;
  district: string;
}

export interface OfficeRow {
  id: number;
  officeName: string;
  ministryName: string;
  district: string;
  efficiencyScore: number;
  transparencyScore: number;
  budgetScore: number;
  citizenSatisfaction: number;
  projectCompletion: number;
  attendanceScore: number;
  overallScore: number;
}

export interface ComplaintRow {
  id: number;
  trackingId: string;
  citizenName: string;
  phone: string;
  subject: string;
  description: string;
  district: string;
  ministryName: string;
  status: string;
  resolutionNote: string;
  createdAt: string;
}
