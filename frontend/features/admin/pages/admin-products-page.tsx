import { AdminProductsWorkspace } from "@/features/admin/components/admin-products-table";
import {
  ADMIN_PRODUCTS_DEFAULT_SORT,
  ADMIN_PRODUCTS_PAGE_SIZE,
  getAdminCategories,
  getAdminProducts,
} from "@/features/admin/services/admin.service";

/** `Admin Productos Wireframe` wired to the real gateway through the adapter. */
export async function AdminProductsPage() {
  const [products, categories] = await Promise.all([
    getAdminProducts({
      page: 1,
      size: ADMIN_PRODUCTS_PAGE_SIZE,
      sort: ADMIN_PRODUCTS_DEFAULT_SORT,
    }),
    getAdminCategories(),
  ]);

  return (
    <AdminProductsWorkspace
      initialProducts={products}
      categories={categories.items}
    />
  );
}
