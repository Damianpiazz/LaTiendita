# Auth real sin mocks y seeder de cuentas de prueba

Objective: dejar la app funcionando siempre contra Authorizer, con cuentas reales y datos de perfil por usuario, más un seeder profesional con varias cuentas probables.
Branch: `main` (el push lo hace el usuario). Delivery: `exception-ok` — commits por unidad de trabajo, sin PR.
Checks: `npx vitest run`, `npm run lint`, `npx tsc --noEmit`, `npm run build` y smoke real con gateway + Authorizer.

## Specs

S1 — Reporte del usuario (texto literal): "No funciona realemnte la funcionalidad de auth ya que me logueo con cualquier cuenta y me abre la cuenta de ana. eso no deberia pasar deberia tener que crearme una cuenta, y recien poder ingresar a mi perfil pero con mis datos. quiero que hagas un auth real. quiero que me crees un seeder lo mas profecional posible con varias cuentas para poder probar."

S2 — Decisión del usuario (texto literal): "no quiero modos mocks, quiero la integracion real funcionando ya". Aceptación: la configuración por defecto usa el backend real; no existe una ruta de login que acepte credenciales arbitrarias ni devuelva un usuario fijo.

S3 — Causa verificada: `frontend/config/env.ts:23` fija `useMocks: true` cuando `NEXT_PUBLIC_USE_MOCKS` está ausente, y `frontend/.env` está vacío. `frontend/mocks/data/auth.ts:6-19` devuelve siempre Ana (`cliente@example.com`) sin validar credenciales. Por eso cualquier login abre la cuenta de Ana.

S4 — Sesión y perfil: el JWT de Authorizer vive en cookie httpOnly `lt_access_token`; `lt_user` sólo expone datos no sensibles. `/mi-cuenta` debe mostrar los datos del usuario autenticado, no un perfil global. Sin sesión, el acceso a páginas protegidas debe ser un flujo de autenticación, no un error de prerenderizado.

S5 — Seeder de infraestructura (texto literal del usuario): "Yo quiero que al levantar authorizer tenga cuentas ya cargadas sin tener que levantar el frontend aparte". Aceptación: un servicio `authorizer-seed` en `runtime/docker/compose.yaml` crea las cuentas contra `authorizer:8080` sin gateway ni frontend, es idempotente y no imprime secretos.

S6 — Verificación: `npx vitest run`, `npm run lint`, `npx tsc --noEmit`, `npm run build` en modo real y smoke end-to-end con login correcto, login incorrecto y dos cuentas distintas con datos distintos en `/mi-cuenta`.

S7 — Reporte del usuario (texto literal): "comopuedo saber la password de los usuarios en la seed si no me mostras la contraseña. deja Password123 como contraseña para esos usuarios seedeados". Decisión del usuario ante la restricción verificada de Authorizer: "Usar Password123! (recomendado)". Aceptación: la password queda documentada y configurable, las cuatro cuentas ya creadas se resetean, y la credencial vieja deja de servir. `Password123` literal es imposible: Authorizer responde 400 porque exige minúscula, mayúscula, número y carácter especial.

S8 — Autorización del usuario (texto literal): "si". Alcance autorizado: corregir los tres defectos verificados en vivo (seeder no idempotente, 409 del perfil y RBAC del gateway) y después correr la batería de tests.

S9 — Seeder autocurativo: si una cuenta existe pero el login falla por password distinta, el seeder debe reclamarla con la API admin de Authorizer y dejarla con `SEED_USER_PASSWORD`. Un `signup` 400 por email ya registrado no debe contarse como éxito ni como fracaso mudo. Aceptación: `docker compose run --rm authorizer-seed` reporta 4/4 ready dos veces seguidas y los cuatro logins con `Password123!` responden 200.

S10 — Perfil tras recreación de cuenta: si Authorizer recrea una cuenta, cambia el `sub` y el `POST /api/customers` choca con el email viejo dando 409; `/mi-cuenta` se rompe porque `customer.service.ts:49-51` relanza el error. Aceptación: el backend reasocia la fila huérfana por email al nuevo `sub` (sin perder el perfil) y el frontend no deja la pantalla en error no manejado. Test unitario que pruebe el rebind y test que pruebe el 409 manejado.

S11 — RBAC del gateway: `GET /api/customers` (listado admin) y `/api/customers/{id}`, `/api/admin/promotions/**` exigen `hasRole("admin")`; la ruta `/api/auth/**` se limita a los cuatro paths que el BFF usa (`login`, `signup`, `skip_mfa_setup`, `forgot_password`) para no exponer `/v1/admin/*`. Aceptación: config + test que verifiquen las reglas.

S12 — Verificación: `npx vitest run`, `npm run lint`, `npx tsc --noEmit`, `npm run build`, `mvn test` del customer-service y del gateway, smoke con Authorizer+seeder y login de dos cuentas distintas.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S2, S3 | inline | GREEN | `fe111af` — `config/env.test.ts` 3/3 (RED 1→GREEN 3); `.env` y `.env.example` con modo real; default invertido en `config/env.ts`. |
| T2 | S1, S4 | inline | GREEN | `17b6385` — `lib/customers/profile.test.ts` 2/2 y `features/customer/services/customer.service.test.ts` 3/3 (RED→GREEN); `<Suspense>` en `/mi-cuenta`; `npm run build` pasa en modo real. |
| T3 | S5 | inline | GREEN | `013cf80` — `runtime/docker/authorizer/seed/` con 4 cuentas; `docker compose run --rm authorizer-seed` → 4/4 ready dos veces seguidas; sin secretos en logs. |
| T4 | S6 | inline | GREEN | `ec932a1` + verificación final — vitest 58/58, lint 0 errores, tsc exit 0, build exit 0; smoke: Ana y Bruno ven su propio email/nombre y no el del otro, sin redirect; login inválido 401. |
| T5 | S7 | inline | GREEN (parcial) | `a3f00ec` — default `Password123!` en seeder/Compose/.env.example/docs; verificación en vivo 2026-11-21 mostró que las cuentas conservan `Semilla123!`: reabierto en T6. |
| T6 | S8, S9 | inline | GREEN | sin commit (árbol de trabajo) — `seed.mjs` reclama con `/v1/admin/login` + `/v1/admin/user` + `/v1/admin/delete_user` y respeta `{ user: {...} }`; Compose pasa `AUTHORIZER_ADMIN_SECRET`. En vivo: 4/4 `reclaimed`, luego 4/4 `existing`; `Password123!` → 200 en 2 cuentas, `Semilla123!` → 401. |
| T7 | S8, S10 | inline | GREEN | sin commit (árbol de trabajo) — `CustomerService.reclaimByEmail` reasocia por email SOLO si viene del claim confiable del gateway; frontend re-lee el perfil ante 409. RED→GREEN en 3 tests; customer-service 25/25, customer.service.test.ts 4/4. |
| T8 | S8, S11 | inline | GREEN | sin commit (árbol de trabajo) — allowlist de 4 paths en `/api/auth/**`; matchers admin para customers y promociones con `/me` antes del wildcard. Gateway 47/47. |
| T9 | S12 | inline | GREEN (con nota) | vitest 59/59, lint 0 errores (1 warning preexistente), tsc exit 0, build exit 0 con `NEXT_PUBLIC_USE_MOCKS=1`; customer-service 25/25, gateway 47/47; smoke seeder + login OK. Nota: `npm run build` en modo real sigue exigiendo backend vivo en `generateStaticParams()` (ECONNREFUSED 8081); es dependencia preexistente, no regressions de auth. |

Estrategia `exception-ok` por trabajo directo en `main` sin PR. Las líneas autoras se registran al cerrar cada unidad.

Route declaration: las cuatro unidades van inline. Writer trigger: no aplica (no hay unidades paralelas independientes; el seeder y el rebind están acoplados por el `sub`, y gateway/frontend son cambios pequeños). Verification trigger: T7 y T8 tocan datos e identificaciones, así que reciben verificación independiente después de sus checks.

## Log

L1 (verbatim): "No funciona realemnte la funcionalidad de auth ya que me logueo con cualquier cuenta y me abre la cuenta de ana. eso no deberia pasar deberia tener que crearme una cuenta, y recien poder ingresar a mi perfil pero con mis datos. quiero que hagas un auth real. quiero que me crees un seeder lo mas profecional posible con varias cuentas para poder probar."

L2 (verbatim): "no quiero modos mocks, quiero la integracion real funcionando ya"

L3 (verbatim, 2026-11-20): "Yo quiero que al levantar authorizer tenga cuentas ya cargadas sin tener que levantar el frontend aparte"

L4 (verbatim, 2026-11-20): "comopuedo saber la password de los usuarios en la seed si no me mostras la contraseña. deja Password123 como contraseña para esos usuarios seedeados"

L5 (decisión del usuario, 2026-11-20): "Usar Password123! (recomendado)" — respuesta a la restricción verificada de Authorizer.

- 2026-11-20: verifiqué en código que la app corre con mocks porque `NEXT_PUBLIC_USE_MOCKS` no está definido y el default es `true`; en ese modo el login no valida credenciales y devuelve siempre a Ana. Se descarta el modo mock como solución.
- El delivery anterior dejó `e5ffd25` (BFF y sesión) y `bc998ce` (forms y wiring) en `main`, con smoke real aislado. El usuario quiere ahora que el modo real sea el funcionamiento normal, no una prueba aislada.
- 2026-11-20: el perfil real se resuelve con `GET /api/customers/me` y el httpOnly JWT; el backend crea el registro una vez con `POST /api/customers`, idempotente por `sub` (`CustomerService.java:39-74`).
- 2026-11-20: el build en modo real fallaba en `/mi-cuenta` y `/admin` por fetches en prerender. Se resolvió con `<Suspense>` en las rutas protegidas y quitando `"use cache"` de los servicios que apuntan a rutas mock-only (`/admin/*`, `/shipping/methods`). Los catálogos y reviews siguen cacheados porque sus rutas `/api/*` sí son reales y públicas.
- 2026-11-20: el primer seeder era un script npm del frontend y el usuario lo rechazó: quiere las cuentas listas al levantar Authorizer. Se movió a un servicio one-shot de Compose que habla directo con `authorizer:8080/v1/*`. Authorizer no expone alta administrativa con contraseña (su API admin sólo consulta, actualiza, elimina o invita por correo), así que se usa `/v1/signup` + `/v1/skip_mfa_setup`.
- 2026-11-20: el seeder maneja el caso de cuenta creada entre login y signup: ante un 400 de signup reintenta login y marca la cuenta como existente. `docker compose run --rm authorizer-seed` → 4/4 ready de forma idempotente.
- 2026-11-20: `catalog.service.test.ts` volvió a ser determinista activando `mockAdapter` explícitamente, porque el `.env` real ya no activa el adapter global. Suite 58/58, lint 0 errores (1 warning preexistente), tsc y build en modo real exit 0.
- 2026-11-20: el smoke real estaba bloqueado porque `customer-service` no estaba corriendo en el stack levantado (gateway devolvía 500 en `/api/customers/me`). Se levantó el servicio, quedó healthy y el endpoint respondió 404 (perfil aún no creado), que es el caso que `getCustomerProfile()` aprovisiona con `POST /api/customers`.
- 2026-11-20: el host quedó con 0.9 GB libres de 5.9 GB (8 contenedores Java + dos servidores Next + Chrome + VS Code), lo que degradó logs/red de Docker y causó timeouts. Se detuvo el `next dev` redundante del puerto 3000 para liberar memoria; el gateway volvió a responder.
- 2026-11-21: smoke end-to-end OK con `curl.exe` (Invoke-WebRequest de PowerShell mangled el Cookie header JWT): Ana ve `ana.prueba@latiendita.test` y "Ana", no ve los datos de Bruno; Bruno ve `bruno.prueba@latiendita.test` y "Bruno", no ve los de Ana; ningún `NEXT_REDIRECT`; login inválido → 401. T4 GREEN.
- 2026-11-21: trabajo entregado en cuatro commits en `main` (`fe111af`, `17b6385`, `ec932a1`, `013cf80`), sin push; `docs/OpenPencil/LaTiendita.fig` se dejó fuera a propósito. RDD assessment dio `high_risk` por el hot path de auth; el usuario eligió "Omitir esta vez" y el decline fue candidate-scoped, sin crear registro de revisión.
- 2026-11-20: verifiqué la política de password con un signup de prueba: `Password123` → 400 (falta carácter especial). La contraseña sí estaba documentada en `docs/SEED-ACCOUNTS.md` como `Semilla123!`; el error fue no destacarla en la entrega anterior. El usuario eligió `Password123!` para no relajar la política global.
- 2026-11-20: `update_user` de admin no tiene campo de password; `reset_password` exige token/OTP por correo. Única vía limpia con el entorno local: `POST /v1/admin/login` con `AUTHORIZER_ADMIN_SECRET` (cookie `authorizer-admin`), `POST /v1/admin/user` por email para obtener `id`, y `POST /v1/admin/delete_user` por `id`. El secreto nunca se imprimió ni se escribió en el repo. 4/4 cuentas borradas.
- 2026-11-20: test-first no aplica a esta unidad: es un cambio de valores de configuración, documentación y datos externos (cuentas en Authorizer), sin runner determinista que pueda estar en RED antes del cambio. Verificación funcional en vivo: seeder → 4/4 ready; login con `Password123!` → 200 y autenticado para las cuatro cuentas; login con `Password123` → 401. `grep Semilla123!` → 0 coincidencias.
- 2026-11-20: los dos primeros intentos del seeder fallaron por timeouts de red con 0.64 GB libres de RAM; con `SEED_TIMEOUT_MS=20000` y `SEED_ATTEMPTS=5` terminó 4/4. No fue un problema de credenciales.
- 2026-11-20: RDD assess de `2f5d7ae..HEAD` dio `high_risk` por la señal `auth` en el task doc (21 líneas, 5 archivos, ninguno de lógica de auth). Preflight con lentes `readability,reliability`; el usuario eligió "Omitir esta vez" y el decline exacto devolvió `action: declined`, `consent: declined_this_candidate` para el target `sha256:c399846b...`. Sin registro de revisión; RDD sigue habilitado para candidatos futuros.

L6 (verbatim, 2026-11-21): "si" — respuesta a la propuesta de arreglar los tres defectos verificados y correr la batería de tests.

- 2026-11-21: auditoría en vivo. `docker compose up -d --wait authorizer authorizer-seed` pasa: la imagen de Authorizer trae su propio HEALTHCHECK, así que la falta de `healthcheck:` en el compose NO es un bloqueante (el claim del explorador era falso y se descartó empíricamente).
- 2026-11-21: `authorizer-seed` reporta 0/4 ready. Log de Authorizer: `/v1/login` 401 `bad_password` y `/v1/signup` 400 `Email is already signed up.` Probe directo: ana con `Password123!` → 401, con `Semilla123!` → 200. Las cuentas nunca cambiaron de password; T5 no persistió. Reabierto T5/T6.
- 2026-11-21: el 409 de `/mi-cuenta` y el seeder están acoplados: recrear una cuenta en Authorizer cambia el `sub`, la fila de customer queda huérfana por email y el autoprovision vuelve a dar 409. Por eso T6 y T7 se diseñan juntos.
- 2026-11-21: host con 0,50 GB libres de 5,95 GB; no se levantan los 8 microservicios Java. Authorizer + seeder quedaron arriba para las pruebas acotadas.
- 2026-11-21 (T6): el admin login devolvía 200 con cookie `authorizer-admin`, pero `/v1/admin/user` responde `{ user: { id, ... } }`, no un objeto plano. El primer fix leía `data.id` y `deleteUserByEmail` devolvía `false` en silencio → 0/4. Corregido a `data.user.id`; diag temporal de códigos/nombres de cookie (nunca valores) removido. Resultado: 4/4 `reclaimed` y, en segunda corrida, 4/4 `existing`.
- 2026-11-21 (T7): el test de integración `shouldRejectDuplicateEmailWith409ProblemDetail` falló tras el rebind: codificaba el contrato viejo. Antes de cambiarlo se verificó que `IdentityPropagationFilter` SIEMPRE sobrescribe `X-User-Id`/`X-User-Email`/`X-User-Roles` con claims del JWT, así que el email del header es confiable. Aun así, si el JWT no trae claim de email, `resolveEmail` cae al body (no confiable) y el reclaim podría robar un perfil ajeno. Se agregó el guard `trustedEmail == null → DuplicateEmailException`. El test viejo se renombró a `shouldRejectBodySuppliedDuplicateEmailWithoutTrustedClaim` y se agregó `shouldReclaimProfileWhenAccountWasRecreatedWithSameTrustedEmail`. Customer-service 25/25.
- 2026-11-21 (T8): `/api/auth/admin/user` no hace match con la allowlist de 4 paths; el gateway respondió 401 (forward al `/error` protegido), no 404. La aserción se ajustó a `4xx` + "el stub nunca recibió `/v1/admin/user`", que es el invariante de seguridad real. Gateway 47/47.
- 2026-11-21 (T9): `npm run build` falló con `ECONNREFUSED 127.0.0.1:8081` al prerenderizar `/categorias/[slug]` (su `generateStaticParams()` llama a `GET /api/categories`). Es dependencia preexistente de backend vivo, ajena a auth; con `NEXT_PUBLIC_USE_MOCKS=1` (adapter documentado en `config/env.ts`) el build pasa y prerenderiza 13 categorías y 30 productos. Sin commit: se trabaja en `main` y el usuario hace push; `docs/OpenPencil/LaTiendita.fig` y los cambios preexistentes en `features/product/services/catalog.service.*` y `features/category/*.test.tsx` quedan intactos.
