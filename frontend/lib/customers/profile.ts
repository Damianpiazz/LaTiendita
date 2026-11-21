import type { CustomerProfile } from "@/features/customer/types";

/** Identity fields returned by `GET /api/customers/me` in customer-service. */
export type CustomerDto = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string | null;
  status?: string | null;
  newsletterOptIn?: boolean;
  newsletterConsentAt?: string | null;
  createdAt?: string | null;
};

const MONTHS = [
  "ene",
  "feb",
  "mar",
  "abr",
  "may",
  "jun",
  "jul",
  "ago",
  "sep",
  "oct",
  "nov",
  "dic",
];

function displayDate(value: string | null | undefined): string | null {
  if (!value) {
    return null;
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return `${String(date.getUTCDate()).padStart(2, "0")} ${MONTHS[date.getUTCMonth()]} ${date.getUTCFullYear()}`;
}

/**
 * Maps the real customer record into the wireframe's profile shape. Sections
 * without a backend source stay empty instead of showing another account's
 * seeded numbers.
 */
export function mapCustomerDtoToProfile(dto: CustomerDto): CustomerProfile {
  const firstName = dto.firstName.trim() || dto.email.split("@")[0] || "Cliente";
  const lastName = dto.lastName.trim();
  const name = [firstName, lastName].filter(Boolean).join(" ");
  const initials = `${firstName[0] ?? ""}${lastName[0] ?? firstName[1] ?? ""}`.toUpperCase();
  const memberSince = displayDate(dto.createdAt);
  const consentDate = displayDate(dto.newsletterConsentAt);

  return {
    firstName,
    name,
    initials,
    email: dto.email,
    phone: dto.phone?.trim() || "Sin registrar",
    statusLabel: dto.status === "ACTIVE" ? "Cliente activo" : "Cliente inactivo",
    memberSince: memberSince ?? "—",
    customerId: `CU-${dto.id.replace(/-/g, "").slice(0, 5).toUpperCase()}`,
    stats: { orders: 0, totalSpent: 0, lastPurchase: "—", reviews: 0 },
    activity: [],
    orders: [],
    address: { recipient: name, lines: [], isDefault: false },
    preferences: {
      newsletter: dto.newsletterOptIn ?? false,
      consentNote: consentDate
        ? `Consentimiento registrado el ${consentDate}`
        : "Sin consentimiento registrado",
    },
  };
}
