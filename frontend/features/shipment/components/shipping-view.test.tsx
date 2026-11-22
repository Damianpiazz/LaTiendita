// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { Product } from "@/features/product/types";
import type { ShippingAddress } from "@/features/shipment/types";

const mocks = vi.hoisted(() => ({
  useCartStore: vi.fn(),
  saveShippingAddress: vi.fn(),
}));

vi.mock("@/features/cart/store/cart-store", () => ({
  useCartStore: (selector: (state: { items: { productId: string; quantity: number }[] }) => unknown) =>
    mocks.useCartStore(selector),
}));

vi.mock("@/features/shipment/actions/shipping.actions", () => ({
  saveShippingAddress: mocks.saveShippingAddress,
}));

import { ShippingView } from "./shipping-view";

const products: Product[] = [
  {
    id: "10",
    name: "Auriculares Bluetooth Pro",
    price: 25999,
    categoryId: "1",
    categoryName: "Tecnología",
    subcategory: "Audio",
    slug: "auriculares-bluetooth-pro",
    featured: true,
  },
];

const addresses: ShippingAddress[] = [
  {
    id: 7,
    recipientName: "Juana Pérez",
    phone: "11 5555 5555",
    addressLine: "Av. Siempre Viva 742",
    city: "CABA",
    province: "Buenos Aires",
    postalCode: "1425",
    references: "",
    isDefault: true,
  },
];

describe("ShippingView", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mocks.useCartStore.mockImplementation(
      (selector: (state: { items: { productId: string; quantity: number }[] }) => unknown) =>
        selector({ items: [{ productId: "10", quantity: 2 }] }),
    );
    mocks.saveShippingAddress.mockResolvedValue({ ok: false });
  });

  it("renders the real address form and submits to payment through the summary CTA", () => {
    render(<ShippingView products={products} addresses={addresses} methodName="Envío a domicilio" />);

    expect(screen.getByText("Envío a domicilio")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Juana Pérez")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Continuar al pago" })).toHaveAttribute(
      "form",
      "shipping-address-form",
    );
    expect(screen.getByRole("link", { name: "Volver al carrito" })).toHaveAttribute(
      "href",
      "/carrito",
    );
  });

  it("renders the empty cart state when there are no cart lines", () => {
    mocks.useCartStore.mockImplementation(
      (selector: (state: { items: { productId: string; quantity: number }[] }) => unknown) =>
        selector({ items: [] }),
    );

    render(<ShippingView products={products} addresses={addresses} methodName="Envío a domicilio" />);

    expect(screen.queryByRole("button", { name: "Continuar al pago" })).not.toBeInTheDocument();
  });
});
