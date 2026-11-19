import Link from "next/link";

/**
 * Empty cart.
 *
 * The wireframe has no empty state, so this is the smallest one that keeps the
 * screen usable: a title, one line of copy and a way back to the catalog.
 */
export function CartEmpty() {
  return (
    <div className="flex flex-col gap-2 rounded-wf-md border border-wf-line bg-wf-surface p-8">
      <h2 className="text-2xl font-bold text-wf-ink">Tu carrito está vacío</h2>
      <p className="text-sm text-wf-ink-muted">Todavía no agregaste productos.</p>
      <Link
        href="/#productos"
        className="mt-2 self-start text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
      >
        Ver productos →
      </Link>
    </div>
  );
}
