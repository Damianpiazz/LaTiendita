# Admin Categorías Wireframe + Mock State (frontend)

Objective: render the `Admin Categorias Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:2093`, 1440 × 828) in the Next.js frontend with no backend, inside the admin
shell already delivered by `Admin Resumen`.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: the frame is read before
the code and edited only when it has to change. Frame `0:2093` already defines this screen
completely, so this task verifies it and follows it; no design edit was needed.

S2 — Reuse the shell. The admin area keeps its route group `app/(admin)` and `AdminShell` (sidebar
240 + topbar 72) untouched; only the page under `/admin/categorias` is new. The sidebar entry
`Categorías` already points there and becomes active through `usePathname`.

S3 — Page header. `Categorías` 24/700, subtitle `6 categorías · 48 productos en total` 14/400 muted.
Right side: `+ Nueva categoría` (155 × 40, inverse, white 14/500). The creation form is out of
scope, so the action is presentational, same criterion as the previous admin screens.

S4 — Toolbar. Full width, 40 tall, search field 320 × 40 radius 8, white with a `border/strong`
outline, 14/400 `Buscar categoría...`; on the right the presentational sort label
`Ordenar: A–Z ↓` in 14/500 secondary. The frame draws no menu behind either control.

S5 — Categories card, 700 wide. Header 78 tall (20 padding): `Todas las categorías` 16/700,
`6 categorías activas en la tienda` 12/400 muted, `Ver en tienda ↗` 14/500 secondary. Six rows 72
tall, 20 horizontal padding, 12 gap, bottom hairline: 40 × 40 radius 8 icon placeholder, name 14/500,
slug `/categorias/<slug>` 12/400 muted, `N productos` 12/400 muted, chevron `›` 14/400 placeholder.
`Remeras` is the selected row: `background/canvas` fill and `background/muted` icon; the rest are
white with a `background/subtle` icon. Footer 40 tall with a top hairline:
`Mostrando 6 de 6 categorías` and `Última actualización: hoy 09:12`, both 12/400 muted.

S6 — Detail card, 412 wide, 20 padding, 16 gap, radius 12, hairline border. Header:
`Detalle de la categoría` 16/700 and a `···` placeholder. Image placeholder 124 tall radius 12
`background/subtle` with `[ Imagen ]` 12/400 `ink/subtle`. Identity: name 18/700 and slug 12/400
muted. Description 14/400 secondary. Two 1px dividers frame the stats: `14`/`productos` and
`3`/`con stock bajo`, values 18/700 left and right, labels 12/400 muted. Actions: `Editar categoría`
40 inverse, `Ver en tienda ↗` 40 outlined, `Eliminar categoría` 32 outlined 12/500 muted, all
full width.

S7 — Width arithmetic. Content inner width 1136 = 700 (card) + 24 (gap) + 412 (detail). Content
756 tall = 32 + 54 header + 24 + 40 toolbar + 24 + 550 layout + 32. The card's 550 is
78 header + 6 × 72 rows + 40 footer.

S8 — Mock state. The screen comes from `GET /admin/categories`: the six categories with their
description, product count and low-stock count, the selected slug and the `hoy 09:12` label.
Totals are derived, not copied: `6 categorías · 48 productos en total` from the list and the sum of
counts (14 + 9 + 8 + 6 + 9 + 2), `6 categorías activas en la tienda` and
`Mostrando 6 de 6 categorías` from the list length. The admin seed is independent of the public
catalog seed, exactly like `Admin Resumen`, because the frame reports store totals the catalog
sample cannot back.

S9 — Structure. Types in `features/admin/types/categories.ts`, seed in `mocks/data/admin.ts`, route
in `mocks/routes.ts`, cached service `getAdminCategories` in `features/admin/services/admin.service.ts`,
components in `features/admin/components/` and page in `features/admin/pages/`. Existing dependencies
only — no new packages.

S10 — Responsive fallback, not designed. Below `xl` the detail card stacks under the list; below
`md` the toolbar wraps and the card keeps full width. Desktop measurements stay as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frame `0:2093` read live; no design edit needed |
| T2 | S8 | inline | done | `features/admin/types/categories.ts`, `mocks/data/admin.ts`, `GET /admin/categories` |
| T3 | S8 | inline | done | `getAdminCategories` with `"use cache"` + `cacheLife("hours")` |
| T4 | S3, S4 | inline | done | `admin-categories-header.tsx`, `admin-categories-toolbar.tsx` |
| T5 | S5, S6 | inline | done | `category-list.tsx`, `category-detail-card.tsx` |
| T6 | S2, S7, S9 | inline | done | `admin-categories-page.tsx` + `app/(admin)/admin/categorias/page.tsx` |
| T7 | all | inline | done | lint, build, `check-admin-categorias.js` |

## Log

L1 (verbatim): "Quiero implementar las pantallas Admin Categorias Wireframe, Admin Productos Wireframe, Admin Pedidos Wireframe, en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (page `0:3`, frame `0:2093`): Sidebar 240 | Main 1200 (Topbar 72 + Content 756).
- The detail image is **372 × 124** inside a 412 card, not a square avatar: it runs full width under
  the header, radius 12, `background/subtle`.
- Selected row: `bg-wf-canvas` (#F5F5F6) with `bg-wf-muted` (#D4D4D8) icon; the other five use
  `bg-wf-subtle` (#E4E4E7). Only `Remeras` is highlighted, and the detail repeats it.
- Footer carries two labels, not a single range: `Mostrando 6 de 6 categorías` and
  `Última actualización: hoy 09:12`.
- Row names are **14/700** (the node reports `weight: 700`), not medium: `font-bold` in the list.
- Verification: `npm run lint` clean except the pre-existing `methodName` warning in
  `shipping-address-card.tsx`; `npm run build` compiles 55 static pages and prerenders
  `/admin/categorias` as static at 1h/1d; `check-admin-categorias.js` 45/45. Regressions:
  `check-admin.js` 125/125, category 69/69, categories 38/38, checkout-shared 18/18,
  checkout-shipping 32/33 (the same pre-existing shipping heading failure documented in
  `admin-resumen-wireframe-mock.md`).
- Risk: none — new route and new feature files; existing edits are additive mock data, one mock
  route and additive barrel exports, so no existing behavior or shared contract changed.
