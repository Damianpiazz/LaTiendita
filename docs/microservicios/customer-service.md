# Microservicio: `customer-service`


## 1. Responsabilidad

Gestiona el **perfil del cliente** de la plataforma: alta del registro de cliente a partir de la identidad emitida por Authorizer, datos de contacto, direcciones guardadas para compras futuras, preferencias de newsletter con consentimiento y la vista de consulta del panel de **Admin Clientes**. Es la única fuente de verdad sobre los datos de negocio del cliente (identidad, contacto, direcciones, estado y actividad); otros servicios la consultan, pero nunca almacenan copias de esta información.

**Lo que NO le pertenece** (para evitar solapamientos):

- **Credenciales, contraseñas y recuperación de acceso**: las resuelve **Authorizer** (servicio externo de autenticación, validado por el gateway con JWT HS256/issuer). Este servicio nunca recibe, almacena ni valida contraseñas ni hashes: su clave de identidad es el claim `sub` de Authorizer, propagado en el header `X-User-Id`.
- **Pedidos**: `order-service`.
- **Reseñas**: `review-service`.

---

## 2. Endpoints

Todas las rutas son relativas a la raíz del servicio (ej. `http://customerService:8080`).

> **Nota:** Propuesta de contrato; aún no implementado. El servicio es un scaffold (solo `CustomerServiceApplication` y su test): los endpoints, entidades y eventos de este documento definen lo que debería implementarse para cumplir el wireframe.

La identidad llega propagada por el gateway en los headers `X-User-Id` (claim `sub`), `X-User-Email` y `X-User-Roles`; toda petición incluye además `X-Correlation-Id`.

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes | — | `200` `{ status }` | No |
| `POST` | `/api/customers` | Alta idempotente del perfil en el primer login (clave = `X-User-Id`); crea el registro si no existe | `CreateCustomerRequest` | `201`/`200` CustomerDto | Sí (cliente) |
| `GET` | `/api/customers/me` | Obtiene el perfil del cliente autenticado | — | `200` CustomerDto | Sí (cliente) |
| `PATCH` | `/api/customers/me` | Actualiza datos de contacto (nombre, apellido, teléfono) | `UpdateCustomerRequest` | `200` CustomerDto | Sí (cliente) |
| `GET` | `/api/customers` | Listado de clientes para Admin Clientes (paginado; filtros por nombre, email y estado) | — (query params) | `200` `Page<CustomerSummaryDto>` | Sí (admin) |
| `GET` | `/api/customers/{id}` | Detalle de un cliente para Admin Clientes (datos, dirección, estado y actividad) | — | `200` CustomerDto | Sí (admin) |
| `GET` | `/api/customers/me/addresses` | Lista las direcciones guardadas del cliente | — | `200` `List<AddressDto>` | Sí (cliente) |
| `POST` | `/api/customers/me/addresses` | Crea una dirección guardada (checkbox «Guardar esta dirección para futuras compras» del checkout) | `CreateAddressRequest` | `201` AddressDto | Sí (cliente) |
| `PUT` | `/api/customers/me/addresses/{addressId}` | Actualiza una dirección guardada | `UpdateAddressRequest` | `200` AddressDto | Sí (cliente) |
| `DELETE` | `/api/customers/me/addresses/{addressId}` | Elimina una dirección guardada | — | `204` | Sí (cliente) |
| `PUT` | `/api/customers/me/newsletter` | Suscribe o da de baja de la newsletter, registrando el consentimiento | `NewsletterRequest` | `200` NewsletterDto | Sí (cliente) |

*Nota de integración:* el gateway aún no expone `/api/customers/**`; se debe agregar una ruta `Path=/api/customers/**` → `lb://customerService`, y las rutas de listado/detalle de clientes deben exigir `hasRole("admin")` (hoy `anyRequest().authenticated()` sólo autentica).

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Customer not found",
  "status": 404,
  "detail": "Customer with id 42 not found"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla | `Email must be valid` |
| `401` | Unauthorized | Token ausente o inválido | `Missing Authorization header` |
| `403` | Forbidden | Usuario sin permiso sobre el recurso | `Cannot access addresses of another customer` |
| `404` | Not Found | Recurso inexistente | `Customer with id 42 not found` |
| `409` | Conflict | Regla de negocio violada (duplicado) | `Email already registered` |
| `422` | Unprocessable | Entidad válida pero no procesable | `Newsletter requires a consent timestamp` |
| `500` | Internal Error | Fallo inesperado | `Unexpected error` |
| `503` | Service Unavailable | Dependencia externa caída (PostgreSQL) | `Database is not available` |

---

## 4. Modelo de datos (Entidad)

### 4.1 Entidades

#### `<Customer>` → tabla `customers`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `authorizer_sub` | `varchar(255)` | ❌ | ❌ | ✅ | Claim `sub` de Authorizer (llega en `X-User-Id`); clave de identidad, nunca una password |
| `email` | `varchar(255)` | ❌ | ❌ | ✅ | Correo del cliente (llega en `X-User-Email`) |
| `first_name` | `varchar(80)` | ❌ | ❌ | ❌ | Nombre |
| `last_name` | `varchar(80)` | ❌ | ❌ | ❌ | Apellido |
| `phone` | `varchar(30)` | ❌ | ✅ | ❌ | Teléfono de contacto |
| `status` | `varchar(20)` | ❌ | ❌ | ❌ | Estado (`ACTIVE`, `SUSPENDED`) |
| `newsletter_opt_in` | `boolean` | ❌ | ❌ | ❌ | Suscripción a la newsletter (consentimiento explícito) |
| `newsletter_consent_at` | `timestamptz` | ❌ | ✅ | ❌ | Fecha del consentimiento (requerido si `newsletter_opt_in = true`) |
| `last_login_at` | `timestamptz` | ❌ | ✅ | ❌ | Última actividad conocida (primer login / actualización de perfil) |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |
| `deleted_at` | `timestamptz` | ❌ | ✅ | ❌ | Borrado lógico (marca de baja) |

#### `<CustomerAddress>` → tabla `customer_addresses`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `customer_id` | `UUID` | ❌ | ❌ | ❌ | FK → `customers.id` |
| `first_name` | `varchar(80)` | ❌ | ❌ | ❌ | Nombre del destinatario |
| `last_name` | `varchar(80)` | ❌ | ❌ | ❌ | Apellido del destinatario |
| `phone` | `varchar(30)` | ❌ | ❌ | ❌ | Teléfono de la dirección |
| `address_line` | `varchar(255)` | ❌ | ❌ | ❌ | Dirección (calle y número) |
| `city` | `varchar(100)` | ❌ | ❌ | ❌ | Ciudad |
| `province` | `varchar(100)` | ❌ | ❌ | ❌ | Provincia / estado |
| `postal_code` | `varchar(20)` | ❌ | ❌ | ❌ | Código postal |
| `reference_notes` | `varchar(255)` | ❌ | ✅ | ❌ | Referencias para la entrega |
| `is_default` | `boolean` | ❌ | ❌ | ❌ | Dirección sugerida en el checkout |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Customer` 1 → N `CustomerAddress` | Uno-a-muchos | Un cliente puede tener varias direcciones guardadas (reside en `customer_addresses`) |
| `Customer` 1 → N `Order` | Uno-a-muchos | Un cliente puede tener muchos pedidos (reside en `order-service`; referencia cruzada por `id`/`authorizer_sub`, sin FK) |
| `Customer` 1 → N `Review` | Uno-a-muchos | Un cliente puede publicar muchas reseñas (reside en `review-service`, sin FK) |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Identidad | La clave del perfil es el `sub` de Authorizer (`authorizer_sub`); este servicio nunca recibe ni almacena contraseñas ni hashes |
| Unicidad | `authorizer_sub` y `email` no pueden repetirse |
| Borrado | Borrado lógico (`deleted_at`); se conserva el historial referenciado por pedidos y reseñas |
| Direcciones | Sólo el dueño (o un rol `admin`) puede leer o modificar las direcciones de un cliente |
| Newsletter | La suscripción exige consentimiento explícito con `newsletter_consent_at`; la baja es inmediata y no requiere consentimiento |
| Invariantes | `created_at <= updated_at`; si `newsletter_opt_in = true` entonces `newsletter_consent_at` no puede ser nulo |

---

## 5. Configuración

### 5.1 Variables de entorno

No hay `JWT_SECRET`: el gateway valida el JWT (firma HS256 e issuer de Authorizer) y propaga la identidad en headers; el servicio sólo lee `X-User-Id`, `X-User-Email`, `X-User-Roles` y `X-Correlation-Id`.

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL | ✅ | — | `jdbc:postgresql://db:5432/customer` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `customer` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `ORDER_SERVICE_BASE_URL` | Base URL de `order-service` (actividad en Admin Clientes) | ❌ | `http://localhost:8080` | `lb://orderService` |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka (consumo de `user-events`) | ❌ | `localhost:9092` | `kafka:9092` |
| `EUREKA_URL` | URL del registro Eureka | ❌ | `http://localhost:8761/eureka` | `http://eureka:8761/eureka` |
| `EUREKA_ENABLED` | Habilita el registro en Eureka | ❌ | `true` | `true` |
| `LOGGING_LEVEL_ROOT` | Nivel de log (`INFO`, `DEBUG`) | ❌ | `INFO` | `INFO` |

*Registro:* `spring.application.name=customerService` (Eureka).

### 5.2 Spring profiles

| Profile | Uso |
|---|---|
| `local` | Desarrollo con `docker compose` |
| `test` | Tests automatizados (Testcontainers) |
| `k8s` | Despliegue en Kubernetes |

### 5.3 Recursos Kubernetes (orientativo)

| Recurso | Request | Limit | Notas |
|---|---|---|---|
| CPU | `250m` | `1000m` | — |
| Memoria | `512Mi` | `768Mi` | JVM con `-XX:MaxRAMPercentage=75` |

---

## 6. Dependencias

### 6.1 Dependencias internas (otros microservicios)

| Servicio | Para qué | Protocolo | Fallback si cae | Crítica |
|---|---|---|---|---|
| `gateway` | Enruta `/api/customers/**`, valida el JWT, inyecta `X-User-Id`/`X-User-Email`/`X-User-Roles` y propaga `X-Correlation-Id` | HTTP/REST (upstream; este servicio no lo llama) | El servicio no está expuesto fuera del gateway | ✅ Sí |
| `order-service` | Último pedido del cliente para la columna de actividad de Admin Clientes | HTTP/REST (`ORDER_SERVICE_BASE_URL`, por defecto `http://localhost:8080`) | Mostrar sólo la actividad de sesión (`last_login_at`) | ❌ No |

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia principal | ✅ Sí | `503`, readiness en `DOWN` |
| Authorizer | Terceros (IdP) | Emite el JWT que el gateway valida (firma/issuer) y publica `user-events` con el registro de usuarios | ✅ Sí (altas) | Siguen atendiendo las peticiones con JWT vigentes; no se crean perfiles nuevos hasta que vuelva |
| Kafka | Mensajería | Consumo de `user-events` (alta de perfil) | ❌ No | El alta se resuelve con `POST /api/customers` en el primer login |
| Eureka | Registro de servicios | Descubrimiento para el gateway (`spring.application.name=customerService`) | ❌ No | `EUREKA_ENABLED=false` para operar sin registro |

### 6.3 Eventos publicados/consumidos

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| `USER_REGISTERED` | Consume (topic `user-events`, grupo `customer-service`) | Registro de usuario en Authorizer → crea el perfil de cliente | `{ sub, email, firstName, lastName, occurredAt }` |
| `CUSTOMER_CREATED` | Publica (topic `customer-events`, vía outbox) | Alta del perfil de cliente (primer login) | `{ id, sub, email, occurredAt }` |

> Si Authorizer no publica `user-events`, el alta se resuelve con el endpoint idempotente `POST /api/customers` (§2); ambos caminos son idempotentes.

---

## 7. Cobertura del wireframe

Fuente: `docs/OpenPencil/LaTiendita.fig`, página "WireFrame".

| Pantalla del wireframe | Endpoints / responsabilidades del servicio |
|---|---|
| `Register` / `Login` (tarjeta «Auth card») | Solo el perfil: alta idempotente en el primer login con `POST /api/customers` (clave = `sub`). Las credenciales y la contraseña las maneja Authorizer: fuera del alcance |
| `Password Recovery` (tarjeta «Auth card») | Sin implementación en este servicio; la recuperación de contraseña es responsabilidad de Authorizer |
| `Checkout → Shipping` («Guardar esta dirección para futuras compras») | CRUD de direcciones guardadas: `GET`/`POST` `/api/customers/me/addresses`, `PUT`/`DELETE` `/api/customers/me/addresses/{addressId}` |
| `Home` (sección Newsletter) | Suscripción y baja con consentimiento: `PUT /api/customers/me/newsletter` |
| `Admin → Clientes` (panel junto a Resumen, Productos, Categorías, Pedidos y Pagos) | Listado y detalle con datos, dirección, estado y actividad: `GET /api/customers`, `GET /api/customers/{id}` |
| `Checkout` / perfil (datos de contacto) | Consulta y actualización: `GET /api/customers/me`, `PATCH /api/customers/me` |
| `Product Detail`, `Cart`, `Listings`, `Pagos` | No aplica a este servicio (sin responsabilidad propuesta) |
