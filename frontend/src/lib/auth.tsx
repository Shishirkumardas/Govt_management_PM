"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { AuthUser } from "./types";
import { authApi, LoginResult } from "./api";

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (username: string, password: string, mfaCode?: string) => Promise<LoginResult>;
  verifyMfa: (mfaToken: string, mfaCode: string) => Promise<void>;
  logout: () => void;
  isPm: boolean;
  isField: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function persistUser(res: LoginResult) {
  if (!res.token) throw new Error("No session token");
  const user: AuthUser = {
    token: res.token,
    username: res.username || "",
    fullName: res.fullName || "",
    role: res.role as AuthUser["role"],
    roleLevel: res.roleLevel || 0,
    ministryId: res.ministryId ?? null,
    ministryName: res.ministryName ?? null,
  };
  localStorage.setItem("govos_token", user.token);
  localStorage.setItem("govos_user", JSON.stringify(user));
  return user;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      const raw = localStorage.getItem("govos_user");
      if (raw) setUser(JSON.parse(raw));
    } catch {
      /* ignore */
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (username: string, password: string, mfaCode?: string) => {
    const res = await authApi.login(username, password, mfaCode);
    if (res.mfaRequired) {
      return res;
    }
    setUser(persistUser(res));
    return res;
  }, []);

  const verifyMfa = useCallback(async (mfaToken: string, mfaCode: string) => {
    const res = await authApi.verifyMfa(mfaToken, mfaCode);
    setUser(persistUser(res));
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("govos_token");
    localStorage.removeItem("govos_user");
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading,
      login,
      verifyMfa,
      logout,
      isPm: user?.role === "PRIME_MINISTER",
      isField:
        user?.role === "FIELD_OFFICER" || user?.role === "DISTRICT_ADMIN",
    }),
    [user, loading, login, verifyMfa, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
