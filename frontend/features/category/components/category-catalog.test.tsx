// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen, within } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { Category } from "@/features/product/types";

import { CategoryCatalog } from "./category-catalog";

const categories: Category[] = [
  { id: "1", slug: "tecnologia", name: "Tecnología", productCount: 24, featured: true },
  { id: "2", slug: "hogar", name: "Hogar", productCount: 18, featured: true },
  { id: "3", slug: "ropa", name: "Ropa", productCount: 32, featured: true },
  { id: "4", slug: "deportes", name: "Deportes", productCount: 14, featured: true },
  { id: "5", slug: "belleza", name: "Belleza", productCount: 21, featured: false },
  { id: "6", slug: "juguetes", name: "Juguetes", productCount: 12, featured: true },
];

describe("CategoryCatalog", () => {
  afterEach(() => {
    cleanup();
  });

  it("renders title, subtitle, count and all categories from backend data", () => {
    render(<CategoryCatalog categories={categories} />);

    expect(screen.getByRole("heading", { name: "Categorías" })).toBeInTheDocument();
    expect(
      screen.getByText("Encontrá lo que buscás entre nuestras 6 categorías."),
    ).toBeInTheDocument();
    expect(screen.getByText("6 categorías")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /tecnología/i })).toHaveAttribute(
      "href",
      "/categorias/tecnologia",
    );
    expect(screen.getByRole("link", { name: /belleza/i })).toHaveAttribute(
      "href",
      "/categorias/belleza",
    );
  });

  it("shows featured categories as filter pills and filters the grid", () => {
    render(<CategoryCatalog categories={categories} />);

    const filters = screen.getByRole("group", { name: "Filtrar por categoría" });
    expect(within(filters).getByRole("button", { name: "Todas" })).toHaveAttribute(
      "aria-pressed",
      "true",
    );
    expect(within(filters).getByRole("button", { name: "Tecnología" })).toBeInTheDocument();
    expect(within(filters).getByRole("button", { name: "Hogar" })).toBeInTheDocument();
    expect(within(filters).getByRole("button", { name: "Ropa" })).toBeInTheDocument();
    expect(within(filters).getByRole("button", { name: "Deportes" })).toBeInTheDocument();
    expect(within(filters).queryByRole("button", { name: "Juguetes" })).not.toBeInTheDocument();

    fireEvent.click(within(filters).getByRole("button", { name: "Tecnología" }));

    expect(within(filters).getByRole("button", { name: "Tecnología" })).toHaveAttribute(
      "aria-pressed",
      "true",
    );
    expect(within(filters).getByRole("button", { name: "Todas" })).toHaveAttribute(
      "aria-pressed",
      "false",
    );
    expect(screen.getByText("1 categoría")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /tecnología/i })).toBeInTheDocument();
    expect(screen.queryByRole("link", { name: /hogar/i })).not.toBeInTheDocument();
  });
});
