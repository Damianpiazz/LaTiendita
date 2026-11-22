"use server";

import { cookies } from "next/headers";

import {
  getAdminClientDetail as readAdminClientDetail,
  getAdminClients,
} from "@/features/admin/services/admin.service";
import type {
  AdminClientDetail,
  AdminClients,
  AdminClientsQuery,
} from "@/features/admin/types";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

/**
 * The admin gateway requires a token on the whole `/api/customers/admin/**`
 * tree and on the reused customer detail endpoint.
 */
async function adminHeaders(): Promise<Record<string, string>> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** Reuse the adapter so the Bearer token and page conversion stay in one place. */
export async function listAdminClients(
  query: AdminClientsQuery,
): Promise<AdminClients> {
  return getAdminClients(query);
}

/**
 * The detail dialog is opened from a server page query param, so this wrapper
 * keeps the token on the server even when a client calls the action directly.
 */
export async function getAdminClientDetail(
  customerId: string,
  row?: AdminClients["items"][number],
): Promise<AdminClientDetail> {
  return readAdminClientDetail(customerId, row);
}

export async function exportAdminClients(
  query: AdminClientsQuery,
): Promise<{ filename: string; content: string }> {
  const headers = await adminHeaders();
  const params: Record<string, string> = {};
  if (query.name) params.name = query.name;
  if (query.email) params.email = query.email;
  if (query.phone) params.phone = query.phone;
  if (query.status) params.status = query.status;
  if (query.segment) params.segment = query.segment;
  if (query.sort) params.sort = query.sort;

  const { data } = await httpClient.get<string>(
    "/api/customers/admin/export.csv",
    {
      params,
      headers: { ...headers, Accept: "text/csv" },
      responseType: "text",
    },
  );

  return { filename: "clientes.csv", content: String(data) };
}
