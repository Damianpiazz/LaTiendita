# Admin Pagos Wireframe + Mock State (frontend)

Objective: render the `Admin Pagos Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:2537`, 1440 × 1113) in the Next.js frontend with no backend, inside the admin
shell already delivered by `Admin Resumen`.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: the frame is read before
the code and edited only when it has to change. Frame `0:2537` already defines this screen
completely, so this task verifies it and follows it; no design edit was needed.

S2 — Route and shell. `/admin/pagos` renders the screen inside the existing `app/(admin)` layout.
The sidebar entry `Pagos` already points there (`admin-nav.ts`, badge `payments`) and becomes
active through `usePathname`; the shell is untouched.

S3 — Page header. `Pagos` 24/700, subtitle `48 pagos · 34 acreditados · 3 rechazados por revisar`
14/400 muted. Right: `Exportar CSV` 40 outlined (14/500 secondary) and `Conciliar pagos` 40
outlined (14/500 muted). Both are presentational: the frame draws no export or reconciliation flow.

S4 — KPI row. Four 115-tall cards, 266 wide each with a 24 gap, radius 12, white with a hairline:
20 padding, 8 gap, label 12/400 muted, value 24/700 ink, caption 12/400 muted.
`Cobrado (30 días)` / `$ 1.284.950` / `34 pagos acreditados`;
`Pendiente de acreditación` / `$ 412.370` / `11 pagos en proceso`;
`Rechazados` / `$ 67.994` / `3 pagos para revisar`;
`Tasa de aprobación` / `91%` / `34 de 37 intentos`.

S5 — Status tabs. 40-tall radius-full controls with an 8 gap: label 14 plus count 12/700.
`Todos 48` is active (inverse fill, white, label 14/500); `Aprobados 34`, `Pendientes 11`,
`Rechazados 3` are white with a `border/strong` outline (label 14/400 secondary, count 12/700
muted). The four counts satisfy 34 + 11 + 3 = 48. Right: presentational `Rango: Últimos 30 días ↓`
control, 40 tall radius 8 white outlined.

S6 — Payments card. Header 80 tall (20 padding): `Movimientos` 16/700,
`Todos los pagos procesados por Mercado Pago` 12/400 muted; on the right a 280 × 40 search field
(`Buscar por pago, pedido o cliente...`, 14/400 placeholder) and `Ordenar: Más recientes ↓`
14/500 secondary. Table head 40 tall with `background/canvas` and hairlines on both edges, 12/400
muted labels, 20 gutters.

S7 — Columns and rows. Widths: pago 110, pedido 100, cliente 210, método 190, fecha 130, monto 110,
estado 130, acciones 116 growing (110 + 100 + 210 + 190 + 130 + 110 + 130 + 116 = 1096 = 1136 −
2 × 20). Eight rows 64 tall with a bottom hairline: payment id 14/500 ink, order 14/400 secondary,
customer 14/500 ink + email 12/400 muted, provider 14/400 secondary + detail 12/400 muted, date
14/400 muted, amount 14/500 ink, status pill, and `Ver →` 14/500 secondary on the right.

S8 — Payment status pill. 24 tall, radius full, 8 horizontal padding, 12/500. `Aprobado`: inverse
fill, white text. `Pendiente`: white, `border/strong` outline, secondary text. `Rechazado`: white,
`border/strong` outline, muted text. The orders `StatusBadge` maps a different vocabulary
(`Pagado`, `Entregado`, …), so payments get their own badge instead of stretching that one.

S9 — Pagination. `Mostrando 1–8 de 48 pagos` 14/400 muted on the left; on the right `←`,
pages `1` (active inverse 40 × 40 radius 8), `2`, `3`, `…`, `6`, and `→`. Same shared
`AdminPagination` used by Productos and Pedidos.

S10 — Mock state. The screen comes from `GET /admin/payments`: totals (`48` payments, `34`
approved, `11` pending, `3` rejected), the four KPI figures, the period label, the eight visible
rows and the page size. Subtitle, tab counts, KPI captions and the pagination range are derived from
those numbers, not copied. The admin seed is independent of the public catalog seed, exactly like
`Admin Resumen`.

S11 — Structure. Types in `features/admin/types/payments.ts`, seed in `mocks/data/admin.ts`, route
in `mocks/routes.ts`, cached service `getAdminPayments` in
`features/admin/services/admin.service.ts`, components in `features/admin/components/` and page in
`features/admin/pages/`. Existing dependencies only — no new packages.

S12 — Responsive fallback, not designed. Below `xl` the table scrolls horizontally keeping the
frame's column widths; the KPI row wraps to two columns, the tabs row and the card header wrap.
Desktop measurements stay as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `0:2537` read live; no design edit needed |
| T2 | S10 | inline | done | `features/admin/types/payments.ts`, `mocks/data/admin.ts`, `GET /admin/payments` |
| T3 | S10 | inline | done | `getAdminPayments` with `"use cache"` + `cacheLife("hours")` |
| T4 | S3, S4 | inline | done | `admin-payments-header.tsx`, `admin-payments-kpis.tsx` |
| T5 | S5, S8 | inline | done | `admin-payments-filters.tsx`, `payment-status-badge.tsx` |
| T6 | S6, S7, S9 | inline | done | `admin-payments-table.tsx` + shared `AdminPagination` |
| T7 | S2, S11, S12 | inline | done | `admin-payments-page.tsx` + `app/(admin)/admin/pagos/page.tsx` |
| T8 | all | inline | done | lint clean, build 60 pages (`/admin/pagos` static), HTML assertions 57/57, regressions 55/55 |

## Log

L1 (verbatim): "Quiero implementar las pantallas Admin Pagos Wireframe, Admin Clientes Wireframe, Password Recovery Wireframe, Register Wireframe, Login Wireframe, en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (page `0:3`, frame `0:2537`): Sidebar 240 | Main 1200 (Topbar 72 + Content 1041).
  Content = 32 + 54 header + 24 + 115 KPI + 24 + 40 tabs + 24 + 632 card + 24 + 40 pagination + 32.
- Card 632 = 80 header + 40 table head + 8 × 64 rows.
- Divisibility check: 266 × 4 + 24 × 3 = 1136; 1096 + 2 × 20 = 1136.
- `Conciliar pagos` is muted (`#71717A`), not secondary like `Exportar CSV` (`#3F3F46`); both are
  outlined because neither has a flow behind it.
- The `91%` rate is seeded, not computed: `34 de 37 intentos` is the frame's own arithmetic
  (34 / 37 = 91.9 % → 91 %), and 37 is not the payment total, so deriving it from the visible rows
  would invent a different number.
- Implementation evidence: `npm run lint` (0 errors, only the pre-existing `methodName` warning in
  `shipping-address-card.tsx`), `npm run build` (60 static pages, `/admin/pagos` prerendered),
  prerendered-HTML assertions 57/57 and cross-page regressions 55/55 through a throwaway script in
  the system temp dir, so no verification file lands in the repo.
- Verb copy: rows and the header use `Ver →` (the frame's arrow), matching Pedidos' `Ver detalle →`
  only in tone, not in wording.
- Risk: none — new route and new feature files; existing edits are additive mock data, one mock
  route and additive barrel exports.
