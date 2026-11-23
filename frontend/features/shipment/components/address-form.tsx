"use client";

import type { SaveShippingAddressState } from "@/features/shipment/actions/shipping.actions";
import type { ShippingAddress } from "@/features/shipment/types";

type AddressFormProps = {
  formId: string;
  action: (formData: FormData) => void;
  state: SaveShippingAddressState;
  address?: ShippingAddress;
  /** Internal path to open after saving; defaults to the checkout step. */
  next?: string;
};

type TextFieldProps = {
  id: string;
  label: string;
  name: string;
  placeholder: string;
  autoComplete?: string;
  type?: "text" | "tel";
  inputMode?: "numeric" | "text";
  defaultValue?: string;
  error?: string;
  required?: boolean;
};

/**
 * Address form of the shipping step, wired to the real shipment address API.
 *
 * Wireframe: 16px between blocks, label 14/500 `text/secondary` 8px above a
 * 40px input with a `border/strong` hairline.
 */
export function AddressForm({ formId, action, state, address, next }: AddressFormProps) {
  return (
    <form id={formId} action={action} className="flex flex-col gap-4" noValidate>
      <input type="hidden" name="addressId" value={address?.id ?? ""} />
      <input type="hidden" name="next" value={next ?? ""} />
      <fieldset className="flex flex-col gap-4 border-0 p-0">
        <legend className="sr-only">Dirección de entrega</legend>

        <div className="grid gap-4 sm:grid-cols-2">
          <TextField
            id="shipping-full-name"
            name="recipientName"
            label="Nombre y apellido"
            placeholder="Ej. Juana Pérez"
            autoComplete="name"
            defaultValue={address?.recipientName}
            error={state.fieldErrors?.recipientName}
            required
          />
          <TextField
            id="shipping-phone"
            name="phone"
            label="Teléfono"
            placeholder="Ej. 11 5555 5555"
            autoComplete="tel"
            type="tel"
            defaultValue={address?.phone}
            error={state.fieldErrors?.phone}
            required
          />
        </div>

        <TextField
          id="shipping-address"
          name="addressLine"
          label="Dirección"
          placeholder="Calle y número"
          autoComplete="street-address"
          defaultValue={address?.addressLine}
          error={state.fieldErrors?.addressLine}
          required
        />

        <div className="grid gap-4 sm:grid-cols-3">
          <TextField
            id="shipping-city"
            name="city"
            label="Ciudad"
            placeholder="Ej. CABA"
            autoComplete="address-level2"
            defaultValue={address?.city}
            error={state.fieldErrors?.city}
            required
          />
          <TextField
            id="shipping-province"
            name="province"
            label="Provincia"
            placeholder="Ej. Buenos Aires"
            autoComplete="address-level1"
            defaultValue={address?.province}
            error={state.fieldErrors?.province}
            required
          />
          <TextField
            id="shipping-postal-code"
            name="postalCode"
            label="Código postal"
            placeholder="Ej. 1425"
            autoComplete="postal-code"
            inputMode="numeric"
            defaultValue={address?.postalCode}
            error={state.fieldErrors?.postalCode}
            required
          />
        </div>

        <TextField
          id="shipping-references"
          name="references"
          label="Referencias (opcional)"
          placeholder="Piso, depto, entre calles..."
          defaultValue={address?.references ?? ""}
          error={state.fieldErrors?.references}
        />

        <label htmlFor="shipping-is-default" className="flex cursor-pointer items-center gap-2">
          <input
            id="shipping-is-default"
            type="checkbox"
            name="isDefault"
            defaultChecked={address?.isDefault ?? false}
            className="peer sr-only"
          />
          <span
            aria-hidden
            className="flex size-6 shrink-0 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm text-wf-on-inverse transition-colors peer-checked:border-wf-inverse peer-checked:bg-wf-inverse peer-focus-visible:ring-2 peer-focus-visible:ring-wf-inverse peer-focus-visible:ring-offset-2"
          >
            ✓
          </span>
          <span className="text-sm text-wf-ink-muted">
            Guardar esta dirección para futuras compras
          </span>
        </label>
      </fieldset>

      {state.message ? (
        <p role="status" className="text-sm text-red-600">
          {state.message}
        </p>
      ) : null}
    </form>
  );
}

function TextField({
  id,
  label,
  name,
  placeholder,
  autoComplete,
  type = "text",
  inputMode,
  defaultValue,
  error,
  required,
}: TextFieldProps) {
  const errorId = `${id}-error`;

  return (
    <div className="flex flex-col gap-2">
      <label htmlFor={id} className="text-sm font-medium text-wf-ink-secondary">
        {label}
      </label>
      <input
        id={id}
        name={name}
        type={type}
        inputMode={inputMode}
        autoComplete={autoComplete}
        placeholder={placeholder}
        defaultValue={defaultValue}
        required={required}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className="h-10 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink placeholder:text-wf-ink-placeholder focus-visible:border-wf-inverse focus-visible:outline-none"
      />
      {error ? (
        <p id={errorId} className="text-xs text-red-600">
          {error}
        </p>
      ) : null}
    </div>
  );
}