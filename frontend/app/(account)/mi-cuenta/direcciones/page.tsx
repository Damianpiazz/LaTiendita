import type { Metadata } from "next";

import { AccountPageShell } from "@/features/customer/components/account-page-shell";
import { AddressManager } from "@/features/customer/components/address-manager";
import { getShippingAddresses } from "@/features/shipment/services/shipping.service";

export const metadata: Metadata = {
  title: "Direcciones",
};

export default async function AddressesPage() {
  const addresses = await getShippingAddresses();

  return (
    <AccountPageShell
      title="Direcciones"
      description="Administrá las direcciones de entrega de tus futuras compras."
    >
      <AddressManager addresses={addresses} />
    </AccountPageShell>
  );
}
