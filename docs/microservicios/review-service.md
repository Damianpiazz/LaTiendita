# Microservicio: `review-service`

## 1. Responsabilidad

Gestiona las reseñas de productos: permite que los clientes que compraron un producto lo evalúen, expone las reseñas aprobadas con promedio y distribución por estrellas, administra la moderación (aprobar/rechazar) y los votos de "reseña útil". Publica el promedio actualizado para que `product-service` lo refleje en el catálogo.

No gestiona el catálogo (pertenece a `product-service`), los pedidos (`order-service`) ni el carrito (`cart-service`). La verificación de compra se delega consultando a `order-service`; nunca almacena copias de pedidos.

---

## 2. Endpoints

> **Propuesta de contrato; aún no implementado.** El servicio es scaffold (solo `ReviewServiceApplication` y su test). Este documento define el contrato propuesto para cumplir el wireframe de `docs/OpenPencil/LaTiendita.fig` (página "WireFrame").

Todas las rutas son relativas a la raíz del servicio (ej. `http://reviewService:8080`). La identidad se resuelve con el header `X-User-Id` (inyectado por el gateway desde el JWT de Authorizer); la moderación exige el rol `admin` (header `X-User-Roles`).

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes | — | `200` `{ status }` | No |
| `GET` | `/api/reviews?productId=&page=&size=` | Lista paginada de reseñas **aprobadas** de un producto (orden: más recientes) | — | `200` Page\<ReviewDto\> | No |
| `GET` | `/api/reviews/summary?productId=` | Resumen del producto: promedio, cantidad y distribución por estrellas | — | `200` ReviewSummaryDto | No |
| `POST` | `/api/reviews` | Crea una reseña (estado inicial `pending`) | `CreateReviewRequest` | `201` ReviewDto | Sí |
| `POST` | `/api/reviews/{reviewId}/helpful` | Vota "reseña útil" | — | `200` ReviewDto | Sí |
| `GET` | `/api/admin/reviews?status=&productId=&page=` | Listado para moderación (incluye `pending` y `rejected`) | — | `200` Page\<ReviewDto\> | Sí (rol `admin`) |
| `PATCH` | `/api/admin/reviews/{reviewId}/status` | Aprueba o rechaza una reseña | `ModerateReviewRequest` | `200` ReviewDto | Sí (rol `admin`) |

*Notas de integración:*
- `GET /api/reviews` y `GET /api/reviews/summary` son de lectura pública (como `GET /api/products` en el gateway), para mostrar la sección "Reviews" del detalle de producto sin sesión.
- El gateway aún no expone `/api/reviews/**` ni `/api/admin/reviews/**`; se deben agregar las rutas `Path=/api/reviews/**` → `lb://reviewService` y `Path=/api/admin/**` → `lb://reviewService` (con `hasRole("admin")`, como las rutas de escritura de `products`).

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Review not found",
  "status": 404,
  "detail": "Review with id 7 not found"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla | `Rating must be between 1 and 5` |
| `401` | Unauthorized | Token ausente o inválido | `Missing Authorization header` |
| `403` | Forbidden | Cliente sin compra del producto, o rol `admin` ausente en moderación | `Only customers who purchased this product can review it` |
| `404` | Not Found | Recurso inexistente (reseña o producto) | `Review with id 7 not found` |
| `409` | Conflict | Regla de negocio violada (reseña duplicada, voto duplicado) | `Customer already reviewed product 42` |
| `422` | Unprocessable | Entidad válida pero no procesable (pedido cancelado, estado inválido) | `Order 9 is not eligible for review: status CANCELLED` |
| `500` | Internal Error | Fallo inesperado | `Unexpected error` |
| `503` | Service Unavailable | `order-service` o `product-service` caídos | `Order service is not available` |

---

## 4. Modelo de datos (Entidad)

### 4.1 Entidades

#### `Review` → tabla `reviews`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `product_id` | `bigint` | ❌ | ❌ | ❌ | Producto reseñado (ref. a `product-service`) |
| `customer_id` | `varchar(36)` | ❌ | ❌ | ❌ | `X-User-Id` del autor |
| `order_id` | `bigint` | ❌ | ❌ | ❌ | Pedido que respalda la compra (ref. a `order-service`) |
| `rating` | `smallint` | ❌ | ❌ | ❌ | Estrellas (1..5) |
| `title` | `varchar(120)` | ❌ | ❌ | ❌ | Título de la reseña |
| `body` | `text` | ❌ | ❌ | ❌ | Texto de la reseña |
| `status` | `varchar(20)` | ❌ | ❌ | ❌ | Moderación: `pending` / `approved` / `rejected` |
| `helpful_votes` | `int` | ❌ | ❌ | ❌ | Contador de votos "útil" (denormalizado) |
| `moderation_reason` | `varchar(500)` | ❌ | ✅ | ❌ | Motivo al rechazar (auditoría de moderación) |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |

#### `ReviewVote` → tabla `review_votes`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `review_id` | `UUID` | ❌ | ❌ | ❌ | FK hacia `reviews` |
| `customer_id` | `varchar(36)` | ❌ | ❌ | ❌ | Votante (`X-User-Id`); único por reseña (constraint `(review_id, customer_id)`) |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha del voto (auditoría) |

#### `ProductRating` → tabla `product_ratings` (promedio materializado)

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `product_id` | `bigint` | ✅ | ❌ | ✅ | Producto (ref. a `product-service`) |
| `average` | `numeric(3,2)` | ❌ | ❌ | ❌ | Promedio de estrellas de las reseñas aprobadas |
| `review_count` | `int` | ❌ | ❌ | ❌ | Cantidad de reseñas aprobadas |
| `distribution` | `jsonb` | ❌ | ❌ | ❌ | Conteo por estrella: `{ "1": n, ..., "5": n }` |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Última recalculación |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Review` N → 1 `Product` | Muchos-a-uno (referencia) | `product_id` resuelve en `product-service` (sin FK: es otro servicio) |
| `Review` N → 1 `Order` | Muchos-a-uno (referencia) | `order_id` respalda la compra; se consulta en `order-service` al crear |
| `Review` 1 → N `ReviewVote` | Uno-a-muchos | Votos "útil" sobre la reseña |
| `ProductRating` 1 → 1 `Product` | Uno-a-uno (referencia) | Fila materializada del promedio, se recalcula al moderar |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Elegibilidad | Solo clientes con un pedido que contenga el producto y en estado `PAID`, `SHIPPED` o `DELIVERED`. Se valida consultando a `order-service` (`GET /api/orders` del usuario, reenviando `X-User-Id`) y verificando que algún pedido incluya el `productId` |
| Unicidad | Una sola reseña por `(customer_id, product_id)` → `409` si repite |
| Moderación | Toda reseña nace `pending`; solo `approved` es pública. Transiciones: `pending → approved`, `pending → rejected` (con motivo), `approved → rejected` (baja posterior) |
| Promedio materializado | Se recalcula al cambiar el conjunto de aprobadas (aprobar, rechazar, bajar). `product_ratings` mantiene `average`, `review_count` y `distribution`; es la fuente de `GET /api/reviews/summary` |
| Evento al aprobar | Al transicionar a `approved` (y al bajar una `approved`) se publica `REVIEW_APPROVED` / `REVIEW_STATUS_CHANGED` por Kafka (vía outbox) para que `product-service` actualice el promedio del producto |
| Votos | Un voto por `(review_id, customer_id)` → `409` si repite; solo sobre reseñas `approved`; el autor no puede votar su propia reseña → `409`. `helpful_votes` se incrementa en la misma transacción |

---

## 5. Configuración

### 5.1 Variables de entorno

No hay `JWT_SECRET`: el gateway valida el JWT (issuer/firma de Authorizer) y propaga la identidad en headers; el servicio solo lee `X-User-Id`, `X-User-Roles` y `X-Correlation-Id`.

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL | ✅ | — | `jdbc:postgresql://db:5432/review` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `review` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `ORDER_SERVICE_BASE_URL` | Base URL de `order-service` (verificación de compra) | ❌ | `http://localhost:8080` | `lb://orderService` |
| `PRODUCT_SERVICE_BASE_URL` | Base URL de `product-service` (validación de producto) | ❌ | `http://localhost:8080` | `lb://productService` |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka (publica `review-events`, consume `product-events`) | ❌ | `localhost:9092` | `kafka:9092` |
| `EUREKA_URL` | URL del registro Eureka | ❌ | `http://localhost:8761/eureka` | `http://eureka:8761/eureka` |
| `OUTBOX_RELAY_POLL_INTERVAL_MS` | Intervalo del relay de outbox | ❌ | `1000` | `1000` |
| `LOGGING_LEVEL_ROOT` | Nivel de log (`INFO`, `DEBUG`) | ❌ | `INFO` | `INFO` |

*Registro:* `spring.application.name=reviewService` (Eureka).

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
| `order-service` | Verificar que el cliente compró el producto (estado `PAID`/`SHIPPED`/`DELIVERED`) | HTTP/REST (`lb://orderService`) | Devolver `503` al crear reseña | ✅ Sí (solo `POST /api/reviews`) |
| `product-service` | Validar que el producto existe; recibir el evento de promedio actualizado | HTTP/REST + Kafka | `503` al crear si no responde; el evento se reintentara por outbox | ❌ No (lecturas de reseña siguen funcionando) |

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia de reseñas, votos y promedios | ✅ Sí | `503`, readiness en `DOWN` |
| Kafka | Mensajería | Publicación de `review-events` (vía outbox transaccional) y consumo de `product-events` | ❌ No | El outbox retiene eventos y el relay los reintenta al volver |

### 6.3 Eventos publicados/consumidos

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| `review-events` (`REVIEW_APPROVED`) | Publica | Moderación aprueba una reseña | `{ reviewId, productId, rating, average, reviewCount }` |
| `review-events` (`REVIEW_STATUS_CHANGED`) | Publica | Baja de una reseña aprobada (`approved → rejected`) | `{ reviewId, productId, status, average, reviewCount }` |
| `product-events` (`PRODUCT_DELETED`) | Consume | Baja de producto en `product-service` | `{ productId }` → se descartan (o marcan) sus reseñas para no promediar productos inexistentes |

---

## 7. Cobertura del wireframe

Fuente: `docs/OpenPencil/LaTiendita.fig`, página "WireFrame".

| Pantalla del wireframe | Endpoints / responsabilidades del servicio |
|---|---|
| **Product Detail → sección "Reviews"** — encabezado con promedio de estrellas y cantidad de reseñas | `GET /api/reviews/summary?productId=` (promedio y conteo materializados en `product_ratings`) |
| **Product Detail → sección "Reviews"** — distribución por cantidad de estrellas | `GET /api/reviews/summary?productId=` (campo `distribution` por estrella) |
| **Product Detail → cuerpo de "Reviews"** — tarjetas con autor, fecha, estrellas, título y texto | `GET /api/reviews?productId=&page=&size=` (solo reseñas `approved`) |
| **Product Detail → enlace "More reviews"** | Paginación de `GET /api/reviews` (`page`, `size`) |
| **Product Detail → botón "Agregar al carrito"** | No aplica: ver `cart-service.md` |
| **Admin (sidebar: Resumen, Productos, Categorías, Pedidos, Pagos, Clientes)** — moderación de reseñas | `GET /api/admin/reviews?status=pending` (bandeja) y `PATCH /api/admin/reviews/{reviewId}/status` (aprobar/rechazar), con recálculo del promedio y publicación del evento |
