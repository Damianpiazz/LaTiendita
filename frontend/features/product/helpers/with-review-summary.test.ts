import { describe, expect, it } from "vitest";

import type { ProductDetail } from "@/features/product/types";
import type { ReviewSummary } from "@/features/review/types";

import { withReviewSummary } from "./with-review-summary";

const product: ProductDetail = {
  id: "8",
  name: "Auriculares inalámbricos",
  price: 25999,
  categoryId: "9",
  categoryName: "Tecnología",
  subcategory: "Audio",
  slug: "auriculares-inalambricos",
  featured: true,
  sku: "SEED-001",
  originalPrice: 32500,
  installmentCount: 1,
  stock: 20,
  fulfillment: "24/48 hs",
  description: ["Descripción real"],
  highlights: ["Resalte real"],
  specs: [{ label: "Marca", value: "SoundCore" }],
  rating: 0,
  reviewCount: 0,
  soldCount: 0,
  galleryCount: 4,
};

const summary: ReviewSummary = {
  productId: "8",
  average: 4.33,
  count: 6,
  distribution: { 1: 0, 2: 0, 3: 1, 4: 2, 5: 3 },
};

describe("withReviewSummary", () => {
  it("replaces the catalog defaults with the real review summary", () => {
    const result = withReviewSummary(product, summary);

    expect(result.rating).toBe(4.33);
    expect(result.reviewCount).toBe(6);
    expect(result).toEqual({ ...product, rating: 4.33, reviewCount: 6 });
  });

  it("applies an empty summary so products without reviews stay at zero", () => {
    const empty: ReviewSummary = {
      productId: "7",
      average: 0,
      count: 0,
      distribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 },
    };

    expect(withReviewSummary(product, empty)).toEqual({
      ...product,
      rating: 0,
      reviewCount: 0,
    });
  });
});
