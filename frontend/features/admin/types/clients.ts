/**
 * Customer state printed in the clients table.
 *
 * The frame prints the feminine `Nueva` and the masculine `Nuevo` for the same
 * state, so both are part of the vocabulary instead of being normalised to one
 * label the design does not use.
 */
export type AdminClientStatus =
  | "nueva"
  | "nuevo"
  | "recurrente"
  | "activa"
  | "inactiva";

/** One row of the admin clients table. */
export type AdminClient = {
  email: string;
  name: string;
  /** Initials drawn inside the 36px avatar, e.g. `LF`. */
  initials: string;
  phone: string;
  orderCount: number;
  totalSpent: number;
  /** Display date of the last order, e.g. `10/11 · 09:42`. */
  lastPurchase: string;
  status: AdminClientStatus;
};

/** `GET /admin/clients` seed. */
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
