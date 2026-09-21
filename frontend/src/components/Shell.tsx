"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Activity,
  Building2,
  ClipboardList,
  FileDown,
  FileText,
  FolderKanban,
  LayoutDashboard,
  LogOut,
  Map,
  Menu,
  Shield,
  ShoppingCart,
  Smartphone,
  Sparkles,
  TriangleAlert,
  Users,
  Wallet,
  Bell,
  Landmark,
  X,
} from "lucide-react";
import { useAuth } from "@/lib/auth";
import { MsgKey, roleLabel, useLang } from "@/lib/i18n";
import { LangToggle } from "@/components/LangToggle";
import { ThemeToggle } from "@/components/ThemeToggle";
import { useEffect, useState } from "react";

const nav: {
  href: string;
  labelKey: MsgKey;
  icon: React.ComponentType<{ size?: number }>;
  roles: string[];
}[] = [
  { href: "/dashboard", labelKey: "nav_dashboard", icon: LayoutDashboard, roles: ["PRIME_MINISTER"] },
  { href: "/ministry", labelKey: "nav_ministry", icon: Building2, roles: ["*"] },
  { href: "/projects", labelKey: "nav_projects", icon: FolderKanban, roles: ["*"] },
  { href: "/budget", labelKey: "nav_budget", icon: Wallet, roles: ["PRIME_MINISTER", "CABINET_MINISTER", "SECRETARY"] },
  { href: "/procurement", labelKey: "nav_procurement", icon: ShoppingCart, roles: ["PRIME_MINISTER", "CABINET_MINISTER", "SECRETARY", "DEPARTMENT_OFFICER"] },
  { href: "/kpis", labelKey: "nav_kpis", icon: Activity, roles: ["*"] },
  { href: "/risks", labelKey: "nav_risks", icon: TriangleAlert, roles: ["PRIME_MINISTER", "CABINET_MINISTER", "SECRETARY"] },
  { href: "/gis", labelKey: "nav_gis", icon: Map, roles: ["*"] },
  { href: "/documents", labelKey: "nav_documents", icon: FileText, roles: ["PRIME_MINISTER", "CABINET_MINISTER", "SECRETARY", "DEPARTMENT_OFFICER"] },
  { href: "/hr", labelKey: "nav_hr", icon: Users, roles: ["PRIME_MINISTER", "CABINET_MINISTER", "SECRETARY"] },
  { href: "/reports", labelKey: "nav_reports", icon: ClipboardList, roles: ["*"] },
  { href: "/field", labelKey: "nav_field", icon: Smartphone, roles: ["FIELD_OFFICER", "DISTRICT_ADMIN", "PRIME_MINISTER"] },
  { href: "/exports", labelKey: "nav_exports", icon: FileDown, roles: ["PRIME_MINISTER", "CABINET_MINISTER", "SECRETARY"] },
  { href: "/notifications", labelKey: "nav_notifications", icon: Bell, roles: ["*"] },
  { href: "/ai", labelKey: "nav_ai", icon: Sparkles, roles: ["PRIME_MINISTER", "CABINET_MINISTER", "SECRETARY"] },
  { href: "/citizen", labelKey: "nav_citizen", icon: Landmark, roles: ["*"] },
];

export function Shell({ children }: { children: React.ReactNode }) {
  const { user, loading, logout } = useAuth();
  const { t, mode, en, bn } = useLang();
  const pathname = usePathname();
  const router = useRouter();
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!loading && !user) router.replace("/login");
  }, [loading, user, router]);

  useEffect(() => {
    setOpen(false);
  }, [pathname]);

  if (loading || !user) {
    return (
      <div className="min-h-screen grid place-items-center bg-surface text-muted">
        <div className="flex items-center gap-3">
          <Activity className="animate-pulse text-bd-green" />
          {t("loading")}
        </div>
      </div>
    );
  }

  const items = nav.filter(
    (n) => n.roles.includes("*") || n.roles.includes(user.role)
  );

  const navLabel = (key: MsgKey) => {
    if (mode === "both") {
      return (
        <span className="leading-tight">
          <span className="block text-[13px]">{bn(key)}</span>
          <span className="block text-[10px] text-muted font-normal">{en(key)}</span>
        </span>
      );
    }
    return <span>{t(key)}</span>;
  };

  const NavLinks = () => (
    <>
      {items.map((item) => {
        const Icon = item.icon;
        const active = pathname === item.href || pathname.startsWith(item.href + "/");
        return (
          <Link
            key={item.href}
            href={item.href}
            className={`flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition ${
              active ? "nav-active" : "nav-idle"
            }`}
          >
            <Icon size={16} />
            {navLabel(item.labelKey)}
          </Link>
        );
      })}
    </>
  );

  return (
    <div className="min-h-screen bg-surface text-foreground flex">
      <aside className="hidden md:flex w-64 shrink-0 border-r border-border bg-surface-sidebar/95 backdrop-blur flex-col max-h-screen sticky top-0 shadow-bd">
        <div className="bd-ribbon" />
        <div className="p-5 border-b border-border">
          <div className="flex items-center gap-2">
            <div className="bd-mark">BD</div>
            <div>
              <div className="font-semibold tracking-tight text-sm leading-tight text-foreground">
                {mode === "en" ? en("appName") : bn("appName")}
              </div>
              <div className="text-[11px] text-bd-green font-medium">
                {mode === "bn" ? bn("appTag") : en("appTag")}
              </div>
              {mode === "both" && (
                <div className="text-[10px] text-muted">{en("appName")}</div>
              )}
            </div>
          </div>
          <div className="mt-3 flex flex-col gap-2">
            <LangToggle compact />
            <ThemeToggle compact />
          </div>
        </div>
        <nav className="p-3 flex-1 space-y-0.5 overflow-y-auto">
          <NavLinks />
        </nav>
        <div className="p-4 border-t border-border">
          <div className="flex items-start gap-2 mb-3">
            <Shield size={16} className="text-bd-green mt-0.5" />
            <div className="min-w-0">
              <div className="text-sm font-medium truncate">{user.fullName}</div>
              <div className="text-[11px] text-muted truncate">
                {roleLabel(user.role, t)}
              </div>
              {user.ministryName && (
                <div className="text-[10px] text-muted truncate">{user.ministryName}</div>
              )}
            </div>
          </div>
          <button
            onClick={() => {
              logout();
              router.push("/login");
            }}
            className="w-full flex items-center justify-center gap-2 text-sm py-2 rounded-lg btn-ghost"
          >
            <LogOut size={16} /> {t("signOut")}
          </button>
        </div>
      </aside>

      {open && (
        <div className="md:hidden fixed inset-0 z-40">
          <div className="absolute inset-0 bg-black/50" onClick={() => setOpen(false)} />
          <aside className="absolute left-0 top-0 bottom-0 w-72 bg-surface-sidebar border-r border-border flex flex-col shadow-bd">
            <div className="bd-ribbon" />
            <div className="p-4 flex items-center justify-between border-b border-border">
              <span className="font-semibold text-sm">{t("menu")}</span>
              <button onClick={() => setOpen(false)} className="p-2">
                <X size={18} />
              </button>
            </div>
            <div className="px-3 py-2 space-y-2">
              <LangToggle />
              <ThemeToggle />
            </div>
            <nav className="p-3 flex-1 overflow-y-auto space-y-0.5">
              <NavLinks />
            </nav>
          </aside>
        </div>
      )}

      <main className="flex-1 overflow-auto min-w-0">
        <header className="sticky top-0 z-10 border-b border-border bg-surface-header backdrop-blur px-4 md:px-6 py-3 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2 min-w-0">
            <button
              className="md:hidden p-2 rounded-lg bg-surface-hover border border-border"
              onClick={() => setOpen(true)}
              aria-label="Open menu"
            >
              <Menu size={18} />
            </button>
            <div className="text-sm text-muted truncate leading-tight">
              {mode === "both" ? (
                <>
                  <span className="block text-foreground text-xs font-medium">{bn("nation")}</span>
                  <span className="block text-[11px] text-muted">{en("nation")}</span>
                </>
              ) : (
                <span>{t("nation")}</span>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            <div className="hidden sm:flex items-center gap-2">
              <LangToggle compact />
              <ThemeToggle compact />
            </div>
            <Link href="/field" className="md:hidden bd-chip">
              {t("field")}
            </Link>
            <div className="bd-chip">{t("live")}</div>
          </div>
        </header>
        <div className="p-4 md:p-6">{children}</div>
      </main>
    </div>
  );
}
