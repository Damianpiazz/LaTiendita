# Integrar Product Detail Wireframe con el backend real

Objective: dejar la pantalla `Product Detail Wireframe` 100% integrada contra los servicios reales, reutilizando el scaffolding existente de `frontend/features/product` y `frontend/features/review`.
Branch: `main` (decisión previa del usuario: trabajar en main, el usuario hace el push).
Delivery: `ask-on-risk`, sin PR. Test runner disponible: `npm run test` (vitest). Checks: `npx vitest run`, `npm run lint`, `npm run build` en modo mock y verificación en vivo con el stack Docker.

## Specs

S1 — Petición literal del usuario: "Quiero implementar la integracion entre frontend y backend para completar la pantalla Product Detail Wireframe. chequea como la estructura de la feature/ para aprovechar el scaffolding". Alcance Confirmado literalmente: "Integración real completa (Recomendado)": levantar review-service, hacer que el gateway sirva las lecturas públicas, cargar reseñas de prueba y alimentar rating/reseñas desde la API real.

S2 — El stack debe exponer las lecturas públicas de reseñas sin 401: `GET /api/reviews/summary?productId=<id>` y `GET /api/reviews?productId=<id>&page=1&size=2`. `review-service` debe estar levantado. Si el 401 persiste con la imagen actual del gateway, se corrige la configuración fuente y se reconstruye el contenedor.

S3 — Datos reales de detalle: los productos sembrados deben devolver `description`, `highlights`, `specs` y `images` no vacíos desde `GET /api/products/{slug}`, y las reseñas deben estar cargadas en la base `review`. Todo se carga con archivos UTF-8 copiados al contenedor (nunca por tubería de PowerShell, que guarda `??`).

S4 — El adaptador `frontend/features/review/services/review.service.ts` debe mapear el contrato real: `ReviewSummaryDto` usa `average` (string decimal), `reviewCount` y `distribution` con claves `"1".."5"`; `GET /reviews` devuelve un `Page` de Spring con `content`, `totalElements`, `number` y `size`. El tipo frontend `ReviewSummary` usa `average: number`, `count: number` y `distribution: Record<1|2|3|4|5, number>`; `ReviewPage` usa `items`, `page`, `size`, `total`. El modo mock debe seguir funcionando igual.

S5 — La pantalla debe mostrar `rating` y `reviewCount` desde el summary real (no los defaults `0`), manteniendo `ProductInfo`, las pestañas de `ProductDetails` y `ReviewsSection` consistentes. `installmentCount` y `soldCount` no tienen fuente en ningún backend: se documentan como pendientes y no se inventan datos.

S6 — Verificación: `npx vitest run` en verde, `npm run lint` sin errores nuevos, `npm run build` en modo mock, y humo en vivo con `NEXT_PUBLIC_USE_MOCKS=false` comprobando que `/productos/auriculares-inalambricos` renderiza descripción, specs y reseñas reales sin 401.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S2 | inline | done | review-service levantado y healthy; gateway 200 en summary y list. Causa del 401: instancia reviewService ausente en Eureka, no la regla de seguridad |
| T2 | S3 | inline | done | semilla UTF-8 aplicada en DB `product` y `review`: 8 productos con detalle, 11 reseñas aprobadas, 3 ratings. Gateway 200: detalle IMAGES=4 DESC=2 HIGHLIGHTS=3 SPECS=6 ORIGINAL=32500.00; summary average=4.33 reviewCount=6; list totalElements=6. Bytes C3-A1 presentes y 3F-3F ausentes en DB y HTTP |
| T3 | S4 | inline | done | RED: 2 fallos de contrato real, 2 pases mock; GREEN: 6/6. review.service.ts mapea ReviewSummaryDto y Spring Page, respeta 1-based mock / 0-based real |
| T4 | S5 | inline | done | withReviewSummary() aplica average/count del summary; ProductInfo y ProductDetails lo consumen; 2 tests verdes |
| T5 | S6 | inline | done | vitest 6 archivos/17 tests verdes; lint 0 errores (1 warning preexistente); build mock OK con /productos/[slug] prerenderizado; humo real HTTP 200 con NC-450, spec de autonomía, rating 4,3, (6), Cliente verificado y fechas formateadas, sin 401/500. Sin errores de Next en el log tras `instant = false` |

## Log

L1 (verbatim): "Quiero implementar la integracion entre frontend y backend para completar la pantalla Product Detail Wireframe. chequea como la estructura de la feature/ para aprovechar el scaffolding"

L2 (verbatim, respuesta a la pregunta de alcance): "Integración real completa (Recomendado)".

- El scaffolding ya existe: `features/product/{types,services,components,pages}`, `features/review/{services,types,components}` y la ruta `app/(public)/productos/[slug]/page.tsx`. No se crea estructura nueva.
- `product-service` ya expone `images`, `description`, `highlights`, `specs`, `originalPrice`, `fulfillment` y `categoryName` en `ProductDto`; `catalog.service.ts#toProductDetail` ya los mapea.
- Los productos sembrados tienen esos arrays vacíos y el stack levantado devuelve 401 en `/api/reviews/**`; `review-service` figura en `runtime/docker/compose.yaml` pero no está corriendo.
- `SecurityConfig.java` ya marca `GET /api/reviews/**` como público (commit `31521ef`, anterior a la imagen del gateway), así que el 401 se investiga en vivo antes de cambiar código.
- Regla del `frontend/AGENTS.md`: leer la documentación de Next.js instalada en `node_modules/next/dist/docs/` antes de escribir código. Ya se revisaron `use-cache.md` y `cacheLife.md`.

- T2: se sembraron los campos JSON con archivos `.sql` UTF-8 copiados al contenedor y ejecutados con `psql -f`. El detalle de producto 8 usa la copy del wireframe; los otros 7 productos reciben copy neutral. Las reseñas y los `product_ratings` se insertaron en la base `review`. La consola de PowerShell muestra `?` por acentos, pero los bytes HTTP son correctos (`C3-A1` presente, `3F-3F` ausente).
- T3/T4 RED→GREEN: 2 tests fallaron contra el contrato real antes de implementar los adaptadores; 6/6 pasaron después. `review.service.ts` maneja la ruta `/reviews/*` del mock y `/api/reviews/*` del gateway, y convierte páginas 1-based (mock) a 0-based (real).
- Humo real 1: HTTP 500 con `AxiosError 401`. Causa: `review.service.ts` llamaba `/reviews/*` (path del mock) contra el gateway, que sirve `/api/reviews/*`. Corregido el path real y actualizados los tests (4/4).
- Humo real 2: HTTP 200 pero las tarjetas mostraban `NaN undefined NaN`. Causa: `formatReviewDate` asumía `YYYY-MM-DD` y concatenaba `T00:00:00Z`; review-service devuelve un ISO timestamp completo. RED (2 tests) → fix en `format-review-date.ts` → GREEN (3 tests). Los datos visibles pasaron a `18 nov 2026`.
- Humo real 3: HTTP 200 con todos los datos reales. El log de `next dev` seguía emitiendo `instant-shell-url-data` por el `await params` de la ruta. Se consultó `node_modules/next/dist/docs/01-app/03-api-reference/03-file-conventions/02-route-segment-config/instant.md` y se aplicó `export const instant = false` en `app/(public)/productos/[slug]/page.tsx`, documentado para rutas que pueden bloquear. Tras el fix, el log solo registra `GET ... 200` sin errores.
- Verificación final: `npx vitest run` 6 archivos/17 tests; `npm run lint` 0 errores y 1 warning preexistente en `shipping-address-card.tsx`; `npm run build` en modo mock con 61 páginas generadas y `/productos/[slug]` prerenderizado.
- Pendiente documentado, no implementado: `installmentCount` y `soldCount` no tienen fuente en `product-service` ni en ningún otro backend; se mantienen los defaults y no se inventan datos.
- Entorno: `review-service` quedó levantado vía Docker Compose; el dev server de prueba en el puerto 3001 se detuvo al terminar la verificación. `docs/OpenPencil/LaTiendita.fig` estaba dirty antes de este trabajo y se excluye de los commits.
- RDD: `gentle-ai review mode status` = on (global). Assess de `b17b3d2..HEAD` = `risk: medium`, `review_due: false`, `review_due_reason: under_budget` (9 paths, 410 líneas). No se inició ciclo de review nativo; el límite de review sigue en `b17b3d2`.
