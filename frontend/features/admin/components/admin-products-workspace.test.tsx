// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { AdminCategory } from "@/features/admin/types";
import type { AdminProducts } from "@/features/admin/types";

const actions = vi.hoisted(() => ({
  listAdminProducts: vi.fn(),
  createAdminProduct: vi.fn(),
  updateAdminProduct: vi.fn(),
  publishAdminProducts: vi.fn(),
  deleteAdminProducts: vi.fn(),
  exportAdminProducts: vi.fn(),
  getAdminProductImages: vi.fn(),
}));

vi.mock("@/features/admin/actions/product-actions", () => actions);

import { AdminPagination } from "./admin-pagination";
import { AdminProductsWorkspace } from "./admin-products-table";

const products: AdminProducts = {
  items: [
    {
      id: "10",
      productId: "10",
      name: "Campera azul",
      sku: "CAM-AZU",
      category: "Camperas",
      price: 89999,
      stock: 12,
      status: "borrador",
      published: false,
    },
    {
      id: "11",
      productId: "11",
      name: "Remera blanca",
      sku: "REM-BLA",
      category: "Remeras",
      price: 12999,
      stock: 3,
      status: "publicado",
      published: true,
      images: ["/images/remera-1.jpg"],
      selected: true,
    },
  ],
  pageSize: 8,
  totalProducts: 20,
  totalCategories: 6,
  totalLowStock: 5,
  lowStockThreshold: 5,
};

const categories: AdminCategory[] = [
  {
    id: "3",
    name: "Remeras",
    slug: "remeras",
    description: "",
    productCount: 8,
    lowStockCount: 2,
    featured: true,
  },
  {
    id: "7",
    name: "Tecnología",
    slug: "tecnologia",
    description: "",
    productCount: 10,
    lowStockCount: 1,
    featured: false,
  },
];

const emptyPage: AdminProducts = {
  ...products,
  items: [],
  totalProducts: 0,
};

function renderWorkspace(initial: AdminProducts = products) {
  return render(
    <AdminProductsWorkspace initialProducts={initial} categories={categories} />,
  );
}

function defaults(overrides: Record<string, unknown> = {}) {
  return { page: 1, size: 8, sort: "id,desc", ...overrides };
}

describe("AdminProductsWorkspace", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    actions.listAdminProducts.mockResolvedValue(emptyPage);
    actions.createAdminProduct.mockResolvedValue(undefined);
    actions.updateAdminProduct.mockResolvedValue(undefined);
    actions.publishAdminProducts.mockResolvedValue(undefined);
    actions.deleteAdminProducts.mockResolvedValue(undefined);
    actions.exportAdminProducts.mockResolvedValue({
      filename: "productos.csv",
      content: "sku,name\nREM-BLA,Remera blanca\n",
    });
    actions.getAdminProductImages.mockResolvedValue([
      "/images/remera-1.jpg",
      "/images/remera-2.jpg",
      "/images/remera-3.jpg",
    ]);
  });

  afterEach(() => {
    cleanup();
  });

  it("shows the real totals returned by the adapter in the header", () => {
    renderWorkspace();

    expect(
      screen.getByText(/20 productos · 6 categorías · 5 con stock bajo/),
    ).toBeInTheDocument();
  });

  it("searches by name or SKU through the real list action", async () => {
    renderWorkspace();

    fireEvent.change(screen.getByRole("searchbox"), {
      target: { value: "campera" },
    });

    await waitFor(() =>
      expect(actions.listAdminProducts).toHaveBeenCalledWith(
        expect.objectContaining({ q: "campera" }),
      ),
    );
    expect(actions.listAdminProducts).toHaveBeenCalledWith(
      expect.objectContaining(defaults({ q: "campera" })),
    );
  });

  it("filters by status pill using the backend status values", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Sin stock" }));

    await waitFor(() =>
      expect(actions.listAdminProducts).toHaveBeenCalledWith(
        expect.objectContaining(defaults({ status: "agotado" })),
      ),
    );
    expect(screen.getByRole("button", { name: "Sin stock" })).toHaveAttribute(
      "aria-current",
      "true",
    );
  });

  it("filters by category and sort through the real list action", async () => {
    renderWorkspace();

    fireEvent.change(screen.getByLabelText("Categoría"), {
      target: { value: "7" },
    });
    fireEvent.change(screen.getByLabelText("Ordenar"), {
      target: { value: "title,asc" },
    });

    await waitFor(() =>
      expect(actions.listAdminProducts).toHaveBeenCalledWith(
        expect.objectContaining(
          defaults({ categoryId: "7", sort: "title,asc" }),
        ),
      ),
    );
  });

  it("changes page and reports the visible range from the returned page", async () => {
    actions.listAdminProducts.mockResolvedValue({
      ...products,
      totalProducts: 20,
    });
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "2" }));

    await waitFor(() =>
      expect(actions.listAdminProducts).toHaveBeenCalledWith(
        expect.objectContaining(defaults({ page: 2 })),
      ),
    );
  });

  it("exports the filtered CSV and downloads it", async () => {
    const createObjectURL = vi.fn(() => "blob:productos");
    const revokeObjectURL = vi.fn();
    Object.defineProperty(URL, "createObjectURL", {
      value: createObjectURL,
      configurable: true,
    });
    Object.defineProperty(URL, "revokeObjectURL", {
      value: revokeObjectURL,
      configurable: true,
    });
    const click = vi
      .spyOn(HTMLAnchorElement.prototype, "click")
      .mockImplementation(() => {});

    renderWorkspace();
    fireEvent.click(screen.getByRole("button", { name: "Exportar CSV" }));

    await waitFor(() =>
      expect(actions.exportAdminProducts).toHaveBeenCalledWith(
        expect.objectContaining(defaults()),
      ),
    );
    expect(createObjectURL).toHaveBeenCalled();
    expect(click).toHaveBeenCalled();
    click.mockRestore();
  });

  it("publishes the selected rows directly and clears the selection", async () => {
    renderWorkspace();

    expect(screen.getByText(/1 productos seleccionados/)).toBeInTheDocument();
    fireEvent.click(
      screen.getByRole("button", { name: "Publicar" }),
    );

    await waitFor(() =>
      expect(actions.publishAdminProducts).toHaveBeenCalledWith(["11"], true),
    );
    expect(screen.getByText(/0 productos seleccionados/)).toBeInTheDocument();
  });

  it("asks for confirmation before archiving the selection", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Archivar" }));

    const dialog = screen.getByRole("dialog");
    expect(within(dialog).getByText(/Archivar 1 productos/)).toBeInTheDocument();
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Archivar productos" }),
    );

    await waitFor(() =>
      expect(actions.publishAdminProducts).toHaveBeenCalledWith(["11"], false),
    );
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("asks for confirmation before deleting the selection", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Eliminar" }));
    const dialog = screen.getByRole("dialog");
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Eliminar productos" }),
    );

    await waitFor(() =>
      expect(actions.deleteAdminProducts).toHaveBeenCalledWith(["11"]),
    );
  });

  it("clears the selection without calling the API", () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Limpiar" }));

    expect(screen.getByText(/0 productos seleccionados/)).toBeInTheDocument();
    expect(actions.publishAdminProducts).not.toHaveBeenCalled();
  });

  it("creates a product from the new-product modal", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "+ Nuevo producto" }));
    const dialog = screen.getByRole("dialog");
    fireEvent.change(within(dialog).getByLabelText(/Nombre/), {
      target: { value: "Auriculares" },
    });
    fireEvent.change(within(dialog).getByLabelText(/SKU/), {
      target: { value: "AUR-NEG" },
    });
    fireEvent.change(within(dialog).getByLabelText(/Categoría/), {
      target: { value: "7" },
    });
    fireEvent.change(within(dialog).getByLabelText(/Precio/), {
      target: { value: "49999" },
    });
    fireEvent.change(within(dialog).getByLabelText(/Stock/), {
      target: { value: "4" },
    });
    fireEvent.change(within(dialog).getByLabelText("Imagen 1"), {
      target: { value: "/images/auriculares-1.jpg" },
    });
    fireEvent.change(within(dialog).getByLabelText("Imagen 2"), {
      target: { value: "/images/auriculares-2.jpg" },
    });
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Guardar producto" }),
    );

    await waitFor(() =>
      expect(actions.createAdminProduct).toHaveBeenCalledWith(
        expect.objectContaining({
          title: "Auriculares",
          sku: "AUR-NEG",
          categoryId: "7",
          price: 49999,
          quantity: 4,
          images: ["/images/auriculares-1.jpg", "/images/auriculares-2.jpg"],
          published: true,
        }),
      ),
    );
  });

  it("prefills the edit modal and updates the real product id", async () => {
    renderWorkspace();

    fireEvent.click(screen.getAllByRole("button", { name: "Editar" })[1]);
    const dialog = screen.getByRole("dialog");
    expect(within(dialog).getByLabelText(/Nombre/)).toHaveValue("Remera blanca");
    expect(within(dialog).getByLabelText(/SKU/)).toHaveValue("REM-BLA");
    expect(within(dialog).getByLabelText(/Publicado/)).toBeChecked();

    await waitFor(() =>
      expect(actions.getAdminProductImages).toHaveBeenCalledWith("11"),
    );
    await waitFor(() =>
      expect(within(dialog).getByLabelText("Imagen 1")).toHaveValue(
        "/images/remera-1.jpg",
      ),
    );

    fireEvent.click(
      within(dialog).getByRole("button", { name: "Guardar cambios" }),
    );

    await waitFor(() =>
      expect(actions.updateAdminProduct).toHaveBeenCalledWith(
        expect.objectContaining({
          productId: "11",
          title: "Remera blanca",
          images: [
            "/images/remera-1.jpg",
            "/images/remera-2.jpg",
            "/images/remera-3.jpg",
          ],
          published: true,
        }),
      ),
    );
  });

  it("adds and removes image references inside the product modal", () => {
    renderWorkspace();
    fireEvent.click(screen.getByRole("button", { name: "+ Nuevo producto" }));

    const dialog = screen.getByRole("dialog");
    expect(
      within(dialog).getByText("Hasta 10 · la primera es la principal"),
    ).toBeInTheDocument();
    expect(
      within(dialog).getAllByRole("textbox", { name: /Imagen \d+/ }),
    ).toHaveLength(3);
    expect(within(dialog).getByText("Principal")).toBeInTheDocument();

    fireEvent.click(
      within(dialog).getByRole("button", { name: "Agregar imagen" }),
    );
    expect(
      within(dialog).getAllByRole("textbox", { name: /Imagen \d+/ }),
    ).toHaveLength(4);

    fireEvent.click(
      within(dialog).getByRole("button", { name: "Quitar imagen 4" }),
    );
    expect(
      within(dialog).getAllByRole("textbox", { name: /Imagen \d+/ }),
    ).toHaveLength(3);
  });

  it("keeps the form body scrollable and the footer inside the dialog", () => {
    renderWorkspace();
    fireEvent.click(screen.getByRole("button", { name: "+ Nuevo producto" }));

    const dialog = screen.getByRole("dialog");
    expect(dialog).toHaveClass("max-h-[calc(100vh-2rem)]");
    expect(dialog.querySelector("[data-admin-modal-body]")).toHaveClass(
      "overflow-y-auto",
    );
    expect(dialog.querySelector("[data-admin-modal-footer]")).toHaveClass(
      "shrink-0",
    );
  });
});

describe("AdminPagination static mode", () => {
  it("keeps presentational tokens when no page callback is provided", () => {
    render(
      <AdminPagination
        label="Mostrando 1–8 de 24 productos"
        pageCount={3}
        current={1}
      />,
    );

    expect(
      screen.getByText("Mostrando 1–8 de 24 productos"),
    ).toBeInTheDocument();
    expect(screen.getByText("1")).toHaveAttribute("aria-current", "page");
    expect(screen.getByText("2").tagName).toBe("SPAN");
    expect(screen.queryByRole("button", { name: "2" })).not.toBeInTheDocument();
  });
});
