/**
 * Customer state printed in the clients table.
 *
 * The frame prints the feminine `Nueva` and the masculine `Nuevo` for the same
 * state, so both are part of the vocabulary instead of being normalised to one
 * label the design does not use. In real mode the backend segment supplies the
 * badge (`nueva`, `recurrente`, `activa`, `inactiva`, `sin-compras`); `nuevo`
 * is kept for the retired wireframe seed. `sin-compras` keeps its own value so
 * the filter never collides with the `inactiva` badge of a suspended account.
 */
export type AdminClientStatus =
  | "nueva"
  | "nuevo"
  | "recurrente"
  | "activa"
  | "inactiva"
  | "sin-compras";

/** Backend segment values served by `AdminCustomerResponse.segment`. */
export type AdminClientSegment =
  | "nueva"
  | "recurrente"
  | "activa"
  | "inactiva"
  | "sin-compras";

/** Backend customer status values used by the admin filters. */
export type AdminClientStatusFilter = "ACTIVE" | "SUSPENDED";

/** Query accepted by `getAdminClients` and the client actions. */
export type AdminClientsQuery = {
  page?: number;
  size?: number;
  sort?: string;
  name?: string;
  email?: string;
  phone?: string;
  status?: AdminClientStatusFilter;
  segment?: AdminClientSegment;
};

/** One row of the admin clients table. */
export type AdminClient = {
  /** Real backend id; absent only in the retired wireframe seed. */
  customerId?: string;
  firstName?: string;
  lastName?: string;
  email: string;
  name: string;
  /** Initials drawn inside the 36px avatar, e.g. `LF`. */
  initials: string;
  phone: string;
  orderCount: number;
  totalSpent: number;
  /** Display date of the last order, e.g. `10/11 · 09:42`. */
  lastPurchase: string;
  /** Raw timestamp kept for the detail and for future date formatting. */
  lastPurchaseAt?: string | null;
  createdAt?: string;
  lastLoginAt?: string | null;
  segment?: AdminClientSegment;
  status: AdminClientStatus;
};

/** Detail returned by the reused `GET /api/customers/{customerId}` endpoint. */
export type AdminClientDetail = AdminClient & {
  customerId: string;
  newsletterOptIn?: boolean;
  newsletterConsentAt?: string | null;
  updatedAt?: string;
};

/** `GET /api/customers/admin` page plus its summary KPIs. */
export type AdminClients = {
  items: AdminClient[];
  /** Table size, e.g. 8, and store totals, e.g. 128. */
  pageSize: number;
  totalClients: number;
  newThisMonth: number;
  recurring: number;
  /** Average ticket shown in the stats strip, e.g. `$ 27.430`. */
  averageTicket: number;
};
