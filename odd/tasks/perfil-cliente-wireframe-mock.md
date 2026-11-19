# Perfil Cliente Wireframe + Mock State (frontend)

Objective: render the `Perfil Cliente Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `2101537842:6`, 1440 × 1788) in the Next.js frontend with no backend.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened. Authored lines forecast: ~750 (docs + components).
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth and is read before the
code. The page carries two frames with the same name: `2101537842:5` is empty (1440 × 0) and
`2101537842:6` (1440 × 1788) is the drawn screen; measurements come from the latter only.

S2 — Route and shell. `/mi-cuenta` lives in a new `app/(account)` route group whose layout renders
`SiteHeader` + `main` + `SiteFooter`, the same public shell every other screen uses. `(public)` is
not touched, so nothing regresses on the 55 already-built pages.

S3 — Header account control. The frame replaces the `Ingresar` button with an `Account` group:
24px avatar with the customer initials (`LF`, 12/700 on `subtle`) and the label `Mi cuenta`
(14/400), 8px gap, inside the 40px action row. `components/layout/site-header.tsx` gains one
**optional** `account` prop; without it the header renders exactly as today, so every existing page
keeps its `Ingresar` link.

S4 — Breadcrumb. White 49px band with bottom hairline, 16px vertical / 48px horizontal padding:
`Inicio` (14/400 muted), `/` (14/400 `ink/placeholder`), `Mi cuenta` (14/500 ink), 8px gap.

S5 — Account content. Gray `#F5F5F6` band with `pt-10 pr-12 pb-16 pl-12` inside a 1440px container:
one row with 24px gap holding the 240px account nav and the 1080px main column. The main column
stacks its six blocks with 32px gaps.

S6 — Account nav. 240px white card, 12px radius, hairline, 16px padding, 4px gap: `MI CUENTA`
(12/400 muted, uppercase) then the entries `Resumen`, `Mis pedidos`, `Direcciones`,
`Datos personales`, `Newsletter`, each 40px with 12px padding, 12px gap and a 20px icon placeholder;
the active `Resumen` is `bg inverse` with 14/500 white, the rest 14/400 `#3F3F46`. After the group:
8px spacer, 1px divider, 8px spacer, then `Cerrar sesión`. Icons are 20 × 20 placeholder squares
(`subtle`, `line/strong` when active), the same convention the admin sidebar already uses.

S7 — Page header. 54px: `Hola, Lucía` (24/700) over
`Revisá tus compras, tu actividad y tus datos en un solo lugar.` (14/400 muted), 8px gap; on the
right the 40px outlined `Editar perfil` control (`#A1A1AA` hairline, 16px padding, 14/500 `#3F3F46`).

S8 — Profile card. 127px white card, 12px radius, hairline, 24px padding, 24px gap: 64px avatar
with `LF` (24/700 `#3F3F46` on `subtle`), identity column (8px gap) with `Lucía Fernández` (18/700),
`lucia.fernandez@mail.com  ·  +54 9 11 5555-0142` (14/400 muted), and the status row (8px gap) with
the `Cliente activo` pill (24px, inverse, 12/400 white) plus `Miembro desde marzo 2025` (12/400
muted); on the right, right-aligned `ID de cliente` (12/400 muted) over `CU-10428` (14/500).

S9 — Stats strip. 81px card, 12px radius, hairline, 20px vertical / 24px horizontal padding: four
figures sharing the width through `grow`, separated by three 1 × 40 hairlines. `Pedidos` / `12`,
`Total gastado` / `$ 184.500`, `Última compra` / `12 mar 2026`, `Reseñas` / `3`; labels 12/400
muted, values 18/700 ink.

S10 — Activity. Section with 16px gap: head (`Actividad reciente` 24/700 +
`Ver toda la actividad →` 14/400 `#3F3F46`) over a white card with 12px radius and hairline. Four
60px rows with 20px horizontal padding and 16px gap, bottom hairline except the last: date 96px
(12/400 muted), description growing (14/400 `#3F3F46`), a 24px status pill and the `→` chevron
(14/400 `ink/placeholder`). Rows:
`12 mar 2026` / `Pedido #1042 entregado` / `Entregado` (inverse);
`28 feb 2026` / `Pedido #0987 enviado` / `Enviado` (subtle);
`14 feb 2026` / `Reseña publicada · Auriculares inalámbricos` / `Publicada` (subtle);
`02 feb 2026` / `Dirección de entrega actualizada` / `Actualizado` (hairline outline).

S11 — Purchases. Section with the same head: `Mis compras` 24/700 +
`Ver todos los pedidos →` 14/400 `#3F3F46`. Card with a 40px head on `canvas` (`Pedido`, `Fecha`,
`Estado`, `Total` and the right-aligned `Acción`, 12/400 muted) and three 56px rows, 20px horizontal
padding, bottom hairline except the last. Columns 160 / 200 / 200 / 180 and the action growing.
Rows: `#1042` / `12 mar 2026` / `Entregado` / `$ 42.900` / `Ver detalle →`;
`#0987` / `28 feb 2026` / `Enviado` / `$ 31.500` / `Ver detalle →`;
`#0951` / `14 feb 2026` / `Entregado` / `$ 18.200` / `Ver detalle →`. Ids and totals are 14/500 ink,
dates 14/400 muted, actions 14/400 `#3F3F46` right-aligned.

S12 — Details row. Two equal cards, 24px apart, 20px padding, 16px gap, 12px radius, hairline.
`Dirección principal` (16/700) with `Editar` (14/400 `#3F3F46`), then three lines 4px apart —
`Lucía Fernández` (14/400 `#3F3F46`), `Av. Corrientes 1234, Piso 4 B` and
`CABA, Buenos Aires · C1043AAZ` (14/400 muted) — and the `Predeterminada` pill (24px, subtle,
12/400 `#3F3F46`). `Preferencias` (16/700) with `Editar`, then the row `Newsletter` (14/400
`#3F3F46`) and a 44 × 24 inverse switch with a 20px white thumb aligned right (2px padding),
`Recibí novedades y ofertas por email.` (14/400 muted) and the consent row: 24px inverse square
(8px radius) plus `Consentimiento registrado el 02 abr 2025` (12/400 muted), 12px gap.

S13 — Benefits. The frame closes the page with the existing `Benefits` strip (white band, 40px
vertical / 48px padding, four 44px icons); `components/layout/benefits-bar.tsx` is reused, and it is
rendered by this page, not by the group layout.

S14 — Mock data contract. `GET /customer/profile` returns the whole screen as one payload
(identity, stats, activity, orders, address, preferences); the seed is `mocks/data/customer.ts`,
the route is appended to `mocks/routes.ts` and the service is
`features/customer/services/customer.service.ts` with `"use cache"`, mirroring the admin services.
Figures are derived from the seed (the stat strip reads the totals instead of repeating them).

S15 — Presentational actions. No destination exists for `Editar perfil`, `Editar`, `Ver detalle →`,
`Ver toda la actividad →` and `Ver todos los pedidos →`; they are rendered as presentational spans,
the same criterion `AdminClientsHeader` already uses. The nav entries point at the planned
`/mi-cuenta/*` routes and `Cerrar sesión` at `/ingresar`, following the admin sidebar precedent.

S16 — Structure and dependencies. Types in `features/customer/types/`, components in
`features/customer/components/`, page in `features/customer/pages/`, barrel in
`features/customer/index.ts`, route in `app/(account)/mi-cuenta/`. Only packages already in
`frontend/package.json`; no new dependency.

S17 — Responsive fallback, not designed. Below `lg` the account row stacks and the nav goes full
width; below `xl` the stat strip falls back to two columns without the vertical dividers; the
details row becomes one column below `md`; the purchases table scrolls sideways under 1080px.
Desktop measurements stay exactly as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `2101537842:6` read live; `2101537842:5` discarded as empty |
| T2 | S14, S16 | inline | done | `features/customer/types/index.ts`, `mocks/data/customer.ts`, `mocks/routes.ts` (`GET /customer/profile`), `services/customer.service.ts` |
| T3 | S3, S2 | inline | done | optional `account` prop on `site-header.tsx`; `app/(account)/layout.tsx` |
| T4 | S6, S5 | inline | done | `account-nav.tsx` (353px vs 352 drawn), `pages/profile-page.tsx` |
| T5 | S4, S7 | inline | done | `account-breadcrumb.tsx` (49px), `profile-header.tsx` |
| T6 | S8, S9 | inline | done | `profile-card.tsx`, `profile-stats.tsx` |
| T7 | S10 | inline | done | `profile-activity.tsx` + `activity-status-badge.tsx` |
| T8 | S11 | inline | done | `profile-purchases.tsx` + `order-status-badge.tsx` |
| T9 | S12, S13 | inline | done | `profile-details.tsx`, `BenefitsBar` reused |
| T10 | S15, S16, S17 | inline | done | `app/(account)/mi-cuenta/page.tsx`, barrel, responsive classes |
| T11 | all | inline | done | lint 0 errors (1 pre-existing warning), build 61 pages, 48/48 copy probes + 5 structural probes, regressions 56/56 |

## Log

L1 (verbatim): "Quiero implementar las pantallas Perfil Cliente Wireframe, en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (page `0:3`): `2101537842:6` 1440 × 1788 = header 72 + breadcrumb 49 +
  account content 1238 + benefits 124 + footer 305.
- Account content height checks out from the main column: 54 + 127 + 81 + 285 + 253 + 174 = 974
  blocks, plus 5 × 32 gaps = 160, plus 40 top and 64 bottom = 1238 exactly, so the nav (352) is
  shorter than the column and does not drive the band height.
- Measured palette: canvas `#F5F5F6`, surface `#FFFFFF`, subtle `#E4E4E7`, inverse `#27272A`,
  hairline `#E4E4E7`, strong `#A1A1AA`, ink `#18181B`, secondary `#3F3F46`, muted `#71717A`,
  placeholder `#A1A1AA`. All of them already exist as `wf-*` tokens.

- Implementation (T2-T10): one screen under `app/(account)/mi-cuenta` composed from
  `features/customer`: breadcrumb, account nav, page header, identity card, stat strip,
  activity, purchases and the address/preferences row. `AccountNav` is the only client
  component (it reads `usePathname` for `aria-current`); the rest stays server-rendered.
- Mock contract: `GET /customer/profile` seed in `mocks/data/customer.ts`; the stat strip
  and the purchases card derive their figures from it instead of repeating literals.
- Header (S3): `SiteHeader` gained one optional `account` prop. The prerendered header of
  `/mi-cuenta` shows the chip (`LF` + `Mi cuenta`, 40px outlined, 16px padding) between
  the cart control and the gutter; every other page keeps the inverse `Ingresar` link.
- Checks (T11): `npm run lint` 0 errors (only the pre-existing `methodName` warning);
  `npm run build` 61 static pages, `/mi-cuenta` prerendered as static;
  `check-profile.mjs` 48 copy probes + 5 structural probes, all present;
  `check-regressions.mjs` 56/56 prerendered pages, no regressions.
- Deviation on record: the two bottom cards are equal-height (grid stretch) while the frame
  draws 174 and 172; the 2px difference comes from the drawn text line boxes, not from spacing.
