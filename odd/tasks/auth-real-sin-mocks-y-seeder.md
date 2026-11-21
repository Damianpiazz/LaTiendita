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

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S2, S3 | inline | GREEN | `fe111af` — `config/env.test.ts` 3/3 (RED 1→GREEN 3); `.env` y `.env.example` con modo real; default invertido en `config/env.ts`. |
| T2 | S1, S4 | inline | GREEN | `17b6385` — `lib/customers/profile.test.ts` 2/2 y `features/customer/services/customer.service.test.ts` 3/3 (RED→GREEN); `<Suspense>` en `/mi-cuenta`; `npm run build` pasa en modo real. |
| T3 | S5 | inline | GREEN | `013cf80` — `runtime/docker/authorizer/seed/` con 4 cuentas; `docker compose run --rm authorizer-seed` → 4/4 ready dos veces seguidas; sin secretos en logs. |
| T4 | S6 | inline | GREEN | `ec932a1` + verificación final — vitest 58/58, lint 0 errores, tsc exit 0, build exit 0; smoke: Ana y Bruno ven su propio email/nombre y no el del otro, sin redirect; login inválido 401. |
| T5 | S7 | inline | GREEN | `a3f00ec` — default `Password123!` en seeder/Compose/.env.example/docs; 4/4 cuentas borradas y recreadas; 4/4 login 200; `Password123` → 401. |

Estrategia `exception-ok` por trabajo directo en `main` sin PR. Las líneas autoras se registran al cerrar cada unidad.

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
