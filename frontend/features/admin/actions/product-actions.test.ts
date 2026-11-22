import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { revalidatePath } = vi.hoisted(() => ({ revalidatePath: vi.fn() }));

vi.mock("next/cache", () => ({ cacheLife: vi.fn(), revalidatePath }));
vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) =>
      name === "lt_access_token" ? { value: "admin-token" } : undefined,
  }),
}));

import { httpClient } from "@/lib/http/client";

import {
  createAdminProduct,
  deleteAdminProducts,
  exportAdminProducts,
  getAdminProductImages,
  listAdminProducts,
  publishAdminProducts,
  updateAdminProduct,
  type AdminProductPayload,
} from "./product-actions";

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

const newProduct: AdminProductPayload = {
  title: "Auriculares inalámbricos",
  sku: "AUR-INL-NEG",
  categoryId: "7",
  price: 49999,
  quantity: 12,
  description: "Sonido bluetooth con estuche.",
  images: ["/images/auriculares-1.jpg", "/images/auriculares-2.jpg"],
  published: true,
};

describe("admin product actions", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    revalidatePath.mockClear();
  });

  it("creates a product through the catalog endpoint with the bearer token", async () => {
    const post = vi
      .spyOn(httpClient, "post")
      .mockImplementationOnce(async () => response({ productId: 42 }) as never);

    await createAdminProduct(newProduct);

    expect(post).toHaveBeenCalledWith(
      "/api/products",
      {
        title: "Auriculares inalámbricos",
        sku: "AUR-INL-NEG",
        categoryId: 7,
        price: 49999,
        quantity: 12,
        description: ["Sonido bluetooth con estuche."],
        images: ["/images/auriculares-1.jpg", "/images/auriculares-2.jpg"],
        published: true,
      },
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(revalidatePath).toHaveBeenCalledWith("/admin/productos");
  });

  it("edits a product through the id-scoped endpoint and revalidates", async () => {
    const put = vi
      .spyOn(httpClient, "put")
      .mockImplementationOnce(async () => response({ productId: 42 }) as never);

    await updateAdminProduct({ ...newProduct, productId: "42", published: false });

    expect(put).toHaveBeenCalledWith(
      "/api/products/42",
      expect.objectContaining({
        productId: 42,
        published: false,
        images: ["/images/auriculares-1.jpg", "/images/auriculares-2.jpg"],
      }),
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(revalidatePath).toHaveBeenCalledWith("/admin/productos");
  });

  it("publishes and archives a batch through the status endpoint", async () => {
    const post = vi
      .spyOn(httpClient, "post")
      .mockImplementation(async () => response(undefined) as never);

    await publishAdminProducts(["42", "43"], true);
    await publishAdminProducts(["44"], false);

    expect(post).toHaveBeenNthCalledWith(
      1,
      "/api/products/admin/status",
      { productIds: [42, 43], published: true },
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(post).toHaveBeenNthCalledWith(
      2,
      "/api/products/admin/status",
      { productIds: [44], published: false },
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(revalidatePath).toHaveBeenCalledTimes(2);
  });

  it("deletes a batch with a request body and the bearer token", async () => {
    const del = vi
      .spyOn(httpClient, "delete")
      .mockImplementationOnce(async () => response(undefined) as never);

    await deleteAdminProducts(["42", "43"]);

    expect(del).toHaveBeenCalledWith("/api/products/admin", {
      data: { productIds: [42, 43] },
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(revalidatePath).toHaveBeenCalledWith("/admin/productos");
  });

  it("exports the filtered CSV as text with the bearer token", async () => {
    const get = vi
      .spyOn(httpClient, "get")
      .mockImplementationOnce(async () => response("sku,name\nAUR-INL-NEG,Auriculares\n") as never);

    const result = await exportAdminProducts({
      q: "auriculares",
      status: "publicado",
      categoryId: "7",
      sort: "id,desc",
    });

    expect(get).toHaveBeenCalledWith("/api/products/admin/export.csv", {
      params: {
        q: "auriculares",
        status: "publicado",
        categoryId: "7",
        sort: "id,desc",
      },
      headers: {
        Authorization: "Bearer admin-token",
        Accept: "text/csv",
      },
      responseType: "text",
    });
    expect(result.filename).toBe("productos.csv");
    expect(result.content).toContain("AUR-INL-NEG");
  });

  it("refreshes the real list through the adapter", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementation(async (url: string) => {
      if (url === "/api/products/admin") {
        return response({ content: [] }) as never;
      }
      if (url === "/api/products/admin/summary") {
        return response({
          totalProducts: 0,
          totalCategories: 0,
          totalLowStock: 0,
          lowStockThreshold: 5,
        }) as never;
      }
      throw new Error(`Unexpected URL ${url}`);
    });

    const result = await listAdminProducts({ status: "agotado", page: 3 });

    expect(get).toHaveBeenCalledWith("/api/products/admin", {
      params: { status: "agotado", page: 2, size: 8, sort: "id,desc" },
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(result.items).toEqual([]);
  });

  it("loads the edit gallery from the real product detail with the bearer token", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({
        images: ["/images/detalle-1.jpg", "/images/detalle-2.jpg"],
        imageUrl: "/images/legacy.jpg",
      }) as never,
    );

    const images = await getAdminProductImages("42");

    expect(get).toHaveBeenCalledWith("/api/products/42", {
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(images).toEqual(["/images/detalle-1.jpg", "/images/detalle-2.jpg"]);
    expect(revalidatePath).not.toHaveBeenCalled();
  });

  it("falls back to the legacy image when the detail has no gallery", async () => {
    vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({ imageUrl: "/images/legacy.jpg" }) as never,
    );

    await expect(getAdminProductImages("42")).resolves.toEqual([
      "/images/legacy.jpg",
    ]);
  });

  it("does not call the API or revalidate for an empty batch", async () => {
    const post = vi.spyOn(httpClient, "post");

    await publishAdminProducts([], true);

    expect(post).not.toHaveBeenCalled();
    expect(revalidatePath).not.toHaveBeenCalled();
  });
});
