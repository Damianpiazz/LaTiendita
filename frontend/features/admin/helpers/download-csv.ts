/** Value of one CSV cell; numbers are written without currency formatting. */
export type CsvCell = string | number | boolean | null | undefined;

/**
 * Exports the rows already loaded by an admin table. Only the visible page is
 * exported, which matches the data the user can actually see and filter.
 */
export function downloadCsv(
  filename: string,
  rows: Array<Record<string, CsvCell>>,
): void {
  if (rows.length === 0) return;

  const headers = Object.keys(rows[0]);
  const escape = (value: CsvCell): string => {
    const text = value === null || value === undefined ? "" : String(value);
    return /[";\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
  };
  const lines = [
    headers.map(escape).join(";"),
    ...rows.map((row) => headers.map((header) => escape(row[header])).join(";")),
  ];

  // BOM keeps Excel from mangling the Spanish accents.
  const blob = new Blob([`\uFEFF${lines.join("\r\n")}`], {
    type: "text/csv;charset=utf-8",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
