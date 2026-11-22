// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { Category, Product } from "@/features/product/types";

const mocks = vi.hoisted(() => ({
  findCategoryBySlug: vi.fn(),
  getCategoryProducts: vi.fn(),
  notFound: vi.fn(),
}));

vi.mock("next/navigation", () => ({
  notFound: mocks.notFound,
}));

vi.mock("@/features/product/services/catalog.service", () => ({
  findCategoryBySlug: mocks.findCategoryBySlug,
  getCategoryProducts: mocks.getCategoryProducts,
}));

import { CategoryPage } from "./category-page";

const category: Category = {
  id: "1",
  slug: "tecnologia",
  name: "Tecnología",
  description: "Auriculares, computación, celulares y accesorios.",
  productCount: 24,
  featured: true,
};

const products: Product[] = [
  {
    id: "10",
    slug: "auriculares-inalambricos",
    name: "Auriculares inalámbricos",
    price: 25999,
    categoryId: "1",
    categoryName: "Tecnología",
    subcategory: "Audio",
    featured: true,
  },
];

async function renderPage(slug = "tecnologia") {
  render(await CategoryPage({ slug }));
}

describe("CategoryPage", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mocks.findCategoryBySlug.mockResolvedValue(category);
    mocks.getCategoryProducts.mockResolvedValue(products);
    mocks.notFound.mockImplementation(() => {
      throw new Error("NOT_FOUND");
    });
  });

  it("loads category and products from the real catalog service", async () => {
    await renderPage();

    expect(mocks.findCategoryBySlug).toHaveBeenCalledWith("tecnologia");
    expect(mocks.getCategoryProducts).toHaveBeenCalledWith("1");
    expect(screen.getByRole("heading", { name: "Tecnología" })).toBeInTheDocument();
    expect(
      screen.getByText("Auriculares, computación, celulares y accesorios."),
    ).toBeInTheDocument();
    expect(
      screen
        .getAllByRole("link", { name: "Auriculares inalámbricos" })
        .map((link) => link.getAttribute("href")),
    ).toContain("/productos/auriculares-inalambricos");
  });

  it("renders the category breadcrumbs from backend data", async () => {
    await renderPage();

    expect(screen.getByRole("link", { name: "Inicio" })).toHaveAttribute("href", "/");
    expect(screen.getByRole("link", { name: "Categorías" })).toHaveAttribute(
      "href",
      "/categorias",
    );
    expect(screen.getByText("Tecnología", { selector: "span" })).toBeInTheDocument();
  });

  it("returns notFound when the category slug does not exist", async () => {
    mocks.findCategoryBySlug.mockResolvedValue(null);

    await expect(CategoryPage({ slug: "missing" })).rejects.toThrow("NOT_FOUND");
    expect(mocks.notFound).toHaveBeenCalled();
    expect(mocks.getCategoryProducts).not.toHaveBeenCalled();
  });
});
