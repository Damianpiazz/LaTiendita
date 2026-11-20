/**
 * Spanish three-letter month abbreviations, as the wireframe writes them
 * (`28 sep 2026`). CLDR abbreviates September as `sept`, so the table keeps the
 * copy exact instead of post-processing the `Intl` output.
 */
const monthAbbreviations = [
  "ene",
  "feb",
  "mar",
  "abr",
  "may",
  "jun",
  "jul",
  "ago",
  "sep",
  "oct",
  "nov",
  "dic",
];

/**
 * Formats an ISO date the way the wireframe shows it: `12 oct 2026`.
 *
 * The mock adapter sends a date-only value (`2026-10-12`) while review-service
 * sends a full timestamp (`2026-11-18T13:43:52Z`). Both are accepted.
 */
export function formatReviewDate(isoDate: string): string {
  const hasTime = isoDate.includes("T");
  const date = new Date(hasTime ? isoDate : `${isoDate}T00:00:00Z`);

  return `${date.getUTCDate()} ${monthAbbreviations[date.getUTCMonth()]} ${date.getUTCFullYear()}`;
}
