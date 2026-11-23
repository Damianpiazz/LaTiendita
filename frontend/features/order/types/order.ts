export type CustomerOrderItem = {
  productId: string;
  productTitle: string;
  sku?: string | null;
  quantity: number;
  unitPrice: number;
  subtotal: number;
};

/** `OrderResponse` fields used by the customer's order detail screen. */
export type CustomerOrderDetail = {
  id: string;
  orderNumber?: string | null;
  status: string;
  total: number;
  createdAt?: string | null;
  cancelReason?: string | null;
  items: CustomerOrderItem[];
};
