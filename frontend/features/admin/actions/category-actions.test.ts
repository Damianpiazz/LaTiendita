import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const { revalidatePath } = vi.hoisted(() => ({ revalidatePath: vi.fn() }));

vi.mock("next/cache", () => ({ cacheLife: vi.fn(), revalidatePath }));

import { httpClient } from "@/lib/http/client";

import { setCategoryFeatured } from "./category-actions";

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
    expect(put).toHaveBeenCalledWith("/api/categories", {
      ...categoryDto,
      featured: true,
    });
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
