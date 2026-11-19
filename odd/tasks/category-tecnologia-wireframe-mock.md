# Category Tecnologia Wireframe + Mock State (frontend)

Objective: make the `Category Tecnologia Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:1270`, 1440 × 1964) render in the Next.js frontend with no backend: the detail
of a category, reached when the user opens one from `/categorias`.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth (S11 of the shipping
feature): the frame is read and, when it has to change, edited and saved before the code. Frame
`0:1270` already defines this screen, so this task verifies it and follows it; no design edit was
needed.

S2 — Route. `/categorias/[slug]` is the generic detail of any category — the target that
`CategoryTile` (`/categorias/${category.slug}`) already points at. Every seeded category is a static
param; an unknown slug renders `notFound()`. Section order: Header, Breadcrumb, Category, Benefits,
Footer.

S3 — Breadcrumb. `md` variant (14px, 49px tall): `Inicio` → `/`, `Categorías` → `/categorias`,
`<category name>` current.

S4 — Title row. Name 24/700 (`Tecnología`), description 14/400 `text/muted` right below it
(`Auriculares, computación, celulares y accesorios.`), and the count 14/400 `text/muted` on the far
right, bottom-aligned. The count follows the visible products, so a filter narrows it.

S5 — Toolbar. Filter pills on the left: `Todos` plus the distinct subcategories of the category in
catalog order (`Audio`, `Computación`, `Celulares`, `Accesorios` for Tecnología). 40px tall, fully
rounded; the active pill is filled inverse (`#27272A`) with white 14/500, the rest are white with a
1px `border/strong` and 14/400. The pills are stateful, like the ones on `/categorias`. `Ordenar
por:` / `Destacados` / `↓` on the right stays presentational: the design draws no menu for it.

S6 — Grid. Four columns, 24px gaps, cards 318 × 352. The card is `ProductCard` with a new `catalog`
variant measured from the frame: 200px image, body with 16px padding and 8px gaps, `subcategory`
12/400 `text/muted`, name 16/700, price 18/700, and a solid `Agregar al carrito` button 40px tall,
radius 8, 14/500. The existing `compact` variant (15/17/13) is the detail "Related" card and does
not change.

S7 — Pagination. 12 products per page. Left: `Mostrando 1–12 de 24 productos` 14/400. Right: `←`,
the page numbers and `→`, 40 × 40, radius 8; the current page is filled inverse and the rest are
white with `border/strong`. The arrows are disabled at the ends with a `border/default` outline. The
page block disappears when a single page holds everything.

S8 — Mock state. Tecnología needs 24 products: the 12 the frame draws, in the frame's order and with
its exact names and prices (`Auriculares inalámbricos` `$ 25.999` … `Soporte para notebook` `$ 9.500`),
plus 12 more so page 2 is real. `Product` gains `subcategory` and `Category` gains an optional
`description`. The mock exposes `GET /categories/:slug` and `/products?categoryId=`, and the
service adds `findCategoryBySlug` (404 → `null`) and a `categoryId` filter on `getProducts`. Every
seeded product gets a static detail page, so the card links never dead-end.

S9 — Filtering and pagination are client state in
`features/category/components/category-products.tsx`; the route stays a server component and keeps
prerendering statically. The title row lives inside it because the count changes with the filter.

S10 — Structure. The screen lives in `features/category/{components,pages}`; the route is
`app/(public)/categorias/[slug]/page.tsx`; catalog types, card and service stay in
`features/product`, which owns the data. Existing dependencies only — no new packages.

S11 — Empty category. Not designed: a category with no seeded products hides the toolbar and the
pagination and states `Todavía no hay productos en esta categoría.` in the grid's place, instead of
showing an empty grid with `Mostrando 0–0 de 0 productos`.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `0:1270` read live; no design edit needed |
| T2 | S8 | inline | done | `Product.subcategory`, `Category.description`, 24 Tecnología products, `mocks/data/categories.ts` |
| T3 | S8 | inline | done | `/categories/:slug` + `categoryId` filter in `mocks/routes.ts`; `findCategoryBySlug`/`getProducts` in `catalog.service.ts`; all products statically prerendered |
| T4 | S6 | inline | done | `ProductCard` `catalog` variant; `compact` untouched |
| T5 | S4, S5, S7, S9, S11 | inline | done | `features/category/components/category-products.tsx` |
| T6 | S2, S3, S10 | inline | done | `features/category/pages/category-page.tsx`, `app/(public)/categorias/[slug]/page.tsx` |
| T7 | all | inline | done | lint 0; build 0 (51 static pages); `check-category.js` 69/69; regressions 38/38, 18/18, 33/33 |

## Log

L1 (verbatim): "Quiero implementar la pantalla Category Tecnologia Wireframe (Representa cuandoel
usuario entra a ver en detalle un categoria, haceme la pantalla que soporte esto) en modo mock el
frontend de LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero
que me prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (`tab-1`, page `0:3`, frame `0:1270`): Header 72, Breadcrumb 49, Category 1414
  (40px padding, 32px gaps: title row 54 + toolbar 40 + grid 1104 + pagination 40), Benefits 124,
  Footer 305.
- Grid arithmetic: 4 × 318 + 3 × 24 = 1344 wide; 3 × 352 + 2 × 24 = 1104 tall.
- The card is not the detail "Related" card: this one is 16/700 name, 18/700 price and 14/500
  button, while the related card is 15/700, 17/700, 13/500 and sits in a 350px frame. Hence a third
  `catalog` variant instead of stretching `compact`.
- Design consistency check: the frame says `24 productos` and draws 12 cards with `Mostrando 1–12 de
  24 productos`, so the mock has to seed 24 Tecnología products, not 12.
- `Product.subcategory` became required. Only the mock seeds `Product`, so the compile-time cost was
  updating `mocks/data/products.ts`; every seeded product got a subcategory so no screen renders an
  empty eyebrow.
- `generateStaticParams` of `/productos/[slug]` moved from `getFeaturedProducts()` to `getProducts()`:
  the 22 new cards must not link to slugs that were never prerendered.
- Checks: `npm run lint` exit 0; `npm run build` exit 0 with 51 static pages, `/categorias/tecnologia`
  and `/categorias/hogar` rendered as `○`; `check-category.js` 69/69 (copy, card names/prices/
  subcategories, 5 pills, 12 placeholders, 24 product links, pagination state, catalog type scale,
  empty category, plus home/detail/categories regressions); `check-categories.js` 38/38;
  `check-checkout-shared.js` 18/18; `check-checkout-shipping.js` 33/33.
- Limits of that evidence: the assertions read prerendered HTML, so they pin page 1 and the initial
  state only. Filtering, page 2 and the arrow enable/disable transitions are React state with no
  browser in this environment — no client interaction was exercised.
- Known mock inconsistency, already documented in `mocks/data/categories.ts`: the `productCount` the
  listing prints (18 Hogar, 32 Ropa, …) is larger than the seeded sample, so a non-Tecnología detail
  shows a smaller count than `/categorias` advertises. The title count follows the rendered products
  on purpose: it is the only number the grid can back with cards.
- `Risk: none` — additive screen and additive card variant; the only shared edits are the optional
  `Category.description`, the `subcategory` field (mock-only producer, all updated) and a prerender
  list that grew.
