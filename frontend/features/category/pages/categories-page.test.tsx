// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { Category } from "@/features/product/types";

const mocks = vi.hoisted(() => ({
  getCategories: vi.fn(),
}));

vi.mock("@/features/product/services/catalog.service", () => ({
  getCategories: mocks.getCategories,
}));

import { CategoriesPage } from "./categories-page";

const categories: Category[] = [
  { id: "1", slug: "tecnologia", name: "Tecnología", productCount: 24, featured: true },
  { id: "2", slug: "hogar", name: "Hogar", productCount: 18, featured: true },
];

async function renderPage() {
  render(await CategoriesPage());
}

describe("CategoriesPage", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mocks.getCategories.mockResolvedValue(categories);
  });

  it("loads categories from the catalog service and renders the wireframe", async () => {
    await renderPage();

    expect(mocks.getCategories).toHaveBeenCalledWith();
    expect(screen.getByRole("heading", { name: "Categorías" })).toBeInTheDocument();
    expect(
      screen.getByText("Encontrá lo que buscás entre nuestras 2 categorías."),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /tecnología/i })).toHaveAttribute(
      "href",
      "/categorias/tecnologia",
    );
  });
});
