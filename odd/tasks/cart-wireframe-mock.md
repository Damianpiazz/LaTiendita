# Cart Wireframe + Mock State (frontend)

Objective: make the `Cart Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:526`, 1440 × 1188) render in the Next.js frontend with no backend.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured (vitest installed without config or `test` script), so there is no
runnable RED/GREEN cycle; verification is lint + build + prerendered HTML.

## Specs

S1 — Mock state. Mock mode already exists (`NEXT_PUBLIC_USE_MOCKS`, adapter in `mocks/`); this
screen only needs cart data. The seed becomes the state the frame draws: `auriculares-inalambricos`
×1, `remera-basica-de-algodon` ×2, `botella-termica-750-ml` ×1 — 3 lines, 4 units, subtotal
`$ 56.279` (25.999 + 2×8.990 + 12.300). Prices, names and categories come from the catalog service;
the cart itself stays a client store, so no new HTTP route is added.

S2 — Route. `/carrito` renders the screen; section order is Header, Breadcrumb, Cart, Benefits,
Footer (header/footer come from the public layout). The header cart control links here.

S3 — Breadcrumb. White band with bottom hairline, 48px gutter, 14px text, 16px vertical padding
(49px tall): `Inicio` 14/400 `text/muted`, `/` separator `text/placeholder`, `Carrito` 14/500
`text/primary`. The shared `Breadcrumbs` gains a `size` variant (`sm` = 13px/44px as on the product
screen, `md` = 14px/49px here).

S4 — Steps. Inside the cart section (48px gutter, 40px vertical padding, 32px gap between blocks):
four steps separated by 24×1px `border/default` connectors, 8px gaps. Active step: 24px circle
filled `background/inverse` with `1` in 12/700 white and label 14/500 `text/primary`; the rest:
24px circle filled `background/muted` with the number 12/400 `text/muted` and label 14/400
`text/muted`. Labels: `Carrito`, `Envío`, `Pago`, `Confirmación`. Static: only step 1 is active.

S5 — Title row. `Tu carrito` 24/700, subtitle 14/400 `text/muted`
`Revisá tus productos. El método de envío se elige en el paso siguiente.`, and the unit count on the
right (`4 productos`) 14/400 `text/muted`, driven by the store.

S6 — Items. 944 wide column, 16px gap between cards. Each card is 944×128, horizontal, 16px
padding and gap, radius 12, white, border `border/default`: 96×96 image placeholder (radius 12,
`background/muted`, label `[ Imagen ]` 12), then a 664-wide info column with 8px gap —
category 12/400 `text/muted`, name 16/700 `text/primary`, unit price 14/400 `text/muted`
(`$ 25.999 c/u`), and `Quitar` 14/400 `text/muted` — then a 120-wide controls column with 12px
gap: line total 18/700 `text/primary` and a 120×40 quantity control (radius 8, white, border
`border/strong`) split into 40px decrement / 40px value / 40px increment, the value cell with
1px `border/default` on both sides, symbols `−` and `+` 16/400 `text/secondary`, value 14/500
`text/secondary`. Quantity starts at the stored value, never drops below 1; `Quitar` removes the
line.

S7 — Summary. 376 wide, self-start, vertical, 16px gap, 20px padding, radius 12, white, border
`border/default`. Content: `Resumen del pedido` 16/700; rows 14/400 `text/muted` with the value in
`text/primary` — `Subtotal (4 productos)` / `$ 56.279` and `Envío` / `A calcular`; hairline; `Total`
16/700 with `$ 56.279` 18/700; primary CTA 336×48 radius 8 `background/inverse` with
`Continuar al envío` 16/500 white; secondary CTA 336×48 radius 8 white with border
`border/strong` and `Seguir comprando` 16/500 `text/secondary`; caption 12/400 `text/muted`
`Impuestos incluidos en los precios`. Both CTAs are links (`/checkout/envio` — not built yet —
and `/`).

S8 — Empty state. The design has no empty cart, so the screen adds one assumption: `Tu carrito está
vacío` 24/700, `Todavía no agregaste productos.` 14/400 `text/muted` and a link back to the catalog
(`Ver productos` → `/#productos`). No steps/summary rendered in that state.

S9 — Shared shell. `BenefitsBar` and `SiteFooter` already measure to `0:645` / `0:666` (white
band, 40px vertical padding, 48px gutter, 24px gap, 44px icon squares; footer 48px padding, 32px
gap, top hairline, brand column 300, 28px social squares, 13/700 titles, 13 links, centered legal
bar `© 2026 La Tiendita`). No changes expected; the only shared edits are `Breadcrumbs` (size
variant) and `CartBadge` (becomes a link).

S10 — Structure. New code lives in `features/cart/{components,helpers,pages}`; the route is
`app/(public)/carrito/page.tsx`; the store keeps its current shape plus `setQuantity` and
`removeItem`. Existing dependencies only — no new packages.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1, S2 | inline | done | `mocks/data/cart.ts` seed (3 lines / 4 units); `store/cart-store.ts` `setQuantity`/`removeItem`; `cart-badge.tsx` becomes a `/carrito` link |
| T2 | S1, S6, S7 | inline | done | `features/cart/helpers/cart-totals.ts` (`buildCartLines`, `countUnits`, `sumSubtotal`) |
| T3 | S4–S7 | inline | done | `checkout-steps.tsx`, `cart-item-row.tsx`, `order-summary.tsx`, `cart-empty.tsx` |
| T4 | S2, S6–S8, S10 | inline | done | `cart-view.tsx`, `pages/cart-page.tsx`, `app/(public)/carrito/page.tsx`, `features/cart/index.ts` |
| T5 | S3 | inline | done | `Breadcrumbs` `size` prop (`sm` 13px/44px, `md` 14px/49px); cart uses `md` |
| T6 | all | inline | done | lint exit 0; build exit 0 (`/carrito` static); 31/31 copy assertions; 8/8 shared-shell regressions |

## Log

L1 (verbatim): "Quiero implementar la pantalla Product Cart Wireframe en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que exixiten en el package.json del frontend"

- Frame read live (`tab-1`, page `0:3`, frame `0:526`): Header 72, Breadcrumb 49, Cart 638
  (steps 24 + title 54 + content 416 with 32px gaps and 40px vertical padding), Benefits 124,
  Footer 305.
- Design inconsistency: the Home frame's header badge shows `2`, the Cart frame's shows `4`. Each
  frame carries its own mock state. The single store is seeded with the cart frame's state, so the
  Home badge will read `4`; recorded as a known deviation instead of forking the cart data.
- Mock mode already exists from the Home feature; the cart screen needs no new adapter route
  because the cart is local client state resolved against the cached catalog.
- Assumption: empty cart state (S8) is not designed; kept minimal and outside the wireframe grid.
- T6 evidence (run on `C:\Repositorios\LaTiendita\frontend`):
  - `npm run lint` → exit 0.
  - `npm run build` → exit 0, 14 static pages; `/carrito` builds as a static route (`○`) with a full
    prerendered HTML file at `.next/server/app/carrito.html`.
  - Copy assertions on `carrito.html`: 31/31 pass after removing React's `<!-- -->` separators;
    `$ 56.279` appears exactly twice (subtotal and total).
  - Shared-shell regressions: 8/8 — Home and the product detail still render, the header cart
    control links to `/carrito` with the `4 unidades` label, and both breadcrumb variants keep
    their own band (`13px` on the detail, `14px` on the cart).
- Three fidelity corrections after the first render, all against frame values: inactive step circle
  is `background/muted` in tokens but `#e4e4e7` in the frame, so it uses `bg-wf-subtle`;
  `A calcular` is `text/muted`, not `text/primary`; the item image label is 12px.
- Deviation from S1, as recorded above: seeding the store with the cart frame's state makes the
  Home header badge read `4`, while the Home frame draws `2`.
- Not verified: quantity changes and `Quitar` were reviewed in code but never executed — the page
  was not opened in a browser and there is still no test runner, so interactions have no runtime
  evidence. Layout is asserted from frame measurements and markup, not visually.
- Risk: item 3 — `CartBadge` (shared header) now renders a link and the seeded cart changed, and
  `Breadcrumbs` gained a variant used by the product detail, so Home and the detail screen are
  affected by this feature.
