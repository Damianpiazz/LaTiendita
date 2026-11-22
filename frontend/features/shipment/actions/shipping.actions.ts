"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import type { ShippingAddress, ShippingAddressInput } from "@/features/shipment/types";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

export type SaveShippingAddressState = {
  ok: boolean;
  message?: string;
  fieldErrors?: Partial<Record<keyof ShippingAddressInput, string>>;
};

function statusOf(error: unknown): number | undefined {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { status?: number } }).response;
    return typeof response?.status === "number" ? response.status : undefined;
  }

  return undefined;
}

function readText(value: FormDataEntryValue | null): string {
  return typeof value === "string" ? value.trim() : "";
}

function validate(input: ShippingAddressInput): Partial<Record<keyof ShippingAddressInput, string>> {
  const errors: Partial<Record<keyof ShippingAddressInput, string>> = {};

  if (!input.recipientName) errors.recipientName = "Ingresá nombre y apellido.";
  if (!input.phone) errors.phone = "Ingresá un teléfono.";
  if (!input.addressLine) errors.addressLine = "Ingresá la dirección.";
  if (!input.city) errors.city = "Ingresá la ciudad.";
  if (!input.province) errors.province = "Ingresá la provincia.";
  if (!input.postalCode) errors.postalCode = "Ingresá el código postal.";

  return errors;
}

function parseAddressInput(formData: FormData): ShippingAddressInput {
  return {
    recipientName: readText(formData.get("recipientName")),
    phone: readText(formData.get("phone")),
    addressLine: readText(formData.get("addressLine")),
    city: readText(formData.get("city")),
    province: readText(formData.get("province")),
    postalCode: readText(formData.get("postalCode")),
    references: readText(formData.get("references")),
    isDefault: formData.get("isDefault") === "on" || formData.get("isDefault") === "true",
  };
}

export async function saveShippingAddress(
  _prevState: SaveShippingAddressState,
  formData: FormData,
): Promise<SaveShippingAddressState> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect("/ingresar?next=/checkout/envio");
  }

  const input = parseAddressInput(formData);
  const fieldErrors = validate(input);

  if (Object.keys(fieldErrors).length > 0) {
    return { ok: false, fieldErrors, message: "Revisá los campos marcados." };
  }

  const addressId = readText(formData.get("addressId"));
  const headers = { Authorization: `Bearer ${token}` };

  try {
    if (addressId) {
      await httpClient.put<ShippingAddress>(`/api/shipments/addresses/${addressId}`, input, {
        headers,
      });
    } else {
      await httpClient.post<ShippingAddress>("/api/shipments/addresses", input, { headers });
    }
  } catch (error) {
    const status = statusOf(error);

    if (status === 401 || status === 403) {
      redirect("/ingresar?next=/checkout/envio");
    }

    return {
      ok: false,
      message: "No pudimos guardar la dirección. Intentá de nuevo.",
    };
  }

  redirect("/checkout/pago");
}
