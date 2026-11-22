import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { state, revalidatePath } = vi.hoisted(() => ({
  state: { useMocks: false, token: "t3-token" as string | null },
  revalidatePath: vi.fn(),
}));

vi.mock("next/cache", () => ({ cacheLife: vi.fn(), revalidatePath }));
vi.mock("next/headers", () => ({
  cookies: async () => ({
    get: (name: string) =>
      name === "lt_access_token" && state.token ? { value: state.token } : undefined,
  }),
}));
vi.mock("@/config/env", () => ({
  env: { apiBaseUrl: "http://localhost:8081", get useMocks() { return state.useMocks; } },
}));

import { httpClient } from "@/lib/http/client";
import { getAdminCategories } from "@/features/admin/services/admin.service";
import {
  createAdminCategory,
  deleteAdminCategory,
  listAdminCategories,
  setCategoryActive,
  updateAdminCategory,
} from "@/features/admin/actions/category-actions";

function res<T>(data: T): AxiosResponse<T> {
  return { data, status: 200, statusText: "OK", headers: {}, config: { headers: {} }, request: {} } as AxiosResponse<T>;
}

const payload = {
  name:  "  Remeras  ", slug: "  remeras  ", description: "  desc  ",
  featured: true, active: false, imageUrl: "   ",
};

beforeEach(() => {
  vi.restoreAllMocks();
  revalidatePath.mockClear();
  state.useMocks = false;
  state.token = "t3-token";
});

describe("T3 S4 adapter probes", () => {
  it("never uses the mock route even with useMocks forced on", async () => {
    state.useMocks = true;
    const get = vi.spyOn(httpClient, "get").mockResolvedValueOnce(res({ content: [], totalElements: 0 }) as never);
    await getAdminCategories();
    expect(get).toHaveBeenCalledTimes(1);
    expect(get).toHaveBeenCalledWith("/api/categories", expect.anything());
    expect(get.mock.calls[0][0]).not.toContain("/admin/categories");
  });

  it("maps defaults, numeric coercion and total fallback", async () => {
    vi.spyOn(httpClient, "get").mockResolvedValueOnce(res({
      content: [
        { categoryId: 7, name: "Hogar", productCount: "18", featured: 1 },
        { categoryId: 8, name: "Vacía" },
      ],
    }) as never);
    const out = await getAdminCategories();
    expect(out.items[0]).toEqual({
      id: "7", name: "Hogar", slug: undefined, description: "",
      productCount: 18, lowStockCount: 0, featured: true, active: true, imageUrl: null,
    });
    expect(out.items[1].active).toBe(true);
    expect(out.items[1].lowStockCount).toBe(0);
    expect(out.total).toBe(2);
  });

  it("omits empty q, forwards false featured, clamps page and forwards size/sort", async () => {
    const get = vi.spyOn(httpClient, "get").mockResolvedValueOnce(res({ content: [] }) as never);
    await getAdminCategories({ q: "", featured: false, page: 0, size: 6, sort: "name,desc" });
    expect(get).toHaveBeenCalledWith("/api/categories", {
      params: { page: 0, sort: "name,desc", featured: false, size: 6 },
      headers: { Authorization: "Bearer t3-token" },
    });
    const get2 = vi.spyOn(httpClient, "get").mockResolvedValueOnce(res({ content: [] }) as never);
    await getAdminCategories({ page: -4 });
    expect(get2.mock.calls[0][1]?.params).toMatchObject({ page: 0 });
  });

  it("sends no Authorization key when the cookie is absent", async () => {
    state.token = null;
    const get = vi.spyOn(httpClient, "get").mockResolvedValueOnce(res({ content: [] }) as never);
    await getAdminCategories();
    expect(get.mock.calls[0][1]?.headers).toEqual({});
  });
});

describe("T3 S5 action probes", () => {
  it("create/update/toggle/passthrough all carry Bearer and revalidate the same path", async () => {
    const post = vi.spyOn(httpClient, "post").mockResolvedValueOnce(res({}) as never);
    await createAdminCategory(payload);
    expect(post.mock.calls[0][1]).toMatchObject({
      name: "Remeras", slug: "remeras", description: "desc", active: false, imageUrl: null,
    });
    expect(post.mock.calls[0][2]).toEqual({ headers: { Authorization: "Bearer t3-token" } });

    const put = vi.spyOn(httpClient, "put").mockResolvedValueOnce(res({}) as never);
    await updateAdminCategory({ ...payload, categoryId: "7" });
    expect(put.mock.calls[0][0]).toBe("/api/categories/7");
    expect(put.mock.calls[0][2]).toEqual({ headers: { Authorization: "Bearer t3-token" } });

    vi.spyOn(httpClient, "get").mockResolvedValueOnce(res({
      categoryId: "7", name: "Hogar", slug: "hogar", description: "d",
      featured: true, imageUrl: "/i.png", active: true,
    }) as never);
    put.mockClear();
    put.mockResolvedValueOnce(res({}) as never);
    await setCategoryActive("7", false);
    expect(put.mock.calls[0][1]).toEqual({
      categoryId: "7", name: "Hogar", slug: "hogar", description: "d",
      featured: true, active: false, imageUrl: "/i.png",
    });
    expect(revalidatePath.mock.calls.map((c) => c[0])).toEqual(
      Array(3).fill("/admin/categorias"),
    );
  });

  it("distinguishes 204 from 409, keeps ProblemDetail visible and never revalidates a rejection", async () => {
    const ok = vi.spyOn(httpClient, "delete").mockResolvedValueOnce(res(undefined) as never);
    await expect(deleteAdminCategory("7")).resolves.toEqual({ ok: true });
    expect(ok.mock.calls[0][1]).toEqual({ headers: { Authorization: "Bearer t3-token" } });
    expect(revalidatePath).toHaveBeenCalledTimes(1);

    revalidatePath.mockClear();
    vi.spyOn(httpClient, "delete").mockRejectedValueOnce(Object.assign(new Error("c"), {
      isAxiosError: true, response: { status: 409, data: { detail: "  409: 12 productos  " } },
    }) as never);
    await expect(deleteAdminCategory("7")).resolves.toEqual({ ok: false, error: "409: 12 productos" });
    expect(revalidatePath).not.toHaveBeenCalled();

    vi.spyOn(httpClient, "delete").mockRejectedValueOnce(Object.assign(new Error("c"), {
      isAxiosError: true, response: { status: 409, data: {} },
    }) as never);
    const fallback = await deleteAdminCategory("7");
    expect(fallback.ok).toBe(false);
    expect((fallback as { error: string }).error).toContain("productos");

    revalidatePath.mockClear();
    vi.spyOn(httpClient, "delete").mockRejectedValueOnce(Object.assign(new Error("boom"), {
      isAxiosError: true, response: { status: 500, data: { detail: "x" } },
    }) as never);
    await expect(deleteAdminCategory("7")).rejects.toThrow("boom");
    expect(revalidatePath).not.toHaveBeenCalled();
  });

  it("guards empty ids without touching the API", async () => {
    const post = vi.spyOn(httpClient, "post");
    const put = vi.spyOn(httpClient, "put");
    const del = vi.spyOn(httpClient, "delete");
    await deleteAdminCategory("");
    await expect(listAdminCategories).toBeDefined();
    expect(post).not.toHaveBeenCalled();
    expect(put).not.toHaveBeenCalled();
    expect(del).not.toHaveBeenCalled();
    expect(revalidatePath).not.toHaveBeenCalled();
  });
});
