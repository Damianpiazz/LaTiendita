import type { AdminSalesDay } from "@/features/admin/types";

/** Tallest bar the frame draws; the series scales to it. */
export const CHART_MAX_HEIGHT = 110;

/** Sum of the seven daily amounts: the week total of the card subtitle. */
export function sumSales(days: AdminSalesDay[]): number {
  return days.reduce((total, day) => total + day.amount, 0);
}

/** Average ticket of the week, rounded the way the frame shows it. */
export function averageTicket(total: number, orders: number): number {
  return orders > 0 ? Math.round(total / orders) : 0;
}

/**
 * Bar height in pixels for one day.
 *
 * Heights are proportional to the series, not decorative: with the seeded
 * amounts they reproduce the frame's 71/90/62/101/85/110/78 exactly.
 */
export function barHeight(
  amount: number,
  maximum: number,
  maxHeight = CHART_MAX_HEIGHT,
): number {
  return maximum > 0 ? Math.round((amount / maximum) * maxHeight) : 0;
}

/** `12.4` → `12,4`, the es-AR decimal the copy uses. */
export function formatDelta(value: number): string {
  return value.toLocaleString("es-AR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });
}
