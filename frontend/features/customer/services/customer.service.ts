import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import type { CustomerProfile } from "@/features/customer/types";
import {
  ACCESS_TOKEN_COOKIE,
  USER_COOKIE,
  decodeUserCookie,
} from "@/lib/auth/session";
import { mapCustomerDtoToProfile, type CustomerDto } from "@/lib/customers/profile";
import { httpClient } from "@/lib/http/client";

function statusOf(error: unknown): number | undefined {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { status?: number } }).response;
    return typeof response?.status === "number" ? response.status : undefined;
  }

  return undefined;
}

/**
 * Loads the signed-in customer's own record from customer-service. The httpOnly
 * JWT is read on the server and sent to the gateway; no profile is shared
 * between accounts. A first-time customer is provisioned once from the session
 * identity, using the service's idempotent creation endpoint.
 */
export async function getCustomerProfile(): Promise<CustomerProfile> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect("/ingresar");
  }

  const headers = { Authorization: `Bearer ${token}` };

  try {
    const { data } = await httpClient.get<CustomerDto>("/api/customers/me", { headers });

    return mapCustomerDtoToProfile(data);
  } catch (error) {
    const status = statusOf(error);

    if (status === 401 || status === 403) {
      redirect("/ingresar");
    }

    if (status !== 404) {
      throw error;
    }

    const user = decodeUserCookie(jar.get(USER_COOKIE)?.value);

    if (!user) {
      redirect("/ingresar");
    }

    let customer: CustomerDto;

    try {
      const { data } = await httpClient.post<CustomerDto>(
        "/api/customers",
        {
          email: user.email,
          firstName: user.givenName || user.email.split("@")[0],
          lastName: user.familyName || "-",
          phone: "",
        },
        { headers },
      );
      customer = data;
    } catch (error) {
      if (statusOf(error) !== 409) {
        throw error;
      }

      // An account recreated in Authorizer can leave an orphaned customer row.
      // The backend reclaims it by email, so re-read instead of crashing here.
      try {
        const { data } = await httpClient.get<CustomerDto>("/api/customers/me", { headers });
        customer = data;
      } catch {
        redirect("/ingresar");
      }
    }

    return mapCustomerDtoToProfile(customer);
  }
}
