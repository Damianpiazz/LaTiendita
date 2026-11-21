# Seeder de cuentas en Authorizer

Al levantar el stack con `docker compose -f runtime/docker/compose.yaml up -d`, el servicio `authorizer-seed` crea las cuentas de prueba **directamente en Authorizer**, sin gateway ni frontend. Es idempotente: una cuenta que ya puede loguearse se reutiliza; una ausente se crea. Puede correrse las veces que haga falta.

## Cuentas

| Email | Nombre |
| --- | --- |
| `ana.prueba@latiendita.test` | Ana Pérez |
| `bruno.prueba@latiendita.test` | Bruno Díaz |
| `camila.prueba@latiendita.test` | Camila Torres |
| `diego.prueba@latiendita.test` | Diego Ruiz |

Todas tienen rol `cliente`. La contraseña de prueba por defecto es `Password123!` y se overridea con `SEED_USER_PASSWORD` en `runtime/docker/.env`. Es una credencial exclusiva de desarrollo local, no de producción. Authorizer exige minúscula, mayúscula, número y carácter especial, por lo que `Password123` sin el `!` es rechazado con 400.

El listado vive en `runtime/docker/authorizer/seed/accounts.json`; agregar o quitar entradas ahí cambia qué cuentas se siembran.

## Flujo

El servicio espera a que Authorizer esté healthy y por cada cuenta:

1. `POST /v1/login` con la contraseña de prueba. Si responde con JWT, la cuenta ya existe.
2. Si Authorizer ofrece MFA de primer ingreso, `POST /v1/skip_mfa_setup` con la cookie `mfa_session`.
3. Si el login falla, `POST /v1/signup` con `given_name`, `family_name` y `roles: ["cliente"]`.
4. Si el signup ofrece MFA, también se omite.

El perfil de cliente en customer-service no se siembra aquí: se crea automáticamente la primera vez que cada usuario entra a `/mi-cuenta`, con `POST /api/customers`, que es idempotente por `sub`.

## Configuración

| Variable | Default | Uso |
| --- | --- | --- |
| `SEED_USER_PASSWORD` | `Password123!` | Contraseña compartida por las cuatro cuentas. |
| `SEED_ORIGIN` | `http://localhost:3000` | Origin requerido por Authorizer en escrituras; debe estar en `--allowed-origins`. |
| `SEED_TIMEOUT_MS` | `10000` | Timeout por petición. |
| `SEED_ATTEMPTS` | `3` | Reintentos ante errores de red o 5xx. |

Authorizer rechaza escrituras con un Origin no permitido: `http://localhost:3100`, por ejemplo, responde 403.

## Seguridad y alcance

El seeder no imprime tokens, cookies MFA ni contraseñas. Los errores reportan únicamente el email y un motivo sanitizado. No crea cuentas `admin`: Authorizer no expone alta administrativa con contraseña (su API admin sólo consulta, actualiza, elimina o invita por correo). Los roles del entorno local se declaran en `runtime/docker/compose.yaml`.

Para ejecutarlo de nuevo sin levantar el resto del stack:

```bash
docker compose -f runtime/docker/compose.yaml run --rm authorizer-seed
```
