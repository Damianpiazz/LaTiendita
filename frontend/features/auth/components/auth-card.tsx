/**
 * 400px card shared by the three auth screens.
 *
 * Wireframe: the auth section centres the card with 64/48 padding, the card is
 * white with a hairline and a 12px radius, 32px padding and 24px between
 * blocks. The title block itself uses an 8px gap.
 */
export function AuthCard({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <section className="flex flex-col items-center px-6 py-16 md:px-12">
      <div className="flex w-full max-w-[400px] flex-col gap-6 rounded-wf-md border border-wf-line bg-wf-surface p-8">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold text-wf-ink">{title}</h1>
          <p className="text-sm text-wf-ink-muted">{subtitle}</p>
        </div>

        {children}
      </div>
    </section>
  );
}
