# Product Detail Wireframe + Mock Data (frontend)

Objective: make the `Product Detail Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:213`, 1440 × 2591) render in the Next.js frontend with no backend.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint` (exit 0), `npm run build` (exit 0, `/productos/[slug]` prerendered),
prerendered-HTML assertions against the design copy.
No test runner is configured (vitest installed without config or `test` script), so there is no
runnable RED/GREEN cycle; verification is lint + build + prerendered HTML.

## Specs

S1 — Mock routes. The mock adapter matches `:param` segments and the seeded data answers the
detail screen: `GET /products/:slug` (detail), `GET /reviews/summary?productId=`,
`GET /reviews?productId=&page=&size=`. Existing `GET /categories`, `GET /products` keep working.

S2 — Route. `/productos/[slug]` renders the detail screen for the eight mock products through
`generateStaticParams`; section order is Header, Breadcrumb, Product, Details, Reviews, Related,
Benefits, Footer (header/footer come from the public layout). The Home cards already link here.

S3 — Breadcrumb. White band, bottom hairline, 48px gutter, 14px vertical padding, 8px gap:
`Inicio / Categorías / Tecnología / Auriculares inalámbricos` at 13px; separators and the first
three items `text/muted`/`text/placeholder`, current item 13/500 `text/primary`.

S4 — Gallery. 640 wide column, 14px gap: main placeholder 640×480 (`background/subtle`, border
`border/strong`, radius 12) labelled `[ Imagen principal — 640 x 480 ]`; four 88×88 thumbnails
(radius 8) labelled `[ img ]` — selected with a 2px `background/inverse` border, the rest
`background/muted`; caption 12 `text/subtle`: `1 de 4 — hacé clic para cambiar la imagen`.
Clicking a thumbnail changes the selected index and the caption.

S5 — Product info. Column 648 wide, 14px gap. Copy verbatim: `Tecnología · SKU 0042`;
title `Auriculares inalámbricos` 34/700; rating row with four filled stars plus one
`background/muted` star, `4,8 de 5` · `124 reseñas` · `200+ vendidos` at 13; hairline;
`$ 25.999` 32/700 + struck `$ 32.500` 16 `text/placeholder` + badge `-20% OFF` (12/700,
`background/inverse`, radius 4, 23px tall); `6 cuotas sin interés de $ 4.333`; stock dot 8px +
`En stock — sale en 24/48 hs`; `Cantidad` 13/500; quantity control 132×48 outlined with
`- 1 +`; `Agregar al carrito` 48px solid, 15/700, filling the row; `Comprar ahora` 48px outline,
15/500; hairline; three perks (36px `background/subtle` icon square, title 13/500, note 12
`text/muted`): `Envío gratis en compras mayores a $ 20.000` / `Recibilo en 24-48 hs en todo el país`,
`30 días de garantía` / `Devoluciones sin cargo, sin preguntas`,
`Pago seguro` / `Tarjeta, transferencia, débito y efectivo`.

S6 — Details. `pt-40 px-48 pb-48`, 28px gap. Tabs 15px, 32px gap, 3px underline (active
`background/inverse` under `Descripción` in 15/700, the rest `text/muted`), hairline rule.
Content row 48px gap: description column 736 (title `Descripción` 18/700, two paragraphs 14/24
`text/secondary` and three bullets); specs card 560 wide, border `border/default` radius 10,
head `Especificaciones` 15/700, six rows (40px, 12/16 padding, top hairline):
`Marca / SoundCore`, `Modelo / NC-450`, `Conectividad / Bluetooth 5.3 · USB-C`,
`Autonomía / 40 horas con cancelación activa`, `Peso / 250 g`, `Garantía / 12 meses oficial`.

S7 — Reviews. 48px padding, 24px gap, white. Head: `Reseñas (124)` 24/700 + outlined control
`Escribir una reseña` 40px, 14/500 (inert: no review POST exists yet). Body: summary card 380
wide, 20px padding, radius 10, border — score `4,8` 44/700, five 16px stars,
`124 reseñas verificadas` 12, hairline, and five bars (`5 ★` … `1 ★`) with counts
`96`, `18`, `6`, `3`, `1`; two review cards 458 wide, 20px padding, radius 10, 10px gap —
`María G.` / `Compra verificada · 12 oct 2026` / five 14px stars / `Excelente sonido` /
`La batería dura lo que dice y la cancelación de ruido funciona muy bien en el subte. Los usé todo el día y no me dolieron.` /
`¿Te sirvió?` `Sí (18)` `No (2)`, and `Lucas P.` / `Compra verificada · 28 sep 2026` /
four stars plus one muted / `Muy buenos, la funda podría mejorar` /
`Son cómodos y el sonido está muy bien por el precio. La funda de transporte es medio floja, pero por todo lo demás va perfecto.` /
`Sí (9)` `No (2)`. Footer link `Ver las 124 reseñas →` at 14/500.

S8 — Related. 48px padding, 24px gap: `Productos relacionados` 24/700 + `Ver todo →`; four
cards 318×350 (image 200 tall, body 16px padding, 8px gap): `Tecnología / Auriculares inalámbricos / $ 25.999`,
`Hogar / Lámpara de mesa / $ 14.500`, `Ropa / Remera básica de algodón / $ 8.990`,
`Deportes / Botella térmica 750 ml / $ 12.300`; name 15/700, price 17/700, solid 40px
`Agregar al carrito` 13/500. Served by the paginated catalog list; the helper returns the first
four featured products because the wireframe grid shows exactly those.

S9 — Shared shell. `BenefitsBar` and `SiteFooter` measure to the frames (`0:476`, `0:497`):
benefits = plain white band, 40px vertical padding, 48px gutter, 24px gap, 44px icon squares,
title 14/700, note 12 `text/muted`; footer = 48px padding, 32px gap, top hairline, brand column
300 wide with 28px mark radius 6, tagline 13, three 28×28 white squares with `border/strong` and
10px gap, three columns on a 64px gap with 13/700 titles and 13 `text/muted` links, legal bar
top hairline, 20px vertical padding, `© 2026 La Tiendita` 12 centered `text/placeholder`.

S10 — Quantity. `Agregar al carrito` adds the chosen quantity to the cart store; the header badge
reflects the total. Quantity starts at 1, never drops below 1.

S11 — Tokens. Every color and radius comes from `wf-*` theme tokens; the product screen needs
`--radius-wf-card: 10px` (detail surfaces), `--radius-wf-xs: 4px` (discount badge) and
`--radius-wf-mark: 6px` (footer mark and social squares).

S12 — Structure. New code lives in `features/product/{components,pages,services,types}`,
`features/review/{components,services,types}`, `features/cart/components`, shared
`components/layout`, mock data in `mocks/data`. Existing dependencies only.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1, S12 | inline | done | `mocks/adapter.ts` param matching + 404 on undefined handler result; `mocks/data/{product-details,reviews}.ts`; `mocks/routes.ts` |
| T2 | S1, S12 | inline | done | `features/product/types/product-detail.ts`, `features/review/types`; `catalog.service.ts#getProductBySlug/getRelatedProducts/findProductBySlug`, `review.service.ts` |
| T3 | S10, S12 | inline | done | `features/cart/store/cart-store.ts` quantity; `AddToCartButton` quantity prop; `purchase-panel.tsx` |
| T4 | S3–S6, S11 | inline | done | `breadcrumbs`, `product-gallery`, `rating-stars`, `product-info`, `product-details`; `--radius-wf-{xs,card,mark}` in `globals.css` |
| T5 | S7, S11 | inline | done | `features/review/components/{review-summary-card,review-card,reviews-section}.tsx`; `helpers/format-review-date.ts` |
| T6 | S8, S9, S11 | inline | done | `product-card.tsx` compact variant; `benefits-bar.tsx`; `site-footer.tsx`; `BrandLogo` mark radius |
| T7 | S2, S12 | inline | done | `app/(public)/productos/[slug]/page.tsx`, `features/product/pages/product-detail-page.tsx`, barrel exports |
| T8 | all | inline | done | `npm run lint` exit 0; `npm run build` exit 0; 8 prerendered detail pages; 44/44 copy assertions on the target page |

## Log

L1 (verbatim): "Quiero implementar la pantalla Product Detail Wireframe en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que exixiten en el package.json del frontend"

- Design read live from the OpenPencil document (`tab-1`, page `0:3`, frame `0:213`) section by
  section; measurements above come from the frame nodes, not from the earlier home analysis.
- Next.js 16.4.5 with `cacheComponents: true`: route `params` is a promise, and the page uses
  `generateStaticParams` so the eight mock slugs are prerendered at build time.
- Mock mode already existed (Home screen). This feature only extends it: `:param` matching plus
  the detail/review routes. `NEXT_PUBLIC_USE_MOCKS=false` keeps sending the same requests to the
  real gateway.
- No commit created, same reason as the Home feature: the repository has an uncommitted scaffold
  (`frontend/{components,features,config,lib,mocks}` untracked) and committing would sweep it in.
- Related products: `docs/microservicios/product-service.md` documents "same category" for the
  related section, but the wireframe grid shows the first four featured products, including the
  product itself. The screen follows the wireframe; `getRelatedProducts` slices the cached featured
  list, so only that helper changes when the backend exposes real recommendations.
- T8 evidence (run on `C:\Repositorios\LaTiendita\frontend`):
  - `npm run lint` → exit 0.
  - `npm run build` → exit 0, 13 static pages; `/productos/[slug]` list eight prerendered paths and
    writes a full HTML file per product under `.next/server/app/productos/`.
  - Copy assertions on `auriculares-inalambricos.html`: 44/44 pass after removing React's `<!-- -->`
    separators (adjacent text children split strings like `Tecnología · SKU 0042` in the markup).
  - `lampara-de-mesa.html` renders the neutral branch: `Hogar · SKU 0043`, `Sin reseñas todavía`,
    `Reseñas (0)`, and no `Ver las N reseñas` link.
- `formatReviewDate` deviation found by the assertions: `Intl.DateTimeFormat("es-AR")` renders
  `12 de oct de 2026` and abbreviates September as `sept`, while the frame nodes say
  `12 oct 2026` / `28 sep 2026`. The helper now formats the UTC parts with an explicit three-letter
  month table so the copy matches the design instead of the locale default.
- Not verified: the screen was never opened in a browser (no dev server was run), so layout/spacing
  is asserted from measurements and markup, not visually. There is still no test runner, so the
  checks are lint, build and the HTML assertions above.
- Risk: item 3 — `ProductCard`, `BenefitsBar`, `SiteFooter` and `BrandLogo` are shared with the Home
  screen, so the Home card/benefits/footer rendering changed with this feature.
