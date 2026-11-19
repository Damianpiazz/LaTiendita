# Admin Resumen Wireframe + Mock State (frontend)

Objective: render the `Admin Resumen Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:1487`, 1440 × 1115) in the Next.js frontend with no backend: the admin
dashboard shell (sidebar + topbar) and the summary screen it opens on.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: the frame is read
before the code and edited only when it has to change. Frame `0:1487` already defines this screen
completely, so this task verifies it and follows it; no design edit was needed.

S2 — Route and shell. `/admin` opens the summary. The admin area is its own route group
`app/(admin)` with a dedicated layout — sidebar plus topbar, no public header, footer or newsletter.
Section order: Sidebar 240 wide, then Main = Topbar 72 tall plus Content with 32px padding and 24px
gaps. The page is one screen that fits: page header 54, KPIs 117, dashboard 760.

S3 — Sidebar. 240 wide, 20px vertical / 16px horizontal padding, 24px gaps, white with a 1px right
hairline. Brand: 32px mark, `LA TIENDITA` 16/700 and `Panel de administración` 12/400 muted. Sections
`General` / `Sistema` are 12/400 muted labels; items are 40 tall, radius 8, 12px horizontal padding,
12px gap, icon placeholder 20 × 20 radius 8. The active item is filled inverse with white 14/500 and
`border/strong` icon; the rest are 14/400 secondary with `background/subtle` icons. Badges are 20
tall, radius full, 8px horizontal padding, `background/subtle`, 12/700: `Pedidos 12`, `Pagos 3`.
Footer: 1px top hairline and 16px top padding, `Ver tienda ↗` outlined 40-tall control, then the
account block (36px round avatar `DP`, name 14/500, role 12/400). The footer sits directly after the
nav block, exactly where the frame draws it — it is not anchored to the bottom of the viewport.

S4 — Topbar. 72 tall, 32px gutters, white with a bottom hairline. Search field 320 × 40, radius 8,
`background/canvas` and hairline border, 14/400 placeholder
`Buscar pedidos, productos o clientes...`. Right: `Avisos` outlined control with its `3` badge, then
the 40px round avatar `DP`. Neither is a working control in this stage; the frame only draws their
resting state.

S5 — Page header. `Resumen` 24/700 with `Martes 10 de noviembre de 2026 · Actualizado hace 2 min`
14/400 muted below; right side carries the period control (210 × 40, outlined, `Período:` muted,
`Últimos 7 días` 14/500, chevron) and `+ Nuevo producto` (153 × 40, inverse, white 14/500). The
period control is presentational because the frame draws no menu for it, same decision as the sort
control of the catalog screens; it links to products since the creation form is out of scope.

S6 — KPIs. Four equal cards, 24px gaps, 20px padding, radius 12, hairline border: label 14/400
muted, value 24/700, meta 12/400 muted. In frame order: `Ventas de hoy` `$ 412.900` with
`+12,4% vs. ayer · 38 pedidos`; `Pedidos pendientes` `12` with `4 sin pago acreditado`;
`Pagos a acreditar` `$ 86.400` with `3 pagos en revisión`; `Stock bajo` `5` with
`productos con menos de 5 u.`.

S7 — Sales card. Full width of the main column, 20px padding, 24px gaps, radius 12. Header: title
`Ventas de los últimos 7 días` 16/700, sub 12/400 muted with the week total, order count and average
ticket, and `+12,4% vs. semana anterior` 12/500 secondary on the right. Chart 180 tall, seven equal
columns, 12px gaps, 48-wide bars, radius 8, `background/subtle`, bottom hairline; the current day
(`Dom`) is inverse with a 12/500 label, the rest are 12/400 muted. Bar heights are the series scaled
to the tallest bar the frame draws (110px), so the seeded amounts reproduce the drawn heights
exactly.

S8 — Recent orders card. Header with 20px padding: title `Últimos pedidos` 16/700, sub
`12 pendientes de despacho · 3 pagos en revisión` 12/400, `Ver todos →` 14/500. Table head 40 tall,
`background/canvas`, hairlines top and bottom, 12/400 muted labels, 20px gutters. Six rows 56 tall
with a bottom hairline: id 14/500, client 14/400 secondary, date 14/400 muted, status badge, total
14/500, `Ver →` 14/400 on the right. The sub line and the `12` badge are the store totals, not the
six visible rows.

S9 — Status badge. 24 tall, radius full, 8px horizontal padding, 12/500. `Pendiente`: white with a
`border/strong` outline and secondary text. `Pagado`: inverse with white text. `Enviado`:
`background/subtle` fill with secondary text. `Entregado`: white with a `border/default` outline and
muted text. `Cancelado`: white with a `border/strong` outline and muted text.

S10 — Side column, 352 wide. Stock card: header 20px padding with `Stock bajo`, the count sub and
`Reponer →`; five rows 56 tall with a bottom hairline — name 14/500, SKU 12/400 muted, and the stock
pill (24 tall, radius full, `background/subtle`, 12/700, `1 u.` … `0 u.`). Activity card: header with
`Actividad reciente`, `Movimientos de la tienda` and `Ver todo →`; five rows 60 tall with a bottom
hairline — 32 × 32 radius 8 icon placeholder, text 14/400 secondary wrapping to two lines, relative
time 12/400 muted.

S11 — Mock state. The dashboard comes from `GET /admin/summary`: today's sales and orders, the
pending counts, the weekly series, the six recent orders, the five low-stock products and the five
activity entries. The seven daily amounts sum to the frame's `$ 412.900` and divide by its 38 orders
into its `$ 10.866` average ticket, so the sub line is derived, not copied. The sidebar badges and the
topbar counter come from `GET /admin/navigation` (`pedidos 12`, `pagos 3`, `avisos 3`), because the
shell is shared by every admin screen and a layout cannot receive them as props. Relative times and
table dates (`10/11 · 09:42`, `Hace 12 min`) are display strings: the mock has no clock and a real
server would format them the same way.

S12 — Structure. The screen lives in `features/admin/{components,config,helpers,pages,services,types}`;
the shell (`AdminShell`, sidebar, topbar) is part of the same feature because every admin screen uses
it. Seed data lives in `mocks/data/admin.ts`, the routes in `mocks/routes.ts`. Existing dependencies
only — no new packages.

S13 — Responsive fallback, not designed. The frame is a 1440 desktop canvas. Below `lg` the sidebar is
hidden (admin navigation on mobile is out of scope), the search field hides below `md`, the KPI row
goes 1 → 2 → 4 columns, the dashboard stacks the side column, and the orders table scrolls
horizontally. Desktop measurements stay exactly as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `0:1487` read live; no design edit needed |
| T2 | S11 | inline | done | `features/admin/types/*`, `mocks/data/admin.ts`, two mock routes |
| T3 | S11 | inline | done | `admin.service.ts` with `getAdminSummary` / `getAdminNavigation` |
| T4 | S3, S4, S2 | inline | done | sidebar, topbar, shell, `app/(admin)/layout.tsx` |
| T5 | S5, S6, S7 | inline | done | header, KPI grid, sales chart |
| T6 | S8, S9, S10 | inline | done | orders table, status badge, stock and activity cards |
| T7 | S2, S12 | inline | done | `admin-summary-page.tsx` + `app/(admin)/admin/page.tsx` |
| T8 | all | inline | done | lint, build (52 pages), `check-admin.js` 125/125 |

## Log

L1 (verbatim): "Quiero implementar la pantalla Admin Resumen en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (`tab-1`, page `0:3`, frame `0:1487`): Sidebar 240 | Main 1200 (Topbar 72 + Content
  1043). Content 32px padding, page header 54, 24 gap, KPIs 117, 24 gap, Dashboard 760 (main 760 +
  24 + side 352).
- Width arithmetic: 4 × 266 + 3 × 24 = 1136 (content inner width); 760 + 24 + 352 = 1136.
- The frame has no footer and no public chrome: it is a second shell, so it gets its own route group
  instead of reusing `(public)/layout.tsx`.
- The sidebar footer is drawn at y=516 inside a 1115-tall sidebar, i.e. directly under `Sistema`, not
  pinned to the bottom. Implemented as drawn; anchoring it is a one-class change if wanted.
- Bar heights are data-proportional, not decorative: the frame's 71/90/62/101/85/110/78px bars are
  exactly the seeded amounts scaled to a 110px maximum, so the chart is computed instead of hardcoded.
- `Product` was not touched here: the admin seed uses its own order/stock/activity shapes, because the
  dashboard reports store totals (12 pending orders) that the catalog sample cannot back.

- Implementation landed in `features/admin/{components,config,helpers,pages,services,types}`, plus
  `mocks/data/admin.ts`, the two routes in `mocks/routes.ts` and the `app/(admin)` route group. The
  admin barrel exports only the shell and the page; the shell imports the sidebar directly.
- The mock keeps domain numbers (`salesByDay`, `weekOrders`, totals) and the screen derives the copy:
  week total `$ 412.900`, ticket `$ 10.866`, `+12,4%`, the four KPI meta lines and the stock count.
- Frame correction while implementing: side-card rows carry a **bottom** hairline (the JSX has
  `strokeWeights bottom: 1`), and the activity time sits **below** the entry text inside the same
  column, not to its right. Both were fixed before the final build.
- Verification: `npm run lint` clean (one pre-existing `methodName` warning in the shipment feature);
  `npm run build` compiles 52 static pages, with `/admin` prerendered as static at 1h/1d.
  `check-admin.js` asserts 125 frame strings and counts: 4 KPIs, 7 day labels, bar heights
  `71/90/62/101/85/110/78`, 6 orders with the five states, 5 stock rows, 5 activity entries, the
  12/3/3 badges and the active `aria-current` entry. Regressions: category 69/69, categories 38/38,
  checkout-shared 18/18, checkout-shipping 32/33.
- The single failing regression is **outside this feature and pre-existing**: `/checkout/envio` no
  longer renders the `Envío a domicilio` heading that frame `0:1105` draws (text node `0:1162`).
  `shipping-address-card.tsx` still declares `methodName` and its comment says the card states the
  method, but the heading block was removed earlier; lint already reports that prop as unused. It was
  not fixed here because it is not part of this authorized scope — the fix is restoring the heading
  block, or dropping the prop and updating frame + check together.
- Risk: none — this adds `/admin` and its feature; the only edit to existing files is two additive
  mock routes, so no existing behavior or shared contract changed.
