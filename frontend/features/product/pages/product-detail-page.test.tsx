// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";

import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import type { ProductDetail } from "@/features/product/types";
import type { Review, ReviewSummary } from "@/features/review/types";
import type { Product } from "@/features/product/types";

const mocks = vi.hoisted(() => ({
  findProductBySlug: vi.fn(),
  getRelatedProducts: vi.fn(),
  getReviewSummary: vi.fn(),
  getReviews: vi.fn(),
}));

vi.mock("@/features/product/services/catalog.service", () => ({
  findProductBySlug: mocks.findProductBySlug,
  getRelatedProducts: mocks.getRelatedProducts,
}));

vi.mock("@/features/review/services/review.service", () => ({
  getReviewSummary: mocks.getReviewSummary,
  getReviews: mocks.getReviews,
}));

vi.mock("next/navigation", () => ({
  notFound: vi.fn(),
  useRouter: () => ({ push: vi.fn() }),
}));

import { ProductDetailPage } from "./product-detail-page";

const product: ProductDetail = {
  id: "8",
  slug: "auriculares-inalambricos",
  name: "Auriculares inalámbricos",
  price: 25999,
  categoryId: "1",
  categoryName: "Tecnología",
  subcategory: "Audio",
  featured: true,
  sku: "NC-450",
  originalPrice: 32500,
  installmentCount: 6,
  stock: 5,
  fulfillment: "24/48 hs",
  description: ["Sonido claro con cancelación activa de ruido."],
  highlights: ["Bluetooth 5.3"],
  specs: [{ label: "Marca", value: "SoundCore" }],
  rating: 4.3,
  reviewCount: 6,
  soldCount: 200,
  galleryCount: 4,
};

const related: Product[] = [
  {
    id: "1",
    slug: "lampara-de-mesa",
    name: "Lámpara de mesa",
    price: 14500,
    categoryId: "2",
    categoryName: "Hogar",
    subcategory: "Iluminación",
    featured: false,
  },
];

const summary: ReviewSummary = {
  productId: product.id,
  average: 4.3,
  count: 6,
  distribution: { 1: 0, 2: 0, 3: 1, 4: 2, 5: 3 },
};

const review: Review = {
  id: "review-1",
  productId: product.id,
  author: "Cliente verificado",
  createdAt: "2026-11-18",
  rating: 5,
  title: "Excelente sonido",
  body: "La batería dura mucho.",
  verified: true,
  helpfulCount: 2,
  notHelpfulCount: 0,
};

async function renderPage() {
  render(await ProductDetailPage({ slug: product.slug }));
}

describe("ProductDetailPage tabs and reviews", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mocks.findProductBySlug.mockResolvedValue(product);
    mocks.getRelatedProducts.mockResolvedValue(related);
    mocks.getReviewSummary.mockResolvedValue(summary);
    mocks.getReviews.mockResolvedValue({ items: [review], page: 1, size: 2, total: 6 });
  });

  it("muestra solo dos tabs y la descripción por defecto", async () => {
    await renderPage();

    expect(screen.getAllByRole("tab")).toHaveLength(2);
    expect(screen.getByRole("tab", { name: "Descripción" })).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Especificaciones" })).toBeInTheDocument();
    expect(screen.queryByText("Envíos y devoluciones")).not.toBeInTheDocument();
    expect(screen.queryByText(/Reseñas \(\d+\)/, { selector: '[role="tab"]' })).not.toBeInTheDocument();
    expect(screen.getByText(product.description[0])).toBeInTheDocument();
    expect(screen.queryByText("SoundCore")).not.toBeInTheDocument();
  });

  it("muestra las especificaciones al cambiar de tab sin ocultar las reseñas", async () => {
    await renderPage();

    const reviewsHeading = screen.getByRole("heading", { name: "Reseñas (6)" });
    expect(reviewsHeading).toBeVisible();
    expect(screen.getByText(review.title)).toBeVisible();

    fireEvent.click(screen.getByRole("tab", { name: "Especificaciones" }));

    expect(screen.queryByText(product.description[0])).not.toBeInTheDocument();
    expect(screen.getByText("SoundCore")).toBeVisible();
    expect(reviewsHeading).toBeVisible();
    expect(screen.getByText(review.title)).toBeVisible();
  });
});
