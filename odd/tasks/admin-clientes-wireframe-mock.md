# Admin Clientes Wireframe + Mock State (frontend)

Objective: render the `Admin Clientes Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:2852`, 1440 × 999) in the Next.js frontend with no backend, inside the admin
shell already delivered by `Admin Resumen`.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: the frame is read before
the code and edited only when it has to change. Frame `0:2852` already defines this screen
completely, so this task verifies it and follows it; no design edit was needed.

S2 — Route and shell. `/admin/clientes` renders the screen inside the existing `app/(admin)`
layout. The sidebar entry `Clientes` already points there and becomes active through `usePathname`;
the shell is untouched.

S3 — Page header. `Clientes` 24/700, subtitle `128 clientes · 24 nuevos este mes` 14/400 muted.
Right: `Exportar CSV` 40 outlined (14/500 secondary) and `Segmentos` 40 outlined (14/500 muted —
measuring frame `0:2929` shows `#71717A`, so it is not secondary like the export label).
Both are presentational: the frame draws no export or segment flow.

S4 — Stats strip. One 81-tall card, radius 12, white with a hairline, 20 vertical / 24 horizontal
padding, 24 gap, items centred: four stats growing equally, each label 12/400 muted with the value
18/700 ink 4 apart, separated by three 1 × 40 `border/default` dividers.
`Clientes totales 128`, `Nuevos este mes 24`, `Recurrentes 41`, `Ticket promedio $ 27.430`.

S5 — Toolbar. Left: search 320 × 40 radius 8 white with a `border/strong` outline
(`Buscar por nombre, email o teléfono...`, 14/400 placeholder), then four 40-tall radius-full
filters: `Todos` active (inverse fill, white 14/500), `Nuevos`, `Recurrentes`, `Sin compras` (white,
`border/strong` outline, 14/400 secondary). Right: presentational `Ordenar: Última compra ↓`
14/500 secondary.

S6 — Clients card. The frame draws **no card header**: the card starts with the table head. Head
40 tall, `background/canvas`, bottom hairline, 12/400 muted labels, 20 gutters.
Columns: cliente 340, teléfono 150, pedidos 90, total gastado 140, última compra 140, estado 130,
acciones 106 growing (340 + 150 + 90 + 140 + 140 + 130 + 106 = 1096 = 1136 − 2 × 20).
Card 552 = 40 head + 8 × 64 rows.

S7 — Rows. Eight 64-tall rows with a bottom hairline: 36 × 36 radius-full avatar on
`background/subtle` with 12/700 secondary initials, name 14/500 ink + email 12/400 muted, phone
14/400 secondary, order count 14/400 secondary, total spent 14/500 ink, last purchase 14/400 muted,
status pill, and `Ver →` 14/500 secondary on the right.

S8 — Client status pill. 24 tall, radius full, 8 horizontal padding, 12/500. `Recurrente`: inverse
fill, white text. `Activa`: `background/subtle` fill and outline, secondary text. `Nueva` and
`Nuevo`: white, `border/strong` outline, secondary text. `Inactiva`: white, `border/strong` outline,
muted text. The frame prints both `Nueva` (Lucía) and `Nuevo` (Julián, Nicolás) for the same state;
both labels are reproduced verbatim instead of normalising the design's copy.

S9 — Pagination. `Mostrando 1–8 de 128 clientes` 14/400 muted on the left; on the right `←`,
pages `1` (active inverse 40 × 40 radius 8), `2`, `3`, `…`, `16`, and `→`. 128 / 8 = 16 pages, so
the shared `AdminPagination` token builder already yields the frame's `1 2 3 … 16`.

S10 — Mock state. The screen comes from `GET /admin/clients`: totals (`128` clients, `24` new this
month, `41` recurring, `$ 27.430` average ticket), the eight visible rows and the page size.
Subtitle, stats strip and the pagination range derive from those numbers, not copied. The admin
seed is independent of the public catalog seed, exactly like `Admin Resumen`.

S11 — Structure. Types in `features/admin/types/clients.ts`, seed in `mocks/data/admin.ts`, route in
`mocks/routes.ts`, cached service `getAdminClients` in
`features/admin/services/admin.service.ts`, components in `features/admin/components/` and page in
`features/admin/pages/`. Existing dependencies only — no new packages.

S12 — Responsive fallback, not designed. Below `xl` the table scrolls horizontally keeping the
frame's column widths; the stats strip wraps to two columns and the toolbar wraps. Desktop
measurements stay as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `0:2852` read live; no design edit needed |
| T2 | S10 | inline | done | `features/admin/types/clients.ts`, `mocks/data/admin.ts`, `GET /admin/clients` |
| T3 | S10 | inline | done | `getAdminClients` with `"use cache"` + `cacheLife("hours")` |
| T4 | S3, S4 | inline | done | `admin-clients-header.tsx`, `admin-clients-stats.tsx` |
| T5 | S5, S8 | inline | done | `admin-clients-toolbar.tsx`, `client-status-badge.tsx` |
| T6 | S6, S7, S9 | inline | done | `admin-clients-table.tsx` + shared `AdminPagination` |
| T7 | S2, S11, S12 | inline | done | `admin-clients-page.tsx` + `app/(admin)/admin/clientes/page.tsx` |
| T8 | all | inline | done | lint clean, build 60 pages (`/admin/clientes` static), HTML assertions 49/49, regressions 55/55 |

## Log

L1 (verbatim): "Quiero implementar las pantallas Admin Pagos Wireframe, Admin Clientes Wireframe, Password Recovery Wireframe, Register Wireframe, Login Wireframe, en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (page `0:3`, frame `0:2852`): Sidebar 240 | Main 1200 (Topbar 72 + Content 927).
  Content = 32 + 54 header + 24 + 81 stats + 24 + 40 toolbar + 24 + 552 card + 24 + 40 pagination +
  32.
- The card has no header: that is why 552 is exactly 40 + 8 × 64, unlike Productos (552 with a head)
  and Pedidos (632 with an 80 header).
- Stats arithmetic: 4 growing stats + 3 × 1 dividers + 6 × 24 gaps fill the 1136 card inner width
  (1136 − 3 − 144 = 989 ≈ 4 × 247).
- Status copy inconsistency: `Nueva` and `Nuevo` both exist in the frame; both are kept verbatim
  and the badge maps five labels.
- `Segmentos` measured as `#71717A` (muted), not `#3F3F46`; S3 was corrected after reading node
  `0:2929` directly instead of trusting the earlier note.
- The stats strip is a single 81px card of four growing stats with three 1 × 40 hairlines; below
  `xl` it falls back to two columns and the vertical rules are dropped because they no longer
  separate anything.
- Implementation evidence: `npm run lint` (0 errors, only the pre-existing `methodName` warning in
  `shipping-address-card.tsx`), `npm run build` (60 static pages, `/admin/clientes` prerendered),
  prerendered-HTML assertions 49/49 and cross-page regressions 55/55 through a throwaway script in
  the system temp dir, so no verification file lands in the repo.
- Risk: none — new route and new feature files; existing edits are additive mock data, one mock
  route and additive barrel exports.
