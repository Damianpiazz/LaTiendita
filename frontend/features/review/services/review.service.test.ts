import type { AxiosResponse } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("next/cache", () => ({ cacheLife: vi.fn() }));

const { envMock } = vi.hoisted(() => ({ envMock: { useMocks: false } }));
vi.mock("@/config/env", () => ({ env: envMock }));

import { httpClient } from "@/lib/http/client";

import { getReviewSummary, getReviews } from "./review.service";

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

describe("review service adapter", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    envMock.useMocks = false;
  });

  it("maps the real ReviewSummaryDto into the frontend ReviewSummary", async () => {
    const get = vi.spyOn(httpClient, "get");
    get.mockImplementationOnce(async () =>
      response({
        productId: 8,
        average: "4.33",
        reviewCount: 6,
        distribution: { "1": 0, "2": 0, "3": 1, "4": 2, "5": 3 },
      }),
    );

    await expect(getReviewSummary("8")).resolves.toEqual({
      productId: "8",
      average: 4.33,
      count: 6,
      distribution: { 1: 0, 2: 0, 3: 1, 4: 2, 5: 3 },
    });
    expect(get).toHaveBeenCalledWith("/api/reviews/summary", { params: { productId: "8" } });
  });

  it("keeps the mock summary unchanged", async () => {
    envMock.useMocks = true;
    const mockSummary = {
      productId: "auriculares-inalambricos",
      average: 4.8,
      count: 124,
      distribution: { 5: 96, 4: 18, 3: 6, 2: 3, 1: 1 },
    };
    const get = vi.spyOn(httpClient, "get");
    get.mockImplementationOnce(async () => response(mockSummary));

    await expect(getReviewSummary("auriculares-inalambricos")).resolves.toEqual(mockSummary);
  });

  it("maps a Spring Page of reviews into ReviewPage and requests a 0-based page", async () => {
    const get = vi.spyOn(httpClient, "get");
    get.mockImplementationOnce(async () =>
      response({
        content: [
          {
            id: "11111111-1111-4111-8111-111111111111",
            productId: 8,
            customerId: "seed-cust-01",
            orderId: 9001,
            rating: 5,
            title: "Excelente cancelación de ruido",
            body: "Funciona increíble en el subte.",
            status: "approved",
            helpfulVotes: 12,
            moderationReason: null,
            createdAt: "2026-11-18T13:43:52.746797Z",
            updatedAt: "2026-11-18T13:43:52.746797Z",
          },
        ],
        number: 0,
        size: 2,
        totalElements: 6,
      }),
    );

    await expect(getReviews("8", { page: 1, size: 2 })).resolves.toEqual({
      items: [
        {
          id: "11111111-1111-4111-8111-111111111111",
          productId: "8",
          author: "Cliente verificado",
          createdAt: "2026-11-18T13:43:52.746797Z",
          rating: 5,
          title: "Excelente cancelación de ruido",
          body: "Funciona increíble en el subte.",
          verified: true,
          helpfulCount: 12,
          notHelpfulCount: 0,
        },
      ],
      page: 1,
      size: 2,
      total: 6,
    });

    expect(get).toHaveBeenCalledWith("/api/reviews", {
      params: { productId: "8", page: 0, size: 2 },
    });
  });

  it("keeps the mock review page unchanged and sends its 1-based page as-is", async () => {
    envMock.useMocks = true;
    const mockPage = {
      items: [
        {
          id: "review-maria-g",
          productId: "auriculares-inalambricos",
          author: "María G.",
          createdAt: "2026-10-12",
          rating: 5,
          title: "Excelente sonido",
          body: "La batería dura lo que dice.",
          verified: true,
          helpfulCount: 18,
          notHelpfulCount: 2,
        },
      ],
      page: 1,
      size: 2,
      total: 124,
    };
    const get = vi.spyOn(httpClient, "get");
    get.mockImplementationOnce(async () => response(mockPage));

    await expect(getReviews("auriculares-inalambricos", { page: 1, size: 2 })).resolves.toEqual(
      mockPage,
    );
    expect(get).toHaveBeenCalledWith("/reviews", {
      params: { productId: "auriculares-inalambricos", page: 1, size: 2 },
    });
  });
});
