"use client";

import { useState, useTransition } from "react";

import { updateNewsletter } from "@/features/customer/actions/profile.actions";
import { cn } from "@/lib/utils";

/**
 * Real newsletter opt-in control. The checked state is local until the server
 * action succeeds, so moving the switch never lies about what was persisted.
 */
export function NewsletterSwitch({
  initialValue,
  compact = false,
}: {
  initialValue: boolean;
  compact?: boolean;
}) {
  const [enabled, setEnabled] = useState(initialValue);
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function toggle() {
    const next = !enabled;

    startTransition(async () => {
      const result = await updateNewsletter(next);

      if (result.ok) {
        setEnabled(next);
      }

      setMessage(result.message ?? null);
    });
  }

  return (
    <div className="flex flex-col gap-2">
      <button
        type="button"
        role="switch"
        aria-checked={enabled}
        aria-label="Recibir novedades y ofertas por email"
        disabled={pending}
        onClick={toggle}
        className={cn(
          "flex h-6 w-11 shrink-0 items-center rounded-full p-0.5 transition-colors disabled:opacity-60",
          enabled ? "justify-end bg-wf-inverse" : "justify-start bg-wf-subtle",
        )}
      >
        <span className="block size-5 rounded-full bg-wf-surface" />
      </button>
      {message && !compact ? (
        <p role="status" className="text-xs text-wf-ink-muted">
          {message}
        </p>
      ) : null}
    </div>
  );
}
