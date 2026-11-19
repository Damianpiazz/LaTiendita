import { AdminClientsHeader } from "@/features/admin/components/admin-clients-header";
import { AdminClientsStats } from "@/features/admin/components/admin-clients-stats";
import { AdminClientsTable } from "@/features/admin/components/admin-clients-table";
import { AdminClientsToolbar } from "@/features/admin/components/admin-clients-toolbar";
import { AdminPagination } from "@/features/admin/components/admin-pagination";
import { getAdminClients } from "@/features/admin/services/admin.service";

/**
 * `Admin Clientes Wireframe` of the design file.
 *
 * One screen: page header, the stats strip, the search/filter toolbar, the
 * clients card and its pagination. Subtitle, strip figures and the pagination
 * range derive from the seeded totals.
 */
export async function AdminClientsPage() {
  const clients = await getAdminClients();
  const pageCount = Math.ceil(clients.totalClients / clients.pageSize);

  return (
    <div className="flex flex-col gap-6">
      <AdminClientsHeader clients={clients} />
      <AdminClientsStats clients={clients} />
      <AdminClientsToolbar />
      <AdminClientsTable clients={clients} />
      <AdminPagination
        label={`Mostrando 1–${clients.items.length} de ${clients.totalClients} clientes`}
        pageCount={pageCount}
      />
    </div>
  );
}
