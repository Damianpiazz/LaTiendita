# Integrar Home Wireframe con el backend

Objective: dejar la pantalla Home Wireframe 100 % funcional contra la API real del gateway y
testeada en ambos lados, sin romper el modo mock ni las pantallas que comparten el catálogo.
Branch: `main` (preferencia explícita del usuario: sin ramas de feature; el push lo hace él).
Delivery: `exception-ok` — trabajo directo en `main`, commits por unidad de trabajo, sin PR.
Checks: backend `.\mvnw.cmd verify` (Docker disponible); frontend `npm run lint`, `npx vitest run`,
`npm run build` y aserciones sobre el HTML prerenderizado de `/`.

## Specs

S1 — Alcance (petición literal del usuario): "Quiero Hacer la integracion entre backend y frontend de la pantalla Home Wireframe que quede 100 % funcional y testeado." Aceptación: con `NEXT_PUBLIC_USE_MOCKS=false` la ruta `/` obtiene categorías y productos del gateway y renderiza el wireframe; con mocks activos el comportamiento actual sigue igual.

S2 — Decisión del usuario (respuesta literal): "Agregar featured al backend (Recomendado)". La opción aceptada añade columna + migración + filtro en `CategoryController` y una opción en el admin para marcar cuáles categorías se muestran. El Home respeta las 5 del wireframe y queda configurable.

S3 — Contrato HTTP: `GET /api/categories?featured=true` responde `Page<CategoryDto>` con `categoryId` (como string en JSON), `name`, `description`, `slug`, `featured` y `productCount`; `GET /api/products?featured=true` responde `Page<ProductDto>` con los alias actuales (`id`, `name`, `stock`, `galleryCount`). El backend conserva su forma canónica de Page; el frontend desenvuelve `.content` y mapea al tipo de pantalla.

S4 — Adaptador frontend: `frontend/features/product/services/catalog.service.ts` usa rutas bajo `/api`, desenvuelve `Page.content` y mapea `categoryId → id` (string), `slug`, `productCount` y `featured`. Se conservan las firmas públicas de `getCategories`, `getProducts`, `getFeaturedProducts`, `getCategoryBySlug`, `getProductBySlug`, `findProductBySlug`, `findCategoryBySlug`, `getCategoryProducts` y `getRelatedProducts` para no romper catálogo, detalle, admin ni mocks.

S5 — Configuración real: `NEXT_PUBLIC_API_URL` por defecto `http://localhost:8081` (puerto real del gateway), documentado en `frontend/README.md` y `README.md`. El modo mock sigue siendo el default.

S6 — Admin: la pantalla de categorías del admin permite marcar/desmarcar una categoría como destacada usando el endpoint existente de actualización, sin cambiar el contrato de los otros DTOs.

S7 — Tests (RED → GREEN → REFACTOR cuando aplica): backend agrega tests MockMvc de los dos GET del Home (filtro `featured` y campos JSON `id`, `name`, `price`, `featured`, `galleryCount`, `productCount`, `slug`), más un test que pruebe el comportamiento previo sin filtro; frontend agrega `vitest.config`, script `test` y tests del adaptador Page→array, del mapeo de categorías y de las firmas existentes. Verificación final: lint, build, tests y HTML prerenderizado.

S8 — Documentación: `docs/microservicios/product-service.md` refleja el filtro `featured` de categorías y los campos reales de `CategoryDto`; `README.md` y `frontend/README.md` reflejan el puerto 8081 y el modo de integración.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S2, S3, S6, S7 | delegated (backend) | done, commit `3d7e7a2` | `HomeCatalogApiIntegrationTest` 4/4, `CategoryLookupIntegrationTest` 3/3, `CategoryFeaturedMigrationIntegrationTest` 2/2; `mvnw verify` 72/0/0 en copia del verificador; RED→GREEN del id negativo |
| T2 | S1, S3, S4, S5, S7 | delegated (frontend) | done, commit `8a55bbd` | `npx vitest run` 8/8 (RED 3/1 del adaptador, RED 1/2 de la action y RED 1/2 del seam real antes de cada fix), `npm run lint` 0 errores (1 warning preexistente), `npm run build` 61 páginas, HTML de `/` con 5 categorías y 8 productos |
| T3 | S8 | inline | done, commit `3d7e7a2` | `docs/microservicios/product-service.md`: filtro `featured`, columna, DTO de 6 campos, búsqueda por slug y ABM; `README.md` ya documentaba 8081 |
| T4 | S7 | inline | done, tracked here | veredicto del verificador con 2 blockers corregidos; stack Docker UP; smoke real `/` (5 categorías, 8 productos, sin `ECONNREFUSED`); `/admin/categorias` real con 5 categorías reales; toggle real verificado vía `PUT` con JWT de admin (200, `hogar` sale y vuelve del filtro `featured`), DB restaurada |

Forecast de líneas autoras: ~550 (T1 ~250, T2 ~280, T3 ~20) por encima del heurístico de 400;
estrategia `exception-ok` acordada por la preferencia de trabajo en `main` sin PR.

## Log

L1 (verbatim): "Quiero Hacer la integracion entre backend y frontend de la pantalla Home Wireframe que quede 100 % funcional y testeado."

- Mapa inicial (2026-11-20): el Home ya existe en modo mock y consume `GET /categories?featured=true` y `GET /products?featured=true` esperando arrays; el backend sirve `Page` en `/api/...`, el gateway está en 8081 sin `RewritePath`, `CategoryDto` no tiene `featured`, `slug` ni `productCount`, y el frontend no tiene script/config de tests.
- L2 (verbatim, respuesta a la decisión de producto): "Agregar featured al backend (Recomendado)".
- La elección implica migración de esquema, filtro en el controlador de categorías y control en el admin; se conserva el contrato Page del backend y se adapta en la capa de servicios del frontend.
- Sin cronograma de push: el usuario ejecuta el push a `main` por su cuenta.
- L3 (2026-11-20, verificación): el backend conserva Page y expone `CategoryDto` de seis campos con `categoryId` serializado como string; la columna `featured` usa default de BD con `ddl-auto=update` (mecanismo único de esquema del repo) y hay test de fila legacy.
- Se corrigió el seam real-mode `getCategoryBySlug`: `GET /api/categories/{categoryIdOrSlug}` acepta id numérico o slug derivado, con test RED que reproducía el 400 previo.
- El frontend migration de mocks a `/api/...` mantiene las nueve firmas públicas del servicio; el HTML prerenderizado con mocks sigue idéntico.
- Hallazgo abierto: la acción del admin para destacar categorías lee `GET /admin/categories`, endpoint que sólo existe en mocks; en modo real debe resolver la categoría con `GET /api/categories/{idOslug}` antes de hacer el PUT. Se corrige después de la verificación aislada.
- Pedido del usuario: "Levanta el docker y el frontend àpara probar que funcione". Se creó `runtime/docker/.env` desde `.env.example` con secretos de desarrollo local (gitignored), `docker compose config --quiet` → 0, y el frontend quedó en `http://localhost:3000` con `NEXT_PUBLIC_USE_MOCKS=false` y `NEXT_PUBLIC_API_URL=http://localhost:8081`. El `up -d` se programa cuando termine el verificador para no competir por memoria con Testcontainers.
- Se corrigió el hallazgo del admin: `setCategoryFeatured` ya no lee `GET /admin/categories` (sólo mock); ahora resuelve la categoría con `findCategoryBySlug` → `GET /api/categories/{idOslug}` y hace el `PUT /api/categories` con el DTO real. Regression test nuevo (`category-actions.test.ts`) con RED 1/2 contra la versión anterior y GREEN 6/6 después del fix; lint y build siguen limpios.
- Primer `docker compose up -d --build` falló por DNS transitorio dentro del build de Maven (`repo.maven.apache.org: Temporary failure in name resolution` al resolver `azure-storage-blob:12.30.0`); el host sí resuelve el artefacto (HTTP 200). Se reintentó el mismo compose.
- L4 (2026-11-20, verificación independiente): el verificador en copia aislada (baseline `fca48c9`) dictaminó S1/S3/S4/S5/S7/S8 met, S2/S6 unmet, con dos blockers: (1) el admin en mocks lista `remeras,pantalones,…` y la Home `tecnologia,hogar,…`, además de que `getAdminCategories` seguíaleyendo `/admin/categories` (sólo mock); (2) regresión del mensaje de error de un id numérico negativo (`con id -1` → `con slug -1`). Advisory aplicado: `cast(:q as string)` en `ProductRepository` corrige un fallo `lower(bytea)` preexistente del baseline (2 errores en `mvnw verify`); con él el verify queda 72/0/0. Sin este cambio el suite completo no podía pasar.
- L5 (verbatim, decisión ante el blocker del admin): "Seam real en el admin (Recomendado)". En modo real `getAdminCategories` lee `GET /api/categories` y mapea `Page.content` a filas del admin (`categoryId ?? id`, `description ?? ""`, `productCount`, `lowStockCount: 0`, `featured`), con `selectedId` = primera fila y `updatedLabel: "hoy"`. El seed mock y el wireframe del admin quedan intactos; en mocks el toggle sigue presentacional, según lo aceptado.
- Blocker 2 corregido con RED→GREEN: `CategoryLookupIntegrationTest#shouldKeepIdWordingForNegativeNumericLookup` falló con `con slug -1` y pasó tras cambiar el regex a `"-?\\d+"` en `CategoryService`. `CategoryLookupIntegrationTest` 3/3.
- Stack Docker UP: `postgres kafka authorizer eureka-server gateway product-service`; gateway `/actuator/health` UP y product-service registrado en Eureka tras reintentos por DNS de `azurite`. Password de Postgres alineado con `runtime/docker/.env` sin borrar `pgdata`. Seed idempotente `INSERT 0 9` / `INSERT 0 8`, `featured_categories=5`, `featured_products=8`.
- Smoke real (2026-11-20): `GET http://localhost:8081/api/categories?featured=true` → 5 categorías; `/api/products?featured=true` → 8; `http://localhost:3000/` → HTML 78506 chars, `ECONNREFUSED=False`, 5 categorías y 8 slugs presentes. El marker `error` del HTML corresponde al error boundary estándar de Next.js (`global-error.js`), no a un fallo de datos.
- Recheck del toggle real: `/admin/categorias` en modo real renderiza 5 categorías reales (`Tecnolog…,Hogar,Ropa,Deportes,Juguetes`) y ninguna del seed mock. `PUT /api/categories` con JWT HS256 local firmado con el secreto de desarrollo y `roles:[admin]` devolvió 200: al poner `featured=false` en `hogar` el filtro pasó a 4 categorías; al restaurar `featured=true` volvió a 5. Estado de la BD restaurado (`description=NULL`, `featured=t`). Sin JWT el gateway devuelve 401: el frontend no tiene infraestructura de token (el login es presentacional) y todas las mutaciones admin ya estaban en esa condición antes de este cambio; queda fuera del seam aceptado.
- Checks finales: `npx vitest run` 8/8, `npm run lint` 0 errores/1 warning preexistente, `npm run build` 61 páginas, `CategoryLookupIntegrationTest` 3/3.
- Work units: `3d7e7a2` (backend + documentación del servicio), `8a55bbd` (frontend + tests + AGENTS.md generado por `next dev`). `docs/OpenPencil/LaTiendita.fig` tenía una modificación previa ajena al feature (02:28, antes de este trabajo) y se dejó fuera de los commits. El push lo ejecuta el usuario.
- RDD (2026-11-20): `gentle-ai review mode status` → on (global). `gentle-ai review assess --base-ref fca48c9 --committed-only --json` sobre el slice completo → `risk: medium`, `review_due: false`, `review_due_reason: under_budget` (32 paths, 1230 líneas); no se inicia lifecycle de revisión. El verificador independiente ya cubrió el cambio con probes en copia aislada.
