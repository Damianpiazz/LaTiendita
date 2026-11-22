export type ShippingMethod = {
  id: string;
  name: string;
};

/** Real delivery address as returned by shipment-service. */
export type ShippingAddress = {
  id: number;
  recipientName: string;
  phone: string;
  addressLine: string;
  city: string;
  province: string;
  postalCode: string;
  references: string | null;
  isDefault: boolean;
};

/** Payload for creating/updating a delivery address. */
export type ShippingAddressInput = {
  recipientName: string;
  phone: string;
  addressLine: string;
  city: string;
  province: string;
  postalCode: string;
  references: string;
  isDefault: boolean;
};
