import { AdminCategoriesHeader } from "@/features/admin/components/admin-categories-header";
import { AdminCategoriesToolbar } from "@/features/admin/components/admin-categories-toolbar";
import { CategoryDetailCard } from "@/features/admin/components/category-detail-card";
import { CategoryList } from "@/features/admin/components/category-list";
import { getAdminCategories } from "@/features/admin/services/admin.service";

/**
 * `Admin Categorias Wireframe` of the design file.
 *
 * One screen: page header, toolbar, then the 700-wide list next to the
 * 412-wide detail of the row the seed marks as selected.
 */
export async function AdminCategoriesPage() {
  const categories = await getAdminCategories();
  const selected =
    categories.items.find((category) => category.id === categories.selectedId) ??
    categories.items[0];

  return (
    <div className="flex flex-col gap-6">
      <AdminCategoriesHeader categories={categories} />
      <AdminCategoriesToolbar />

      <div className="flex flex-col gap-6 xl:flex-row xl:items-start">
        <div className="min-w-0 flex-1">
          <CategoryList
            items={categories.items}
            selectedId={selected.id}
            updatedLabel={categories.updatedLabel}
          />
        </div>
        <CategoryDetailCard category={selected} />
      </div>
    </div>
  );
}
