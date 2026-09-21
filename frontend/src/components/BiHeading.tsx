"use client";

import { MsgKey, useLang } from "@/lib/i18n";

/** Title with optional bilingual subtitle styling when mode is both */
export function BiHeading({
  titleKey,
  subKey,
  as: Tag = "h1",
  className = "text-2xl font-semibold",
}: {
  titleKey: MsgKey;
  subKey?: MsgKey;
  as?: "h1" | "h2" | "h3";
  className?: string;
}) {
  const { mode, en, bn } = useLang();

  if (mode === "both") {
    return (
      <div className="mb-6">
        <Tag className={className}>
          <span className="block text-foreground">{bn(titleKey)}</span>
          <span className="block text-base font-normal text-muted mt-0.5">
            {en(titleKey)}
          </span>
        </Tag>
        {subKey && (
          <p className="text-sm text-muted-foreground mt-2">
            <span className="block">{bn(subKey)}</span>
            <span className="block text-muted text-xs mt-0.5">{en(subKey)}</span>
          </p>
        )}
      </div>
    );
  }

  return (
    <div className="mb-6">
      <Tag className={`${className} text-foreground`}>
        {mode === "bn" ? bn(titleKey) : en(titleKey)}
      </Tag>
      {subKey && (
        <p className="text-sm text-muted mt-1">
          {mode === "bn" ? bn(subKey) : en(subKey)}
        </p>
      )}
    </div>
  );
}
