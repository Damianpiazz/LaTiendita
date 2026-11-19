import { AdminPagination } from "@/features/admin/components/admin-pagination";
import { AdminProductsBulkBar } from "@/features/admin/components/admin-products-bulk-bar";
import { AdminProductsHeader } from "@/features/admin/components/admin-products-header";
import { AdminProductsTable } from "@/features/admin/components/admin-products-table";
import { AdminProductsToolbar } from "@/features/admin/components/admin-products-toolbar";
import { getAdminProducts } from "@/features/admin/services/admin.service";

/**
 * `Admin Productos Wireframe` of the design file.
 *
 * One screen: page header, toolbar, bulk bar, the 1136-wide table and its
 * pagination. Copy is derived from the seeded totals, so the header, the bulk
 * count and the range stay consistent with the rows.
 */
export async function AdminProductsPage() {
  const products = await getAdminProducts();
  const selectedCount = products.items.filter(
    (product) => product.selected,
  ).length;
  const pageCount = Math.ceil(products.totalProducts / products.pageSize);

  return (
    <div className="flex flex-col gap-6">
      <AdminProductsHeader products={products} />
      <AdminProductsToolbar />
      <AdminProductsBulkBar count={selectedCount} />
      <AdminProductsTable products={products} />
      <AdminPagination
        label={`Mostrando 1–${products.items.length} de ${products.totalProducts} productos`}
        pageCount={pageCount}
      />
    </div>
  );
}
