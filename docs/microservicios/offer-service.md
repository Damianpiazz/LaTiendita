# Microservicio: `offer-service`


## 1. Responsabilidad

Define y publica las **promociones y beneficios** de la plataforma: descuentos por producto, categoría o pedido con sus vigencias, las reglas de los beneficios mostrados en la UI (tarjetas "Benefits") y la **evaluación de ofertas** que `product-service` y `order-service` consultan para aplicar precios promocionados y totales. Es la única fuente de verdad sobre qué promociones están vigentes y cómo se evalúan; los demás servicios consultan su evaluación pero no duplican las reglas.

**Límites:**

- **No aplica descuentos ni calcula precios**: `product-service` muestra el precio base y `order-service` calcula el total del pedido. Este servicio solo decide *qué* promoción aplica y con qué descuento.
- **No calcula ni cobra envío**: el cálculo de costos de envío (zona, peso, transportista) y su aplicación al pedido pertenecen a `shipment-service`. **Regla propuesta:** el umbral «Gratis desde $ 50.000» se define aquí como una promoción de tipo `FREE_SHIPPING` —es una decisión de marketing que cambia con cada campaña— y `shipment-service` la consume al calcular el costo. Justificación: si esa regla viviera dentro de `shipment-service`, cada campaña de marketing obligaría a modificar el código de cálculo de envíos, acoplando marketing a logística.
- **Credenciales y usuarios**: Authorizer. **Pedidos**: `order-service`. **Productos y categorías**: `product-service`. **Reseñas**: `review-service`.

---

## 2. Endpoints

Todas las rutas son relativas a la raíz del servicio (ej. `http://offerService:8080`).

> **Nota:** Propuesta de contrato; aún no implementado. El servicio es un scaffold (solo `OfferServiceApplication` y su test): los endpoints, entidades y eventos de este documento definen lo que debería implementarse para cumplir el wireframe.

La identidad llega propagada por el gateway en los headers `X-User-Id` (claim `sub`), `X-User-Email` y `X-User-Roles`; toda petición incluye además `X-Correlation-Id`.

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes | — | `200` `{ status }` | No |
| `GET` | `/api/benefits` | Tarjetas de "Benefits" activas (Home y pies de página) | — | `200` `List<BenefitDto>` | No |
| `GET` | `/api/promotions/active` | Promociones vigentes (precios promocionados y destacados) | — | `200` `List<PromotionDto>` | No |
| `POST` | `/api/promotions/evaluate` | Evalúa qué promociones aplican a un carrito o pedido | `EvaluateRequest` | `200` EvaluationDto | Sí |
| `GET` | `/api/coupons/{code}` | Valida un cupón (existencia, vigencia y usos) | — | `200` CouponDto | Sí |
| `GET` | `/api/admin/promotions` | Listado de promociones para el panel admin | — (query params) | `200` `Page<PromotionDto>` | Sí (admin) |
| `POST` | `/api/admin/promotions` | Crea una promoción | `CreatePromotionRequest` | `201` PromotionDto | Sí (admin) |
| `PUT` | `/api/admin/promotions/{id}` | Actualiza una promoción | `UpdatePromotionRequest` | `200` PromotionDto | Sí (admin) |
| `DELETE` | `/api/admin/promotions/{id}` | Desactiva o elimina una promoción | — | `204` | Sí (admin) |

*Nota de integración:* el gateway aún no expone `/api/promotions/**` ni `/api/benefits/**`; se deben agregar las rutas `Path=/api/promotions/**,/api/benefits/**` → `lb://offerService`, y las rutas bajo `/api/admin/promotions/**` deben exigir `hasRole("admin")` (hoy `anyRequest().authenticated()` sólo autentica).

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Promotion not found",
  "status": 404,
  "detail": "Promotion with id 42 not found"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla | `discountValue must be greater than zero` |
| `401` | Unauthorized | Token ausente o inválido | `Missing Authorization header` |
| `403` | Forbidden | Usuario sin permiso sobre el recurso | `Requires admin role` |
| `404` | Not Found | Recurso inexistente | `Promotion with id 42 not found` |
| `409` | Conflict | Regla de negocio violada (duplicado) | `Promotion code SAVE10 already exists` |
| `422` | Unprocessable | Entidad válida pero no procesable | `Start date must be before end date` |
| `500` | Internal Error | Fallo inesperado | `Unexpected error` |
| `503` | Service Unavailable | Dependencia externa caída | `Product service is not available` |

---

## 4. Modelo de datos (Entidad)

### 4.1 Entidades

#### `<Promotion>` → tabla `promotions`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `code` | `varchar(50)` | ❌ | ✅ | ✅ | Código del cupón; nulo en promociones automáticas (sin canje) |
| `name` | `varchar(120)` | ❌ | ❌ | ❌ | Nombre visible de la promoción |
| `type` | `varchar(30)` | ❌ | ❌ | ❌ | `PERCENTAGE`, `FIXED_AMOUNT`, `FREE_SHIPPING`, `FEATURED` |
| `scope` | `varchar(20)` | ❌ | ❌ | ❌ | `PRODUCT`, `CATEGORY`, `ORDER` |
| `product_id` | `varchar(36)` | ❌ | ✅ | ❌ | Referencia al producto en `product-service` (sin FK cross-service) |
| `category_id` | `varchar(36)` | ❌ | ✅ | ❌ | Referencia a la categoría en `product-service` (sin FK cross-service) |
| `discount_value` | `numeric(12,2)` | ❌ | ✅ | ❌ | Porcentaje o monto fijo según `type` |
| `min_subtotal` | `numeric(12,2)` | ❌ | ✅ | ❌ | Subtotal mínimo para que aplique (umbral de envío gratis: `50000`) |
| `starts_at` | `timestamptz` | ❌ | ❌ | ❌ | Inicio de vigencia |
| `ends_at` | `timestamptz` | ❌ | ✅ | ❌ | Fin de vigencia; nulo = sin fecha de fin |
| `priority` | `int` | ❌ | ❌ | ❌ | Prioridad de aplicación (gana la mayor) |
| `stackable` | `boolean` | ❌ | ❌ | ❌ | Se puede combinar con otras promociones |
| `active` | `boolean` | ❌ | ❌ | ❌ | Promoción habilitada |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |

#### `<Benefit>` → tabla `benefits`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `code` | `varchar(50)` | ❌ | ❌ | ✅ | Identificador estable (`FREE_SHIPPING`, `SECURE_PAYMENTS`, `RETURNS`, `SUPPORT`) |
| `title` | `varchar(120)` | ❌ | ❌ | ❌ | Título de la tarjeta |
| `description` | `varchar(255)` | ❌ | ❌ | ❌ | Texto de la tarjeta |
| `icon` | `varchar(50)` | ❌ | ❌ | ❌ | Icono del design system |
| `sort_order` | `int` | ❌ | ❌ | ❌ | Orden de aparición (1-4 en el wireframe) |
| `active` | `boolean` | ❌ | ❌ | ❌ | Tarjeta visible |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |

#### `<CouponUsage>` → tabla `coupon_usages` (opcional, si se habilitan cupones)

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `promotion_id` | `UUID` | ❌ | ❌ | ❌ | FK → `promotions.id` |
| `customer_sub` | `varchar(255)` | ❌ | ❌ | ❌ | Claim `sub` del cliente que canjea (sin FK) |
| `order_id` | `varchar(36)` | ❌ | ❌ | ✅ (con `promotion_id`) | Pedido en `order-service` que consume el cupón |
| `used_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de uso (auditoría) |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Promotion` 1 → N `CouponUsage` | Uno-a-muchos | Usos de un cupón (reside en `coupon_usages`) |
| `Promotion` N → 1 `Product` / `Category` | Muchos-a-uno | La promoción apunta a un producto o categoría del catálogo (reside en `product-service`; referencia por `product_id`/`category_id`, sin FK) |
| `Order` N → 1 `Promotion` | Muchos-a-uno | El pedido aplica la evaluación devuelta por este servicio (reside en `order-service`, sin FK) |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Vigencia | Aplica si `starts_at <= now` y (`ends_at` nulo o `now < ends_at`); fuera de vigencia no se evalúa |
| Prioridad | Ante varias promociones aplicables al mismo target gana la de mayor `priority`; luego la de inicio más reciente |
| Unicidad | `code` no puede repetirse; cada cupón se usa una sola vez por pedido (`UNIQUE (promotion_id, order_id)`) |
| Envío gratis | Aplica solo si hay una promoción `FREE_SHIPPING` vigente y el subtotal alcanza `min_subtotal` (`50000` en el wireframe) |
| Descuentos | El descuento nunca deja el subtotal en negativo; las promociones no `stackable` se excluyen entre sí |
| Borrado | Las promociones ya usadas no se borran: se desactivan (`active = false`) para conservar la auditoría |
| Beneficios | Las tarjetas de `benefits` se cargan por seed/migración y se editan solo desde el panel admin |
| Invariantes | `created_at <= updated_at`; `starts_at < ends_at` cuando `ends_at` tiene valor |

---

## 5. Configuración

### 5.1 Variables de entorno

No hay `JWT_SECRET`: el gateway valida el JWT (firma HS256 e issuer de Authorizer) y propaga la identidad en headers; el servicio sólo lee `X-User-Id`, `X-User-Email`, `X-User-Roles` y `X-Correlation-Id`.

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL | ✅ | — | `jdbc:postgresql://db:5432/offer` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `offer` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `PRODUCT_SERVICE_BASE_URL` | Base URL de `product-service` (precio y categoría en la evaluación) | ❌ | `http://localhost:8080` | `lb://productService` |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka (publica `promotion-events`, consume `order-events`) | ❌ | `localhost:9092` | `kafka:9092` |
| `EUREKA_URL` | URL del registro Eureka | ❌ | `http://localhost:8761/eureka` | `http://eureka:8761/eureka` |
| `EUREKA_ENABLED` | Habilita el registro en Eureka | ❌ | `true` | `true` |
| `LOGGING_LEVEL_ROOT` | Nivel de log (`INFO`, `DEBUG`) | ❌ | `INFO` | `INFO` |

*Registro:* `spring.application.name=offerService` (Eureka).

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
| `gateway` | Enruta `/api/promotions/**` y `/api/benefits/**`, valida el JWT, inyecta `X-User-Id`/`X-User-Email`/`X-User-Roles` y propaga `X-Correlation-Id` | HTTP/REST (upstream; este servicio no lo llama) | El servicio no está expuesto fuera del gateway | ✅ Sí |
| `product-service` | Obtener precio base y categoría cuando la evaluación no los trae en el request | HTTP/REST (`PRODUCT_SERVICE_BASE_URL`, por defecto `http://localhost:8080`) | Evaluar sólo con los datos enviados en el request | ❌ No |

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia principal | ✅ Sí | `503`, readiness en `DOWN` |
| Authorizer | Terceros (IdP) | Emite el JWT que el gateway valida (firma/issuer) y los roles `admin`/`cliente` | ❌ No | El gateway ya validó el token; siguen las rutas públicas de beneficios y promociones |
| Kafka | Mensajería | Publicación de `promotion-events` (outbox) y consumo de `order-events` | ❌ No | Los eventos quedan en el outbox y el relay los reintenta al volver; la evaluación por API sigue funcionando |
| Eureka | Registro de servicios | Descubrimiento para el gateway (`spring.application.name=offerService`) | ❌ No | `EUREKA_ENABLED=false` para operar sin registro |

### 6.3 Eventos publicados/consumidos

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| `PROMOTION_CHANGED` | Publica (topic `promotion-events`, vía outbox) | Alta, modificación o baja de una promoción | `{ promotionId, type, scope, targetId, active, occurredAt }` |
| `ORDER_CREATED` | Consume (topic `order-events`, grupo `offer-service`) | Pedido creado con cupón aplicado → registra el uso en `coupon_usages` | `{ orderId, orderNumber, customerId, total, occurredAt }` |

> El canje de cupones queda pendiente hasta que `order-service` agregue `couponCode` al payload de `ORDER_CREATED` (hoy no lo incluye).

---

## 7. Cobertura del wireframe

Fuente: `docs/OpenPencil/LaTiendita.fig`, página "WireFrame".

| Pantalla del wireframe | Endpoints / responsabilidades del servicio |
|---|---|
| `Home` — sección "Benefits" (4 tarjetas) | Catálogo de beneficios: `GET /api/benefits` (envío gratis, pagos seguros, devoluciones, soporte) |
| Pie de `Product Detail`, `Cart`, `Checkout` y `Listings` | Reutiliza el mismo catálogo: `GET /api/benefits` |
| `Checkout → Shipping` — nota «Gratis desde $ 50.000» | Promoción `FREE_SHIPPING` con `min_subtotal = 50000`: `GET /api/promotions/active` y `POST /api/promotions/evaluate`. El cobro de envío lo aplica `shipment-service` |
| `Home` / `Listings` — precios promocionados y sección "Featured" | Promociones `PERCENTAGE`, `FIXED_AMOUNT` y `FEATURED` vigentes: `GET /api/promotions/active`, `POST /api/promotions/evaluate` |
| `Admin` — pantalla de ofertas | **Pantalla nueva**: no existe en el diseño actual. Si se propone, incluiría `GET`/`POST`/`PUT`/`DELETE /api/admin/promotions` (§2) |
| `Admin` — Resumen, Productos, Categorías, Pedidos, Pagos, Clientes | Sin cobertura propuesta: el panel de ofertas no está en el wireframe y las demás pantallas no dependen de este servicio |
