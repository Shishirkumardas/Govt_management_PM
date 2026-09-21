"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

export default function Home() {
  const { user, loading, isPm, isField } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;
    if (!user) router.replace("/login");
    else if (isPm) router.replace("/dashboard");
    else if (isField) router.replace("/field");
    else router.replace("/ministry");
  }, [user, loading, isPm, isField, router]);

  return (
    <div className="min-h-screen grid place-items-center bg-surface text-muted">
      Redirecting to GovOS…
    </div>
  );
}
