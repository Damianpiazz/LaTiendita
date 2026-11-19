# Auth Wireframes + Mock State (frontend)

Objective: render the `Login Wireframe`, `Register Wireframe` and `Password Recovery Wireframe`
frames from `docs/OpenPencil/LaTiendita.fig` (page `0:3`, frames `0:695`, `0:766` and `0:847`)
in the Next.js frontend with no backend, using the public shell that already ships
`SiteHeader` + `SiteFooter`.
Branch: `main` (working tree already holds an uncommitted scaffold; no branch created).
Delivery: `ask-on-risk`, no PR opened.
Checks: `npm run lint`, `npm run build`, prerendered-HTML assertions against the design copy.
No test runner is configured, so verification is lint + build + prerendered HTML.

## Specs

S1 — Design first. `docs/OpenPencil/LaTiendita.fig` is the source of truth: each frame is read
before the code and edited only when it has to change. All three frames already define their
screen completely, so this task verifies them and follows them; no design edit was needed.

S2 — Routes and shell. `/ingresar`, `/crear-cuenta` and `/recuperar-password` live under
`app/(public)`, whose layout already renders `SiteHeader` and `SiteFooter`; the screens only add
the card. No admin shell, no sidebar. The header's `Ingresar` control becomes `Link
href="/ingresar"` so the login screen is reachable from every public page.

S3 — Shared auth card. 400px wide, white with a `border/default` hairline, 12px radius, 32px
padding, 24px between blocks. The card is centred in a section with 64/48 padding. Title 24/700
ink, subtitle 14/400 muted, 8px below it.

S4 — Login. Title `Ingresá a tu cuenta`, subtitle `Comprá más rápido y guardá tus favoritos.`
Form (16px gap): `Email` with `tu@email.com`, `Contraseña` with `••••••••`, then the
right-aligned `¿Olvidaste tu contraseña?` (14/500 `#52525B`) linking to `/recuperar-password`.
Submit `Ingresar` (48px, inverse fill, 16/500 white). Footer: 1px separator, then the centred
line `¿No tenés cuenta?` + `Creá tu cuenta` linking to `/crear-cuenta`.

S5 — Register. Title `Creá tu cuenta`, subtitle `Completá tus datos para comprar en La Tiendita.`
Form: `Nombre y apellido` (`Ana Pérez`), `Email` (`tu@email.com`), `Contraseña` (`••••••••`) with
the 12/400 muted hint `Mínimo 8 caracteres, una mayúscula y un número.`, `Confirmar contraseña`
(`••••••••`), and the terms row: a 16 × 16 square checkbox (white, `border/strong` hairline, no
radius) with a 12px gap to `Acepto los términos y condiciones y la política de privacidad`
(14/400 muted). Submit `Crear cuenta`. Footer: `¿Ya tenés cuenta?` + `Ingresá` linking to
`/ingresar`.

S6 — Password recovery. Title `Recuperá tu contraseña`, subtitle `Ingresá tu email y te enviamos
las instrucciones.` Form: `Email` (`tu@email.com`). Submit `Enviar instrucciones`. Footer:
`¿Recordaste tu contraseña?` + `Ingresá` linking to `/ingresar`. Below the footer block, as a
card-level line separated by the card's own 24px gap, the 12/400 centred note
`Si no encontrás el email, revisá tu carpeta de spam.`

S7 — Fields. Block with 8px gap: label 14/500 ink, then a 48px input with 16px horizontal
padding, 8px radius, white fill and a `border/strong` hairline; placeholder 14/400
`ink/placeholder`. The design defines no error state and no helper text except the password
hint, so nothing is validated.

S8 — Presentational forms. No auth endpoint exists in the mock, so the submit controls are
`type="button"` and never send anything; no loading, success or error state is invented. Links
between the three screens go to screens that exist, so they are real `Link`s.

S9 — Structure. Components in `features/auth/components/`, pages in `features/auth/pages/`,
barrel in `features/auth/index.ts`, routes in `app/(public)/`. Existing dependencies only —
no new packages.

S10 — Header contract. `Ingresar` in `components/layout/site-header.tsx` keeps its measured look
(40px, inverse fill, 18px horizontal padding, 14/500) but becomes a `Link`, which touches a
component shared by every public page; the rest of the header is unchanged.

S11 — Responsive fallback, not designed. Below the card's 400px width the section padding drops
to 24px so the card keeps its own 32px inner padding; desktop measurements stay as measured.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S1 | inline | done | frames `0:695`, `0:766`, `0:847` read live; no design edit needed |
| T2 | S3, S7 | inline | done | `auth-card.tsx`, `auth-field.tsx`, `auth-submit.tsx`, `auth-footer.tsx` |
| T3 | S4 | inline | done | `login-form.tsx` + `pages/login-page.tsx` |
| T4 | S5 | inline | done | `register-form.tsx` + `pages/register-page.tsx` |
| T5 | S6 | inline | done | `password-recovery-form.tsx` + `pages/password-recovery-page.tsx` |
| T6 | S2, S9, S11 | inline | done | `app/(public)/ingresar`, `crear-cuenta`, `recuperar-password` + barrel |
| T7 | S10 | inline | done | `site-header.tsx`: `Button` → `Link href="/ingresar"` |
| T8 | all | inline | done | lint clean, build 60 pages, HTML assertions 36/36, regressions 55/55 |

## Log

L1 (verbatim): "Quiero implementar las pantallas Admin Pagos Wireframe, Admin Clientes Wireframe, Password Recovery Wireframe, Register Wireframe, Login Wireframe, en modo mock el frontend de
LaTiendita (OpenPencil MCP), Primero entende la estructuras del frontend, luego quiero que me
prepares un modo mock, y luego implementame la pantalla respetando la estructura de carpetas
definida. utilizando las librerias y dependencias que existen en el package.json del frontend"

- Frames read live (page `0:3`): `0:695` 1440 × 972 = header 72 + auth 595 + footer 305;
  `0:766` 1440 × 1190 = 72 + 813 + 305; `0:847` 1440 × 889 = 72 + 512 + 305.
- Card heights check out from the measured blocks: login 467 = 32 + 54 + 24 + 195 + 24 + 48 + 24 +
  34 + 32; register 685 = 32 + 54 + 24 + 413 + 24 + 48 + 24 + 34 + 32, with the form's 413 =
  73 + 16 + 73 + 16 + 96 + 16 + 73 + 16 + 34; recovery 384 adds the 15px spam note under the
  footer block (24px card gap), which is why the note is a card child and not part of
  `AuthFooter`.
- Measured ink: title `#18181B`, subtitle and hint `#71717A`, labels `#18181B`, input hairline
  `#A1A1AA`, placeholders `#A1A1AA`, switch link and forgot link `#52525B`, separator `#E4E4E7`,
  submit `#27272A` with `#FFFFFF` copy.
- Route names follow the store's Spanish URLs (`/carrito`, `/categorias`, `/checkout/envio`).
- `Ingresar` changed from `Button` to `Link`: the button was drawn by the frame but the login
  screen now exists, and a control without a destination is the only reason the other mock
  actions stay presentational. Risk: item 3 — `site-header` is consumed by every public page;
  regression assertions across all 55 prerendered pages cover the change.
- Risk: item 3 — the shared public header changes; everything else is new files plus additive
  barrel exports.
