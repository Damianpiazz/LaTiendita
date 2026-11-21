import { describe, expect, it } from "vitest";

import { mapCustomerDtoToProfile } from "./profile";

describe("mapCustomerDtoToProfile", () => {
  it("uses the authenticated customer's own identity and contact data", () => {
    const profile = mapCustomerDtoToProfile({
      id: "d2c4f1a0-1234-4c56-8abc-99ff00112233",
      email: "ana@example.com",
      firstName: "Ana",
      lastName: "Pérez",
      phone: "+54 9 11 5555 0101",
      status: "ACTIVE",
      newsletterOptIn: true,
      newsletterConsentAt: "2025-04-02T10:00:00Z",
      createdAt: "2025-04-02T10:00:00Z",
    });

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
});
