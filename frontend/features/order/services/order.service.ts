import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import type { CustomerOrderDetail, CustomerOrderItem } from "@/features/order/types/order";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth/session";
import { httpClient } from "@/lib/http/client";

type OrderItemDto = {
  productId: string | number;
  productTitle?: string | null;
  sku?: string | null;
  quantity?: number | null;
  unitPrice?: string | number | null;
  subtotal?: string | number | null;
};

type OrderDetailDto = {
  id: string | number;
  orderNumber?: string | null;
  status: string;
  total: string | number;
  createdAt?: string | null;
  cancelReason?: string | null;
  items?: OrderItemDto[] | null;
};

function toItem(dto: OrderItemDto): CustomerOrderItem {
  return {
    productId: String(dto.productId),
    productTitle: dto.productTitle?.trim() || `Producto #${dto.productId}`,
    sku: dto.sku,
    quantity: Number(dto.quantity) || 0,
    unitPrice: Number(dto.unitPrice) || 0,
    subtotal: Number(dto.subtotal) || 0,
  };
}

/** Loads one of the signed-in customer's own orders from order-service. */
export async function getCustomerOrder(orderId: string): Promise<CustomerOrderDetail | null> {
  const jar = await cookies();
  const token = jar.get(ACCESS_TOKEN_COOKIE)?.value;

  if (!token) {
    redirect(`/ingresar?next=${encodeURIComponent(`/mi-cuenta/pedidos/${orderId}`)}`);
  }

  try {
    const { data } = await httpClient.get<OrderDetailDto>(`/api/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    return {
      id: String(data.id),
      orderNumber: data.orderNumber,
      status: data.status,
      total: Number(data.total) || 0,
      createdAt: data.createdAt,
      cancelReason: data.cancelReason,
      items: (data.items ?? []).map(toItem),
    };
  } catch (error) {
    const status =
      error && typeof error === "object" && "response" in error
        ? (error as { response?: { status?: number } }).response?.status
        : undefined;

    if (status === 404) return null;
    if (status === 401 || status === 403) redirect("/ingresar");
    throw error;
  }
}
