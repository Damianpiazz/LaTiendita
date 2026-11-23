import type { CustomerProfile } from "@/features/customer/types";

/**
 * `GET /customer/profile` seed, read from the `Perfil Cliente Wireframe` frame
 * (`2101537842:6`).
 *
 * The stat strip and the purchases table are derived from these rows: the
 * profile has 12 orders and `$ 184.500` spent in total, while `orders` and
 * `activity` only hold the most recent movement the frame draws.
 */
export const customerProfile: CustomerProfile = {
  firstName: "Lucía",
  lastName: "Fernández",
  name: "Lucía Fernández",
  initials: "LF",
  email: "lucia.fernandez@mail.com",
  phone: "+54 9 11 5555-0142",
  statusLabel: "Cliente activo",
  memberSince: "Miembro desde marzo 2025",
  customerId: "CU-10428",
  stats: {
    orders: 12,
    totalSpent: 184500,
    lastPurchase: "12 mar 2026",
    reviews: 3,
  },
  activity: [
    { date: "12 mar 2026", description: "Pedido #1042 entregado", status: "entregado" },
    { date: "28 feb 2026", description: "Pedido #0987 enviado", status: "enviado" },
    {
      date: "14 feb 2026",
      description: "Reseña publicada · Auriculares inalámbricos",
      status: "publicada",
    },
    {
      date: "02 feb 2026",
      description: "Dirección de entrega actualizada",
      status: "actualizado",
    },
  ],
  orders: [
    { id: "#1042", date: "12 mar 2026", status: "entregado", total: 42900 },
    { id: "#0987", date: "28 feb 2026", status: "enviado", total: 31500 },
    { id: "#0951", date: "14 feb 2026", status: "entregado", total: 18200 },
  ],
  address: {
    recipient: "Lucía Fernández",
    lines: ["Av. Corrientes 1234, Piso 4 B", "CABA, Buenos Aires · C1043AAZ"],
    isDefault: true,
  },
  preferences: {
    newsletter: true,
    consentNote: "Consentimiento registrado el 02 abr 2025",
  },
};

/** API-shaped fixtures for the same account, used by the mock HTTP adapter. */
export const customerApi = {
  id: "d2c4f1a0-1234-4c56-8abc-99ff00112233",
  email: customerProfile.email,
  firstName: "Lucía",
  lastName: "Fernández",
  phone: customerProfile.phone,
  status: "ACTIVE",
  newsletterOptIn: true,
  newsletterConsentAt: "2025-04-02T10:00:00Z",
  createdAt: "2025-03-01T10:00:00Z",
};

const orderDates = [
  "2026-03-12T10:00:00Z",
  "2026-02-28T10:00:00Z",
  "2026-02-14T10:00:00Z",
];

export const customerOrdersApi = {
  content: customerProfile.orders.map((order, index) => ({
    id: Number(order.id.replace("#", "")),
    orderNumber: order.id,
    status: order.status === "entregado" ? "DELIVERED" : "SHIPPED",
    total: String(order.total),
    createdAt: orderDates[index],
  })),
  totalElements: customerProfile.stats.orders,
};

export const customerReviewsApi = [
  {
    id: "11111111-1111-4111-8111-111111111111",
    productId: 7,
    title: "Auriculares inalámbricos",
    status: "approved",
    createdAt: "2026-02-14T10:00:00Z",
  },
  {
    id: "22222222-2222-4222-8222-222222222222",
    productId: 12,
    title: "Lámpara de escritorio",
    status: "approved",
    createdAt: "2026-01-20T10:00:00Z",
  },
  {
    id: "33333333-3333-4333-8333-333333333333",
    productId: 18,
    title: "Mochila urbana",
    status: "approved",
    createdAt: "2025-12-05T10:00:00Z",
  },
];
