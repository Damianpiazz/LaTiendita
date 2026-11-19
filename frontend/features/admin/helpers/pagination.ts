/**
 * Page buttons of the admin tables, drawn the way the frame shows them: three
 * first pages, an ellipsis, then the last one.
 */
export function buildPageTokens(pageCount: number): (number | "ellipsis")[] {
  if (pageCount <= 5) {
    return Array.from({ length: pageCount }, (_, index) => index + 1);
  }

  return [1, 2, 3, "ellipsis", pageCount];
}
