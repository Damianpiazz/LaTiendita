// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import type { ShippingAddress } from "@/features/shipment/types";

import { AddressForm } from "./address-form";

const address: ShippingAddress = {
  id: 10,
  recipientName: "Juana Pérez",
  phone: "11 5555 5555",
  addressLine: "Av. Siempre Viva 742",
  city: "CABA",
  province: "Buenos Aires",
  postalCode: "1425",
  references: "Piso 2",
  isDefault: true,
};

describe("AddressForm", () => {
  afterEach(() => {
    cleanup();
  });

  it("renders the wireframe delivery fields", () => {
    render(
      <AddressForm
        formId="shipping-address-form"
        action={vi.fn()}
        state={{ ok: false }}
      />,
    );

    expect(screen.getByLabelText("Nombre y apellido")).toHaveAttribute("name", "recipientName");
    expect(screen.getByLabelText("Teléfono")).toHaveAttribute("name", "phone");
    expect(screen.getByLabelText("Dirección")).toHaveAttribute("name", "addressLine");
    expect(screen.getByLabelText("Ciudad")).toHaveAttribute("name", "city");
    expect(screen.getByLabelText("Provincia")).toHaveAttribute("name", "province");
    expect(screen.getByLabelText("Código postal")).toHaveAttribute("name", "postalCode");
    expect(screen.getByLabelText("Referencias (opcional)")).toHaveAttribute("name", "references");
    expect(
      screen.getByRole("checkbox", { name: /guardar esta dirección para futuras compras/i }),
    ).toHaveAttribute("name", "isDefault");
  });

  it("prefills values from a saved address", () => {
    render(
      <AddressForm
        formId="shipping-address-form"
        action={vi.fn()}
        state={{ ok: false }}
        address={address}
      />,
    );

    expect(screen.getByDisplayValue("Juana Pérez")).toBeInTheDocument();
    expect(screen.getByDisplayValue("11 5555 5555")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Av. Siempre Viva 742")).toBeInTheDocument();
    expect(screen.getByDisplayValue("CABA")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Buenos Aires")).toBeInTheDocument();
    expect(screen.getByDisplayValue("1425")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Piso 2")).toBeInTheDocument();
    expect(
      screen.getByRole("checkbox", { name: /guardar esta dirección para futuras compras/i }),
    ).toBeChecked();
  });

  it("shows validation errors returned by the server action", () => {
    render(
      <AddressForm
        formId="shipping-address-form"
        action={vi.fn()}
        state={{
          ok: false,
          message: "Revisá los campos marcados.",
          fieldErrors: { recipientName: "Ingresá nombre y apellido." },
        }}
      />,
    );

    expect(screen.getByRole("status")).toHaveTextContent("Revisá los campos marcados.");
    expect(screen.getByText("Ingresá nombre y apellido.")).toBeInTheDocument();
  });
});
