type AuthSubmitProps = {
  label: string;
  /** Standalone by default; auth forms opt into `submit` inside their form. */
  type?: "button" | "submit";
  disabled?: boolean;
};

/**
 * Full-width submit of the auth forms: 48px, inverse fill, 16/500 white.
 * Behaviour is supplied by the owning form so the wireframe markup stays intact.
 */
export function AuthSubmit({ label, type = "button", disabled }: AuthSubmitProps) {
  return (
    <button
      type={type}
      disabled={disabled}
      className="flex h-12 w-full items-center justify-center rounded-wf-sm bg-wf-inverse text-base font-medium text-wf-on-inverse transition-colors hover:bg-wf-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-wf-inverse focus-visible:ring-offset-2 disabled:opacity-60"
    >
      {label}
    </button>
  );
}
