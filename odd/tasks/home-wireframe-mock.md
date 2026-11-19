# Home Wireframe + Mock Mode (frontend)

Objective: make the `Home Wireframe` screen from `docs/OpenPencil/LaTiendita.fig` run in the
Next.js frontend without any backend, through a mock data layer.
Branch: `main` (working tree already holds uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint` (exit 0), `npm run build` (exit 0, `/` prerendered static, revalidate 1h).
No test runner is configured (vitest is installed but has no config or `test` script), so there is
no runnable RED/GREEN cycle; verification is lint + build + prerendered-HTML assertions.

## Specs

S1 — Mock mode. A single switch, `NEXT_PUBLIC_USE_MOCKS` (default `true`), routes every axios
call through the mock adapter in `frontend/mocks/`. `NEXT_PUBLIC_USE_MOCKS=false` sends the same
calls to `NEXT_PUBLIC_API_URL` (default `http://localhost:8080`). Mock routes mirror the future
API: `GET /categories?featured=true`, `GET /products?featured=true`.

S2 — Home route. `/` renders the `Home Wireframe` on a `1440` canvas with `#F5F5F6` page
background, in this order: Header, Hero, Categories, Featured, Benefits, Newsletter, Footer.

S3 — Header. Three groups on `justify-between` (logo / nav / actions), 48px gutter, 16px vertical
padding around 40px controls => 72px, bottom hairline. Logo: 28×28 mark with `background/muted`
and no content (undefined placeholder) + `LA TIENDITA` 18/700. Nav: `Inicio`, `Categorías`,
`Ofertas`, `Contacto` at 14/400, 28px gap. Actions (12px gap): search box 260×40, radius 8,
`background/surface` + `border/strong`, placeholder `Buscar productos...` at 14; `Carrito` as a
40px outlined control with the label at 14 plus an 18×18 inverse badge (10/700) showing `2`;
`Ingresar` 40px inverse button, 18px horizontal padding, 14/500.

S4 — Hero. H1 `Lorem ipsum dolor sit amet, consectetur elit.` (48/700), sub
`Lorem ipsum dolor sit amet, consectetur adipiscing elit Integer nec odio praesent libero sed cursus.`,
CTAs `Comprar ahora` (solid, 48px) and `Ver catálogo` (outline, 48px), image placeholder
560×340 labelled `Imagen 560 x 340`, dashed border.

S5 — Categories. Title `Comprá por categoría` + `Ver todas →`, five cards:
`Tecnología`, `Hogar`, `Ropa`, `Deportes`, `Juguetes`.

S6 — Featured. Title `Productos destacados` + `Ver todo →`, eight cards with category, name,
price and `Agregar al carrito`:
`Tecnología / Auriculares inalámbricos / $ 25.999`,
`Hogar / Lámpara de mesa / $ 14.500`,
`Ropa / Remera básica de algodón / $ 8.990`,
`Deportes / Botella térmica 750 ml / $ 12.300`,
`Tecnología / Teclado mecánico 60% / $ 45.000`,
`Belleza / Set de cuidado facial / $ 19.750`,
`Hogar / Juego de tazas ×6 / $ 11.200`,
`Juguetes / Rompecabezas 1000 piezas / $ 9.800`.

S7 — Benefits. Four items: `Envíos a todo el país` / `Recibilo en 24-48 hs`,
`Pagos seguros` / `Tarjeta, transferencia y más`,
`Devoluciones sin cargo` / `30 días de garantía`,
`Soporte humano` / `Atención real, sin robots`.

S8 — Newsletter. Box with title `Suscribite a las novedades`, copy
`Ofertas, restocks y features nuevos de La Tiendita.`, input placeholder `tu@email.com`,
button `Suscribirme`.

S9 — Footer. Brand + `Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.`
+ three social squares, columns `Tienda` (Productos, Categorías, Ofertas),
`Ayuda` (Envíos, Devoluciones, Preguntas frecuentes), `Contacto` (WhatsApp, Email, Instagram),
legal `© 2026 La Tiendita`.

S10 — Design tokens. Colors and radii come from `docs/design-system/tokens/` and are exposed as
Tailwind theme tokens (`wf-*`); components never hardcode hex values.

S11 — Structure. `app/(public)` route group, `features/<domain>/{components,config,helpers,pages,services,store,types}`,
shared `components/{ui,layout}`, root `config`, `lib/http`, `mocks`. Existing dependencies only.

S12 — Mock cart. Cart badge is seeded with 2 units; `Agregar al carrito` increments it through
`features/cart/store` (zustand, already installed).

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1, S10 | inline | done | `config/env.ts`, `lib/http/client.ts`, `mocks/{adapter,routes,index,data/*}.ts`; build prerendered `/` through the mock adapter |
| T2 | S11, S12 | inline | done | `features/cart/{types,store,components,index}`, `features/product/{types,services,helpers,components,pages,index}`; prerendered HTML shows badge `Carrito, 2 unidades` |
| T3 | S2–S9 | inline | done | `app/(public)/{layout,page}.tsx`, `components/layout/*`, `app/page.tsx` removed, root layout `lang="es"` + metadata; header re-measured against the `Header` frame (L2) |
| T4 | S10 | inline | done | wireframe theme tokens in `app/globals.css`; `button.tsx` base radius switched to `rounded-wf-sm` |
| T5 | S11 | inline | partial | `frontend/README.md` mock + structure sections; `.env.example` write denied by the tool permission layer (defaults live in `config/env.ts`, and `.env*` is gitignored) |

## Log

L1 (verbatim): "Quiero implementar la pantalla Home Wireframe en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que exixiten en el package.json del frontend"

- Design read from `docs/OpenPencil/LaTiendita.fig` (MCP `get_jsx`, node `0:5` and its sections)
  and `docs/design-system/README.md`.
- No commit created: the repository already has an uncommitted scaffold (`frontend/components`,
  `features`, `config`, `lib` are untracked). Committing this work would require sweeping in
  someone else's pending changes, so the change stays in the working tree.
- Font fix: `globals.css` mapped `--font-sans` to itself, so the app rendered with the browser
  default font. Pointed it at the already-loaded `--font-geist-sans`.
- Cart state starts from `mocks/data/cart.ts` (`2 × auriculares-inalambricos`), so the header badge
  renders `2` on the server and hydrates without a mismatch.
- Search box and `Suscribirme` are intentionally inert: no catalog or newsletter route/service
  exists yet, so the wireframe renders the layout without faking a success state.
- Cards and section links point to future routes (`/productos`, `/categorias`,
  `/productos/[slug]`, `/categorias/[slug]`) which 404 until those screens are built; the nav and
  footer anchors (`#categorias`, `#productos`, `#contacto`) resolve on this page.
- Verification evidence (2026-11-18): `npm run lint` exit 0; `npm run build` exit 0 with
  `┌ ○ /  1h  1d`; prerendered `/` HTML contains the eight exact prices, the five featured
  categories (`/categorias/belleza` absent), `Carrito, 2 unidades`, and all section copy.

L2 (verbatim): "Hace que el Header de LaTiendita respete al del Wireframe, tanto el boton de
carrito, como la barra de busqueda que debe ser mas pequeña, como el Logo Que debe estar sin
definir."

- Header measured from the `Header` frame JSX recorded in `docs/IA-SESSIONS/02-OpenPencil-mcp.md`
  (OpenPencil app was not running, so the frame could not be re-read live): logo mark is a bare
  28×28 `background/muted` square, search is a fixed 260×40 box, cart is a 40px outlined control
  with an 18×18 inverse badge, and the three groups sit on `justify-between`.
- Removed the search magnifier and the cart cart-icon: the wireframe has neither; both controls are
  text-only.
- Re-verified after L2: `npm run lint` exit 0, `npm run build` exit 0, `/` still static at 1h; the
  prerendered header renders the empty `bg-wf-muted` mark, `w-[260px]` search, outlined
  `Carrito` control and `px-[18px]` `Ingresar` button.
