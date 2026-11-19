import Link from "next/link";

/**
 * Footer block of the auth card: a hairline separator 16px above a centred
 * switch line (`¿No tenés cuenta?` + link).
 *
 * The link goes to a screen that exists, so it is a real `Link`; the copy and
 * colours are the frame's, 14/400 muted plus 14/500 tertiary.
 */
export function AuthFooter({
  prompt,
  linkLabel,
  href,
}: {
  prompt: string;
  linkLabel: string;
  href: string;
}) {
  return (
    <div className="flex flex-col gap-4">
      <span aria-hidden className="h-px w-full bg-wf-line" />
      <p className="flex items-center justify-center gap-1 text-sm text-wf-ink-muted">
        <span>{prompt}</span>
        <Link
          href={href}
          className="font-medium text-wf-ink-tertiary transition-colors hover:text-wf-ink"
        >
          {linkLabel}
        </Link>
      </p>
    </div>
  );
}
