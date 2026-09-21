"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

export type LangMode = "both" | "en" | "bn";

const messages = {
  appName: { en: "GovOS Bangladesh", bn: "গভঅস বাংলাদেশ" },
  appTag: { en: "AI Government OS", bn: "এআই সরকারি অপারেটিং সিস্টেম" },
  nation: {
    en: "People's Republic of Bangladesh",
    bn: "গণপ্রজাতন্ত্রী বাংলাদেশ",
  },
  pmo: { en: "Office of the Prime Minister", bn: "প্রধানমন্ত্রীর কার্যালয়" },
  live: { en: "Live", bn: "লাইভ" },
  loading: { en: "Loading GovOS…", bn: "গভঅস লোড হচ্ছে…" },
  signOut: { en: "Sign out", bn: "সাইন আউট" },
  signIn: { en: "Sign in", bn: "সাইন ইন" },
  username: { en: "Username", bn: "ব্যবহারকারীর নাম" },
  password: { en: "Password", bn: "পাসওয়ার্ড" },
  mfaTitle: { en: "Multi-factor authentication", bn: "মাল্টি-ফ্যাক্টর প্রমাণীকরণ" },
  mfaHint: {
    en: "Enter the 6-digit TOTP code",
    bn: "৬ সংখ্যার টিওটিপি কোড দিন",
  },
  verifyMfa: { en: "Verify MFA", bn: "এমএফএ যাচাই" },
  demoAccounts: { en: "Demo accounts", bn: "ডেমো অ্যাকাউন্ট" },
  secureAccess: {
    en: "Secure access for authorized officials",
    bn: "অনুমোদিত কর্মকর্তাদের জন্য সুরক্ষিত প্রবেশ",
  },
  signInTo: { en: "Sign in to GovOS", bn: "গভঅস-এ সাইন ইন করুন" },
  menu: { en: "GovOS Menu", bn: "গভঅস মেনু" },
  field: { en: "Field", bn: "মাঠ" },

  // Nav
  nav_dashboard: { en: "National Dashboard", bn: "জাতীয় ড্যাশবোর্ড" },
  nav_ministry: { en: "Ministry", bn: "মন্ত্রণালয়" },
  nav_projects: { en: "Projects", bn: "প্রকল্পসমূহ" },
  nav_budget: { en: "Budget", bn: "বাজেট" },
  nav_procurement: { en: "Procurement", bn: "ক্রয় / সংগ্রহ" },
  nav_kpis: { en: "KPIs", bn: "সূচক (KPI)" },
  nav_risks: { en: "Risk Engine", bn: "ঝুঁকি ইঞ্জিন" },
  nav_gis: { en: "GIS Map", bn: "জিআইএস মানচিত্র" },
  nav_documents: { en: "Documents", bn: "নথিপত্র" },
  nav_hr: { en: "HR & Scores", bn: "এইচআর ও স্কোর" },
  nav_reports: { en: "Daily Reports", bn: "দৈনিক প্রতিবেদন" },
  nav_field: { en: "Field Mobile", bn: "মাঠ পর্যায় (মোবাইল)" },
  nav_exports: { en: "PDF / PPT", bn: "পিডিএফ / পাওয়ারপয়েন্ট" },
  nav_notifications: { en: "Alerts", bn: "সতর্কতা" },
  nav_ai: { en: "AI Assistant", bn: "এআই সহকারী" },
  nav_citizen: { en: "Citizen Portal", bn: "নাগরিক পোর্টাল" },

  // Dashboard
  dashTitle: {
    en: "National Command Dashboard",
    bn: "জাতীয় কমান্ড ড্যাশবোর্ড",
  },
  dashSub: {
    en: "Live view for the Prime Minister — economy, budgets, mega projects, alerts & AI",
    bn: "মাননীয় প্রধানমন্ত্রীর জন্য লাইভ দৃশ্য — অর্থনীতি, বাজেট, মেগা প্রকল্প, সতর্কতা ও এআই",
  },
  reportsToday: { en: "Reports today", bn: "আজকের প্রতিবেদন" },
  activeAlerts: { en: "Active alerts", bn: "সক্রিয় সতর্কতা" },
  ongoingProjects: { en: "Ongoing projects", bn: "চলমান প্রকল্প" },
  delayedProjects: { en: "Delayed projects", bn: "বিলম্বিত প্রকল্প" },
  fdi: { en: "FDI (active)", bn: "বিদেশি বিনিয়োগ (সক্রিয়)" },
  fieldExpenses: { en: "Field expenses today", bn: "আজকের মাঠ ব্যয়" },
  nationalIndicators: { en: "National indicators", bn: "জাতীয় সূচক" },
  megaProjects: { en: "Mega projects", bn: "মেগা প্রকল্প" },
  viewAll: { en: "View all", bn: "সব দেখুন" },
  aiRecs: { en: "AI recommendations", bn: "এআই সুপারিশ" },
  askAi: { en: "Ask AI", bn: "এআইকে জিজ্ঞাসা" },
  ministryBudget: {
    en: "Ministry budget utilization",
    bn: "মন্ত্রণালয়ের বাজেট ব্যবহার",
  },
  foreignInv: { en: "Foreign investment", bn: "বিদেশি বিনিয়োগ" },
  ministry: { en: "Ministry", bn: "মন্ত্রণালয়" },
  budget: { en: "Budget", bn: "বাজেট" },
  spent: { en: "Spent", bn: "খরচ" },
  remaining: { en: "Remaining", bn: "অবশিষ্ট" },
  usePct: { en: "Use %", bn: "ব্যবহার %" },
  status: { en: "Status", bn: "অবস্থা" },
  progress: { en: "Progress", bn: "অগ্রগতি" },
  delay: { en: "Delay", bn: "বিলম্ব" },
  contractor: { en: "Contractor", bn: "ঠিকাদার" },
  aiRisk: { en: "AI risk", bn: "এআই ঝুঁকি" },
  latestUpdate: { en: "Latest update", bn: "সর্বশেষ হালনাগাদ" },

  // Projects
  projectsTitle: { en: "Project Management", bn: "প্রকল্প ব্যবস্থাপনা" },
  projectsSub: {
    en: "Status, cost, delay, contractor, and AI risk for every government project",
    bn: "প্রতিটি সরকারি প্রকল্পের অবস্থা, ব্যয়, বিলম্ব, ঠিকাদার ও এআই ঝুঁকি",
  },

  // Field
  fieldApp: { en: "GovOS Field", bn: "গভঅস মাঠ" },
  fieldToday: { en: "Today's field report", bn: "আজকের মাঠ প্রতিবেদন" },
  fieldTodaySub: {
    en: "Capture work, expenses, problems and GPS in under 2 minutes.",
    bn: "কাজ, ব্যয়, সমস্যা ও জিপিএস ২ মিনিটের মধ্যে জমা দিন।",
  },
  startReport: { en: "Start daily report", bn: "দৈনিক প্রতিবেদন শুরু" },
  recentSubmissions: { en: "Recent submissions", bn: "সাম্প্রতিক জমা" },
  workCompleted: { en: "Work completed", bn: "সম্পন্ন কাজ" },
  visits: { en: "Visits", bn: "পরিদর্শন" },
  expenses: { en: "Expenses (BDT)", bn: "ব্যয় (টাকা)" },
  problems: { en: "Problems / need manpower", bn: "সমস্যা / জনবল প্রয়োজন" },
  recommendations: { en: "Recommendations", bn: "সুপারিশ" },
  submitReport: { en: "Submit report", bn: "প্রতিবেদন জমা দিন" },
  cancel: { en: "Cancel", bn: "বাতিল" },
  gpsLocation: { en: "GPS location", bn: "জিপিএস অবস্থান" },
  reportSent: { en: "Report sent", bn: "প্রতিবেদন পাঠানো হয়েছে" },
  backHome: { en: "Back to home", bn: "হোমে ফিরুন" },
  home: { en: "Home", bn: "হোম" },
  report: { en: "Report", bn: "প্রতিবেদন" },
  alerts: { en: "Alerts", bn: "সতর্কতা" },
  inspections: { en: "Inspect", bn: "পরিদর্শন" },
  schools: { en: "Schools", bn: "স্কুল" },
  approved: { en: "Approved", bn: "অনুমোদিত" },

  // Reports
  dailyReporting: { en: "Daily Reporting", bn: "দৈনিক প্রতিবেদন" },
  dailyReportingSub: {
    en: "Every employee submits work, expenses, and problems. Supervisors approve; data rolls into national stats.",
    bn: "প্রতি কর্মচারী কাজ, ব্যয় ও সমস্যা জমা দেন। তত্ত্বাবধায়ক অনুমোদন করেন; তথ্য জাতীয় পরিসংখ্যানে যায়।",
  },
  submitToday: { en: "Submit today's report", bn: "আজকের প্রতিবেদন জমা" },
  myReports: { en: "My recent reports", bn: "আমার সাম্প্রতিক প্রতিবেদন" },
  date: { en: "Date", bn: "তারিখ" },

  // GIS
  gisTitle: {
    en: "GIS — Interactive Bangladesh Map",
    bn: "জিআইএস — ইন্টারঅ্যাকটিভ বাংলাদেশ মানচিত্র",
  },
  gisSub: {
    en: "Leaflet map · click markers for projects, population, hospitals, schools, crime, flood & dengue risk",
    bn: "লিফলেট মানচিত্র · প্রকল্প, জনসংখ্যা, হাসপাতাল, স্কুল, অপরাধ, বন্যা ও ডেঙ্গু ঝুঁকি দেখতে মার্কারে ক্লিক করুন",
  },
  districts: { en: "Districts", bn: "জেলাসমূহ" },
  population: { en: "Population", bn: "জনসংখ্যা" },
  hospitals: { en: "Hospitals", bn: "হাসপাতাল" },
  weather: { en: "Weather", bn: "আবহাওয়া" },
  investments: { en: "Investments", bn: "বিনিয়োগ" },
  floodRisk: { en: "Flood risk", bn: "বন্যা ঝুঁকি" },
  dengueRisk: { en: "Dengue risk", bn: "ডেঙ্গু ঝুঁকি" },
  lowRisk: { en: "Low risk", bn: "কম ঝুঁকি" },
  moderate: { en: "Moderate", bn: "মাঝারি" },
  elevated: { en: "Elevated", bn: "উচ্চতর" },
  highRisk: { en: "High (flood/dengue)", bn: "উচ্চ (বন্যা/ডেঙ্গু)" },

  // Budget / etc
  budgetTitle: { en: "Budget Monitoring", bn: "বাজেট পর্যবেক্ষণ" },
  budgetSub: {
    en: "Every ministry expense tracked — AI flags unusual spending patterns",
    bn: "প্রতি মন্ত্রণালয়ের ব্যয় ট্র্যাক — অস্বাভাবিক খরচ এআই চিহ্নিত করে",
  },
  unusual: { en: "Unusual spending?", bn: "অস্বাভাবিক খরচ?" },
  normal: { en: "Normal", bn: "স্বাভাবিক" },
  utilization: { en: "Utilization", bn: "ব্যবহার" },

  exportsTitle: { en: "Automatic Reports", bn: "স্বয়ংক্রিয় প্রতিবেদন" },
  exportsSub: {
    en: "Generate national PDF briefings and PowerPoint decks for Cabinet / PMO",
    bn: "মন্ত্রিসভা / পিএমও-র জন্য জাতীয় পিডিএফ ও পাওয়ারপয়েন্ট তৈরি করুন",
  },
  reportPeriod: { en: "Report period", bn: "প্রতিবেদনের সময়কাল" },
  downloadPdf: { en: "Download PDF", bn: "পিডিএফ ডাউনলোড" },
  downloadPpt: { en: "Download PowerPoint", bn: "পাওয়ারপয়েন্ট ডাউনলোড" },
  auditLog: { en: "Recent audit log (security)", bn: "সাম্প্রতিক অডিট লগ (নিরাপত্তা)" },

  aiTitle: { en: "AI Decision Assistant", bn: "এআই সিদ্ধান্ত সহকারী" },
  aiSub: {
    en: "Ask the PM desk in natural language — answers grounded in live national data",
    bn: "প্রধানমন্ত্রীর ডেস্কে স্বাভাবিক ভাষায় জিজ্ঞাসা — লাইভ জাতীয় তথ্যভিত্তিক উত্তর",
  },
  yourQuestion: { en: "Your question", bn: "আপনার প্রশ্ন" },
  askGovos: { en: "Ask GovOS AI", bn: "গভঅস এআইকে জিজ্ঞাসা" },
  tryAsking: { en: "Try asking", bn: "জিজ্ঞাসা করে দেখুন" },
  nationalBriefing: { en: "Generate national briefing", bn: "জাতীয় ব্রিফিং তৈরি" },

  citizenTitle: { en: "Citizen Portal", bn: "নাগরিক পোর্টাল" },
  citizenSub: {
    en: "Complaints, tracking, and foundation for licenses, birth registration, tax, utilities, land & NID",
    bn: "অভিযোগ, ট্র্যাকিং এবং লাইসেন্স, জন্ম নিবন্ধন, কর, ইউটিলিটি, ভূমি ও এনআইডি-র ভিত্তি",
  },
  submitComplaint: { en: "Submit a complaint", bn: "অভিযোগ জমা দিন" },
  trackComplaint: { en: "Track complaint", bn: "অভিযোগ ট্র্যাক করুন" },
  yourName: { en: "Your name", bn: "আপনার নাম" },
  phone: { en: "Phone", bn: "ফোন" },
  subject: { en: "Subject", bn: "বিষয়" },
  description: { en: "Describe the issue…", bn: "সমস্যার বিবরণ লিখুন…" },
  district: { en: "District", bn: "জেলা" },

  riskTitle: { en: "AI Risk Engine", bn: "এআই ঝুঁকি ইঞ্জিন" },
  riskSub: {
    en: "Flags corruption signals, budget leakage, delayed reports, overspending, ghost employees, inactive projects",
    bn: "দুর্নীতি সংকেত, বাজেট ফাঁস, বিলম্বিত প্রতিবেদন, অতিরিক্ত খরচ, ভূত কর্মচারী, নিষ্ক্রিয় প্রকল্প চিহ্নিত করে",
  },
  action: { en: "Action", bn: "পদক্ষেপ" },

  notifTitle: { en: "AI Notification System", bn: "এআই নোটিফিকেশন সিস্টেম" },
  notifSub: {
    en: "Flood, budget, project delay, medicine shortage, tax drop, food stock, power demand",
    bn: "বন্যা, বাজেট, প্রকল্প বিলম্ব, ঔষধ সংকট, কর হ্রাস, খাদ্য মজুদ, বিদ্যুৎ চাহিদা",
  },
  markRead: { en: "Mark read", bn: "পঠিত চিহ্নিত" },

  langEn: { en: "EN", bn: "EN" },
  langBn: { en: "বাংলা", bn: "বাংলা" },
  langBoth: { en: "Both", bn: "উভয়" },
  language: { en: "Language", bn: "ভাষা" },

  role_PRIME_MINISTER: { en: "Prime Minister", bn: "প্রধানমন্ত্রী" },
  role_CABINET_MINISTER: { en: "Cabinet Minister", bn: "মন্ত্রিসভার মন্ত্রী" },
  role_SECRETARY: { en: "Secretary", bn: "সচিব" },
  role_DEPARTMENT_OFFICER: { en: "Department Officer", bn: "বিভাগীয় কর্মকর্তা" },
  role_DISTRICT_ADMIN: { en: "District Administration", bn: "জেলা প্রশাসন" },
  role_FIELD_OFFICER: { en: "Field Officer", bn: "মাঠ কর্মকর্তা" },
} as const;

export type MsgKey = keyof typeof messages;

type LangContextValue = {
  mode: LangMode;
  setMode: (m: LangMode) => void;
  /** Formatted label for current mode */
  t: (key: MsgKey) => string;
  /** Always bilingual "EN / বাংলা" regardless of mode preference for titles */
  bi: (key: MsgKey) => string;
  en: (key: MsgKey) => string;
  bn: (key: MsgKey) => string;
};

const LangContext = createContext<LangContextValue | null>(null);

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [mode, setModeState] = useState<LangMode>("both");

  useEffect(() => {
    try {
      const saved = localStorage.getItem("govos_lang") as LangMode | null;
      if (saved === "en" || saved === "bn" || saved === "both") setModeState(saved);
    } catch {
      /* ignore */
    }
  }, []);

  const setMode = useCallback((m: LangMode) => {
    setModeState(m);
    try {
      localStorage.setItem("govos_lang", m);
    } catch {
      /* ignore */
    }
  }, []);

  const en = useCallback((key: MsgKey) => messages[key].en, []);
  const bn = useCallback((key: MsgKey) => messages[key].bn, []);

  const t = useCallback(
    (key: MsgKey) => {
      const m = messages[key];
      if (mode === "en") return m.en;
      if (mode === "bn") return m.bn;
      // both: primary Bangla first for national identity, English secondary when different
      if (m.en === m.bn) return m.en;
      return `${m.bn} · ${m.en}`;
    },
    [mode]
  );

  const bi = useCallback((key: MsgKey) => {
    const m = messages[key];
    if (m.en === m.bn) return m.en;
    return `${m.bn} · ${m.en}`;
  }, []);

  const value = useMemo(
    () => ({ mode, setMode, t, bi, en, bn }),
    [mode, setMode, t, bi, en, bn]
  );

  return <LangContext.Provider value={value}>{children}</LangContext.Provider>;
}

export function useLang() {
  const ctx = useContext(LangContext);
  if (!ctx) throw new Error("useLang must be used within LanguageProvider");
  return ctx;
}

/** Bilingual line helper for custom pairs not in dictionary */
export function dual(en: string, bn: string, mode: LangMode = "both"): string {
  if (mode === "en") return en;
  if (mode === "bn") return bn;
  if (en === bn) return en;
  return `${bn} · ${en}`;
}

/** Role display */
export function roleLabel(role: string, t: (k: MsgKey) => string): string {
  const key = `role_${role}` as MsgKey;
  if (key in messages) return t(key);
  return role.replaceAll("_", " ");
}

/** Common district names EN → BN */
export const DISTRICT_BN: Record<string, string> = {
  Dhaka: "ঢাকা",
  Chattogram: "চট্টগ্রাম",
  Rajshahi: "রাজশাহী",
  Khulna: "খুলনা",
  Sylhet: "সিলেট",
  Rangpur: "রংপুর",
  Barishal: "বরিশাল",
  Mymensingh: "ময়মনসিংহ",
  Kurigram: "কুড়িগ্রাম",
  Gaibandha: "গাইবান্ধা",
};

export function districtLabel(name: string, mode: LangMode): string {
  const bn = DISTRICT_BN[name];
  if (!bn) return name;
  return dual(name, bn, mode);
}

export const DIVISION_BN: Record<string, string> = {
  Dhaka: "ঢাকা",
  Chattogram: "চট্টগ্রাম",
  Rajshahi: "রাজশাহী",
  Khulna: "খুলনা",
  Sylhet: "সিলেট",
  Rangpur: "রংপুর",
  Barishal: "বরিশাল",
  Mymensingh: "ময়মনসিংহ",
};

/** National indicator labels */
export const INDICATOR_BN: Record<string, string> = {
  "GDP Growth": "জিডিপি প্রবৃদ্ধি",
  "Inflation (CPI)": "মূল্যস্ফীতি (সিপিআই)",
  "Foreign Exchange Reserve": "বৈদেশিক মুদ্রার রিজার্ভ",
  "Tax Collection (FYTD)": "কর আদায় (অর্থবছর)",
  "National Debt": "জাতীয় ঋণ",
  "Electricity Generation Capacity": "বিদ্যুৎ উৎপাদন ক্ষমতা",
  "Load Shedding (avg)": "লোডশেডিং (গড়)",
  "Rice Production (est.)": "ধান উৎপাদন (আনুমানিক)",
  "Public Food Stock": "সরকারি খাদ্য মজুদ",
  "Fertilizer Stock": "সার মজুদ",
  "School Attendance Rate": "স্কুল উপস্থিতির হার",
  "Secondary Dropout Rate": "মাধ্যমিক ঝরে পড়ার হার",
  "Hospital Bed Occupancy": "হাসপাতাল শয্যা দখল",
  "Childhood Vaccination Coverage": "শিশু টিকাদান কভারেজ",
  "Reported Crimes (month)": "অপরাধ রিপোর্ট (মাস)",
  "Employment Rate": "কর্মসংস্থান হার",
  "Unemployment Rate": "বেকারত্বের হার",
  "Poverty Rate": "দারিদ্র্যের হার",
  "Digital Services Transactions": "ডিজিটাল সেবা লেনদেন",
  "National Highway Projects Progress": "জাতীয় মহাসড়ক প্রকল্প অগ্রগতি",
};

export function indicatorLabel(enLabel: string, mode: LangMode): string {
  const bn = INDICATOR_BN[enLabel];
  if (!bn) return enLabel;
  return dual(enLabel, bn, mode);
}
