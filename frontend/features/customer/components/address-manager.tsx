"use client";

import { useActionState, useState, useTransition } from "react";

import {
  deleteShippingAddress,
  saveShippingAddress,
  type SaveShippingAddressState,
} from "@/features/shipment/actions/shipping.actions";
import { AddressForm } from "@/features/shipment/components/address-form";
import type { ShippingAddress } from "@/features/shipment/types";

const INITIAL_STATE: SaveShippingAddressState = { ok: false };
const FORM_ID = "account-address-form";

/** Address book of the customer account, backed by the real shipment API. */
export function AddressManager({ addresses }: { addresses: ShippingAddress[] }) {
  const [state, formAction, isPending] = useActionState(saveShippingAddress, INITIAL_STATE);
  const [selectedId, setSelectedId] = useState<number | "new">(
    addresses.find((address) => address.isDefault)?.id ?? addresses[0]?.id ?? "new",
  );
  const [, startTransition] = useTransition();
  const selected = addresses.find((address) => address.id === selectedId);

  function remove(addressId: number) {
    startTransition(async () => {
      await deleteShippingAddress(addressId);
      setSelectedId("new");
    });
  }

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <article className="flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-5">
        <h2 className="text-base font-bold text-wf-ink">Direcciones guardadas</h2>

        {addresses.length === 0 ? (
          <p className="text-sm text-wf-ink-muted">
            Todavía no guardaste direcciones. Cargá una a la derecha.
          </p>
        ) : (
          <ul className="flex flex-col gap-3">
            {addresses.map((address) => (
              <li
                key={address.id}
                className="flex flex-wrap items-start justify-between gap-3 rounded-wf-sm border border-wf-line p-4"
              >
                <div className="flex flex-col gap-1 text-sm">
                  <span className="font-medium text-wf-ink">{address.recipientName}</span>
                  <span className="text-wf-ink-secondary">{address.addressLine}</span>
                  <span className="text-wf-ink-muted">
                    {address.city}, {address.province} · {address.postalCode}
                  </span>
                  {address.isDefault ? (
                    <span className="text-xs text-wf-ink-muted">Predeterminada</span>
                  ) : null}
                </div>
                <div className="flex gap-3 text-sm">
                  <button
                    type="button"
                    onClick={() => setSelectedId(address.id)}
                    className="text-wf-ink-secondary underline-offset-2 hover:underline"
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    onClick={() => remove(address.id)}
                    className="text-red-600 underline-offset-2 hover:underline"
                  >
                    Eliminar
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}

        <button
          type="button"
          onClick={() => setSelectedId("new")}
          className="self-start text-sm font-medium text-wf-ink-secondary underline-offset-2 hover:text-wf-ink hover:underline"
        >
          + Agregar dirección
        </button>
      </article>

      <article className="flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-surface p-5">
        <h2 className="text-base font-bold text-wf-ink">
          {selected ? "Editar dirección" : "Nueva dirección"}
        </h2>

        <AddressForm
          key={selectedId}
          formId={FORM_ID}
          action={formAction}
          state={state}
          address={selected}
          next="/mi-cuenta/direcciones"
        />

        <button
          type="submit"
          form={FORM_ID}
          disabled={isPending}
          className="h-10 self-start rounded-wf-sm bg-wf-inverse px-5 text-sm font-medium text-wf-on-inverse disabled:opacity-60"
        >
          {isPending ? "Guardando..." : "Guardar dirección"}
        </button>
      </article>
    </div>
  );
}
