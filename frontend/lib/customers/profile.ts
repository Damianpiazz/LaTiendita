import type {
  CustomerActivity,
  CustomerActivityStatus,
  CustomerOrder,
  CustomerOrderStatus,
  CustomerProfile,
} from "@/features/customer/types";

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

/** Spring `Page` shape, with the array form kept for mock mode and tests. */
type Page<T> = { content?: T[]; totalElements?: number };

/** `OrderResponse` fields used by `/mi-cuenta`. */
export type CustomerOrderDto = {
  id: string | number;
  orderNumber?: string | null;
  status: string;
  total: string | number;
  createdAt?: string | null;
};

/** `ReviewDto` fields used by `/mi-cuenta`. */
export type CustomerReviewDto = {
  productId: string | number;
  title?: string | null;
  status: string;
  createdAt?: string | null;
};

/** Optional sections loaded from order-service and review-service. */
export type CustomerProfileSources = {
  orders?: Page<CustomerOrderDto> | CustomerOrderDto[];
  reviews?: Page<CustomerReviewDto> | CustomerReviewDto[];
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

function timestamp(value: string | null | undefined): number {
  const time = Date.parse(value ?? "");
  return Number.isNaN(time) ? 0 : time;
}

function unwrap<T>(payload?: Page<T> | T[]): T[] {
  if (Array.isArray(payload)) {
    return payload;
  }

  return payload?.content ?? [];
}

function countOf<T>(payload: Page<T> | T[] | undefined, items: T[]): number {
  if (Array.isArray(payload)) {
    return items.length;
  }

  return payload?.totalElements ?? items.length;
}

export function mapOrderStatus(status: string): CustomerOrderStatus {
  switch (status.trim().toUpperCase()) {
    case "PAID":
      return "pagado";
    case "SHIPPED":
      return "enviado";
    case "DELIVERED":
      return "entregado";
    case "CANCELLED":
      return "cancelado";
    default:
      return "pendiente";
  }
}

function orderLabel(dto: CustomerOrderDto): string {
  return dto.orderNumber?.trim() || `#${dto.id}`;
}

type TimedActivity = CustomerActivity & { time: number };

function toOrderActivity(dto: CustomerOrderDto): TimedActivity {
  const status = mapOrderStatus(dto.status);

  return {
    time: timestamp(dto.createdAt),
    date: displayDate(dto.createdAt) ?? "—",
    description: `Pedido ${orderLabel(dto)} ${status}`,
    status,
    href: `/mi-cuenta/pedidos/${encodeURIComponent(String(dto.id))}`,
  };
}

function toReviewActivity(dto: CustomerReviewDto): TimedActivity {
  const subject = dto.title?.trim() || `Producto #${dto.productId}`;

  let status: CustomerActivityStatus = "actualizado";
  let description = `Reseña actualizada · ${subject}`;

  switch (dto.status.trim().toLowerCase()) {
    case "approved":
      status = "publicada";
      description = `Reseña publicada · ${subject}`;
      break;
    case "pending":
      status = "pendiente";
      description = `Reseña pendiente · ${subject}`;
      break;
    case "rejected":
      status = "rechazada";
      description = `Reseña rechazada · ${subject}`;
      break;
  }

  return {
    time: timestamp(dto.createdAt),
    date: displayDate(dto.createdAt) ?? "—",
    description,
    status,
  };
}

function toCustomerOrder(dto: CustomerOrderDto): CustomerOrder {
  const orderId = String(dto.id);

  return {
    id: orderLabel(dto),
    orderId,
    date: displayDate(dto.createdAt) ?? "—",
    status: mapOrderStatus(dto.status),
    total: Number(dto.total) || 0,
  };
}

/**
 * Maps the customer's own records into the wireframe's profile shape. Identity
 * comes from customer-service, the stats/activity/purchases from order-service
 * and review-service. Sections with no data stay empty; no seeded numbers from
 * another account are ever copied into the screen.
 */
export function mapCustomerDtoToProfile(
  dto: CustomerDto,
  sources: CustomerProfileSources = {},
  activityLimit = 6,
): CustomerProfile {
  const firstName = dto.firstName.trim() || dto.email.split("@")[0] || "Cliente";
  const lastName = dto.lastName.trim();
  const name = [firstName, lastName].filter(Boolean).join(" ");
  const initials = `${firstName[0] ?? ""}${lastName[0] ?? firstName[1] ?? ""}`.toUpperCase();
  const memberSince = displayDate(dto.createdAt);
  const consentDate = displayDate(dto.newsletterConsentAt);

  const orders = unwrap(sources.orders)
    .slice()
    .sort((a, b) => timestamp(b.createdAt) - timestamp(a.createdAt));
  const reviews = unwrap(sources.reviews)
    .slice()
    .sort((a, b) => timestamp(b.createdAt) - timestamp(a.createdAt));

  // The service fetches up to 100 rows; totals use the page count when present.
  const totalSpent = orders.reduce((sum, order) => sum + (Number(order.total) || 0), 0);
  const timedActivity = [...orders.slice(0, 4).map(toOrderActivity), ...reviews.slice(0, 4).map(toReviewActivity)]
    .sort((a, b) => b.time - a.time)
    .slice(0, activityLimit);

  return {
    firstName,
    lastName: lastName || undefined,
    name,
    initials,
    email: dto.email,
    phone: dto.phone?.trim() || "Sin registrar",
    statusLabel: dto.status === "ACTIVE" ? "Cliente activo" : "Cliente inactivo",
    memberSince: memberSince ?? "—",
    customerId: `CU-${dto.id.replace(/-/g, "").slice(0, 5).toUpperCase()}`,
    stats: {
      orders: countOf(sources.orders, orders),
      totalSpent,
      lastPurchase: displayDate(orders[0]?.createdAt) ?? "—",
      reviews: countOf(sources.reviews, reviews),
    },
    activity: timedActivity.map(({ date, description, status, href }) => ({
      date,
      description,
      status,
      href,
    })),
    orders: orders.slice(0, 5).map(toCustomerOrder),
    // customer-service has no address fields yet, so the UI shows an empty state.
    address: { recipient: name, lines: [], isDefault: false },
    preferences: {
      newsletter: dto.newsletterOptIn ?? false,
      consentNote: consentDate
        ? `Consentimiento registrado el ${consentDate}`
        : "Sin consentimiento registrado",
    },
  };
}
