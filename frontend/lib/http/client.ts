import axios from "axios";

import { env } from "@/config/env";
import { mockAdapter } from "@/mocks";

/**
 * Shared axios instance.
 *
 * Services are written against the real API; the only thing mock mode swaps is
 * the transport adapter.
 */
export const httpClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { Accept: "application/json" },
  ...(env.useMocks ? { adapter: mockAdapter } : {}),
});
