# Seeder de cuentas en Authorizer

Al levantar el stack con `docker compose -f runtime/docker/compose.yaml up -d`, el servicio `authorizer-seed` crea las cuentas de prueba **directamente en Authorizer**, sin gateway ni frontend. Es idempotente: una cuenta que ya puede loguearse se reutiliza; una ausente se crea. Puede correrse las veces que haga falta.

## Cuentas

| Email | Nombre | Rol |
| --- | --- | --- |
| `ana.prueba@latiendita.test` | Ana Pérez | `cliente` |
| `bruno.prueba@latiendita.test` | Bruno Díaz | `cliente` |
| `camila.prueba@latiendita.test` | Camila Torres | `cliente` |
| `diego.prueba@latiendita.test` | Diego Ruiz | `cliente` |
| `elena.admin@latiendita.test` | Elena Morales | `admin` |
| `franco.admin@latiendita.test` | Franco Vega | `admin` |

La contraseña de prueba por defecto es `Password123!` y se overridea con `SEED_USER_PASSWORD` en `runtime/docker/.env`. Es una credencial exclusiva de desarrollo local, no de producción. Authorizer exige minúscula, mayúscula, número y carácter especial, por lo que `Password123` sin el `!` es rechazado con 400.

El listado vive en `runtime/docker/authorizer/seed/accounts.json`; agregar o quitar entradas ahí cambia qué cuentas se siembran. Cada entrada puede incluir `roles` (por defecto `["cliente"]`).

## Flujo

El servicio espera a que Authorizer responda en `/v1/meta` y por cada cuenta:

1. `POST /v1/login` con la contraseña de prueba. Si responde con JWT o desafío MFA, la cuenta ya existe.
2. Si el login falla con credenciales inválidas, `POST /v1/signup` con `given_name`, `family_name` y `roles: ["cliente"]`.
3. Si el email ya existe pero la contraseña no coincide, el seeder reclama la cuenta con la API admin (`/v1/admin/login`, `/v1/admin/user`, `/v1/admin/delete_user`) y la recrea con la contraseña configurada.
4. Si Authorizer ofrece MFA de primer ingreso, `POST /v1/skip_mfa_setup` con la cookie `mfa_session`.
5. Sincroniza los roles declarados en `accounts.json` con `POST /v1/admin/update_user`. Las cuentas con rol `admin` se crean como `cliente` y luego se promueven por API admin, porque `admin` es un rol protegido en Authorizer.

El perfil de cliente en customer-service no se siembra aquí: se crea automáticamente la primera vez que cada usuario entra a `/mi-cuenta`, con `POST /api/customers`, que es idempotente por `sub`.

## Configuración

| Variable | Default | Uso |
| --- | --- | --- |
| `AUTHORIZER_ADMIN_SECRET` | (requerido) | Secreto admin de Authorizer; se usa para reclamar cuentas y sincronizar roles. |
| `SEED_USER_PASSWORD` | `Password123!` | Contraseña compartida por las cuentas de prueba. |
| `SEED_ORIGIN` | `http://localhost:3000` | Origin requerido por Authorizer en escrituras; debe estar en `--allowed-origins`. |
| `SEED_TIMEOUT_MS` | `10000` | Timeout por petición. |
| `SEED_ATTEMPTS` | `3` | Reintentos ante errores de red o 5xx. |

Authorizer rechaza escrituras con un Origin no permitido: `http://localhost:3100`, por ejemplo, responde 403.

## Seguridad y alcance

El seeder no imprime tokens, cookies MFA ni contraseñas. Los errores reportan únicamente el email y un motivo sanitizado. Usa `AUTHORIZER_ADMIN_SECRET` para iniciar sesión en la API admin de Authorizer, reclamar cuentas existentes y sincronizar roles protegidos como `admin`; el secreto se lee desde el entorno y no se persiste en el repo ni se loguea. Los roles del entorno local se declaran en `runtime/docker/compose.yaml`.

Para ejecutarlo de nuevo sin levantar el resto del stack:

```bash
docker compose -f runtime/docker/compose.yaml run --rm authorizer-seed
```
