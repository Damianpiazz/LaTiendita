import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("next/cache", () => ({ cacheLife: vi.fn() }));

import { httpClient } from "@/lib/http/client";
import { mockAdapter } from "@/mocks";

import {
  findCategoryBySlug,
  findProductBySlug,
  getCategories,
  getCategoryBySlug,
  getCategoryProducts,
  getFeaturedProducts,
  getProductBySlug,
  getProducts,
  getRelatedProducts,
} from "./catalog.service";

function response<T>(data: T): AxiosResponse<T> {
  return {
    data,
    status: 200,
    statusText: "OK",
    headers: {},
    config: { headers: {} },
    request: {},
  } as AxiosResponse<T>;
}

describe("catalog service adapter", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("unwraps Page.content into arrays", async () => {
    const get = vi.spyOn(httpClient, "get");
    get.mockImplementationOnce(async () =>
      response({
        content: [
          {
            categoryId: "1",
            name: "Tecnología",
            description: "Auriculares",
            slug: "tecnologia",
            featured: true,
            productCount: 24,
          },
        ],
        totalElements: 1,
      }) as never,
    );

    expect(await getCategories({ featured: true })).toHaveLength(1);

    get.mockImplementationOnce(async () =>
      response({
        content: [
          {
            productId: "10",
            title: "Auriculares",
            price: "25999",
            categoryId: "1",
            slug: "auriculares",
            featured: true,
          },
        ],
        totalElements: 1,
      }) as never,
    );

    expect(await getProducts()).toHaveLength(1);
  });

  it("maps category DTO fields into the screen type", async () => {
    vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({
        content: [
          {
            categoryId: 7,
            name: "Hogar",
            description: "Artículos del hogar",
            slug: "hogar",
            featured: true,
            productCount: 18,
          },
        ],
      }) as never,
    );

    expect(await getCategories()).toEqual([
      {
        id: "7",
        name: "Hogar",
        description: "Artículos del hogar",
        slug: "hogar",
        featured: true,
        productCount: 18,
      },
    ]);
  });

  it("maps product and detail DTO fields into the screen types", async () => {
    vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({
        content: [
          {
            productId: 42,
            title: "Auriculares NC-450",
            price: "25999.50",
            categoryId: 7,
            categoryName: "Tecnología",
            subcategory: "Audio",
            slug: "auriculares-nc-450",
            featured: true,
          },
        ],
      }) as never,
    );

    expect(await getProducts()).toEqual([
      {
        id: "42",
        name: "Auriculares NC-450",
        price: 25999.5,
        categoryId: "7",
        categoryName: "Tecnología",
        subcategory: "Audio",
        slug: "auriculares-nc-450",
        featured: true,
      },
    ]);

    vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({
        productId: 42,
        title: "Auriculares NC-450",
        price: "25999",
        originalPrice: "32500",
        quantity: 24,
        categoryId: 7,
        categoryName: "Tecnología",
        subcategory: "Audio",
        slug: "auriculares-nc-450",
        featured: true,
        sku: "0042",
        fulfillment: "24/48 hs",
        images: ["one", "two", "three"],
        description: ["Detalle"],
        highlights: ["ANC"],
        specs: [{ label: "Marca", value: "SoundCore" }],
      }) as never,
    );

    expect(await getProductBySlug("auriculares-nc-450")).toEqual({
      id: "42",
      name: "Auriculares NC-450",
      price: 25999,
      categoryId: "7",
      categoryName: "Tecnología",
      subcategory: "Audio",
      slug: "auriculares-nc-450",
      featured: true,
      sku: "0042",
      originalPrice: 32500,
      installmentCount: 1,
      stock: 24,
      fulfillment: "24/48 hs",
      description: ["Detalle"],
      highlights: ["ANC"],
      specs: [{ label: "Marca", value: "SoundCore" }],
      rating: 0,
      reviewCount: 0,
      soldCount: 0,
      galleryCount: 3,
    });
  });

  it("keeps the existing service functions working through the mock adapter", async () => {
    // The default transport is real; opt this regression check into the mock
    // adapter explicitly so it does not depend on the local .env.
    const originalAdapter = httpClient.defaults.adapter;
    httpClient.defaults.adapter = mockAdapter;

    try {
      expect((await getCategories({ featured: true })).map((item) => item.slug)).toEqual([
        "tecnologia",
        "hogar",
        "ropa",
        "deportes",
        "juguetes",
      ]);

      const featured = await getFeaturedProducts();
      expect(featured).toHaveLength(8);
      expect(featured[0]).toMatchObject({
        name: "Auriculares inalámbricos",
        price: 25999,
      });
      expect((await getProducts()).length).toBeGreaterThan(featured.length);
      expect(
        (await getCategoryProducts("tecnologia")).every((item) => item.categoryId === "tecnologia"),
      ).toBe(true);
      expect(await getRelatedProducts(3)).toHaveLength(3);

      expect((await getCategoryBySlug("tecnologia")).name).toBe("Tecnología");
      expect(await findCategoryBySlug("missing")).toBeNull();

      expect((await getProductBySlug("auriculares-inalambricos")).sku).toBe("0042");
      expect(await findProductBySlug("missing")).toBeNull();
    } finally {
      httpClient.defaults.adapter = originalAdapter;
    }
  });
});
