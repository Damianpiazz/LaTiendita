// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import type { AdminCategory } from "@/features/admin/types";

import { AdminCategoryFormModal } from "./admin-category-form-modal";

const category: AdminCategory = {
  id: "3",
  name: "Remeras",
  slug: "remeras",
  description: "Remeras, polos y remerones.",
  productCount: 14,
  lowStockCount: 3,
  featured: true,
  active: false,
  imageUrl: "/images/remeras.jpg",
};

describe("AdminCategoryFormModal", () => {
  afterEach(() => {
    cleanup();
  });

  it("creates a category with a generated slug and the active flag", () => {
    const onSave = vi.fn();
    render(<AdminCategoryFormModal onClose={vi.fn()} onSave={onSave} />);

    const dialog = screen.getByRole("dialog");
    expect(
      within(dialog).getByRole("heading", { name: "Nueva categoría" }),
    ).toBeInTheDocument();
    expect(
      within(dialog).getByText("Completá los datos para publicarla en la tienda."),
    ).toBeInTheDocument();

    fireEvent.change(within(dialog).getByLabelText(/Nombre/), {
      target: { value: "Remeras" },
    });
    expect(within(dialog).getByLabelText(/Slug/)).toHaveValue("remeras");
    fireEvent.change(within(dialog).getByLabelText(/Descripción/), {
      target: { value: "Remeras y polos." },
    });
    fireEvent.change(within(dialog).getByLabelText("URL de imagen"), {
      target: { value: "/images/remeras.jpg" },
    });

    fireEvent.click(
      within(dialog).getByRole("button", { name: "Crear categoría" }),
    );

    expect(onSave).toHaveBeenCalledWith({
      name: "Remeras",
      slug: "remeras",
      description: "Remeras y polos.",
      featured: false,
      active: true,
      imageUrl: "/images/remeras.jpg",
    });
  });

  it("prefills the edit form and preserves the stored slug and featured flag", () => {
    const onSave = vi.fn();
    render(
      <AdminCategoryFormModal
        category={category}
        onClose={vi.fn()}
        onSave={onSave}
      />,
    );

    const dialog = screen.getByRole("dialog");
    expect(
      within(dialog).getByRole("heading", { name: "Editar categoría" }),
    ).toBeInTheDocument();
    expect(
      within(dialog).getByText("Actualizá los datos de Remeras."),
    ).toBeInTheDocument();
    expect(within(dialog).getByLabelText(/Nombre/)).toHaveValue("Remeras");
    expect(within(dialog).getByLabelText(/Slug/)).toHaveValue("remeras");
    expect(within(dialog).getByLabelText(/Descripción/)).toHaveValue(
      "Remeras, polos y remerones.",
    );
    expect(within(dialog).getByLabelText("URL de imagen")).toHaveValue(
      "/images/remeras.jpg",
    );
    expect(within(dialog).getByLabelText(/Categoría activa/)).not.toBeChecked();

    fireEvent.change(within(dialog).getByLabelText(/Nombre/), {
      target: { value: "Remerones" },
    });
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Guardar cambios" }),
    );

    expect(onSave).toHaveBeenCalledWith({
      categoryId: "3",
      name: "Remerones",
      slug: "remeras",
      description: "Remeras, polos y remerones.",
      featured: true,
      active: false,
      imageUrl: "/images/remeras.jpg",
    });
  });

  it("keeps a manually edited slug and reports the close control", () => {
    const onClose = vi.fn();
    render(<AdminCategoryFormModal onClose={onClose} onSave={vi.fn()} />);

    const dialog = screen.getByRole("dialog");
    fireEvent.change(within(dialog).getByLabelText(/Slug/), {
      target: { value: "camisetas" },
    });
    fireEvent.change(within(dialog).getByLabelText(/Nombre/), {
      target: { value: "Camisetas" },
    });

    expect(within(dialog).getByLabelText(/Slug/)).toHaveValue("camisetas");
    fireEvent.click(within(dialog).getByRole("button", { name: "Cerrar" }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
