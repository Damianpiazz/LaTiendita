# Integrar Login, Register y Password Recovery con Authorizer

Objective: dejar las tres pantallas de autenticación consumiendo Authorizer a través del BFF de Next, con sesión JWT httpOnly y sin cambiar el wireframe.
Branch: `main` (el push lo hace el usuario). Delivery: `exception-ok` — commits por unidad de trabajo, sin PR.
Checks: `npx vitest run`, `npm run lint`, `npm run build` y smoke real con gateway + Authorizer.

## Specs

S1 — Alcance (petición literal del usuario): "Mira primero Implementame la integracion de las pantallas Login Wireframe , Register Wireframe, Password Recovery Wireframe, quiero la integracion completa entre frontend y backend. completamente funcional." Aceptación: las tres pantallas envían datos reales, muestran errores reales y dejan una sesión usable; el modo mock sigue funcionando.

S2 — Decisión previa vigente (respuesta literal): "Login real con Authorizer". El JWT se guarda en cookie httpOnly `lt_access_token`; `lt_user` sólo expone datos no sensibles para el header. Ningún token se expone a JavaScript del browser.

S3 — Contrato Authorizer verificado en vivo (gateway `http://localhost:8081`, OpenAPI `http://127.0.0.1:8080/openapi.json`): `POST /api/auth/login` → `/v1/login`; `POST /api/auth/signup` → `/v1/signup`; `POST /api/auth/forgot_password` → `/v1/forgot_password`; `POST /api/auth/skip_mfa_setup` → `/v1/skip_mfa_setup`. Las peticiones de escritura sin `Origin`/`Referer` responden `403 csrf_validation_failed`, así que el BFF reenvía el `Origin` del navegador. `v1AuthResponse` trae `access_token`, `expires_in`, `user`; `authorizerv1User` trae `id`, `email`, `given_name`, `family_name`, `roles`.

S4 — MFA del entorno real: un usuario nuevo obtiene `message: "Proceed to mfa setup"`, `should_show_totp_screen: true`, `access_token: ""` y cookies `mfa_session` / `mfa_session_domain`. Como los wireframes no tienen pantalla MFA, el BFF conserva esas cookies y el cliente llama una vez a `skip_mfa_setup` con el email para obtener el token. Si la organización exigiera MFA, el skip falla y se muestra el error; no se inventa un token.

S5 — Política de contraseña real Authorizer: al menos 6 caracteres con minúscula, mayúscula, número y carácter especial. El wireframe muestra "Mínimo 8 caracteres, una mayúscula y un número."; se conserva ese texto por fidelidad al diseño y se muestra el error real del backend cuando la contraseña no cumple la política efectiva.

S6 — Login Wireframe: "Ingresá a tu cuenta"; "Comprá más rápido y guardá tus favoritos."; "Email"; "Contraseña"; "¿Olvidaste tu contraseña?"; "Ingresar"; "¿No tenés cuenta?"; "Creá tu cuenta". El submit va al BFF; éxito redirige a `/carrito`, error muestra un `alert`.

S7 — Register Wireframe: "Creá tu cuenta"; "Completá tus datos para comprar en La Tiendita."; "Nombre y apellido"; "Ana Pérez"; "Confirmar contraseña"; "Mínimo 8 caracteres, una mayúscula y un número."; "Acepto los términos y condiciones y la política de privacidad"; "Crear cuenta"; "¿Ya tenés cuenta?"; "Ingresá". `Nombre y apellido` se divide en `given_name` / `family_name`; el checkbox de términos es obligatorio; `confirm_password` se envía tal cual.

S8 — Password Recovery Wireframe: "Recuperá tu contraseña"; "Ingresá tu email y te enviamos las instrucciones."; "Enviar instrucciones"; "¿Recordaste tu contraseña?"; "Ingresá"; "Si no encontrás el email, revisá tu carpeta de spam." El submit llama a `forgot_password` y muestra el mensaje del backend; no se agrega pantalla de reset porque el wireframe no la define.

S9 — Tests (RED → GREEN → REFACTOR cuando aplica): sesión/parseo y cookies, MFA auto-skip, servicios de auth contra BFF con `baseURL: ""`, tres forms con submit/éxito/error, header anónimo vs. autenticado y logout. Verificación final: `npx vitest run`, `npm run lint`, `npm run build`.

S10 — Smoke real: usuario de prueba creado con `POST /api/auth/signup` (`cliente.wireframe@example.com`); recorrer login → MFA skip → cookie httpOnly, register → sesión, forgot → 200; verificar que el HTML del header cambia a chip con logout y que ningún token aparece en JS. Documentar variables/flags nuevos.

## Tasks

| ID | Specs | Route | Status | Evidence |
| --- | --- | --- | --- | --- |
| T1 | S2, S3, S4, S6, S9 | inline | GREEN | 16/16 tests enfocados; `tsc --noEmit` limpio; lint 0 errores (1 warning preexistente en `shipment`); smoke real en T4; commit `e5ffd25` (BFF y helpers) |
| T2 | S3, S5, S7, S9 | inline | GREEN | RED 6 fallos; implementado `signup` + ruta `signup` + `RegisterForm` con términos/confirmación; 5 archivos/15 tests |
| T3 | S3, S8, S9 | inline | GREEN | RED 4 fallos + suite sin módulo; implementada ruta `forgot-password` + `requestPasswordReset` + `PasswordRecoveryForm` + mock; 3 archivos/13 tests |
| T4 | S1, S10 | inline | GREEN | smoke real: forgot 200, login bad 401, signup MFA, skip-mfa JWT httpOnly, sesión, logout; sin token en HTML |

Forecast de líneas autoras: ~520 (T1 ~240, T2 ~140, T3 ~90, T4 ~50); estrategia `exception-ok` por trabajo directo en `main`. Esta feature desbloquea T1 del carrito.

## Log

L1 (verbatim): "Mira primero Implementame la integracion de las pantallas Login Wireframe , Register Wireframe, Password Recovery Wireframe, quiero la integracion completa entre frontend y backend. completamente funcional."

- 2026-11-20: el usuario cambia prioridad; el carrito sigue pendiente y su T1 queda marcado como dependiente de esta feature. No se hizo push ni commit: los archivos de sesión ya escritos se reasignan a esta feature.
- OpenPencil estaba desconectado al reconsultar los frames; se usa el texto ya volcado en los componentes y el mapa anterior, sin releer el `.fig`.
- Descubrimiento verificado: Authorizer rechaza signup/forgot sin `Origin`; exige carácter especial en la contraseña; el login de usuario nuevo devuelve oferta TOTP sin token y `skip_mfa_setup` con las cookies `mfa_session` entrega el JWT real. `forgot_password` devuelve 200 con mensaje de instrucciones.
- `docs/OpenPencil/LaTiendita.fig` quedó modificado antes de este trabajo: se excluye de los commits.
- 2026-11-20, T1: RED confirmado con 6 fallos/4 archivos (más 2 suites nuevas sin módulo). Implementado `lib/auth/authorizer-proxy.ts` (Origin, Set-Cookie, detección de oferta MFA), rutas `login` y `skip-mfa`, auto-skip en `auth.service`, `LoginForm` con submit/error, `SiteHeader` con `Salir`, wrapper `SiteHeaderSession` y rutas mock de sesión. GREEN: 7 archivos/16 tests. `tsc --noEmit` limpio. El layout público sigue prerenderable porque la sesión se lee en un componente cliente. Pendiente: smoke real contra gateway + Authorizer.
- 2026-11-20, T2: RED con 6 fallos (ruta sin módulo, servicio y form). `signup` divide `Nombre y apellido` en `given_name`/`family_name`, envía `confirm_password` y `roles: ["cliente"]`, propaga el error real de política de contraseña y conserva las cookies MFA. `RegisterForm` valida términos y coincidencia de contraseñas. GREEN: 5 archivos/15 tests.
- 2026-11-20, T3: RED con 4 fallos (ruta sin módulo, servicio y form). Ruta `forgot-password` reenvía `Origin` a `/api/auth/forgot_password` y devuelve el mensaje real; `requestPasswordReset` usa `baseURL: ""`; `PasswordRecoveryForm` muestra el mensaje en `role="status"` y errores en `role="alert"`, conservando la nota de spam; mock agregado. GREEN: 3 archivos/13 tests.
- 2026-11-20, verificación global: `npx vitest run` → 19 archivos/50 tests; `npm run lint` → 0 errores (1 warning preexistente en `shipment`); `npx tsc --noEmit` limpio; `npm run build` correcto (Next 16.4.5, 68 páginas estáticas, rutas `/api/auth/*` dinámicas).
- 2026-11-20, T4 smoke real (servidor aislado con `NEXT_PUBLIC_USE_MOCKS=false`, `NEXT_PUBLIC_API_URL=http://localhost:8081`, puerto 3100 y `Origin` permitido `http://localhost:3000`): forgot-password → 200 con mensaje real de Authorizer; login con password inválida → 401 "Email o contraseña incorrectos"; signup de usuario desechable → 200 `mfaRequired` con cookies `mfa_session`; skip-mfa → 200 con `lt_access_token` HttpOnly; login posterior → 200 con cookie HttpOnly; `/api/auth/session` → autenticado; HTML público sin `lt_access_token` ni JWT; logout → 200 y sesión desautenticada. El build de producción con mocks desactivados falla al prerenderizar `/admin` y `/mi-cuenta` (401 preexistentes), por lo que el smoke se hizo en modo dev. Config temporal (`distDir`, includes de tsconfig) revertida. No se registran contraseñas ni JWT en documentos.
