"use client";

import { useEffect } from "react";

/** Shared right-hand panel used by the admin tables' row actions. */
export function AdminDetailDrawer({
  title,
  onClose,
  children,
}: {
  title: string;
  onClose: () => void;
  children: React.ReactNode;
}) {
  useEffect(() => {
    function handleKey(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, [onClose]);

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button
        type="button"
        aria-label="Cerrar detalle"
        onClick={onClose}
        className="absolute inset-0 bg-black/30"
      />
      <aside
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="relative z-10 flex h-full w-full max-w-md flex-col gap-5 overflow-y-auto border-l border-wf-line bg-wf-surface p-6 shadow-xl"
      >
        <header className="flex items-center justify-between gap-4">
          <h2 className="text-lg font-bold text-wf-ink">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Cerrar"
            className="flex size-8 items-center justify-center rounded-wf-sm border border-wf-line-strong text-wf-ink-secondary hover:bg-wf-canvas"
          >
            ✕
          </button>
        </header>
        {children}
      </aside>
    </div>
  );
}

/** Label/value pair inside an admin detail drawer. */
export function AdminDetailRow({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1 border-b border-wf-line pb-3 last:border-b-0">
      <dt className="text-xs text-wf-ink-muted">{label}</dt>
      <dd className="text-sm text-wf-ink">{children}</dd>
    </div>
  );
}
