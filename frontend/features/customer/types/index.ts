/**
 * Shape of `GET /customer/profile`: the whole `/mi-cuenta` screen in one
 * payload, read from the `Perfil Cliente Wireframe` frame (`2101537842:6`).
 *
 * The wireframe draws one customer, so the contract is a single object instead
 * of a collection.
 */

/** State printed in the activity rows. Orders reuse the purchase vocabulary. */
export type CustomerActivityStatus =
  | CustomerOrderStatus
  | "publicada"
  | "rechazada"
  | "actualizado";

/** One row of `Actividad reciente`. */
export type CustomerActivity = {
  /** Display date, e.g. `12 mar 2026`. */
  date: string;
  description: string;
  status: CustomerActivityStatus;
  /** Optional destination when the movement points to a detail screen. */
  href?: string;
};

/** UI states of the purchases table, mapped from the backend `OrderStatus`. */
export type CustomerOrderStatus =
  | "pendiente"
  | "pagado"
  | "enviado"
  | "entregado"
  | "cancelado";

/** One row of `Mis compras`. */
export type CustomerOrder = {
  /** Display id including the hash, e.g. `#1042`. */
  id: string;
  /** Numeric id used by `/api/orders/{id}` for the detail route. */
  orderId?: string;
  date: string;
  status: CustomerOrderStatus;
  total: number;
};

/** Figures of the stat strip under the profile card. */
export type CustomerStats = {
  orders: number;
  totalSpent: number;
  /** Display date of the last order, e.g. `12 mar 2026`. */
  lastPurchase: string;
  reviews: number;
};

/** Default shipping address of the `Dirección principal` card. */
export type CustomerAddress = {
  recipient: string;
  /** Address lines under the recipient, in drawn order. */
  lines: string[];
  isDefault: boolean;
};

/** Notification preferences of the `Preferencias` card. */
export type CustomerPreferences = {
  newsletter: boolean;
  /** Line under the consent check, e.g. `Consentimiento registrado el 02 abr 2025`. */
  consentNote: string;
};

/** `GET /customer/profile` seed. */
export type CustomerProfile = {
  /** Given name used by the greeting, e.g. `Lucía`. */
  firstName: string;
  /** Surname returned by customer-service; optional for legacy fixtures. */
  lastName?: string;
  name: string;
  /** Initials drawn in the header chip and the 64px avatar, e.g. `LF`. */
  initials: string;
  email: string;
  phone: string;
  /** Pill next to the member line, e.g. `Cliente activo`. */
  statusLabel: string;
  memberSince: string;
  /** Customer code on the right of the card, e.g. `CU-10428`. */
  customerId: string;
  stats: CustomerStats;
  activity: CustomerActivity[];
  orders: CustomerOrder[];
  address: CustomerAddress;
  preferences: CustomerPreferences;
};
