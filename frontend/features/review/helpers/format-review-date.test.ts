import { describe, expect, it } from "vitest";

import { formatReviewDate } from "./format-review-date";

describe("formatReviewDate", () => {
  it("formats a date-only value from the mock adapter", () => {
    expect(formatReviewDate("2026-10-12")).toBe("12 oct 2026");
  });

  it("formats a full ISO timestamp from review-service", () => {
    expect(formatReviewDate("2026-11-18T13:43:52.746797Z")).toBe("18 nov 2026");
  });

  it("formats a full ISO timestamp with a negative offset", () => {
    expect(formatReviewDate("2026-09-28T23:30:00-03:00")).toBe("29 sep 2026");
  });
});
