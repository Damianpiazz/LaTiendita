import { AdminPaymentsWorkspace } from "@/features/admin/components/admin-payments-workspace";
import { getAdminPayments } from "@/features/admin/services/admin.service";

/**
 * `Admin Pagos Wireframe` of the design file.
 *
 * The server loads the first page from the real gateway and hands it to the
 * client workspace, which owns filters, sort, pagination and load errors.
 */
export async function AdminPaymentsPage() {
  const payments = await getAdminPayments();

  return <AdminPaymentsWorkspace initialPayments={payments} />;
}
