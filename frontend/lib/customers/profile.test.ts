import { describe, expect, it } from "vitest";

import { mapCustomerDtoToProfile } from "./profile";

const customerDto = {
  id: "d2c4f1a0-1234-4c56-8abc-99ff00112233",
  email: "ana@example.com",
  firstName: "Ana",
  lastName: "Pérez",
  phone: "+54 9 11 5555 0101",
  status: "ACTIVE",
  newsletterOptIn: true,
  newsletterConsentAt: "2025-04-02T10:00:00Z",
  createdAt: "2025-04-02T10:00:00Z",
};

describe("mapCustomerDtoToProfile", () => {
  it("uses the authenticated customer's own identity and contact data", () => {
    const profile = mapCustomerDtoToProfile(customerDto);

    expect(profile).toMatchObject({
      firstName: "Ana",
      name: "Ana Pérez",
      initials: "AP",
      email: "ana@example.com",
      phone: "+54 9 11 5555 0101",
      statusLabel: "Cliente activo",
      preferences: { newsletter: true, consentNote: "Consentimiento registrado el 02 abr 2025" },
    });
    expect(profile.customerId).toMatch(/^CU-[0-9A-F]{5}$/);
  });

  it("never copies another account's identity when the customer is missing fields", () => {
    const profile = mapCustomerDtoToProfile({
      ...customerDto,
      id: "11111111-1111-1111-1111-111111111111",
      email: "bruno@example.com",
      firstName: "",
      lastName: "",
      phone: null,
      status: "INACTIVE",
      newsletterOptIn: false,
      newsletterConsentAt: null,
      createdAt: null,
    });

    expect(profile.firstName).toBe("bruno");
    expect(profile.initials).toBe("BR");
    expect(profile.statusLabel).toBe("Cliente inactivo");
    expect(profile.activity).toEqual([]);
    expect(profile.orders).toEqual([]);
    expect(profile.stats.orders).toBe(0);
    expect(profile.preferences.newsletter).toBe(false);
  });

  it("derives stats, activity and purchases from the customer's own orders and reviews", () => {
    const profile = mapCustomerDtoToProfile(customerDto, {
      orders: {
        content: [
          {
            id: 1042,
            orderNumber: "#1042",
            status: "DELIVERED",
            total: "42900.00",
            createdAt: "2026-03-12T10:00:00Z",
          },
          {
            id: 987,
            orderNumber: "#0987",
            status: "SHIPPED",
            total: "31500.00",
            createdAt: "2026-02-28T10:00:00Z",
          },
        ],
        totalElements: 12,
      },
      reviews: [
        {
          productId: 7,
          title: "Auriculares inalámbricos",
          status: "approved",
          createdAt: "2026-03-05T10:00:00Z",
        },
      ],
    });

    expect(profile.stats).toEqual({
      orders: 12,
      totalSpent: 74400,
      lastPurchase: "12 mar 2026",
      reviews: 1,
    });
    expect(profile.orders).toEqual([
      {
        id: "#1042",
        orderId: "1042",
        date: "12 mar 2026",
        status: "entregado",
        total: 42900,
      },
      {
        id: "#0987",
        orderId: "987",
        date: "28 feb 2026",
        status: "enviado",
        total: 31500,
      },
    ]);
    expect(profile.activity).toEqual([
      {
        date: "12 mar 2026",
        description: "Pedido #1042 entregado",
        status: "entregado",
        href: "/mi-cuenta/pedidos/1042",
      },
      {
        date: "05 mar 2026",
        description: "Reseña publicada · Auriculares inalámbricos",
        status: "publicada",
        href: undefined,
      },
      {
        date: "28 feb 2026",
        description: "Pedido #0987 enviado",
        status: "enviado",
        href: "/mi-cuenta/pedidos/987",
      },
    ]);
  });
});
