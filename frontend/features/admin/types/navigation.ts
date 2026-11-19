/**
 * `GET /admin/navigation` response.
 *
 * The admin shell is shared by every screen, so its badges cannot arrive as
 * page props: the layout reads them from their own endpoint.
 */
export type AdminNavigationCounts = {
  pendingOrders: number;
  payments: number;
  alerts: number;
};
