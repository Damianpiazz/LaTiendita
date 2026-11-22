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
  createAdminCategory,
  deleteAdminCategory,
  listAdminCategories,
  setCategoryActive,
  setCategoryFeatured,
  updateAdminCategory,
  type AdminCategoryPayload,
} from "./category-actions";

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

const categoryDto = {
  categoryId: "7",
  name: "Hogar",
  description: "Artículos del hogar",
  slug: "hogar",
  featured: false,
  productCount: 18,
};

describe("setCategoryFeatured", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    revalidatePath.mockClear();
  });

  it("reads the category from the public endpoint and updates it through the catalog API", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () => response(categoryDto) as never);
    const put = vi.spyOn(httpClient, "put").mockImplementationOnce(async () => response(categoryDto) as never);

    const formData = new FormData();
    formData.set("categoryId", "7");
    formData.set("featured", "true");

    await setCategoryFeatured(formData);

    expect(get).toHaveBeenCalledWith("/api/categories/7");
    expect(put).toHaveBeenCalledWith(
      "/api/categories",
      {
        ...categoryDto,
        featured: true,
      },
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(revalidatePath).toHaveBeenCalledWith("/admin/categorias");
    expect(revalidatePath).toHaveBeenCalledWith("/");
  });

  it("keeps the empty-id guard without calling the API", async () => {
    const get = vi.spyOn(httpClient, "get");
    const put = vi.spyOn(httpClient, "put");

    const formData = new FormData();
    formData.set("categoryId", "");
    formData.set("featured", "true");

    await setCategoryFeatured(formData);

    expect(get).not.toHaveBeenCalled();
    expect(put).not.toHaveBeenCalled();
  });
});

const payload: AdminCategoryPayload = {
  name: "Remeras",
  slug: "remeras",
  description: "Remeras y polos.",
  featured: false,
  active: true,
  imageUrl: "/images/remeras.jpg",
};

describe("admin category actions", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    revalidatePath.mockClear();
  });

  it("delegates the filtered list to the real adapter", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({ content: [], totalElements: 0 }) as never,
    );

    await listAdminCategories({ q: "remer", page: 2, size: 6, sort: "name,desc" });

    expect(get).toHaveBeenCalledWith("/api/categories", {
      params: { q: "remer", page: 1, size: 6, sort: "name,desc" },
      headers: { Authorization: "Bearer admin-token" },
    });
  });

  it("creates a category with the bearer token and revalidates the screen", async () => {
    const post = vi.spyOn(httpClient, "post").mockImplementationOnce(async () =>
      response({ categoryId: "7" }) as never,
    );

    await createAdminCategory(payload);

    expect(post).toHaveBeenCalledWith(
      "/api/categories",
      {
        categoryId: undefined,
        name: "Remeras",
        slug: "remeras",
        description: "Remeras y polos.",
        featured: false,
        active: true,
        imageUrl: "/images/remeras.jpg",
      },
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(revalidatePath).toHaveBeenCalledWith("/admin/categorias");
  });

  it("edits a category through the id-scoped endpoint with the bearer token", async () => {
    const put = vi.spyOn(httpClient, "put").mockImplementationOnce(async () =>
      response({ categoryId: "7" }) as never,
    );

    await updateAdminCategory({ ...payload, categoryId: "7", active: false });

    expect(put).toHaveBeenCalledWith(
      "/api/categories/7",
      expect.objectContaining({
        categoryId: "7",
        name: "Remeras",
        slug: "remeras",
        active: false,
        imageUrl: "/images/remeras.jpg",
      }),
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(revalidatePath).toHaveBeenCalledWith("/admin/categorias");
  });

  it("deletes a category with the bearer token and revalidates on 204", async () => {
    const del = vi.spyOn(httpClient, "delete").mockImplementationOnce(async () =>
      response(undefined) as never,
    );

    await expect(deleteAdminCategory("7")).resolves.toEqual({ ok: true });

    expect(del).toHaveBeenCalledWith("/api/categories/7", {
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(revalidatePath).toHaveBeenCalledWith("/admin/categorias");
  });

  it("reports the 409 guard without revalidating a failed delete", async () => {
    const del = vi.spyOn(httpClient, "delete").mockRejectedValueOnce(
      Object.assign(new Error("Conflict"), {
        isAxiosError: true,
        response: {
          status: 409,
          data: { detail: "La categoría tiene productos asociados." },
        },
      }) as never,
    );

    const result = await deleteAdminCategory("7");

    expect(result).toEqual({
      ok: false,
      error: "La categoría tiene productos asociados.",
    });
    expect(del).toHaveBeenCalledWith("/api/categories/7", {
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(revalidatePath).not.toHaveBeenCalled();
  });

  it("toggles active from the real detail while preserving the stored slug", async () => {
    const get = vi.spyOn(httpClient, "get").mockImplementationOnce(async () =>
      response({ ...categoryDto, active: true, imageUrl: "/images/hogar.jpg" }) as never,
    );
    const put = vi.spyOn(httpClient, "put").mockImplementationOnce(async () =>
      response({ categoryId: "7" }) as never,
    );

    await setCategoryActive("7", false);

    expect(get).toHaveBeenCalledWith("/api/categories/7", {
      headers: { Authorization: "Bearer admin-token" },
    });
    expect(put).toHaveBeenCalledWith(
      "/api/categories/7",
      {
        categoryId: "7",
        name: "Hogar",
        slug: "hogar",
        description: "Artículos del hogar",
        featured: false,
        active: false,
        imageUrl: "/images/hogar.jpg",
      },
      { headers: { Authorization: "Bearer admin-token" } },
    );
    expect(revalidatePath).toHaveBeenCalledWith("/admin/categorias");
  });
});
