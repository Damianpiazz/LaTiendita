// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";

import type { Category } from "@/features/product/types";

import { CategoryTile } from "./category-tile";

const category: Category = {
  id: "1",
  slug: "tecnologia",
  name: "Tecnología",
  productCount: 24,
  featured: true,
};

describe("CategoryTile", () => {
  afterEach(() => {
    cleanup();
  });

  it("renders the category name, product count and link to the category page", () => {
    render(<CategoryTile category={category} />);

    expect(screen.getByRole("link", { name: /tecnología/i })).toHaveAttribute(
      "href",
      "/categorias/tecnologia",
    );
    expect(screen.getByText("Tecnología")).toBeInTheDocument();
    expect(screen.getByText("24 productos")).toBeInTheDocument();
  });

  it("uses singular label when the category has one product", () => {
    render(<CategoryTile category={{ ...category, productCount: 1 }} />);

    expect(screen.getByText("1 producto")).toBeInTheDocument();
  });
});
