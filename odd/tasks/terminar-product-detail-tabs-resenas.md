# Terminar Product Detail: dos tabs y reseñas siempre visibles

Objective: alinear la pantalla `Product Detail Wireframe` con el wireframe actualizado del usuario y dejar todas sus funcionalidades operativas contra mock y backend real, reutilizando `frontend/features/product`, `frontend/features/review` y shadcn.
Branch: `main` (decisión previa del usuario: trabajar en main, el usuario hace el push).
Delivery: `ask-on-risk`, sin PR. Test runner: `npm run test` (vitest). Checks: `npx vitest run`, `npm run lint`, `npm run build` en modo mock y humo en vivo con `NEXT_PUBLIC_USE_MOCKS=false`.

## Specs

S1 — Petición literal del usuario: "Quiero que me termines de implementar la Product Detail Wireframe hice cambios en el wireframe, quiero que haya 2 tabs una para la descripcion del producto y otra para las especificaciones,las resñas siempre van a estar. lee el wireframe y implementa en el frontend la integracion completa entre el frontend y el backend. que funcionen todas las funcionalidades de la page. aprovecha shadcn. aprovecha el scaffolding. separa responsabilidades. como un ingeniero de software experto en frontend. usa las skills pertientes de react, java script typescript, test."

S2 — Wireframe leído desde `docs/OpenPencil/LaTiendita.fig` (frame `0:213`, `Product Detail Wireframe`): el bloque `Details` (`0:306`) contiene exactamente dos tabs, `Descripción` (`0:309`) y `Especificaciones` (`0:312`), con `Tab content` (`0:316`) para `Description` y `Specs`. Ya no existen los tabs `Envíos y devoluciones` ni `Reseñas (n)`.

S3 — `Reviews` (`0:346`) es un frame hermano de `Details`, con `Reseñas (124)`, `Escribir una reseña`, resumen, dos cards y `Ver las 124 reseñas →`. Las reseñas deben renderizarse siempre, sea cual sea el tab activo, fuera del árbol de tabs y alimentadas por `getReviewSummary` y `getReviews` (mock y real).

S4 — Los tabs deben implementarse con el componente shadcn `tabs`, instalado con el runner del proyecto. `ProductDetails` pasa a ser el único cliente con estado del tab; la descripción y las especificaciones se extraen a vistas presentacionales dentro de `features/product`. La página `ProductDetailPage` sigue siendo el orquestador de datos y no gana estado de UI.

S5 — Funcionalidades observables de la página que deben seguir operando: galería con selección de miniaturas, cantidad y `Agregar al carrito`, `Comprar ahora`, breadcrumbs, tabs, enlace de reseñas completas, tarjetas relacionadas y beneficios. El enlace `Ver las N reseñas →` debe resolver a una ruta real y paginar el mismo adaptador de reseñas en ambos modos; `Escribir una reseña` se mantiene como etiqueta informativa porque el wireframe no define un formulario y el backend exige auth/orden, fuera de este cambio.

S6 — Verificación: RED antes de GREEN para las reglas nuevas; `npx vitest run` en verde, `npm run lint` sin errores nuevos, `npm run build` en modo mock y humo real comprobando que `/productos/auriculares-inalambricos` muestra los dos tabs, cambia de contenido y mantiene las reseñas visibles en ambos estados.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S2-S3, S6 | inline | done | RED: `product-detail-page.test.tsx` falló con `Unable to find role "tab"` y `Found multiple elements ... "Reseñas (6)"`; commit `afd294c` |
| T2 | S2-S4 | inline | done | shadcn `tabs.tsx` instalado; `ProductDescription`/`ProductSpecifications` extraídos; `ProductDetails` es Client Component con 2 tabs; GREEN 2/2; commit `afd294c` |
| T3 | S5 | inline | done | `ReviewsPage` + ruta `/productos/[slug]/resenas`; RED (módulo ausente) → GREEN 1/1; `getReviews` 1-based mock / 0-based real vía adaptador |
| T4 | S6 | inline | done | vitest 8 archivos/20 tests verdes; lint 0 errores (1 warning preexistente); build mock OK con la nueva ruta en Partial Prerender; humo real 200 con 2 tabs, `resenas-titulo`, `Cliente verificado` y ruta de reseñas; RDD: ambos work units `medium`/`under_budget`, sin review nativo |

## Log

L1 (verbatim): "Quiero que me termines de implementar la Product Detail Wireframe hice cambios en el wireframe, quiero que haya 2 tabs una para la descripcion del producto y otra para las especificaciones,las resñas siempre van a estar. lee el wireframe y implementa en el frontend la integracion completa entre el frontend y el backend. que funcionen todas las funcionalidades de la page. aprovecha shadcn. aprovecha el scaffolding. separa responsabilidades. como un ingeniero de software experto en frontend. usa las skills pertientes de react, java script typescript, test."

- El `.fig` estaba dirty antes de este trabajo; se lee, no se escribe ni se commitea.
- OpenPencil MCP conectado al documento `tab-1`, página `WireFrame`; se confirmaron los ids del wireframe listados en S2 y S3.
- Regla de `frontend/AGENTS.md`: leer la documentación de Next.js instalada antes de escribir código. Ya se revisó `01-app/01-getting-started/05-server-and-client-components.md`; la interacción de tabs será un Client Component y la página permanece como Server Component.
- La integración anterior quedó commiteada hasta `993dcb9`; este documento es una feature nueva y no sobrescribe `odd/tasks/integrar-product-detail-backend-frontend.md`.
- T1/T2: `TabsContent` con `keepMounted=false` (default de Base UI) desmonta el panel inactivo; el test se ajustó a ese contrato (descripción activa por defecto, specs tras el click). La guía de `ui-ux-pro-max` (`tab navigation active indicator`, `tabs underline indicator`) confirmó subrayado activo, feedback de press, foco visible y uso del primitivo shadcn con `defaultValue`.
- Los tabs se ven feos con las clases crudas del primitivo: éste aplica `flex-1`, `h-[calc(100%-1px)]` y `data-active:bg-background`. Se corrigió con `flex-none`, `h-auto`, `bg-transparent`, subrayado `after:-bottom-px after:h-[3px] after:bg-wf-inverse` (idéntico al frame `0:309`), `gap-8` (32 px del wireframe) y tokens `wf-*`; verificado en el HTML renderizado del humo real.
- `ReviewsSection` queda fuera del árbol de tabs, así que las reseñas no dependen del tab activo. `ProductDetails` es el único Client Component nuevo; la página sigue siendo orquestador de datos.
- Estrategia de entrega `ask-on-risk`: el forecast supera ~400 líneas authored, pero el usuario hace push directo de `main` sin PR, así que no hay cadena de PR aplicable; se entregan work-unit commits en `main`, igual que la feature anterior.
- T3: `ReviewsPage` paraleliza `getReviewSummary` y `getReviews` (regla `async-parallel` de `vercel-react-best-practices`). `PAGE_SIZE=6`, paginación con `<Link>` y `aria-current="page"`; la ruta normaliza `searchParams.page` (string o string[]) a un entero positivo y el componente lo acota al total real.
- Humo real con `NEXT_PUBLIC_USE_MOCKS=false` y stack Docker healthy: `/productos/auriculares-inalambricos` y `/productos/auriculares-inalambricos/resenas?page=1` devolvieron 200; el HTML del detalle tiene exactamente 2 `role="tab"`, `resenas-titulo` y `Cliente verificado`, sin 401/500. El dev server de prueba se detuvo al terminar.
- Nota de datos: el mock declara 124 reseñas pero siembra 2 tarjetas, así que las páginas 2+ del mock están vacías por diseño de la semilla; el backend real sirve 6 reseñas y el adaptador pagina correctamente. `installmentCount` y `soldCount` siguen con defaults por falta de fuente backend.
- RDD: `gentle-ai review mode status` = on (global). Assess de `993dcb9..afd294c` = `risk: medium`, `review_due: false`, `review_due_reason: under_budget` (6 paths, 424 líneas; el primer intento se bloqueó por archivos untracked del siguiente work unit y se reran con `--untracked-scope=exclude`). Assess de `afd294c..778ed53` = `risk: medium`, `review_due: false`, `under_budget` (4 paths, 227 líneas). No se inició ciclo de review nativo; el límite de review sigue en `778ed53`.
- `docs/OpenPencil/LaTiendita.fig` sigue dirty desde antes de este trabajo y se excluye de todos los commits; el usuario hace el push.
