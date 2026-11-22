// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { AdminCategories } from "@/features/admin/types";

const actions = vi.hoisted(() => ({
  listAdminCategories: vi.fn(),
  createAdminCategory: vi.fn(),
  updateAdminCategory: vi.fn(),
  deleteAdminCategory: vi.fn(),
  setCategoryActive: vi.fn(),
  setCategoryFeatured: vi.fn(),
}));

vi.mock("@/features/admin/actions/category-actions", () => actions);

import { AdminCategoriesWorkspace } from "./admin-categories-workspace";

const categories: AdminCategories = {
  items: [
    {
      id: "3",
      name: "Remeras",
      slug: "remeras",
      description: "Remeras, polos y remerones.",
      productCount: 14,
      lowStockCount: 3,
      featured: true,
      active: true,
      imageUrl: "/images/remeras.jpg",
    },
    {
      id: "7",
      name: "Tecnología",
      slug: "tecnologia",
      description: "Accesorios tecnológicos.",
      productCount: 8,
      lowStockCount: 1,
      featured: false,
      active: false,
      imageUrl: null,
    },
  ],
  selectedId: "3",
  updatedLabel: "hoy 09:12",
  total: 6,
};

function renderWorkspace() {
  return render(<AdminCategoriesWorkspace initialCategories={categories} />);
}

describe("AdminCategoriesWorkspace", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    actions.listAdminCategories.mockResolvedValue(categories);
    actions.createAdminCategory.mockResolvedValue(undefined);
    actions.updateAdminCategory.mockResolvedValue(undefined);
    actions.deleteAdminCategory.mockResolvedValue({ ok: true });
    actions.setCategoryActive.mockResolvedValue(undefined);
    actions.setCategoryFeatured.mockResolvedValue(undefined);
  });

  afterEach(() => {
    cleanup();
  });

  it("searches by name through the list action and resets the page", async () => {
    renderWorkspace();

    fireEvent.change(screen.getByPlaceholderText("Buscar categoría..."), {
      target: { value: "Remeras" },
    });

    await waitFor(() =>
      expect(actions.listAdminCategories).toHaveBeenCalledWith(
        expect.objectContaining({ q: "Remeras", page: 1, size: 6, sort: "name,asc" }),
      ),
    );
  });

  it("toggles the sort control and reloads with the new order", async () => {
    renderWorkspace();

    expect(screen.getByRole("button", { name: "Ordenar: A–Z ↓" })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Ordenar: A–Z ↓" }));

    await waitFor(() =>
      expect(actions.listAdminCategories).toHaveBeenCalledWith(
        expect.objectContaining({ sort: "name,desc" }),
      ),
    );
    expect(
      await screen.findByRole("button", { name: "Ordenar: Z–A ↑" }),
    ).toBeInTheDocument();
  });

  it("selects a row and shows its image or the frame placeholder", async () => {
    renderWorkspace();

    expect(screen.getByRole("img", { name: "Remeras" })).toHaveAttribute(
      "src",
      "/images/remeras.jpg",
    );

    fireEvent.click(screen.getByRole("button", { name: /Tecnología/ }));

    expect(
      await screen.findByRole("heading", { name: "Tecnología" }),
    ).toBeInTheDocument();
    expect(screen.queryByRole("img", { name: "Tecnología" })).not.toBeInTheDocument();
    expect(screen.getByText("[ Imagen ]")).toBeInTheDocument();
  });

  it("creates a category from the header form", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "+ Nueva categoría" }));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByLabelText(/Nombre/), {
      target: { value: "Camisetas" },
    });
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Crear categoría" }),
    );

    await waitFor(() =>
      expect(actions.createAdminCategory).toHaveBeenCalledWith(
        expect.objectContaining({ name: "Camisetas", slug: "camisetas", active: true }),
      ),
    );
    await waitFor(() => expect(actions.listAdminCategories).toHaveBeenCalled());
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("edits the selected category without changing its stored slug", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Editar categoría" }));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByLabelText(/Nombre/), {
      target: { value: "Remerones" },
    });
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Guardar cambios" }),
    );

    await waitFor(() =>
      expect(actions.updateAdminCategory).toHaveBeenCalledWith(
        expect.objectContaining({
          categoryId: "3",
          name: "Remerones",
          slug: "remeras",
          featured: true,
        }),
      ),
    );
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("keeps the dialog open and shows the 409 detail after a blocked delete", async () => {
    actions.deleteAdminCategory.mockResolvedValue({
      ok: false,
      error: "No se puede eliminar la categoría porque tiene productos asociados.",
    });
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Eliminar categoría" }));
    const dialog = await screen.findByRole("dialog");
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Eliminar categoría" }),
    );

    await waitFor(() =>
      expect(actions.deleteAdminCategory).toHaveBeenCalledWith("3"),
    );
    expect(
      await within(dialog).findByRole("alert"),
    ).toHaveTextContent(
      "No se puede eliminar la categoría porque tiene productos asociados.",
    );
    expect(dialog).toBeInTheDocument();
  });

  it("closes the dialog and reloads after a successful delete", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByRole("button", { name: "Eliminar categoría" }));
    const dialog = await screen.findByRole("dialog");
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Eliminar categoría" }),
    );

    await waitFor(() =>
      expect(actions.deleteAdminCategory).toHaveBeenCalledWith("3"),
    );
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it("updates the active flag through its action", async () => {
    renderWorkspace();

    fireEvent.click(screen.getByLabelText(/Categoría activa/));

    await waitFor(() =>
      expect(actions.setCategoryActive).toHaveBeenCalledWith("3", false),
    );
    await waitFor(() => expect(actions.listAdminCategories).toHaveBeenCalled());
  });

  it("preserves the featured control and its hidden payload", () => {
    renderWorkspace();

    expect(
      screen.getByRole("button", { name: "Quitar de destacadas" }),
    ).toBeInTheDocument();
    expect(document.querySelector('input[name="categoryId"]')).toHaveValue("3");
    expect(document.querySelector('input[name="featured"]')).toHaveValue("false");
    expect(actions.setCategoryFeatured).not.toHaveBeenCalled();
  });

  it("preserves the list copy and both store links", () => {
    renderWorkspace();

    expect(
      screen.getByRole("heading", { name: "Todas las categorías" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Mostrando 2 de 2 categorías")).toBeInTheDocument();
    expect(
      screen.getByText("Última actualización: hoy 09:12"),
    ).toBeInTheDocument();
    expect(
      screen.getAllByRole("link", { name: "Ver en tienda ↗" }).map((link) =>
        link.getAttribute("href"),
      ),
    ).toEqual(["/categorias", "/categorias/remeras"]);
  });
});
