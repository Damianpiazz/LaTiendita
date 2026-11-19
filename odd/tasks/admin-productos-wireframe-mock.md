# Admin Productos Wireframe + Mock State (frontend)

Objective: render the `Admin Productos Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:1785`, 1440 × 974) in the Next.js frontend with no backend, inside the admin
shell already delivered by `Admin Resumen`.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: the frame is read before
the code and edited only when it has to change. Frame `0:1785` already defines this screen
completely, so this task verifies it and follows it; no design edit was needed.

S2 — Page header. `Productos` 24/700, subtitle `48 productos · 6 categorías · 5 con stock bajo`
14/400 muted. Right: `Exportar CSV` outlined 40 (14/500 secondary) and `+ Nuevo producto` inverse
40 (white 14/500). Both are presentational: the frame draws no export or creation flow.

S3 — Toolbar. Left: search 320 × 40 radius 8 white, 14/400 `Buscar por nombre o SKU...`, then four
40-tall radius-full filters: `Todos` active (inverse fill, white 14/500), `Publicados`,
`Borradores`, `Sin stock` (white, `border/strong` outline, 14/400 secondary). Right: presentational
`Categoría: Todas ↓` and `Ordenar: Recientes ↓` (14/500 secondary).

S4 — Bulk bar. Visible because two rows are selected. 56 tall radius 12, `background/canvas` fill,
`border/strong` outline, 20 horizontal padding: 24 × 24 radius-full inverse badge with `2` 12/700
white, `productos seleccionados` 14/500 secondary; on the right `Publicar`, `Archivar`, `Eliminar`
as 32-tall radius-8 white outlined controls with 12/500 labels and `Limpiar` 12/400 muted.

S5 — Table. Card radius 12 with hairline border. Head 40 tall, `background/canvas`, bottom hairline,
12/400 muted labels with 20 gutters. Column widths: check 40, producto 440, categoría 150,
precio 120, stock 110, estado 130, acciones 106 growing. Eight rows 64 tall with a bottom hairline:
24 × 24 radius-8 checkbox (two checked, inverse with `✓`), name 14/500 ink + SKU 12/400 muted,
category 14/400 secondary, price 14/500 ink, stock 14/400 secondary, status pill, and `Editar`
14/400 + `···` on the right. The product thumbnail drawn in the frame is hidden, so the cell renders
name + SKU only.

S6 — Stock cell. Quantities below the healthy threshold are a 24-tall radius-full
`background/subtle` pill with 12/700 secondary text; the rest are plain 14/400 secondary.
Seeded rows: 12 u., 3 u., 2 u., 4 u., 0 u., 1 u., 22 u., 30 u. — the five below the threshold are
pills and match the header's `5 con stock bajo`.

S7 — Status pill. 24 tall, radius full, 8 horizontal padding, 12/500. `Publicado`: inverse fill,
white text. `Borrador`: white, `border/strong` outline, muted text. `Agotado`: white,
`border/strong` outline, `ink/placeholder` text.

S8 — Pagination. `Mostrando 1–8 de 48 productos` 14/400 muted on the left; on the right
`←` placeholder, pages `1` (active inverse 40 × 40 radius 8), `2`, `3`, `…`, `6`, and `→`.

S9 — Mock state. The screen comes from `GET /admin/products`: totals (`48` products, `6` categories,
`5` low stock), the low-stock threshold, the eight visible rows and the two selected SKUs.
Pagination copy and page buttons are derived from the eight rendered rows and the `48` total
(6 pages of 8). The admin seed is independent of the public catalog seed, exactly like
`Admin Resumen`.

S10 — Structure. Types in `features/admin/types/products.ts`, seed in `mocks/data/admin.ts`, route in
`mocks/routes.ts`, cached service `getAdminProducts` in `features/admin/services/admin.service.ts`,
components in `features/admin/components/` and page in `features/admin/pages/`. The pagination block
is shared with `Admin Pedidos`. Existing dependencies only — no new packages.

S11 — Responsive fallback, not designed. Below `xl` the table scrolls horizontally keeping the
frame's column widths; the toolbar wraps; the header actions wrap under the title. Desktop
measurements stay as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `0:1785` read live; no design edit needed |
| T2 | S9 | inline | done | `features/admin/types/products.ts`, `mocks/data/admin.ts`, `GET /admin/products` |
| T3 | S9 | inline | done | `getAdminProducts` cached |
| T4 | S2, S3 | inline | done | header + toolbar |
| T5 | S4, S7 | inline | done | bulk bar + product status badge |
| T6 | S5, S6, S8 | inline | done | products table + shared pagination |
| T7 | S10, S11 | inline | done | page + `app/(admin)/admin/productos/page.tsx` |
| T8 | all | inline | done | lint, build, `check-admin-productos.js` |

## Log

L1 (verbatim): "Quiero implementar las pantallas Admin Categorias Wireframe, Admin Productos Wireframe, Admin Pedidos Wireframe, en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (page `0:3`, frame `0:1785`): Sidebar 240 | Main 1200 (Topbar 72 + Content 902).
  Content = 32 + 54 header + 24 + 40 toolbar + 24 + 56 bulk + 24 + 552 card + 24 + 40 pagination + 32.
- Column arithmetic: 40 + 440 + 150 + 120 + 110 + 130 + 106 = 1096 = 1136 − 2 × 20 gutters.
- The card is 552 tall = 40 head + 8 × 64 rows.
- Selection is part of the seed: `Remera básica blanca` and `Buzo con capucha verde` are checked, and
  the bulk bar reports `2`. The frame always draws the bar, so the screen renders it from the seed
  instead of hiding it when nothing is selected.
- The row thumbnail node exists but is hidden in the frame, so the implemented cell is name + SKU.
- The low-stock pill starts below `5 u.` (the five pills are 3, 2, 4, 0 and 1), which matches the
  header's `5 con stock bajo`.
- Verification: `npm run lint` clean except the pre-existing `methodName` warning; `npm run build`
  compiles 55 static pages and prerenders `/admin/productos` as static at 1h/1d;
  `check-admin-productos.js` 82/82 (copy, eight rows with price/stock, four filter pills, two
  selected rows, three status states, pagination). Regressions: `check-admin.js` 125/125,
  category 69/69, categories 38/38, checkout-shared 18/18, checkout-shipping 32/33 (pre-existing).
- Risk: none — new route and new feature files; existing edits are additive mock data, one mock
  route, one additive helper and additive barrel exports.
