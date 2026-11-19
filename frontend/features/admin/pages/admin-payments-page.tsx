import { AdminPagination } from "@/features/admin/components/admin-pagination";
import { AdminPaymentsFilters } from "@/features/admin/components/admin-payments-filters";
import { AdminPaymentsHeader } from "@/features/admin/components/admin-payments-header";
import { AdminPaymentsKpis } from "@/features/admin/components/admin-payments-kpis";
import { AdminPaymentsTable } from "@/features/admin/components/admin-payments-table";
import { getAdminPayments } from "@/features/admin/services/admin.service";

/**
 * `Admin Pagos Wireframe` of the design file.
 *
 * One screen: page header, the four settlement KPIs, the status tabs with the
 * period control, the payments card and its pagination. Subtitle, tab counts,
 * captions and the pagination range derive from the seeded totals.
 */
export async function AdminPaymentsPage() {
  const payments = await getAdminPayments();
  const pageCount = Math.ceil(payments.totalPayments / payments.pageSize);

  return (
    <div className="flex flex-col gap-6">
      <AdminPaymentsHeader payments={payments} />
      <AdminPaymentsKpis payments={payments} />
      <AdminPaymentsFilters payments={payments} />
      <AdminPaymentsTable payments={payments} />
      <AdminPagination
        label={`Mostrando 1–${payments.items.length} de ${payments.totalPayments} pagos`}
        pageCount={pageCount}
      />
    </div>
  );
}
