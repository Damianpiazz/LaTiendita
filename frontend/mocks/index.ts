import { createMockAdapter } from "./adapter";
import { mockRoutes } from "./routes";

/** Axios adapter that answers from the in-memory seed data in `mocks/data`. */
export const mockAdapter = createMockAdapter(mockRoutes);
