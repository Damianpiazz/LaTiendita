# Admin Pedidos Wireframe + Mock State (frontend)

Objective: render the `Admin Pedidos Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:2252`, 1440 × 974) in the Next.js frontend with no backend, inside the admin
shell already delivered by `Admin Resumen`.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: the frame is read before
the code and edited only when it has to change. Frame `0:2252` already defines this screen
completely, so this task verifies it and follows it; no design edit was needed.

S2 — Page header. `Pedidos` 24/700, subtitle `48 pedidos · 12 pendientes de despacho` 14/400 muted.
Right: `Exportar CSV` outlined 40 (14/500 secondary) and `Imprimir etiquetas` outlined 40
(14/500 muted). Both are presentational: the frame draws no export or label flow.

S3 — Status tabs. 40-tall radius-full controls with an 8 gap: label 14/400 plus count 12/700.
`Todos 48` is active (inverse fill, white text); `Pendientes 12`, `Pagados 8`, `Enviados 5`,
`Entregados 21`, `Cancelados 2` are white with a `border/strong` outline. Right: presentational
`Rango: Últimos 30 días ↓` control, 40 tall radius 8 white outlined.

S4 — Orders card. Header 80 tall (20 padding): `Todos los pedidos` 16/700,
`48 pedidos · 2 cancelados en el período` 12/400 muted; on the right a 280 × 40 search field
(`Buscar por pedido, cliente o email...`, 14/400 placeholder) and `Ordenar: Más recientes ↓`
14/500 secondary. Table head 40 tall with `background/canvas` and hairlines on both edges, 12/400
muted labels, 20 gutters.

S5 — Columns and rows. Widths: pedido 110, cliente 240, fecha 130, artículos 100, pago 130,
total 110, estado 140, acciones 136 growing (110 + 240 + 130 + 100 + 130 + 110 + 140 + 136 = 1096 =
1136 − 2 × 20). Eight rows 64 tall with a bottom hairline: id 14/500 ink, customer 14/500 ink +
email 12/400 muted, date 14/400 muted, `N artículos` 14/400 secondary, payment 14/400 secondary,
total 14/500 ink, status pill, and `Ver detalle →` 14/500 secondary on the right.

S6 — Status pill. Reuses the existing `StatusBadge` (24 tall, radius full, 12/500) with the five
states the frame prints: `Pendiente`, `Pagado`, `Enviado`, `Entregado`, `Cancelado`.

S7 — Pagination. `Mostrando 1–8 de 48 pedidos` 14/400 muted on the left; on the right `←`
placeholder, pages `1` (active inverse 40 × 40 radius 8), `2`, `3`, `…`, `6`, `→`. Same block as
`Admin Productos`.

S8 — Mock state. The screen comes from `GET /admin/orders`: totals (`48` orders, `12` pending
dispatch, `2` cancelled in the period), the tab counts, the period label, the eight visible rows and
the page size. Subtitle, card sub line, tabs and pagination range are derived from those numbers,
not copied. The admin seed is independent of the public catalog seed, exactly like `Admin Resumen`.

S9 — Structure. Types in `features/admin/types/orders.ts`, seed in `mocks/data/admin.ts`, route in
`mocks/routes.ts`, cached service `getAdminOrders` in `features/admin/services/admin.service.ts`,
components in `features/admin/components/` and page in `features/admin/pages/`. Existing
dependencies only — no new packages.

S10 — Responsive fallback, not designed. Below `xl` the table scrolls horizontally keeping the
frame's column widths; the tabs row and the card header wrap. Desktop measurements stay as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `0:2252` read live; no design edit needed |
| T2 | S8 | inline | done | `features/admin/types/orders.ts`, `mocks/data/admin.ts`, `GET /admin/orders` |
| T3 | S8 | inline | done | `getAdminOrders` cached |
| T4 | S2, S3 | inline | done | header + status tabs |
| T5 | S4, S5, S6 | inline | done | orders table reusing `StatusBadge` |
| T6 | S7, S9 | inline | done | shared pagination + page + `/admin/pedidos` route |
| T7 | all | inline | done | lint, build, `check-admin-pedidos.js` |

## Log

L1 (verbatim): "Quiero implementar las pantallas Admin Categorias Wireframe, Admin Productos Wireframe, Admin Pedidos Wireframe, en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (page `0:3`, frame `0:2252`): Sidebar 240 | Main 1200 (Topbar 72 + Content 902).
  Content = 32 + 54 header + 24 + 40 tabs + 24 + 632 card + 24 + 40 pagination + 32.
- Card 632 = 80 header + 40 table head + 8 × 64 rows.
- The five tab counts sum to 48 (12 + 8 + 5 + 21 + 2), matching the total; they are seeded, not
  derived from the eight visible rows.
- `StatusBadge` already maps exactly those five labels, so the orders screen reuses it instead of
  duplicating a second pill set.
- Verification: `npm run lint` clean except the pre-existing `methodName` warning; `npm run build`
  compiles 55 static pages and prerenders `/admin/pedidos` as static at 1h/1d;
  `check-admin-pedidos.js` 99/99 (six tabs with their counts, eight rows with email/items/payment/
  total, five status pills, pagination and the singular `1 artículo` case). Regressions:
  `check-admin.js` 125/125, category 69/69, categories 38/38, checkout-shared 18/18,
  checkout-shipping 32/33 (pre-existing).
- Risk: none — new route and new feature files; existing edits are additive mock data, one mock
  route and additive barrel exports.
