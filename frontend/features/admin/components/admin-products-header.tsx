import type { AdminProducts } from "@/features/admin/types";

type AdminProductsHeaderProps = {
  products: AdminProducts;
  onExport: () => void;
  onNewProduct: () => void;
  exporting?: boolean;
};

/** Page header of the products screen: title, store totals and both actions. */
export function AdminProductsHeader({
  products,
  onExport,
  onNewProduct,
  exporting = false,
}: AdminProductsHeaderProps) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold text-wf-ink">Productos</h1>
        <p className="text-sm text-wf-ink-muted">
          {products.totalProducts} productos · {products.totalCategories} categorías
          · {products.totalLowStock} con stock bajo
        </p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={onExport}
          disabled={exporting}
          className="flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-4 text-sm font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas disabled:cursor-not-allowed disabled:opacity-60"
        >
          {exporting ? "Exportando…" : "Exportar CSV"}
        </button>
        <button
          type="button"
          onClick={onNewProduct}
          className="flex h-10 items-center rounded-wf-sm bg-wf-inverse px-4 text-sm font-medium text-wf-on-inverse transition-opacity hover:opacity-90"
        >
          + Nuevo producto
        </button>
      </div>
    </header>
  );
}
