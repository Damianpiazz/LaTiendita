/** Date ranges shared by the admin orders and payments screens. */
export const ADMIN_RANGE_OPTIONS = [
  { days: 7, label: "Últimos 7 días" },
  { days: 30, label: "Últimos 30 días" },
  { days: 90, label: "Últimos 90 días" },
] as const;

export const ADMIN_RANGE_DEFAULT_DAYS = 30;

export function adminRangeLabel(days: number): string {
  return `Últimos ${days} días`;
}

/** Accepts only the ranges the UI and the backend adapters understand. */
export function parseAdminRangeDays(value: string | undefined): number | undefined {
  if (!value) return undefined;
  const days = Number(value);
  return ADMIN_RANGE_OPTIONS.some((option) => option.days === days) ? days : undefined;
}
