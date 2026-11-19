type TextFieldProps = {
  id: string;
  label: string;
  name: string;
  placeholder: string;
  autoComplete?: string;
  type?: "text" | "tel";
  inputMode?: "numeric" | "text";
};

/**
 * Address form of the shipping step.
 *
 * Wireframe: 16px between blocks, label 14/500 `text/secondary` 8px above a
 * 40px input with a `border/strong` hairline. Fields are native and carry
 * `name`/`autocomplete`; the design defines no error state and no submit
 * target, so nothing is validated or persisted.
 *
 * The form has no visible title: the card already reads `Envío a domicilio`, so
 * `Dirección de entrega` was a second heading for the same thing. The legend
 * stays for assistive tech, which needs the group named.
 */
export function AddressForm() {
  return (
    <fieldset className="flex flex-col gap-4 border-0 p-0">
      <legend className="sr-only">Dirección de entrega</legend>

      <div className="grid gap-4 sm:grid-cols-2">
        <TextField
          id="shipping-full-name"
          name="fullName"
          label="Nombre y apellido"
          placeholder="Ej. Juana Pérez"
          autoComplete="name"
        />
        <TextField
          id="shipping-phone"
          name="phone"
          label="Teléfono"
          placeholder="Ej. 11 5555 5555"
          autoComplete="tel"
          type="tel"
        />
      </div>

      <TextField
        id="shipping-address"
        name="address"
        label="Dirección"
        placeholder="Calle y número"
        autoComplete="street-address"
      />

      <div className="grid gap-4 sm:grid-cols-3">
        <TextField
          id="shipping-city"
          name="city"
          label="Ciudad"
          placeholder="Ej. CABA"
          autoComplete="address-level2"
        />
        <TextField
          id="shipping-province"
          name="province"
          label="Provincia"
          placeholder="Ej. Buenos Aires"
          autoComplete="address-level1"
        />
        <TextField
          id="shipping-postal-code"
          name="postalCode"
          label="Código postal"
          placeholder="Ej. 1425"
          autoComplete="postal-code"
          inputMode="numeric"
        />
      </div>

      <TextField
        id="shipping-references"
        name="references"
        label="Referencias (opcional)"
        placeholder="Piso, depto, entre calles..."
      />

      <label className="flex cursor-pointer items-center gap-2">
        <input type="checkbox" name="saveAddress" className="peer sr-only" />
        <span
          aria-hidden
          className="flex size-6 shrink-0 items-center justify-center rounded-wf-sm border border-wf-line-strong bg-wf-surface text-sm text-wf-on-inverse transition-colors peer-checked:border-wf-inverse peer-checked:bg-wf-inverse peer-focus-visible:ring-2 peer-focus-visible:ring-wf-inverse peer-focus-visible:ring-offset-2"
        >
          {/* White on white while unchecked; the tick only shows on the inverse box. */}
          ✓
        </span>
        <span className="text-sm text-wf-ink-muted">
          Guardar esta dirección para futuras compras
        </span>
      </label>
    </fieldset>
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
}: TextFieldProps) {
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
        className="h-10 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink placeholder:text-wf-ink-placeholder focus-visible:border-wf-inverse focus-visible:outline-none"
      />
    </div>
  );
}
