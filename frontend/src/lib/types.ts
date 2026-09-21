export type UserRole =
  | "PRIME_MINISTER"
  | "CABINET_MINISTER"
  | "SECRETARY"
  | "DEPARTMENT_OFFICER"
  | "DISTRICT_ADMIN"
  | "FIELD_OFFICER";

export interface AuthUser {
  token: string;
  username: string;
  fullName: string;
  role: UserRole;
  roleLevel: number;
  ministryId: number | null;
  ministryName: string | null;
}

export interface IndicatorItem {
  key: string;
  label: string;
  category: string;
  value: number;
  unit: string;
  changePercent: number;
  asOfDate: string;
}

export interface ProjectItem {
  id: number;
  projectCode: string;
  title: string;
  ministryName: string;
  status: string;
  totalCostCrore: number;
  spentCostCrore: number;
  remainingCostCrore: number;
  progressPercent: number;
  location: string;
  latestUpdate: string;
  expectedEndDate: string;
  contractor?: string;
  delayDays?: number;
  riskLevel?: string;
  riskReason?: string;
  aiRecommendation?: string;
}

export interface MinistryOverview {
  id: number;
  code: string;
  nameEn: string;
  nameBn: string;
  annualBudgetCrore: number;
  spentBudgetCrore: number;
  utilizationPercent: number;
  activeProjects: number;
  reportsToday: number;
}

export interface AlertItem {
  id: number;
  title: string;
  message: string;
  severity: string;
  category: string;
  location: string;
  ministryName: string;
}

export interface ForeignInvestmentItem {
  id: number;
  investorName: string;
  country: string;
  sector: string;
  amountUsdMillion: number;
  status: string;
  agreementDate: string;
}

export interface AiRecommendationItem {
  id: number;
  title: string;
  body: string;
  scope: string;
  priority: string;
}

export interface BudgetSnapshot {
  totalRevenueBdt: number;
  totalExpenditureBdt: number;
  deficitBdt: number;
  totalMinistryBudgetCrore: number;
  totalMinistrySpentCrore: number;
  foreignInvestmentUsdMillion: number;
}

export interface DailyOpsSnapshot {
  reportsSubmittedToday: number;
  expensesTodayBdt: number;
  activeAlerts: number;
  ongoingProjects: number;
  delayedProjects: number;
}

export interface NationalDashboard {
  generatedAt: string;
  indicators: IndicatorItem[];
  indicatorsByCategory: Record<string, IndicatorItem[]>;
  megaProjects: ProjectItem[];
  ministries: MinistryOverview[];
  activeAlerts: AlertItem[];
  foreignInvestments: ForeignInvestmentItem[];
  aiRecommendations: AiRecommendationItem[];
  budget: BudgetSnapshot;
  dailyOps: DailyOpsSnapshot;
}

export interface DailyReport {
  id: number;
  userId: number;
  userName: string;
  designation: string;
  ministryId: number;
  ministryName: string;
  reportDate: string;
  workCompleted: string;
  visits: string;
  approvals: string;
  expensesBdt: number;
  problems: string;
  recommendations: string;
  inspectionsCount: number;
  schoolsVisited: number;
  projectsApproved: number;
  status: string;
}

export interface MinistryDashboard {
  ministry: MinistryOverview;
  projects: ProjectItem[];
  alerts: AlertItem[];
  recentReports: DailyReport[];
  aiRecommendations: AiRecommendationItem[];
  budgetByType: Record<string, number>;
}

export interface OrgItem {
  id: number;
  orgCode: string;
  nameEn: string;
  nameBn: string;
  orgType: string;
  headName: string;
  headDesignation: string;
  budgetCrore: number;
  spentCrore: number;
  utilizationPercent: number;
  sanctionedPosts: number;
  filledPosts: number;
  vacancy: number;
  anomalyHeadcount: number;
  transparencyScore: number;
  efficiencyScore: number;
  corruptionRiskScore: number;
  district: string;
  mandate: string;
}

export interface BudgetLineItem {
  id: number;
  category: string;
  titleEn: string;
  titleBn: string;
  organizationName: string;
  projectCode: string;
  allocatedCrore: number;
  releasedCrore: number;
  spentCrore: number;
  remainingCrore: number;
  utilizationPercent: number;
  fiscalYear: string;
  flagged: boolean;
  flagReason: string;
}

export interface WorkItem {
  id: number;
  workCode: string;
  titleEn: string;
  titleBn: string;
  organizationName: string;
  status: string;
  budgetCrore: number;
  spentCrore: number;
  progressPercent: number;
  location: string;
  beneficiaries: number;
  startDate: string;
  endDate: string;
  latestUpdate: string;
  transparencyIssue: boolean;
  issueNote: string;
}

export interface AntiCorruptionScorecard {
  overallRiskScore: number;
  overallRiskLevel: string;
  flaggedProcurements: number;
  openRiskFlags: number;
  ghostEmployeeSignals: number;
  highRiskOrganizations: number;
  flaggedBudgetLines: number;
  delayedProjects: number;
  worksWithTransparencyIssues: number;
  delayedFieldReports: number;
  unusualSpendCrore: number;
  redFlags: string[];
  recommendedActions: string[];
}

export interface MinistryIntelligence {
  generatedAt: string;
  ministry: MinistryOverview;
  headcount: {
    sanctionedPosts: number;
    filledPosts: number;
    vacancy: number;
    anomalyHeadcount: number;
    fillRatePercent: number;
  };
  antiCorruption: AntiCorruptionScorecard;
  organizations: OrgItem[];
  budgetLines: BudgetLineItem[];
  budgetByCategoryAllocated: Record<string, number>;
  budgetByCategorySpent: Record<string, number>;
  projects: ProjectItem[];
  workPrograms: WorkItem[];
  procurement: Array<Record<string, unknown>>;
  risks: Array<Record<string, unknown>>;
  kpis: Array<Record<string, unknown>>;
  employees: Array<Record<string, unknown>>;
  officeScores: Array<Record<string, unknown>>;
  recentReports: DailyReport[];
  alerts: AlertItem[];
  aiRecommendations: AiRecommendationItem[];
  documents: Array<Record<string, unknown>>;
}
