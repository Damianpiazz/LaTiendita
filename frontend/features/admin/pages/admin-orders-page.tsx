import { AdminOrdersWorkspace } from "@/features/admin/components/admin-orders-workspace";
import {
  ADMIN_ORDERS_DEFAULT_SORT,
  ADMIN_ORDERS_PAGE_SIZE,
  getAdminOrders,
} from "@/features/admin/services/admin.service";

/** `Admin Pedidos Wireframe` wired to real backend through the admin adapter. */
export async function AdminOrdersPage() {
  const orders = await getAdminOrders({
    page: 1,
    size: ADMIN_ORDERS_PAGE_SIZE,
    sort: ADMIN_ORDERS_DEFAULT_SORT,
  });

  return <AdminOrdersWorkspace initialOrders={orders} />;
}
