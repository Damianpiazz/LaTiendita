import {
  AxiosError,
  type AxiosAdapter,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from "axios";

export type MockRouteContext = {
  searchParams: URLSearchParams;
  /** Dynamic segments captured from the route pattern, e.g. `{ slug: "..." }`. */
  params: Record<string, string>;
};

export type MockRoute = {
  method: "GET" | "POST";
  /** Static path or a pattern with `:param` segments, e.g. `/products/:slug`. */
  path: string;
  handler: (context: MockRouteContext) => unknown;
};

function resolveBaseUrl(baseUrl: string | undefined): string {
  return baseUrl && /^https?:\/\//.test(baseUrl) ? baseUrl : "http://mock.local";
}

/** Matches a pattern like `/products/:slug` against a pathname, capturing its params. */
function matchPath(pattern: string, pathname: string): Record<string, string> | null {
  const patternSegments = pattern.split("/").filter(Boolean);
  const pathSegments = pathname.split("/").filter(Boolean);

  if (patternSegments.length !== pathSegments.length) {
    return null;
  }

  const params: Record<string, string> = {};

  for (const [index, segment] of patternSegments.entries()) {
    const pathSegment = pathSegments[index];
    const capture = /^:(.+)$/.exec(segment);

    if (capture) {
      params[capture[1]] = decodeURIComponent(pathSegment);
      continue;
    }

    if (segment !== pathSegment) {
      return null;
    }
  }

  return params;
}

function buildResponse<T>(
  config: InternalAxiosRequestConfig,
  data: T,
  status: number,
): AxiosResponse<T> {
  return {
    data,
    status,
    statusText: status === 200 ? "OK" : "Error",
    headers: {},
    config,
    request: {},
  };
}

/**
 * Axios adapter backed by an in-memory route table.
 *
 * It keeps the service layer written against the real API contract: turning
 * `NEXT_PUBLIC_USE_MOCKS` off only swaps the adapter for the network one.
 * Patterns may carry `:param` segments so detail routes mirror REST paths.
 */
export function createMockAdapter(routes: MockRoute[]): AxiosAdapter {
  return async (config) => {
    const url = new URL(config.url ?? "/", resolveBaseUrl(config.baseURL));
    const searchParams = new URLSearchParams(url.search);

    for (const [key, value] of Object.entries(config.params ?? {})) {
      if (value !== undefined && value !== null) {
        searchParams.set(key, String(value));
      }
    }

    const method = (config.method ?? "get").toUpperCase();

    for (const candidate of routes) {
      if (candidate.method !== method) {
        continue;
      }

      const params = matchPath(candidate.path, url.pathname);

      if (!params) {
        continue;
      }

      const data = candidate.handler({ searchParams, params });

      if (data === undefined) {
        throw new AxiosError(
          `Mock resource not found: ${method} ${url.pathname}`,
          AxiosError.ERR_BAD_REQUEST,
          config,
          {},
          buildResponse(config, { message: "Mock resource not found" }, 404),
        );
      }

      return buildResponse(config, data, 200);
    }

    throw new AxiosError(
      `Mock route not found: ${method} ${url.pathname}`,
      AxiosError.ERR_BAD_REQUEST,
      config,
      {},
      buildResponse(config, { message: "Mock route not found" }, 404),
    );
  };
}
