// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { Product } from "@/features/product/types";
import type { ShippingAddress } from "@/features/shipment/types";

const mocks = vi.hoisted(() => ({
  getProducts: vi.fn(),
  getShippingMethods: vi.fn(),
  getShippingAddresses: vi.fn(),
  saveShippingAddress: vi.fn(),
}));

vi.mock("@/features/product/services/catalog.service", () => ({
  getProducts: mocks.getProducts,
}));

vi.mock("@/features/shipment/services/shipping.service", () => ({
  getShippingMethods: mocks.getShippingMethods,
  getShippingAddresses: mocks.getShippingAddresses,
}));

vi.mock("@/features/shipment/actions/shipping.actions", () => ({
  saveShippingAddress: mocks.saveShippingAddress,
}));

vi.mock("@/features/cart/store/cart-store", () => ({
  useCartStore: (selector: (state: { items: { productId: string; quantity: number }[] }) => unknown) =>
    selector({ items: [{ productId: "10", quantity: 1 }] }),
}));

import { ShippingPage } from "./shipping-page";

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

async function renderPage() {
  render(await ShippingPage());
}

describe("ShippingPage", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mocks.getProducts.mockResolvedValue(products);
    mocks.getShippingMethods.mockResolvedValue([{ id: "envio-a-domicilio", name: "Envío a domicilio" }]);
    mocks.getShippingAddresses.mockResolvedValue(addresses);
    mocks.saveShippingAddress.mockResolvedValue({ ok: false });
  });

  it("loads real shipping data and renders the checkout shipping wireframe", async () => {
    await renderPage();

    expect(mocks.getProducts).toHaveBeenCalledWith();
    expect(mocks.getShippingMethods).toHaveBeenCalledWith();
    expect(mocks.getShippingAddresses).toHaveBeenCalledWith();
    expect(screen.getByRole("heading", { name: "Envío" })).toBeInTheDocument();
    expect(screen.getByText("Envío a domicilio")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Juana Pérez")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Continuar al pago" })).toBeInTheDocument();
  });
});
