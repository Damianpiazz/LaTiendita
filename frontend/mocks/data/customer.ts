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
