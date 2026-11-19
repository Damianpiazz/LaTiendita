import { AdminOrdersFilters } from "@/features/admin/components/admin-orders-filters";
import { AdminOrdersHeader } from "@/features/admin/components/admin-orders-header";
import { AdminOrdersTable } from "@/features/admin/components/admin-orders-table";
import { AdminPagination } from "@/features/admin/components/admin-pagination";
import { getAdminOrders } from "@/features/admin/services/admin.service";

/**
 * `Admin Pedidos Wireframe` of the design file.
 *
 * One screen: page header, status tabs with the period control, the orders
 * card and its pagination. Totals, tabs and range derive from the seeded
 * store numbers.
 */
export async function AdminOrdersPage() {
  const orders = await getAdminOrders();
  const pageCount = Math.ceil(orders.totalOrders / orders.pageSize);

  return (
    <div className="flex flex-col gap-6">
      <AdminOrdersHeader orders={orders} />
      <AdminOrdersFilters orders={orders} />
      <AdminOrdersTable orders={orders} />
      <AdminPagination
        label={`Mostrando 1–${orders.items.length} de ${orders.totalOrders} pedidos`}
        pageCount={pageCount}
      />
    </div>
  );
}
