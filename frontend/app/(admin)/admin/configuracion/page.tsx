import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Configuración",
};

export default function Page() {
  return (
    <section className="flex flex-col gap-2 rounded-wf-md border border-wf-line bg-wf-surface p-8">
      <h1 className="text-2xl font-bold text-wf-ink">Configuración</h1>
      <p className="text-sm text-wf-ink-muted">
        Esta pantalla está en preparación. Las opciones de la tienda se ajustan desde el backend.
      </p>
    </section>
  );
}
