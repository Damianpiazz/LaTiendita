// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { Category, Product } from "@/features/product/types";

import { CategoryProducts } from "./category-products";

const category: Category = {
  id: "1",
  slug: "tecnologia",
  name: "Tecnología",
  description: "Auriculares, computación, celulares y accesorios.",
  productCount: 24,
  featured: true,
};

const products: Product[] = Array.from({ length: 24 }, (_, index) => {
  const n = index + 1;
  return {
    id: String(n),
    slug: `producto-${n}`,
    name: `Producto ${n}`,
    price: 1000 * n,
    categoryId: "1",
    categoryName: "Tecnología",
    subcategory:
      n <= 6 ? "Audio" : n <= 12 ? "Computación" : n <= 18 ? "Celulares" : "Accesorios",
    featured: n <= 4,
  };
});

describe("CategoryProducts", () => {
  afterEach(() => {
    cleanup();
  });

  it("renders the category wireframe header, filters, grid and pagination", () => {
    render(<CategoryProducts category={category} products={products} />);

    expect(screen.getByRole("heading", { name: "Tecnología" })).toBeInTheDocument();
    expect(
      screen.getByText("Auriculares, computación, celulares y accesorios."),
    ).toBeInTheDocument();
    expect(screen.getByText("24 productos")).toBeInTheDocument();

    const filters = screen.getByRole("group", { name: "Filtrar por subcategoría" });
    expect(within(filters).getByRole("button", { name: "Todos" })).toHaveAttribute(
      "aria-pressed",
      "true",
    );
    for (const label of ["Audio", "Computación", "Celulares", "Accesorios"]) {
      expect(within(filters).getByRole("button", { name: label })).toBeInTheDocument();
    }

    expect(screen.getByText("Ordenar por:")).toBeInTheDocument();
    expect(screen.getByText("Destacados")).toBeInTheDocument();
    expect(
      screen.getAllByRole("link", { name: "Producto 1" }).map((link) => link.getAttribute("href")),
    ).toContain("/productos/producto-1");
    expect(screen.getByText("Mostrando 1–12 de 24 productos")).toBeInTheDocument();
    expect(screen.getByRole("navigation", { name: "Paginación" })).toBeInTheDocument();
  });

  it("filters by subcategory and resets pagination", () => {
    render(<CategoryProducts category={category} products={products} />);

    fireEvent.click(screen.getByRole("button", { name: "Página 2" }));
    expect(screen.getByText("Mostrando 13–24 de 24 productos")).toBeInTheDocument();

    const filters = screen.getByRole("group", { name: "Filtrar por subcategoría" });
    fireEvent.click(within(filters).getByRole("button", { name: "Audio" }));

    expect(within(filters).getByRole("button", { name: "Audio" })).toHaveAttribute(
      "aria-pressed",
      "true",
    );
    expect(screen.getByText("6 productos")).toBeInTheDocument();
    expect(screen.getByText("Mostrando 1–6 de 6 productos")).toBeInTheDocument();
    expect(screen.queryByRole("navigation", { name: "Paginación" })).not.toBeInTheDocument();
    expect(screen.getAllByRole("link", { name: "Producto 1" }).length).toBeGreaterThan(0);
    expect(screen.queryByRole("link", { name: "Producto 7" })).not.toBeInTheDocument();
  });
});
