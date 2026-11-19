const priceFormatter = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  maximumFractionDigits: 0,
});

/**
 * Renders an ARS amount the way the wireframe does: `$ 25.999`.
 *
 * `Intl` uses a non-breaking space after the symbol, which makes the price
 * impossible to match with a normal space in copy and tests, so it is
 * normalized to a regular space.
 */
export function formatPrice(price: number): string {
  return priceFormatter.format(price).replace(/\u00a0/g, " ");
}
