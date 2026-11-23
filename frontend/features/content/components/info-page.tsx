import Link from "next/link";

import { BenefitsBar } from "@/components/layout/benefits-bar";
import { Breadcrumbs } from "@/features/product/components/breadcrumbs";
import { siteConfig } from "@/config/site";

type InfoSection = {
  title: string;
  body: string;
};

type InfoPageProps = {
  title: string;
  intro: string;
  sections: InfoSection[];
};

/** Shared layout for the store's static help pages. */
export function InfoPage({ title, intro, sections }: InfoPageProps) {
  return (
    <>
      <Breadcrumbs size="md" items={[{ label: "Inicio", href: "/" }, { label: title }]} />

      <section className="px-12 py-12">
        <div className="mx-auto flex w-full max-w-[900px] flex-col gap-8">
          <div className="flex flex-col gap-3">
            <h1 className="text-3xl font-bold text-wf-ink">{title}</h1>
            <p className="text-base text-wf-ink-secondary">{intro}</p>
          </div>

          <div className="flex flex-col gap-6 rounded-wf-md border border-wf-line bg-wf-surface p-8">
            {sections.map((section) => (
              <article key={section.title} className="flex flex-col gap-2">
                <h2 className="text-lg font-bold text-wf-ink">{section.title}</h2>
                <p className="text-sm leading-6 text-wf-ink-secondary">{section.body}</p>
              </article>
            ))}
          </div>

          <p className="text-sm text-wf-ink-muted">
            ¿Necesitás ayuda? Escribinos a{" "}
            <a
              href={`mailto:${siteConfig.contact.email}`}
              className="font-medium text-wf-ink underline underline-offset-2"
            >
              {siteConfig.contact.email}
            </a>{" "}
            o volvé al{" "}
            <Link href="/" className="font-medium text-wf-ink underline underline-offset-2">
              inicio
            </Link>
            .
          </p>
        </div>
      </section>

      <BenefitsBar />
    </>
  );
}
