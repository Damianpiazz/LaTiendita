import { ActivityCard } from "@/features/admin/components/activity-card";
import { AdminSummaryHeader } from "@/features/admin/components/admin-summary-header";
import { KpiGrid } from "@/features/admin/components/kpi-grid";
import { LowStockCard } from "@/features/admin/components/low-stock-card";
import { RecentOrders } from "@/features/admin/components/recent-orders";
import { SalesChart } from "@/features/admin/components/sales-chart";
import { getAdminSummary } from "@/features/admin/services/admin.service";

/**
 * `Admin Resumen Wireframe` of the design file.
 *
 * One screen: page header, four KPIs, then the sales/orders column next to the
 * stock/activity column, all fed by the cached summary endpoint.
 */
export async function AdminSummaryPage() {
  const summary = await getAdminSummary();

  return (
    <div className="flex flex-col gap-6">
      <AdminSummaryHeader summary={summary} />
      <KpiGrid summary={summary} />

      <div className="flex flex-col gap-6 xl:flex-row">
        <div className="flex min-w-0 flex-1 flex-col gap-6">
          <SalesChart summary={summary} />
          <RecentOrders summary={summary} />
        </div>

        <div className="flex flex-col gap-6 xl:w-[352px] xl:shrink-0">
          <LowStockCard
            products={summary.lowStockProducts}
            threshold={summary.lowStockThreshold}
          />
          <ActivityCard entries={summary.recentActivity} />
        </div>
      </div>
    </div>
  );
}
