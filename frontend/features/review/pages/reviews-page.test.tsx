// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { ProductDetail } from "@/features/product/types";
import type { Review } from "@/features/review/types";

const mocks = vi.hoisted(() => ({
  findProductBySlug: vi.fn(),
  getReviewSummary: vi.fn(),
  getReviews: vi.fn(),
}));

vi.mock("@/features/product/services/catalog.service", () => ({
  findProductBySlug: mocks.findProductBySlug,
}));

vi.mock("@/features/review/services/review.service", () => ({
  getReviewSummary: mocks.getReviewSummary,
  getReviews: mocks.getReviews,
}));

vi.mock("next/navigation", () => ({
  notFound: vi.fn(),
}));

import { ReviewsPage } from "./reviews-page";

const product = {
  id: "auriculares-inalambricos",
  slug: "auriculares-inalambricos",
  name: "Auriculares inalámbricos",
} as ProductDetail;

const reviews: Review[] = Array.from({ length: 6 }, (_, index) => ({
  id: `review-${index + 1}`,
  productId: product.id,
  author: `Cliente ${index + 1}`,
  createdAt: "2026-10-12",
  rating: 5,
  title: `Reseña ${index + 1}`,
  body: "Muy buen producto.",
  verified: true,
  helpfulCount: 1,
  notHelpfulCount: 0,
}));

describe("ReviewsPage", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mocks.findProductBySlug.mockResolvedValue(product);
    mocks.getReviewSummary.mockResolvedValue({
      productId: product.id,
      average: 4.8,
      count: 12,
      distribution: { 1: 0, 2: 0, 3: 0, 4: 2, 5: 10 },
    });
    mocks.getReviews.mockResolvedValue({ items: reviews, page: 1, size: 6, total: 12 });
  });

  it("muestra el resumen, las reseñas de la página y enlaces de paginación reales", async () => {
    render(await ReviewsPage({ slug: product.slug, page: 1 }));

    expect(screen.getByRole("heading", { name: "Reseñas (12)" })).toBeInTheDocument();
    expect(screen.getByText("Reseña 1")).toBeInTheDocument();
    expect(screen.getByText("Reseña 6")).toBeInTheDocument();

    expect(mocks.getReviews).toHaveBeenCalledWith(product.id, { page: 1, size: 6 });

    const nextPage = screen.getByRole("link", { name: "2" });
    expect(nextPage).toHaveAttribute("href", "/productos/auriculares-inalambricos/resenas?page=2");

    const backLink = screen.getByRole("link", { name: product.name });
    expect(backLink).toHaveAttribute("href", "/productos/auriculares-inalambricos");
  });
});
