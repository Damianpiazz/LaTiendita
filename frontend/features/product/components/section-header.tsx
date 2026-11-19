import { ArrowRight } from "lucide-react";
import Link from "next/link";

type SectionHeaderProps = {
  title: string;
  actionLabel: string;
  actionHref: string;
};

/** Section title plus the `Ver todas` / `Ver todo` link on the right. */
export function SectionHeader({ title, actionLabel, actionHref }: SectionHeaderProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <h2 className="text-2xl font-bold tracking-tight text-wf-ink">{title}</h2>
      <Link
        href={actionHref}
        className="inline-flex items-center gap-1 text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
      >
        {actionLabel}
        <ArrowRight aria-hidden className="size-4" />
      </Link>
    </div>
  );
}
