// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import type { AdminCategory } from "@/features/admin/types";

import { AdminCategoryConfirmDialog } from "./admin-category-confirm-dialog";

const category: AdminCategory = {
  id: "3",
  name: "Remeras",
  slug: "remeras",
  description: "Remeras, polos y remerones.",
  productCount: 14,
  lowStockCount: 3,
  featured: true,
  active: true,
  imageUrl: "/images/remeras.jpg",
};

describe("AdminCategoryConfirmDialog", () => {
  afterEach(() => {
    cleanup();
  });

  it("explains the deletion impact and reports the 409 detail", () => {
    const onCancel = vi.fn();
    const onConfirm = vi.fn();

    render(
      <AdminCategoryConfirmDialog
        category={category}
        error="No se puede eliminar la categoría porque tiene productos asociados."
        onCancel={onCancel}
        onConfirm={onConfirm}
      />,
    );

    const dialog = screen.getByRole("dialog");
    expect(
      within(dialog).getByRole("heading", { name: "Eliminar categoría" }),
    ).toBeInTheDocument();
    expect(
      within(dialog).getByText("Revisá el impacto antes de continuar."),
    ).toBeInTheDocument();
    expect(within(dialog).getByText("Eliminar Remeras")).toBeInTheDocument();
    expect(
      within(dialog).getByText(
        "Los 14 productos dejarán de estar asociados y pasarán a Sin categoría. La categoría y su URL /categorias/remeras dejarán de estar disponibles.",
      ),
    ).toBeInTheDocument();
    expect(
      within(dialog).getByText("Esta acción no se puede deshacer."),
    ).toBeInTheDocument();
    expect(within(dialog).getByRole("alert")).toHaveTextContent(
      "No se puede eliminar la categoría porque tiene productos asociados.",
    );

    fireEvent.click(within(dialog).getByRole("button", { name: "Cancelar" }));
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Eliminar categoría" }),
    );

    expect(onCancel).toHaveBeenCalledTimes(1);
    expect(onConfirm).toHaveBeenCalledTimes(1);
  });
});
