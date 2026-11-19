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

/** Formats an ISO date (`2026-10-12`) the way the wireframe shows it: `12 oct 2026`. */
export function formatReviewDate(isoDate: string): string {
  const date = new Date(`${isoDate}T00:00:00Z`);

  return `${date.getUTCDate()} ${monthAbbreviations[date.getUTCMonth()]} ${date.getUTCFullYear()}`;
}
