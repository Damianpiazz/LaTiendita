// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { AdminCategories } from "@/features/admin/types";

const actions = vi.hoisted(() => ({
  listAdminCategories: vi.fn(), createAdminCategory: vi.fn(), updateAdminCategory: vi.fn(),
  deleteAdminCategory: vi.fn(), setCategoryActive: vi.fn(), setCategoryFeatured: vi.fn(),
}));
vi.mock("@/features/admin/actions/category-actions", () => actions);
import { AdminCategoriesWorkspace } from "@/features/admin/components/admin-categories-workspace";

const seed: AdminCategories = {
  items: [
    { id: "3", name: "Remeras", slug: "remeras", description: "d1", productCount: 14,
      lowStockCount: 3, featured: true, active: true, imageUrl: "" },
    { id: "7", name: "Tecnología", slug: "tecnologia", description: "d2", productCount: 8,
      lowStockCount: 1, featured: false, active: false, imageUrl: "/img/tec.png" },
  ],
  selectedId: "3", updatedLabel: "hoy 09:12", total: 6,
};

beforeEach(() => {
  vi.clearAllMocks();
  actions.listAdminCategories.mockResolvedValue(seed);
  actions.createAdminCategory.mockResolvedValue(undefined);
  actions.updateAdminCategory.mockResolvedValue(undefined);
  actions.deleteAdminCategory.mockResolvedValue({ ok: true });
  actions.setCategoryActive.mockResolvedValue(undefined);
  actions.setCategoryFeatured.mockResolvedValue(undefined);
});
afterEach(() => cleanup());

describe("T3 S6 UI probes", () => {
  it("search, row selection, placeholder and active checkbox operate", async () => {
    const { container } = render(<AdminCategoriesWorkspace initialCategories={seed} />);
    expect(screen.getByText("[ Imagen ]")).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText("Buscar categoría..."), { target: { value: "tec" } });
    await waitFor(() => expect(actions.listAdminCategories).toHaveBeenCalledWith(
      expect.objectContaining({ q: "tec", page: 1, size: 6 })));
    fireEvent.click(screen.getByRole("button", { name: /Tecnología/ }));
    expect(await screen.findByRole("img", { name: "Tecnología" })).toHaveAttribute("src", "/img/tec.png");
    fireEvent.click(screen.getByLabelText(/Categoría activa/));
    await waitFor(() => expect(actions.setCategoryActive).toHaveBeenCalledWith("7", true));
    expect(container.querySelector('input[name="categoryId"]')).toHaveValue("7");
  });

  it("edit keeps stored slug and 409 delete stays open with the exact detail", async () => {
    actions.deleteAdminCategory.mockResolvedValue({ ok: false, error: "409 t3: 12 productos" });
    const { container } = render(<AdminCategoriesWorkspace initialCategories={seed} />);

    fireEvent.click(screen.getByRole("button", { name: "Editar categoría" }));
    const form = await screen.findByRole("dialog");
    fireEvent.change(within(form).getByLabelText(/Nombre/), { target: { value: "Remeras Pro" } });
    fireEvent.click(within(form).getByRole("button", { name: "Guardar cambios" }));
    await waitFor(() => expect(actions.updateAdminCategory).toHaveBeenCalledWith(
      expect.objectContaining({ categoryId: "3", name: "Remeras Pro", slug: "remeras" })));

    fireEvent.click(screen.getByRole("button", { name: "Eliminar categoría" }));
    const dialog = await screen.findByRole("dialog");
    const scrim = dialog.parentElement?.querySelector('[aria-hidden="true"]');
    expect(scrim?.className).toContain("bg-wf-inverse/45");
    expect(dialog.className).toContain("max-w-[480px]");
    fireEvent.click(within(dialog).getByRole("button", { name: "Eliminar categoría" }));
    expect(await within(dialog).findByRole("alert")).toHaveTextContent("409 t3: 12 productos");
    expect(dialog).toBeInTheDocument();
    expect(container.textContent).not.toContain("danger");
  });
});
