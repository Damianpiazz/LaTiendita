# Categories Wireframe + Mock State (frontend)

Objective: make the `Categories Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:913`, 1440 × 1532) render in the Next.js frontend with no backend.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Mock state. The catalog mock grows from 6 to the 12 categories the frame draws, in frame
order, each with its product count: Tecnología 24, Hogar 18, Ropa 32, Deportes 14, Belleza 21,
Juguetes 12, Libros 16, Mascotas 9, Jardín 11, Cocina 27, Audio 19, Accesorios 23. `Category` gains
`productCount: number` (the count is data the listing API returns, not something the client derives
from the 8 mock products). `featured` keeps its current five values so the Home grid is unchanged.
No new HTTP route: `GET /categories` already serves the list.

S2 — Route. `/categorias` renders the screen; section order is Header, Breadcrumb, Categories,
Benefits, Footer (header/footer come from the public layout). The Home `Ver todas` link already
points here.

S3 — Breadcrumb. White band with bottom hairline, 48px gutter, 14px text, 16px padding (49px tall):
`Inicio` 14/400 `text/muted` → `/`, `/` separator `text/placeholder`, `Categorías` 14/500
`text/primary`. Reuses the shared `Breadcrumbs` `md` variant added by the cart screen.

S4 — Title row. `Categorías` 24/700 and subtitle 14/400 `text/muted`
`Encontrá lo que buscás entre nuestras 12 categorías.` on the left; the count on the right
(`12 categorías`) 14/400 `text/muted`, driven by the categories on screen.

S5 — Toolbar. 40px tall row, filters left and sort right. Filters: 8px gap, `Todas` active — 40px
pill, radius full, `background/inverse`, label 14/500 white — and the rest white with a
`border/strong` hairline and label 14/400 `text/secondary`; 16px horizontal padding. The strip is
`Todas` plus the first four featured categories, and selecting one filters the grid to that
category (client state, `aria-pressed`). Sort control: 225×40, 8px gap, 16px padding, radius 8,
white with a `border/strong` hairline, `Ordenar por:` 14/400 `text/muted`, `Destacados` 14/500
`text/primary` and `↓` 14/400 `text/placeholder`. It stays presentational: the design has no menu
for it, so it renders no interaction.

S6 — Grid. 4 columns on the 1344 content width, 24px gaps, 3 rows of 232px. Each tile is a
318×232 link: white, `border/default` hairline, radius 12, clipped; image 318×160 on
`background/muted` with `[ Imagen ]` 12/400 `text/subtle`; body 72px tall with 16px padding and
`space-between` — name 16/500 `text/primary`, count 14/400 `text/muted` 4px below it, and `→`
16/400 `text/placeholder` on the right. Tiles link to `/categorias/<slug>` (the category screen is
not built yet).

S7 — Shared wiring. The header and footer `Categorías` entries currently point to the home anchor
`#categorias`; with the screen in place they point to `/categorias` (a `#categorias` href on
`/categorias` would be a dead link). No other shell change.

S8 — Structure. New code lives in `features/category/{components,pages}`; the route is
`app/(public)/categorias/page.tsx`; the barrel is `features/category/index.ts`. Existing
dependencies only — no new packages.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | `types/category.ts` gains `productCount`; `mocks/data/categories.ts` holds the 12 frame entries |
| T2 | S6 | inline | done | `features/category/components/category-tile.tsx` (318×232, image 160, body 72) |
| T3 | S4, S5, S6 | inline | done | `category-catalog.tsx` (client: title row, filter pills, sort, grid) |
| T4 | S2, S8 | inline | done | `pages/categories-page.tsx`, `app/(public)/categorias/page.tsx`, `features/category/index.ts` |
| T5 | S7 | inline | done | `config/site.ts` nav + footer `Categorías` → `/categorias` |
| T6 | all | inline | done | lint exit 0; build exit 0 (`/categorias` static); 38/38 copy assertions; 12/12 shared regressions |

## Log

L1 (verbatim): "Quiero implementar la pantalla Categories Wireframe en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (`tab-1`, page `0:3`, frame `0:913`): Header 72, Breadcrumb 49, Categories 982
  (title row 54 + toolbar 40 + grid 744 with 32px gaps and 40px vertical padding), Benefits 124,
  Footer 305. 982 = 40 + 54 + 32 + 40 + 32 + 744 + 40.
- Grid arithmetic: 4 × 318 + 3 × 24 = 1344 and 3 × 232 + 2 × 24 = 744, so 4 columns with 24px gaps.
- Filter strip assumption: the frame draws `Todas` plus exactly four category names, and the mock
  catalog's four first featured categories are Tecnología, Hogar, Ropa and Deportes — the strip is
  derived from the catalog (`featured`, first four) and filters the grid in place, so it stays
  correct if the API returns a different order. The frame shows `12 categorías` with `Todas` active,
  which is what the derived count prints.
- Sort control: the frame draws `Ordenar por: / Destacados / ↓` but no menu, so it renders as a
  presentational box. Sorting is therefore not implemented.
- `productCount` is data, not a derived count: the mock catalog has 8 sample products, while the
  frame prints 226 products across the 12 categories.
- T6 evidence (run on `C:\Repositorios\LaTiendita\frontend`):
  - `npm run lint` → exit 0.
  - `npm run build` → exit 0, 15 static pages; `/categorias` builds as a static route (`○`).
  - Copy assertions on `categorias.html`: 38/38 — breadcrumb, title, subtitle with the total, the
    count, sort labels, 12 tiles with their names and counts, 12 category links, 5 filter pills and
    12 image placeholders.
  - Shared regressions: 12/12 — Home still renders 5 featured tiles plus hero and products, nav and
    footer now point to `/categorias` with no `#categorias` anchor left, the cart still totals
    `$ 56.279`, and the detail keeps its `13px` breadcrumb band and price.
- Not verified: the filter pills were reviewed in code but never clicked — no browser run and no
  test runner, so selection has no runtime evidence. Layout is asserted from frame measurements and
  markup, not visually.
- Risk: item 3 — `Category` gained a required field that the Home grid and the mocks consume, and
  the shared `siteConfig` nav/footer entries changed target, so Home and every public screen render
  different links than before.
