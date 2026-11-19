type AuthFieldProps = {
  id: string;
  label: string;
  name: string;
  placeholder: string;
  type?: "text" | "email" | "password";
  autoComplete?: string;
  /** Helper line under the input, e.g. the password rules. */
  hint?: string;
};

/**
 * Labelled input of the auth forms.
 *
 * Wireframe: 14/500 label 8px above a 48px input with a `border/strong`
 * hairline and a 8px radius; the optional hint is 12/400 muted 8px below. The
 * design defines no error state, so nothing is validated here.
 */
export function AuthField({
  id,
  label,
  name,
  placeholder,
  type = "text",
  autoComplete,
  hint,
}: AuthFieldProps) {
  return (
    <div className="flex flex-col gap-2">
      <label htmlFor={id} className="text-sm font-medium text-wf-ink">
        {label}
      </label>
      <input
        id={id}
        name={name}
        type={type}
        autoComplete={autoComplete}
        placeholder={placeholder}
        className="h-12 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder focus-visible:border-wf-inverse"
      />
      {hint ? <p className="text-xs text-wf-ink-muted">{hint}</p> : null}
    </div>
  );
}
