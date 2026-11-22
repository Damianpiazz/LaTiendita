import { AdminCategoriesWorkspace } from "@/features/admin/components/admin-categories-workspace";
import {
  ADMIN_CATEGORIES_DEFAULT_SORT,
  ADMIN_CATEGORIES_PAGE_SIZE,
  getAdminCategories,
} from "@/features/admin/services/admin.service";

/** Server entry point: seed the first real page, then delegate interactions. */
export async function AdminCategoriesPage() {
  const categories = await getAdminCategories({
    size: ADMIN_CATEGORIES_PAGE_SIZE,
    sort: ADMIN_CATEGORIES_DEFAULT_SORT,
  });

  return <AdminCategoriesWorkspace initialCategories={categories} />;
}
