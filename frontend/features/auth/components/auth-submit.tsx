/**
 * Full-width submit of the auth forms: 48px, inverse fill, 16/500 white.
 *
 * `type="button"` on purpose: the mock has no auth endpoint behind it, so the
 * control is drawn but never submits anything.
 */
export function AuthSubmit({ label }: { label: string }) {
  return (
    <button
      type="button"
      className="flex h-12 w-full items-center justify-center rounded-wf-sm bg-wf-inverse text-base font-medium text-wf-on-inverse transition-colors hover:bg-wf-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-wf-inverse focus-visible:ring-offset-2"
    >
      {label}
    </button>
  );
}
