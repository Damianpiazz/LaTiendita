# Integrar Cart Wireframe con backend y login de Authorizer

Objective: dejar `Cart Wireframe` consumiendo `/api/carts` real con sesión JWT de Authorizer, sin romper el modo mock ni las pantallas que comparten carrito.
Branch: `main` (preferencia explícita del usuario: el push lo hace él). Delivery: `exception-ok` — commits por unidad de trabajo, sin PR.
Checks: `npx vitest run`, `npm run lint`, `npm run build` y smoke real con `cart-service` + gateway + Authorizer.

## Specs

S1 — Alcance (petición literal del usuario): "QUiero ahora que me hagas la integracion completa entre frontend y backend de la pantalla de OpenPencil Cart Wireframe. tiene que quedar el frontend consumiendo al backend. usa las skills necesarias." Aceptación: con `NEXT_PUBLIC_USE_MOCKS=false` la ruta `/carrito` lee y escribe el carrito real a través del BFF de Next; con mocks activos el comportamiento actual del wireframe sigue igual.

S2 — Decisión del usuario (respuesta literal): "Login real con Authorizer". Aceptación: `/ingresar` envía credenciales a Authorizer, el JWT queda en una cookie httpOnly y el carrito lo reenvía; el logout limpia la sesión. Authorizer tenía 0 usuarios, así que el smoke crea uno con `/api/auth/signup`.

S3 — Contrato backend real: `GET /api/carts`; `POST /api/carts/items` con body `{ productId: number, quantity: number }`; `PATCH /api/carts/items/{productId}` con body `{ quantity }`; `DELETE /api/carts/items/{productId}`; `DELETE /api/carts`. DTOs: `CartDto(id, items, subtotal, itemCount, total)` y `CartItemDto(productId, productTitle, imageUrl, quantity, unitPrice, lineSubtotal, priceChanged)`. Cantidad `1..99`; el servidor valida stock y deriva totales. El gateway exige JWT, valida con Authorizer e inyecta `X-User-Id` desde el `sub` del token.

S4 — BFF de sesión: route handlers de Next `app/api/auth/login`, `app/api/auth/logout`, `app/api/auth/session` y `app/api/cart/...`. El access token vive en cookie httpOnly `lt_access_token`; `lt_user` es legible para la UI. Ningún token se expone a JavaScript del browser. `SiteHeader` sin sesión queda idéntico al wireframe; con sesión muestra el chip y una acción de logout.

S5 — Adaptador frontend: `frontend/features/cart/services/cart.service.ts` expone `getCart`, `addCartItem`, `updateCartItem`, `removeCartItem` y `clearCart` contra `/api/cart...` con `baseURL: ""` (mismo origen). `frontend/mocks/adapter.ts` extiende `MockRoute.method` a `"GET" | "POST" | "PUT" | "PATCH" | "DELETE"` y agrega rutas mock de auth y carrito. `useCartStore` conserva `items`, `addItem`, `setQuantity`, `removeItem`, `clear` y `selectCartCount`, y agrega `hydrate`, `error` y `requiresAuth` con actualización optimista y reconciliación contra el snapshot. `CartItem` gana campos opcionales del snapshot; `buildCartLines` prefiere precio/título del backend pero conserva el catálogo como fallback.

S6 — Wireframe (cadenas literales): "Tu carrito"; "Revisá tus productos. El método de envío se elige en el paso siguiente."; "4 productos"; "Subtotal (4 productos)"; "Envío" / "A calcular"; "Continuar al envío"; "Seguir comprando"; "Impuestos incluidos en los precios"; pasos "1 Carrito", "2 Envío", "3 Pago", "4 Confirmación". Los números cambian con datos reales; textos y estructura no.

S7 — Tests (RED → GREEN → REFACTOR cuando aplica): service BFF con method/body de cada verbo, store hydrate/optimista/rollback, totales con snapshot y fallback, login form submit/error, más un test por comportamiento previo tocado (`selectCartCount`, `buildCartLines` sin snapshot, `PurchasePanel`). Verificación final: `npx vitest run`, `npm run lint`, `npm run build`.

S8 — Smoke real y documentación: levantar `cart-service`, crear usuario `cliente` vía `/api/auth/signup`, recorrer login → agregar → cantidad/quitar en carrito → totales; documentar cualquier variable nueva. No tocar `docs/OpenPencil/LaTiendita.fig`.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S2, S4, S7 | inline | pending (auth entregada, ya desbloqueada) | Auth en commits `e5ffd25` y `bc998ce`; ver `odd/tasks/integrar-auth-wireframes-backend.md` |
| T2 | S3, S5, S7 | inline | pending | — |
| T3 | S1, S5, S6, S7 | inline | pending | — |
| T4 | S2, S3, S8 | inline | pending | — |

Forecast de líneas autoras: ~650 (T1 ~180, T2 ~160, T3 ~240, T4 ~70), por encima del heurístico de 400; estrategia `exception-ok` por la preferencia de trabajo directo en `main` sin PR.

## Log

L1 (verbatim): "QUiero ahora que me hagas la integracion completa entre frontend y backend de la pantalla de OpenPencil Cart Wireframe. tiene que quedar el frontend consumiendo al backend. usa las skills necesarias."

- Mapa inicial (2026-11-20): el carrito frontend es un store de Zustand sembrado con `seedCartItems`; `CartPage` sólo trae catálogo; `PurchasePanel`, `CartBadge` y `CartItemRow` comparten el store. El backend real `cart-service` ya expone el contrato completo, pero el gateway exige JWT y el frontend no tiene sesión conectada (el login es presentacional). Authorizer responde `POST /v1/login` y `/v1/signup` vía `/api/auth/**`; su DB tenía 0 usuarios.
- L2 (verbatim, respuesta a la decisión de producto): "Login real con Authorizer".
- La elección amplía el alcance a autenticación: BFF de Next con cookie httpOnly, formulario conectado, header con logout y smoke con usuario real. No se usa token dev ni se deja el carrito en mocks en modo real.
- `docs/OpenPencil/LaTiendita.fig` ya estaba modificado antes de este trabajo: se deja fuera de todos los commits.
- Skills cargadas: `vercel-react-best-practices`, `vitest`, `ui-ux-pro-max`, `work-unit-commits`.
- RDD: switch global en `on`; reviewed boundary inicial `778ed53`. Cada unidad se evalúa contra ese límite y el que corresponda tras cada acknowledged.
- L3 (verbatim, cambio de prioridad del usuario): "Mira primero Implementame la integracion de las pantallas Login Wireframe , Register Wireframe, Password Recovery Wireframe, quiero la integracion completa entre frontend y backend. completamente funcional."
- 2026-11-20: el usuario pide resolver primero las tres pantallas de autenticación. Se crea `odd/tasks/integrar-auth-wireframes-backend.md`; esta feature queda en espera y su T1 depende de que la sesión real quede funcionando. Los archivos de sesión ya escritos se reasignan a la feature de auth.
- 2026-11-20: auth entregada y verificada en `e5ffd25` + `bc998ce` (login, register y recovery reales con JWT httpOnly). La dependencia de T1 quedó resuelta; esta feature puede continuar.
