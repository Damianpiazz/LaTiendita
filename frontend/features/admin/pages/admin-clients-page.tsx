import { getAdminClientDetail } from "@/features/admin/actions/client-actions";
import { AdminClientsWorkspace } from "@/features/admin/components/admin-clients-workspace";
import {
  ADMIN_CLIENTS_DEFAULT_SORT,
  ADMIN_CLIENTS_PAGE_SIZE,
} from "@/features/admin/helpers/admin-clients.constants";
import { getAdminClients } from "@/features/admin/services/admin.service";
import type {
  AdminClientDetail,
  AdminClientSegment,
  AdminClientStatusFilter,
  AdminClientsQuery,
} from "@/features/admin/types";

type SearchParams = Record<string, string | string[] | undefined>;

function firstValue(value: string | string[] | undefined): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}

function positiveInt(value: string | string[] | undefined, fallback: number) {
  const parsed = Number(firstValue(value));
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

function clientSegment(
  value: string | string[] | undefined,
): AdminClientSegment | undefined {
  const candidate = firstValue(value);
  if (
    candidate === "nueva" ||
    candidate === "recurrente" ||
    candidate === "activa" ||
    candidate === "inactiva" ||
    candidate === "sin-compras"
  ) {
    return candidate;
  }
  return undefined;
}

function clientStatus(
  value: string | string[] | undefined,
): AdminClientStatusFilter | undefined {
  const candidate = firstValue(value);
  return candidate === "ACTIVE" || candidate === "SUSPENDED"
    ? candidate
    : undefined;
}

/** Only the query params the backend contract understands reach the adapter. */
function parseClientsQuery(searchParams: SearchParams): AdminClientsQuery {
  const name = firstValue(searchParams.name);
  const email = firstValue(searchParams.email);
  const phone = firstValue(searchParams.phone);
  const segment = clientSegment(searchParams.segment);
  const status = clientStatus(searchParams.status);

  return {
    page: positiveInt(searchParams.page, 1),
    size: positiveInt(searchParams.size, ADMIN_CLIENTS_PAGE_SIZE),
    sort: firstValue(searchParams.sort) ?? ADMIN_CLIENTS_DEFAULT_SORT,
    ...(name ? { name } : {}),
    ...(email ? { email } : {}),
    ...(phone ? { phone } : {}),
    ...(segment ? { segment } : {}),
    ...(status ? { status } : {}),
  };
}

/**
 * Server page for the clients screen. It owns the query parsing and resolves
 * `?customer=...` through the server action so the Bearer token never reaches
 * the browser.
 */
export async function AdminClientsPage({
  searchParams,
}: {
  searchParams: Promise<SearchParams>;
}) {
  const params = await searchParams;
  const query = parseClientsQuery(params);
  const clients = await getAdminClients(query);
  const customerId = firstValue(params.customer);
  let detail: AdminClientDetail | null = null;

  if (customerId) {
    const row = clients.items.find((item) => item.customerId === customerId);
    detail = await getAdminClientDetail(customerId, row);
  }

  return (
    <AdminClientsWorkspace
      initialClients={clients}
      initialQuery={query}
      detail={detail}
    />
  );
}
