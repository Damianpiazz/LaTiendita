# Checkout Shipping Wireframe + Mock State (frontend)

Objective: make the `Checkout Shipping Wireframe` frame from `docs/OpenPencil/LaTiendita.fig`
(page `0:3`, frame `0:1105`, 1440 × 1195 after L4) render in the Next.js frontend with no backend.
The frame is the source of truth: the design is edited first and the code follows it.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Mock state. The design has no shipping data source yet, so the screen needs one, shaped like
the rest: `ShippingMethod { id, name }`, one mock entry (`envio-a-domicilio`, `Envío a domicilio`),
served by `GET /shipping/methods` through the existing mock adapter, plus a cached service. Shipping
carries no price and no delivery estimate on screen (L2), so the contract has neither. The cart
subtotal and unit count keep coming from the cart store resolved against the cached catalog.

S2 — Route. `/checkout/envio` renders the screen — the target the cart's `Continuar al envío` button
already points at. Section order: Header, Breadcrumb, Checkout (steps + title + content), Benefits,
Footer.

S3 — Breadcrumb. `md` variant: `Inicio` → `/`, `Carrito` → `/carrito`, `Envío` current 14/500
`text/primary`, separated by `/` in `text/placeholder`.

S4 — Steps. Reuses the cart strip, now parameterized by `currentStep`: a completed step shows `✓`
12/400 white on the inverse circle with its label 14/400 `text/secondary`; the current step shows its
number 12/700 white with the label 14/500 `text/primary`; pending steps keep the `background/muted`
circle, number and label in `text/muted`. The cart keeps `currentStep={1}`; this screen renders
`currentStep={2}`.

S5 — Title block. `Envío` 24/700, with no subtitle: the screen has no method to choose (L2), so the
`Método de envío` title and its subtitle are gone.

S6 — Shipping block. White card with a `border/default` hairline, radius 12, 20px padding and 20px
gaps. Header `Envío a domicilio` 16/700, then a hairline and the address form. The frame's radio,
the delivery estimate, `$ 2.499` and `Gratis desde $ 50.000` are gone (L2): the store only ships to
a home address, so there is nothing to select and no shipping amount to show.

S7 — Address form. No visible title (L4): the card heading `Envío a domicilio` already names the block,
so `Dirección de entrega` was a second heading for the same thing and is gone from the frame; the
`fieldset` keeps it as an `sr-only` legend so assistive tech still gets the group name. 16px gaps
between blocks, label 14/500 `text/secondary` 8px above a 40px input (white, radius 8,
`border/strong`, placeholder 14/400 `text/placeholder`). Fields: `Nombre y apellido` (`Ej. Juana
Pérez`) and `Teléfono` (`Ej. 11 5555 5555`) side by side; `Dirección` (`Calle y número`) full width;
`Ciudad` (`Ej. CABA`), `Provincia` (`Ej. Buenos Aires`) and `Código postal` (`Ej. 1425`) in three
columns; `Referencias (opcional)` (`Piso, depto, entre calles...`) full width; then a 24px checkbox
with `Guardar esta dirección para futuras compras` 14/400 `text/muted`. Inputs are native and carry
`name`/`autocomplete`; nothing is validated or persisted, because the design defines no error state
and no submit target.

S8 — Summary. The cart's summary card moves to the checkout chrome and becomes configurable, with
the shipping row optional. The cart keeps `Envío` / `A calcular` and its own buttons; this screen
omits the row because shipping has no amount on screen, so Subtotal and Total both read `$ 56.279`.
Actions: `Continuar al pago` → `/checkout/pago` (not built yet) and `Volver al carrito` →
`/carrito`.

S9 — Empty cart. Not designed: the screen reuses the cart's `CartEmpty` block (`Tu carrito está
vacío`, `Todavía no agregaste productos.`, `Ver productos →` to `/#productos`) instead of a shipping
form for nothing. The first draft invented a second empty state; reusing the existing one keeps one
copy and does not send an empty cart back to `/carrito`.

S10 — Structure. Shared checkout chrome (steps strip, order summary) moves to
`features/checkout/components`; the shipping screen lives in
`features/shipment/{components,pages,services,types}`; the route is
`app/(public)/checkout/envio/page.tsx`. Existing dependencies only — no new packages.

S11 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: every change to this
screen is made on frame `0:1105` in the design and saved, and only then reflected in the frontend
code. A code change without its design change is incomplete. (L3.)

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | `features/shipment/types/index.ts` (`{ id, name }`), `mocks/data/shipping.ts`, `/shipping/methods` mock route, cached `shipping.service.ts` |
| T2 | S4, S8, S10 | inline | done | `features/checkout/components/{checkout-steps,order-summary}.tsx`; cart page/view updated; `shipping` row optional |
| T3 | S6, S7 | inline | done | `features/shipment/components/{shipping-address-card,address-form}.tsx` |
| T4 | S3, S5, S9 | inline | done | `shipping-view.tsx` (totals + empty guard), `pages/shipping-page.tsx` |
| T5 | S2, S10 | inline | done | `app/(public)/checkout/envio/page.tsx`, `features/shipment/index.ts` |
| T6 | all | inline | done | lint 0; build 0 (16 static, `/checkout/envio` ○); 32/32 copy; 18/18 shared; 38/38 categories |
| T7 | S11, S5, S6, S8 | inline | done | frame `0:1105` edited in OpenPencil and saved: subtitle, radio, description, price block and summary shipping row deleted; `Envío` H1; card stroke 1px `border/default`; card 481; Summary 306; frame 1440 × 1228 |
| T8 | S11, S7 | inline | done | `0:1169` `Form title` deleted and saved (card 448, frame 1440 × 1195); `legend` → `sr-only`; lint 0; build 0; 33/33 copy; 18/18 shared |

## Log

L1 (verbatim): "Quiero implementar la pantalla Checkout Shipping Wireframe en modo mock el frontend
de LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frame read live (`tab-1`, page `0:3`, frame `0:1105`): Header 72, Breadcrumb 49, Checkout 724
  (steps 24 + title 54 + content 502 with 32px gaps and 40px vertical padding), Benefits 124,
  Footer 305.
- Content arithmetic: 944 (methods) + 24 (gap) + 376 (summary) = 1344; the card's 502 =
  20 + 40 + 20 + 1 + 20 + 381 + 20, which is the 20px padding/gaps around header, divider and form.
- Design inconsistency: the method says `Gratis desde $ 50.000` while the same frame charges
  `$ 2.499` on a `$ 56.279` subtotal and totals `$ 58.778`. L2 resolves it by removing the shipping
  amount from the screen entirely.
- First build failure: `ShippingView` (client) imported `CartEmpty` from the `@/features/cart`
  barrel, which re-exports `CartPage` and drags `catalog.service` (inline `"use cache"`) into the
  client graph; Turbopack rejects that. Fixed by importing `@/features/cart/components/cart-empty`
  directly. Barrels are safe only when every module they re-export is client-safe.

L2 (verbatim): "Actualizame La Pantalla y la page para Envios, los envios solo seran a una domicilio
o direccion por lo tanto es redundante que haya una forma de seleccionar envios. siempre va a ser
igual Envío a domicilio
Llega en 3–5 días hábiles a tu puerta. esto no va. ademas no pongas para indicar el precio del
envio."

- Requirement change applied to S1, S5, S6, S8; S9 corrected, S2/S3/S4/S7/S10 unchanged.
- Selection removed: the store only delivers to a home address, so the frame's radio is gone and the
  card states the single method as a heading. `ShippingMethod` drops `description`, `price` and
  `freeFrom`, because nothing renders them anymore and dead contract fields are worse than none.
- Card outline goes from the frame's 2px `border/strong` to the 1px `border/default` used by every
  other surface: the 2px was the selected state, and there is no selection left.
- No shipping amount anywhere: the card shows none, `OrderSummary`'s `shipping` row became optional,
  and this screen omits it, so Total equals Subtotal (`$ 56.279`). The cart keeps `Envío` /
  `A calcular`.
- H1 goes from `Método de envío` to `Envío` and loses its subtitle, because there is no method to
  choose.
- T6 evidence (run on `C:\Repositorios\LaTiendita\frontend`):
  - `npm run lint` → exit 0.
  - `npm run build` → exit 0, 16 static pages; `/checkout/envio` builds as a static route (`○`).
  - Copy assertions on `checkout/envio.html`: 32/32 — breadcrumb, steps (`2` current, `Carrito`
    ticked), `Envío` title, `Envío a domicilio` as the card heading, the seven fields with their
    placeholders and autocomplete tokens, the checkbox, the summary with `$ 56.279` twice and the
    two CTAs, plus the negative checks: no `Método de envío`, no delivery estimate, no `$ 2.499`, no
    `Gratis desde`, no radio.
  - Shared regressions: 18/18 — the cart keeps its step strip (`currentStep={1}`), `A calcular` and
    `$ 56.279`, Home keeps its 5 tiles and badge, the detail keeps its price and 13px breadcrumb,
    and Categories keeps its 12 tiles and 5 filters. Categories copy: 38/38.
- Not verified: the inputs, the radio-free card and the checkbox were reviewed in code but never
  driven in a browser — no test runner, so nothing covers typing, the checkbox toggle or focus
  rings. Layout is asserted from frame measurements and markup, not visually.
- Risk: none — new route and new feature folder, an internal prop made optional without changing
  what the cart renders (asserted), and no stored data, auth, published API, concurrency or delivery
  surface touched.

L3 (verbatim): "No modificaste la Pantalla de OpenPencil Primero tenias que Hacer el nuevo diseño
Sobre la pantalla que ya exisiste y luego implementarlo en codigo del frontned"

- Process correction: L2 was implemented in code only. `Pantalla` meant the OpenPencil frame and
  `page` the Next route; the design has to be edited first. S11 added to make the order explicit.
- Frame `0:1105` edited in place on page `0:3`, document `tab-1`, and saved to
  `docs/OpenPencil/LaTiendita.fig`:
  - `0:1152` Title `Método de envío` → `Envío`; `0:1153` Subtitle deleted (Title block 54 → 29).
  - `0:1159` `Radio selected` deleted, `0:1163` `Description` deleted, `0:1164` `Price` deleted
    (card header 40 → 19, card 502 → 481).
  - `0:1156` stroke 2px `border/strong` → 1px `border/default` (`#E4E4E7`, rebound to the
    `border/default` variable after `set_stroke` dropped the binding); renamed
    `Method - Envío a domicilio` → `Shipping - Envío a domicilio`, and `0:1158` `Option` →
    `Method name`, because there is no option left to pick.
  - `0:1208` summary row (`Envío a domicilio` / `$ 2.499`) deleted; `0:1214` Total `$ 58.778` →
    `$ 56.279` (Summary 339 → 306).
  - Auto-layout reflowed the page: Checkout 724 → 678, frame `0:1105` 1440 × 1274 → 1440 × 1228.
- Design verification: visible text of the frame (71 nodes) has no `Método de envío`, no
  `Llega en 3–5 días hábiles a tu puerta.`, no `$ 2.499`, no `Gratis desde $ 50.000`, no `$ 58.778`
  and no `Elegí cómo querés recibir…`; it reads `Envío` / `Envío a domicilio` / `Subtotal (4
  productos) $ 56.279` / `Total $ 56.279`. `analyze_overlaps` reports 4 findings, none of them inside
  frame `0:1105` (they are pre-existing, in Home and a `0:371`-`0:380` group). `lint` reports 228
  errors on the page, all pre-existing `color-contrast` on placeholder text in other wireframes.
- Code reconciliation: no code change was needed — the L2 implementation already matched the updated
  design (`wf-line` = `#e4e4e7` = `border/default`, 1px card border, `Envío` H1 with no subtitle,
  no shipping row, Total = Subtotal). The 32/32 + 18/18 run from L2 still stands, since no code moved.
- Not verified visually: this model cannot read images, so the exported PNG was written to disk
  (`Temp/opencode/shipping-after.png`) but not inspected. Geometry was asserted from node properties
  and the text dump.
- Risk: none — a design file in `docs/` and its mirror; no code, data, auth, API or delivery surface.

L4 (verbatim): "Fijate que quedo Dirección de entrega suelto que no tiene sentido"

- Redundancy left by L2/L3: with the choice model gone, the card reads `Envío a domicilio` and then
  the form repeated `Dirección de entrega` as a second heading for the same block.
- Design: `0:1169` `Form title` deleted from `Address form` and the file saved. Auto-layout reflowed
  the form 381 → 348, card 481 → 448, Content 448, frame `0:1105` 1440 × 1228 → 1440 × 1195. The
  card tree now reads Header `Envío a domicilio` → Divider → two-up field row → `Dirección` →
  three-up row → `Referencias` → checkbox.
- Code: the `legend` stays but becomes `sr-only`. Deleting it would have left a `fieldset` with no
  accessible name, and the visible text was the only thing wrong — a screen reader still needs to
  hear the group.
- Verification: `npm run lint` exit 0; `npm run build` exit 0, 16 static pages, `/checkout/envio`
  still static (`○`); copy assertions 33/33 — the old `form title` check became `group name kept for
  assistive tech` (`class="sr-only">Dirección de entrega</legend>`) plus `no visible form title` (no
  `p-0 text-sm font-medium text-wf-ink` legend class left); shared regressions 18/18.
- Visible text of the frame no longer contains `Dirección de entrega`.
- Risk: none — one presentational class on an existing element plus its design counterpart; no data,
  auth, API or delivery surface.
