import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Reportes",
};

export default function Page() {
  return (
    <section className="flex flex-col gap-2 rounded-wf-md border border-wf-line bg-wf-surface p-8">
      <h1 className="text-2xl font-bold text-wf-ink">Reportes</h1>
      <p className="text-sm text-wf-ink-muted">
        Esta pantalla está en preparación. Los indicadores ya están disponibles en el resumen.
      </p>
    </section>
  );
}
