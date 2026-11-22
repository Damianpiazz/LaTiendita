# Microservicio: `customer-service`


## 1. Responsabilidad

Gestiona el **perfil del cliente** de la plataforma: alta del registro de cliente a partir de la identidad emitida por Authorizer, datos de contacto, preferencias de newsletter con consentimiento y la vista de consulta del panel de **Admin Clientes**. Es la única fuente de verdad sobre los datos de negocio del cliente (identidad, contacto, estado y actividad); otros servicios la consultan, pero nunca almacenan copias de esta información.

**Lo que NO le pertenece** (para evitar solapamientos):

- **Credenciales, contraseñas y recuperación de acceso**: las resuelve **Authorizer** (servicio externo de autenticación, validado por el gateway con JWT HS256/issuer). Este servicio nunca recibe, almacena ni valida contraseñas ni hashes: su clave de identidad es el claim `sub` de Authorizer, propagado en el header `X-User-Id`.
- **Pedidos**: `order-service`.
- **Reseñas**: `review-service`.
- **Direcciones de entrega**: `shipment-service` (`delivery_addresses` es la única fuente de verdad; este servicio no almacena ni expone direcciones).

---

## 2. Endpoints

Todas las rutas son relativas a la raíz del servicio (ej. `http://customerService:8080`).

> **Estado:** el servicio ya está integrado al gateway. `GET /api/customers` conserva su contrato histórico y se agregó el prefijo `/api/customers/admin/**` para Admin Clientes. Los agregados de pedidos son calculados por `order-service`; este servicio sólo compone los DTOs de respuesta.

La identidad llega propagada por el gateway en los headers `X-User-Id` (claim `sub`), `X-User-Email` y `X-User-Roles`; toda petición incluye además `X-Correlation-Id`.

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes | — | `200` `{ status }` | No |
| `POST` | `/api/customers` | Alta idempotente del perfil en el primer login (clave = `X-User-Id`); crea el registro si no existe | `CreateCustomerRequest` | `201`/`200` CustomerDto | Sí (cliente) |
| `GET` | `/api/customers/me` | Obtiene el perfil del cliente autenticado | — | `200` CustomerDto | Sí (cliente) |
| `PATCH` | `/api/customers/me` | Actualiza datos de contacto (nombre, apellido, teléfono) | `UpdateCustomerRequest` | `200` CustomerDto | Sí (cliente) |
| `GET` | `/api/customers` | Listado histórico de clientes (paginado; filtros por nombre, email y estado); contrato sin cambios | — (query params) | `200` `Page<CustomerSummaryDto>` | Sí (admin) |
| `GET` | `/api/customers/admin` | Listado de Admin Clientes con `name`, `email`, `phone`, `status`, `segment`, `sort`, `page` y `size` | — (query params) | `200` `Page<AdminCustomerResponse>` | Sí (admin) |
| `GET` | `/api/customers/admin/summary` | KPIs del wireframe con los mismos filtros que el listado (incluye `phone`) | — (query params) | `200` `AdminCustomerSummaryResponse` | Sí (admin) |
| `GET` | `/api/customers/admin/export.csv` | Exportación CSV con el mismo filtrado y orden (incluye `phone`) | — (query params) | `200` `text/csv` | Sí (admin) |
| `GET` | `/api/customers/{id}` | Detalle de un cliente para Admin Clientes (datos, estado y actividad) | — | `200` CustomerDto | Sí (admin) |
| `PUT` | `/api/customers/me/newsletter` | Suscribe o da de baja de la newsletter, registrando el consentimiento | `NewsletterRequest` | `200` NewsletterDto | Sí (cliente) |

*Nota de integración:* el gateway enruta `/api/customers/**` a `customerService` y exige `hasRole("admin")` para `/api/customers` y `/api/customers/admin/**`. Las llamadas internas a `/api/orders/admin/**` reenvían únicamente los headers de identidad inyectados por el gateway (`X-User-Id`, `X-User-Email`, `X-User-Roles`), nunca datos del body ni otros headers.

### 2.1 Reglas de Admin Clientes

- `segment`: `sin-compras` si `orderCount == 0`; si no, `recurrente` si `orderCount >= 2`; si no, `nueva` si `createdAt` está en los últimos 30 días; si no, `activa` para `ACTIVE` o `inactiva` para `SUSPENDED`. `sin-compras` se evalúa primero y se mantiene distinto de `SUSPENDED`: el badge del wireframe muestra `Inactiva`, pero el filtro `Sin compras` usa su propio segmento. En JSON se serializa en minúsculas.
- `phone`: filtro dedicado sobre `CustomerEntity.phone` con el mismo patrón `LIKE` recortado y en minúsculas que `email`; `/summary` y `/export.csv` aceptan el mismo `phone`. El parámetro `name` es de búsqueda libre y coincide con `firstName`, `lastName`, `email` o `phone`. Cuando `name`, `email` y `phone` reciben el mismo término —como hace el único cuadro de búsqueda del wireframe— se trata como una sola búsqueda libre (OR) y no como tres filtros AND; valores distintos en `email` o `phone` conservan su significado estrecho.
- `orderCount`, `totalSpent` y `lastPurchaseAt` llegan de `GET /api/orders/admin/customers/stats`; los pedidos `CANCELLED` quedan excluidos. Un cliente sin pedidos no cancelados recibe ceros y `lastPurchaseAt = null`.
- `summary` sin filtros toma `recurring` y `averageTicket` de `GET /api/orders/admin/customers/summary`. Con cualquier filtro, ambos se recalculan a partir de las estadísticas de los clientes filtrados. `averageTicket` = total gastado / cantidad de clientes con al menos un pedido.
- Si `order-service` no responde, el cliente HTTP registra `warn` y degrada a listas vacías o ceros; nunca inventa pedidos ni modifica los datos del cliente.

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

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Customer` 1 → N `Order` | Uno-a-muchos | Un cliente puede tener muchos pedidos (reside en `order-service`; referencia cruzada por `id`/`authorizer_sub`, sin FK) |
| `Customer` 1 → N `Review` | Uno-a-muchos | Un cliente puede publicar muchas reseñas (reside en `review-service`, sin FK) |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Identidad | La clave del perfil es el `sub` de Authorizer (`authorizer_sub`); este servicio nunca recibe ni almacena contraseñas ni hashes |
| Unicidad | `authorizer_sub` y `email` no pueden repetirse |
| Borrado | Borrado lógico (`deleted_at`); se conserva el historial referenciado por pedidos y reseñas |
| Newsletter | La suscripción exige consentimiento explícito con `newsletter_consent_at`; la baja es inmediata y no requiere consentimiento |
| Invariantes | `created_at <= updated_at`; si `newsletter_opt_in = true` entonces `newsletter_consent_at` no puede ser nulo |

### 4.4 DTOs (contratos de API)

> Los campos se derivan de la entidad de §4.1 y del contrato de Admin Clientes (§2.1). Sin direcciones: viven en `shipment-service` (`delivery_addresses`).

| DTO | Uso | Campos | Origen / nota |
|---|---|---|---|
| `CustomerDto` | `POST /api/customers`, `GET /api/customers/me`, `GET /api/customers/{id}` | `id`, `authorizerSub`, `email`, `firstName`, `lastName`, `phone`, `status`, `newsletterOptIn`, `newsletterConsentAt`, `lastLoginAt`, `createdAt`, `updatedAt` | De `Customer`; se omite `deletedAt` (borrado lógico interno) |
| `CustomerSummaryDto` | `GET /api/customers` (listado histórico) | `id`, `firstName`, `lastName`, `email`, `status`, `lastLoginAt` | Proyección reducida; contrato intacto, filtros por nombre, email y estado (§2) |
| `AdminCustomerResponse` | `GET /api/customers/admin` | `customerId`, `firstName`, `lastName`, `email`, `phone`, `status`, `createdAt`, `lastLoginAt`, `orderCount`, `totalSpent`, `lastPurchaseAt`, `segment` | Fusión del cliente con los agregados de `order-service`; `segment` en minúsculas (§2.1) |
| `AdminCustomerSummaryResponse` | `GET /api/customers/admin/summary` | `totalCustomers`, `newThisMonth`, `recurring`, `averageTicket` | KPIs con los mismos filtros que el listado (§2.1) |
| `CustomerOrderStats` / `CustomerOrderSummary` | DTO internos del `OrderClient` | `customerId`, `orderCount`, `totalSpent`, `lastPurchaseAt` / `recurringCustomers`, `averageTicket` | Espejo de los agregados de `order-service`; nunca se construyen con datos mock |
| `CreateCustomerRequest` | `POST /api/customers` | `authorizerSub`, `email`, `firstName`, `lastName`, `phone` | `authorizerSub` se toma del header `X-User-Id`; alta idempotente en el primer login |
| `UpdateCustomerRequest` | `PATCH /api/customers/me` | `firstName`, `lastName`, `phone` | Datos de contacto (§2) |
| `NewsletterRequest` | `PUT /api/customers/me/newsletter` | `newsletterOptIn` | El alta exige consentimiento explícito; la baja es inmediata (§4.3) |
| `NewsletterDto` | `PUT /api/customers/me/newsletter` | `newsletterOptIn`, `newsletterConsentAt` | De `Customer` |

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
| `order-service` | Agregados reales para Admin Clientes: `/api/orders/admin/customers/stats` y `/api/orders/admin/customers/summary` | HTTP/REST (`ORDER_SERVICE_BASE_URL`, por defecto `http://localhost:8080`) | Ceros y listas vacías con log `warn`; nunca se inventan pedidos | ❌ No |

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
| `Home` (sección Newsletter) | Suscripción y baja con consentimiento: `PUT /api/customers/me/newsletter` |
| `Admin → Clientes` (panel junto a Resumen, Productos, Categorías, Pedidos y Pagos) | Listado, KPIs, CSV y detalle: `GET /api/customers/admin`, `/admin/summary`, `/admin/export.csv` y `GET /api/customers/{id}`; segmentos y métricas derivadas de agregados reales de pedidos (§2.1) |
| `Checkout` / perfil (datos de contacto) | Consulta y actualización: `GET /api/customers/me`, `PATCH /api/customers/me` |
| `Product Detail`, `Cart`, `Listings`, `Pagos` | No aplica a este servicio (sin responsabilidad propuesta) |
