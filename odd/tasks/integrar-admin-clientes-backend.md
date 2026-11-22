# Integrar Admin Clientes Wireframe con los microservicios

Objective: dejar `/admin/clientes` 100 % funcional contra la API real del gateway, con KPIs y
columnas alimentados por agregados reales de pedidos, sin rama mock y reutilizando el scaffolding
de `features/admin`. Branch: `main` (preferencia registrada; push/PR los decide el usuario).
Delivery: `exception-ok`, commits por unidad de trabajo. Checks: `./mvnw test` y `./mvnw verify`
en los dos servicios, `npx vitest run`, `npm run lint`, `npx tsc --noEmit`, `npm run build`.

## Specs

S1 — Alcance (petición literal del usuario): "Admin Clientes Wireframe haceme la integracion de front y back de esta pantalla con las skills que hay en el projecto". Decisión aceptada (respuesta literal): "Agregar agregados reales de pedidos (Recomendado)". Aceptación: con el stack real levantado, `/admin/clientes` obtiene listado, búsqueda, filtros, orden, paginación, KPIs, exportación CSV y detalle desde el gateway; no queda fallback mock en esta pantalla. Se mantiene la preferencia registrada: "no quiero modos mocks, quiero la integracion real funcionando ya".

S2 — Diseño y UI: la fuente es `docs/OpenPencil/LaTiendita.fig`, page `0:3`, frame real
`0:2846` "Admin Clientes Wireframe" (`0:2852` es el nodo de texto "Panel de administración", no el frame). Se conservan sidebar 240, main 1200, contenido 1136, stats de 81 px, toolbar de 40 px, filas de 64 px y los textos literales del wireframe (`Clientes`, `Exportar CSV`, `Segmentos`, `Buscar por nombre, email o teléfono...`, `Todos`, `Nuevos`, `Recurrentes`, `Sin compras`, `Ordenar: Última compra ↓`, `Ver →`, `Mostrando 1–8 de 128 clientes`). No se editan los `.fig` ni `docs/design-system/README.md`: ambos tienen cambios ajenos sin commitear y esta petición pide integración, no rediseño. Los dos componentes no definidos por el frame (`Segmentos` y el detalle de `Ver →`) se resuelven con componentes shadcn ya instalados y tokens de `docs/design-system`, sin paquetes nuevos. Supuesto documentado: `Segmentos` abre un menú con los mismos segmentos de las pills y `Ver →` abre un diálogo con el detalle real; si el usuario lo corrige, se reescribe este S2 y se reabre T2.

S3 — Contrato HTTP congelado para backend y frontend:
- `GET /api/customers/admin?name=&email=&phone=&status=&segment=&sort=&page=&size=` → `Page<AdminCustomerResponse>`. Cada item expone `customerId` (string), `firstName`, `lastName`, `email`, `phone`, `status` (`ACTIVE|SUSPENDED`), `createdAt`, `lastLoginAt`, `orderCount`, `totalSpent` (number), `lastPurchaseAt` (string|null) y `segment` (`nueva|recurrente|activa|inactiva|sin-compras`). El listado existente `GET /api/customers` y su DTO `CustomerSummaryDto` NO se rompen: el nuevo contrato es un prefijo `/admin` explícito.
- `GET /api/customers/admin/summary?name=&email=&phone=&status=&segment=` → `AdminCustomerSummaryResponse { totalCustomers, newThisMonth, recurring, averageTicket }`, con los mismos filtros que el listado.
- `GET /api/customers/admin/export.csv?name=&email=&phone=&status=&segment=&sort=` → `text/csv` con el mismo filtrado y las columnas visibles del wireframe.
- Semántica de filtros: `name` es búsqueda libre sobre `firstName`, `lastName`, `email` y `phone`. Cuando `name`, `email` y `phone` traen exactamente el mismo término —lo que hace el único cuadro de búsqueda del wireframe— se interpreta como una sola búsqueda libre (OR), no como tres filtros AND. Valores distintos en `email` o `phone` siguen siendo filtros estrechos e independientes.
- `GET /api/customers/{customerId}` se reutiliza para el detalle; no se crea un contrato paralelo.
- `GET /api/orders/admin/customers/stats?customerIds=<uuid,uuid,...>` → `List<AdminCustomerOrderStatsResponse> { customerId, orderCount, totalSpent, lastPurchaseAt }`. `GET /api/orders/admin/customers/summary` → `AdminCustomerOrderSummaryResponse { recurringCustomers, averageTicket }`. El dueño de los cálculos es `order-service`; `customer-service` sólo compone. Se excluyen pedidos `CANCELLED` de conteo, gasto y ticket. Supuestos documentados: recurrente = 2 o más pedidos; ticket promedio = total gastado / cantidad de clientes con al menos un pedido; nueva = alta en los últimos 30 días.
- Segmento (backend): `sin-compras` si `orderCount == 0`; si no, `recurrente` si `orderCount >= 2`; si no, `nueva` si `createdAt` está en los últimos 30 días; si no, `activa` para `ACTIVE` o `inactiva` para `SUSPENDED`. El wireframe imprime también `Nuevo` para algunos hombres; no existe campo de género, así que `nueva` es la etiqueta canónica en modo real y `nuevo` se conserva sólo en el badge para datos mock. El frontend muestra el badge `Inactiva` para `sin-compras`; el segmento existe como filtro distinto y no se confunde con `SUSPENDED`.
- Errores: se conserva `ProblemDetail` de `GlobalExceptionHandler` (404, 409, 400). El cliente HTTP a `order-service` degradea a ceros con logging cuando el servicio no responde; nunca inventa pedidos.
- Seguridad: `GET /api/customers` ya exige `hasRole("admin")` en el gateway; se agrega la regla para `/api/customers/admin/**`. La llamada interna de `customer-service` a `/api/orders/admin/**` reenvía únicamente los headers de identidad inyectados por el gateway (`X-User-*`) desde la petición entrante; jamás datos del body. `/api/orders/admin/**` ya requiere admin.

S4 — Adaptador frontend: `features/admin/services/admin.service.ts` conserva firmas públicas de las demás funciones y agrega `getAdminClients(query)` con rama real única: `adminAuthHeaders()` (cookie `lt_access_token` → `Authorization: Bearer`), reenvío de `name/email/status/segment/sort`, conversión 1-based→0-based ya establecida para `Pageable`, `unwrapPage` y mapeo DTO → `AdminClient`. Los KPIs y el subtítulo `128 clientes · 24 nuevos este mes` dejan de venir del seed mock; se derivan de `/api/customers/admin/summary`. `features/admin/types/clients.ts` incorpora los campos nuevos sin cambiar el significado de los existentes. Se eliminan la ruta mock `GET /admin/clients` y el seed de clientes de esta pantalla.

S5 — Mutaciones y detalles: `features/admin/actions/client-actions.ts` con `"use server"` expone exportación CSV con Bearer y `revalidatePath("/admin/clientes")` cuando corresponda; sigue el patrón de `product-actions.ts` y `category-actions.ts`. `Segmentos` Cambia el filtro activo y `Ver →` abre el detalle con `GET /api/customers/{customerId}`; la petición se hace desde el servidor (page con `searchParams`) para no exponer el token al browser. No se agrega alta/edición/borrado de clientes: el wireframe no los define.

S6 — UI: los controles hoy inertes pasan a operar contra S3: búsqueda por nombre, email o teléfono; pills `Todos`/`Nuevos`/`Recurrentes`/`Sin compras`; `Ordenar: Última compra ↓`; paginación compartida `AdminPagination`; `Exportar CSV`; `Segmentos`; `Ver →`. Se usa `ToggleGroup`, `DropdownMenu`/`Dialog`, `Badge`, `Table` y `Button` ya instalados, respetando `frontend/AGENTS.md`: leer la guía de Next.js relevante en `node_modules/next/dist/docs/` antes de escribir código. Sin `space-x-*`/`space-y-*`, con `gap-*`, `size-*`, `cn()` y colores semánticos. responsive actual se conserva.

S7 — Tests (RED → GREEN → REFACTOR): una prueba RED por regla pedida, por interfaz pública y con efectos observables. Backend: segmentos, summary, stats por cliente, CSV, filtros y paginación, propagación de headers, degradación ante caída de `order-service`, y un test que pruebe que `GET /api/customers` sin filtros sigue igual. Frontend: adaptador real (Bearer, `Page.content`, conversión de página), actions (auth y CSV), UI de filtros/detalle y regresión de `getAdminProducts`/`getAdminCategories`. Todo test pasa por la interfaz pública.

S8 — Documentación: `docs/microservicios/customer-service.md` y `docs/microservicios/order-service.md` reflejan los endpoints de S3 y la regla de segmentos; se corrige el inventario obsoleto que dice que el listado no está implementado. `docs/design-system/README.md` y `docs/OpenPencil/LaTiendita.fig` NO se tocan por contener cambios previos no separables.

S9 — Verificación y riesgo: riesgo alto por (1) datos y efectos irreversibles (exportación y contratos de lectura), (3) contratos que otros consumen (nuevos endpoints y DTOs) y seguridad (propagación de identidad entre servicios). Por eso el change recibe verificador independiente con baseline y probes derivados de S1–S8, además de los checks propios. RDD está `on` (global): se evalúa cada work-unit commit y se sigue el lifecycle nativo sólo si aplica. Se registran todos los checks, incluidos los fallidos o no disponibles.

S10 — Worktree compartido: el árbol trae cambios ajenos sin commitear en `admin.service.ts`, sus tests, `CustomerService.java`, tests de customer, gateway y docs. Los writers NO pueden sobrescribirlos: leen el archivo completo antes de editar y conservan cada hunk ajeno. El parent es dueño de git; `--untracked-scope=exclude` y `git add` explícito por archivo. Si un archivo tiene hunks ajenos mezclados, se reporta y se decide con el usuario antes de commitear.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S3, S7, S8, S10 (backend) | delegated (writer, backend) | done | order `./mvnw test` 64/64 y `verify` OK; customer `./mvnw test` 43/43 y `verify` OK; parent 18/18 enfocado y 10/10 segmentos |
| T2 | S2, S4, S5, S6, S7, S10 (frontend) | delegated (writer, frontend) | done | vitest 46 archivos/187 tests; lint 0 errores, 1 warning ajeno en `admin.service.ts:41`; parent 5 archivos/39 tests; commit `38e788a` |
| T3 | S9 | delegated (verifier) → inline correction | done | veredicto FAIL por B1 (búsqueda libre ANDADA); corrección aplicada en `AdminCustomerService.matches`; recheck B1 verde: customer 51/51, order 68/68, frontend 49 archivos/197 tests; 5 probes incorporados como regression tests |

Route declaration: T1 y T2 son dos unidades paralelas con superficies de edición disjuntas
(`services/order-service/**`, `services/customer-service/**`, gateway y docs del servicio vs
`frontend/**`), cada una más pesada que iniciar un subagente: dispara el Writer trigger. T3 dispara
el Verification trigger por datos, contrato y seguridad. T1 y T2 no van inline porque ambas
superficies requieren lectura y escritura extensas y el parent debe mantenerse delgado; la corrección
de B1 de T3 va inline por ser una única causa raíz ya localizada, sin Writer trigger.

Forecast de líneas autoras: ~1000 (T1 ~420, T2 ~500, docs/probes ~80), por encima del heurístico
de 400; la estrategia `exception-ok` ya está acordada en el historial del proyecto.

## Log

L1 (verbatim): "Admin Clientes Wireframe haceme la integracion de front y back de esta pantalla con las skills que hay en el projecto"

L2 (verbatim, respuesta a la pregunta de alcance): "Agregar agregados reales de pedidos (Recomendado)"

L3 — Mapa inicial (explorador de solo lectura): `getAdminClients()` hoy hace GET `/admin/clients` sin Bearer ni argumentos; los controles del wireframe son inertes. `customer-service` expone `GET /api/customers` con `Page<CustomerSummaryDto>` y gateway ya exige admin; no hay `/admin`, ni phone, ni createdAt en el DTO, ni agregados de pedidos. `order-service` tiene `GET /api/orders/admin` con `q` por customerId, `status`, `from`, `to` y `Pageable`, pero ningún agregado por cliente. Existe patrón de `RestClient` con interfaz en `review-service` y `shipment-service`. El frame vivo es `0:2846` y no hay frames de detalle, exportación ni segmentos.

L4 — RDD: `gentle-ai review mode status` → on (global). Los commits de este feature se evalúan con `--base-ref <boundary> --committed-only --untracked-scope=exclude`; si el assessment es `high` o no disponible se sigue el lifecycle nativo, sin inventar PASS.

L5 — 2026-11-22 — Corrección de S3 tras el resultado del writer frontend: la búsqueda del wireframe incluye teléfono y el filtro `Sin compras` no podía representarse con el vocabulario original. Se agrega `phone` a listado/summary/CSV y el segmento `sin-compras` con prioridad sobre `recurrente`; se reabre T1 para reflejarlo en backend y T2 para usar el segmento correcto. El badge sigue mostrando `Inactiva` para `sin-compras`, sin confundirlo con `SUSPENDED`.

L6 — 2026-11-22 — Veredicto T3 `FAIL`: B1, change-caused y reproducido, `T3_PROBE freeTextRows=0`. El cuadro único del wireframe envía el mismo término en `name`, `email` y `phone`, y `AdminCustomerService.matches` los combinaba con AND; `ana` o `555-1038` devolvían cero filas. El test del writer `admin-clients-workspace.test.tsx:100-110` fijaba esa forma de llamada sin comprobar el efecto end-to-end. Corrección en un solo batch: `name` pasa a buscar también `email`, y un término idéntico en los tres parámetros se trata como una sola búsqueda libre OR; los valores distintos conservan su filtro estrecho. Se documenta en S3 y en `docs/microservicios/customer-service.md:43`, y se incorporan como regression tests los cinco probes del verificador (3 Java, 2 TS) sin modificar su contenido. El probe de paridad legacy y los de degradación/agregados ya pasaban en el veredicto. Queda pendiente el recheck acotado a B1.

L7 — 2026-11-22 — Cierre: recheck acotado a B1 verde (`customer-service 51/51`, `order-service 68/68`, `frontend 49 archivos/197 tests`); spot checks previos `customer 18/18`, segmentos `10/10`, frontend foco `5 archivos/39 tests`; lint `0 errores, 1 warning ajeno`. RDD assess de `7628055` devolvió `risk: high`, `review_due: true`; el usuario eligió "Omitir esta vez" en el envelope de consentimiento y se ejecutó una sola vez la invocación exacta con `--consent declined`, que respondió `consent: declined_this_candidate`. Sin revisión para ese candidato; el lifecycle nativo no se continuó. T2 se commiteó como `38e788a` con `git add` explícito; en `frontend/mocks/routes.ts` se stageó sólo la eliminación de `adminClients`, quedando sin stagear la eliminación de `adminPayments` ajena. `npx tsc --noEmit` y `npm run build` siguen fallando por errores preexistentes ajenos: no son regressiones de este feature. Push/PR quedan a decisión del usuario.

L8 — 2026-11-22 — Assess del rango acumulado (`6810176..HEAD`, `--committed-only`, inventario de untracked `sha256:c58ad868af9376b59e4cf4c7b2fa1d45538b870b6b2f4ebd0c26f899c92badcb`): `risk: high`, `review_due: true`, motivo `hot_path` en `services/gateway/src/main/java/com/latiendita/gateway/security/SecurityConfig.java`. Es el mismo cambio de este feature y el usuario ya eligió "Omitir esta vez", así que no se abre otra transacción ni se repite el consentimiento; RDD permanece habilitado globalmente para cambios futuros.
