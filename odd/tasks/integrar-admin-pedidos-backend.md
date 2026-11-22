# Integrar Admin Pedidos Wireframe con microservicios

Objective: dejar la pantalla `Admin Pedidos Wireframe` (`0:2235`) funcional contra backend real del gateway, sin mock, con separación de responsabilidades y aprovechando el scaffolding existente.
Branch: `main` (el push lo hace el usuario).
Delivery: `exception-ok` — commits por unidad de trabajo, sin PR.

## Specs

S1 (verbatim): "Ahora implementame la integracion back y front de la pantalla Admin Pedidos Wireframe"

Contexto de sesión (verbatim, restricción ya establecida en la tarea anterior): "Integracion ... entre frontend y los microservicios hechos en spring boot. que no sea mockup. usa las skills necesarias para lograrlo. utiliza el mcp de openpencil pàra ver la pantalla. hacelo con buenas practicas escalable sin mucho acoplamiento aprovechando el scaffolding y estructura que ya existe"

S2 — Contrato backend (order-service, vía gateway `/api/orders/**`): `GET /api/orders/admin` acepta `q`, `status`, `page` (0-based Spring), `size`, `sort` y devuelve `Page<AdminOrderResponse>`. `AdminOrderResponse` agrega `customerEmail` y `paymentMethod` opcionales/nullable (sin migración destructiva; fallback null si no hay dato). Nuevo `GET /api/orders/admin/stats` devuelve `AdminOrderStatsResponse { totalOrders, pendingDispatch, cancelledInPeriod, statusCounts { todos, pendiente, pagado, enviado, entregado, cancelado } }`. La pantalla necesita estos totales/status counts; el dashboard existente no se rompe. `GET /api/orders/admin/summary` se mantiene.

S3 — Frontend real: `getAdminOrders` en `frontend/features/admin/services/admin.service.ts` no usa `env.useMocks` ni `/admin/orders`. Compone `GET /api/orders/admin` + `GET /api/orders/admin/stats` en paralelo con Bearer. Paginación UI 1-based, adaptador envía 0-based. Filtros por estado, búsqueda `q`, paginación y labels/totales reflejan datos reales. Email de fila: `customerEmail` o fallback `customerId`. Pago de fila: se resuelve best-effort desde `GET /api/payments/order/{id}` para la página visible (8 máx, `Promise.allSettled`, fallback "—"). `rangeLabel` permanece "Últimos 30 días" (wireframe). Exportar CSV/Imprimir etiquetas/Ver detalle quedan presentacionales si no hay contrato real; se documenta explícitamente.

S4 — Separación de responsabilidades: componentes de tabla/filtros/header reciben datos y callbacks; estado/fetch en workspace/cliente como el patrón de productos; tipos y mappers en `features/admin/types` y `helpers`; sin acoplar UI a Axios/Spring.

S5 — Verificación y docs: tests backend (stats, filtros, q, contrato) y frontend (service sin mock, mapper/paginación, workspace básico). Actualizar `docs/microservicios/order-service.md` (y `docs/design-system/README.md` solo si cambia componente). Checks aplicables: order-service `./mvnw test`/`verify`; frontend `npx vitest run`, `npm run lint`, `npx tsc --noEmit`, `npm run build` (registrar preexistentes conocidos).

## Tasks

| ID | Specs | Route | Status | Evidence |
|---|---|---|---|---|
| T1 | S2 | inline (parent) | done | AdminOrderResponse+customerEmail/paymentMethod nullables, q filter, GET /api/orders/admin/stats, AdminOrderServiceTest 6/6 PASS. Commit `e95b942`. |
| T2 | S3,S4 | inline (parent) | done | getAdminOrders real (list+stats+payment best-effort), workspace filtros/búsqueda/paginación, sin mock/env.useMocks. admin.service.test 10/10 PASS. |
| T3 | S5 | inline | done | `docs/microservicios/order-service.md` actualizado (q, /stats, AdminOrderStatsResponse, DTOs, reglas, wireframe). Checks: order-service `.\mvnw.cmd test` 56 tests, 0 failures, BUILD SUCCESS (excluidos temporalmente 2 tests untracked ajenos `AdminCustomerOrderControllerTest/AdminCustomerOrderServiceTest` que no compilan; restaurados). Frontend vitest 38 passed/5 failed files, 148 passed/5 failed tests (fallos ajenos/preexistentes: client-actions, payment-actions, admin-payments.mapper.test módulo ausente, admin-clients-adapter, 2 tests pagos en admin.service por impl mock). lint 0 errors (2 warnings ajenos en clientes). tsc falla por preexistentes conocidos, sin errores en admin-orders. build falla por preexistente `next/headers` en admin-clients-workspace.tsx; admin-orders-workspace corregido a server action. |
| T4 | S1-S5 | delegated (verifier) | pending | veredicto independiente |

## Log

L1 (verbatim): "Ahora implementame la integracion back y front de la pantalla Admin Pedidos Wireframe "

- OpenPencil verificado (tab-1, page `0:3`): frame `Admin Pedidos Wireframe` `0:2235` (1440x974) contiene header, tabs de estado con counts, rango "Últimos 30 días", tabla con búsqueda/orden, 8 filas, paginación "Mostrando 1–8 de 48 pedidos". Columnas: Pedido, Cliente(email), Fecha, Artículos, Pago, Total, Estado, Acciones "Ver detalle →". Acciones de header: Exportar CSV, Imprimir etiquetas.
- Riesgo/contrato: OrderEntity no tiene email ni payment method. S2 agrega campos nullable en AdminOrderResponse; frontend resuelve pago best-effort vía `/api/payments/order/{id}` para la página visible y usa fallback de email/customerId. No se inventa migración destructiva.
- Archivos de order-service con cambios ajenos en working tree (no revertir): `AdminOrderService.java`, `AdminOrderSummaryResponse.java`, `AdminOrderServiceTest.java`, gateway/payment-service modificados también. Writers deben trabajar sobre el estado actual.
- T3: doc `docs/microservicios/order-service.md` actualizado con `/api/orders/admin/stats`, filtro `q`, campos nullable `customerEmail/paymentMethod` en `AdminOrderResponse`, `AdminOrderStatsResponse`, regla stats y cobertura wireframe. Lint warnings propios corregidos. Build rompió por `next/headers` en `admin-orders-workspace` (client) importando `admin.service`; corregido con `order-actions.ts` server action y constantes locales. Checks: order-service test 56/0/0 BUILD SUCCESS (2 tests untracked ajenos excluidos temporalmente y restaurados). Vitest 148/5 fallos ajenos. tsc/build fallan por preexistentes ajenos; sin errores en archivos admin-orders.
