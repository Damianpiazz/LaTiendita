# Microservicio: `cart-service`

## 1. Responsabilidad

Gestiona el carrito de compras por usuario: persiste los ítems agregados desde el catálogo, controla cantidades contra el stock, calcula subtotales por línea y el total, y permite quitar ítems o vaciar el carrito. Es la fuente de verdad del carrito previo al checkout.

No calcula costos de envío (pertenece a `shipment-service`) ni crea ni modifica pedidos (pertenece a `order-service`). El checkout envía los ítems del carrito a `POST /api/orders`, servicio que vuelve a validar precios y stock en el servidor.

---

## 2. Endpoints

> **Propuesta de contrato; aún no implementado.** El servicio es scaffold (solo `CartServiceApplication` y su test). Este documento define el contrato propuesto para cumplir el wireframe de `docs/OpenPencil/LaTiendita.fig` (página "WireFrame").

Todas las rutas son relativas a la raíz del servicio (ej. `http://cartService:8080`). La identidad se resuelve con el header `X-User-Id` (inyectado por el gateway desde el JWT de Authorizer); no se reciben identificadores de usuario en el body.

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes | — | `200` `{ status }` | No |
| `GET` | `/api/carts` | Obtiene el carrito del usuario actual (ítems, subtotales, contador, total) | — | `200` CartDto | Sí |
| `POST` | `/api/carts/items` | Agrega un ítem al carrito (o suma cantidad si ya existe) | `AddCartItemRequest` | `200` CartDto | Sí |
| `PATCH` | `/api/carts/items/{productId}` | Actualiza la cantidad de un ítem existente | `UpdateCartItemRequest` | `200` CartDto | Sí |
| `DELETE` | `/api/carts/items/{productId}` | Quita un ítem del carrito | — | `204` | Sí |
| `DELETE` | `/api/carts` | Vacía el carrito completo | — | `204` | Sí |

*Nota de integración:* el gateway aún no expone `/api/carts/**`; se debe agregar una ruta `Path=/api/carts/**` → `lb://cartService` (mismo patrón que `orders`/`products`).

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Product not found",
  "status": 404,
  "detail": "Product with id 42 not found"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla | `Quantity must be between 1 and 99` |
| `401` | Unauthorized | Token ausente o inválido | `Missing Authorization header` |
| `403` | Forbidden | Header `X-User-Id` ausente (peticiones sin pasar por el gateway) | `Missing gateway identity header` |
| `404` | Not Found | Recurso inexistente (ítem del carrito o producto) | `Cart item for product 42 not found` |
| `409` | Conflict | Regla de negocio violada (stock, producto dado de baja) | `Requested quantity 5 exceeds available stock 3` |
| `422` | Unprocessable | Entidad válida pero no procesable (producto sin precio) | `Product 42 has no price assigned` |
| `500` | Internal Error | Fallo inesperado | `Unexpected error` |
| `503` | Service Unavailable | `product-service` caído en una operación de escritura | `Product service is not available` |

---

## 4. Modelo de datos (Entidad)

### 4.1 Entidades

#### `Cart` → tabla `carts`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `user_id` | `varchar(36)` | ❌ | ❌ | ✅ | `X-User-Id` (sub de Authorizer); un carrito por usuario |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |
| `version` | `bigint` | ❌ | ❌ | ❌ | Bloqueo optimista (`@Version`) |

#### `CartItem` → tabla `cart_items`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `UUID` | ✅ | ❌ | ✅ | Identificador único |
| `cart_id` | `UUID` | ❌ | ❌ | ❌ | FK hacia `carts` (cascade + orphanRemoval) |
| `product_id` | `bigint` | ❌ | ❌ | ❌ | Referencia al catálogo; único por carrito (constraint `(cart_id, product_id)`) |
| `quantity` | `int` | ❌ | ❌ | ❌ | Cantidad solicitada (1..stock) |
| `unit_price` | `numeric(12,2)` | ❌ | ❌ | ❌ | Snapshot del precio al agregar/última sincronización |
| `product_title` | `varchar(255)` | ❌ | ❌ | ❌ | Snapshot del nombre (lista del wireframe) |
| `image_url` | `varchar(500)` | ❌ | ❌ | ❌ | Snapshot de la imagen (lista del wireframe) |
| `price_changed` | `boolean` | ❌ | ❌ | ❌ | Bandera de aviso cuando cambió el precio en el catálogo |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de agregado (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Cart` 1 → N `CartItem` | Uno-a-muchos | Ítems del carrito |
| `Cart` 1 → 1 `User` | Uno-a-uno (referencia) | `user_id` coincide con `X-User-Id`; el usuario vive en Authorizer |
| `CartItem` N → 1 `Product` | Muchos-a-uno (referencia) | `product_id` resuelve en `product-service` (sin FK: es otro servicio) |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Unicidad | Un solo carrito por `user_id` |
| Cantidades | `quantity` entre 1 y el stock disponible (`Product.quantity`). Al agregar un ítem existente se suman las cantidades y se vuelve a validar contra el stock; si lo supera → `409` |
| Precio | `unit_price` es un snapshot: se refresca con el evento `PRODUCT_PRICE_CHANGED` y se marca `price_changed=true` para que la UI informe el cambio; la bandera se limpia al reconfirmar el ítem (`POST`/`PATCH`) |
| Borrado de producto | Con `PRODUCT_DELETED` se elimina el ítem del carrito: no puede comprarse un producto inexistente |
| Totales | Subtotal por línea = `unit_price × quantity`; el total del carrito es informativo, `order-service` recalcula precios en el servidor al crear el pedido |
| Carrito anónimo | No soportado (justificado): la identidad llega únicamente en `X-User-Id` vía gateway y el checkout exige sesión; un carrito anónimo requeriría IDs generados en el cliente y fusionar al iniciar sesión, sin ganancia actual |
| Checkout | Al crear el pedido con éxito, el flujo de checkout vacía el carrito (`DELETE /api/carts`) |

---

## 5. Configuración

### 5.1 Variables de entorno

No hay `JWT_SECRET`: el gateway valida el JWT (issuer/firma de Authorizer) y propaga la identidad en headers; el servicio solo lee `X-User-Id` y `X-Correlation-Id`.

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL | ✅ | — | `jdbc:postgresql://db:5432/cart` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `cart` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `PRODUCT_SERVICE_BASE_URL` | Base URL de `product-service` | ❌ | `http://localhost:8080` | `lb://productService` |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka (consumo de `product-events`) | ❌ | `localhost:9092` | `kafka:9092` |
| `EUREKA_URL` | URL del registro Eureka | ❌ | `http://localhost:8761/eureka` | `http://eureka:8761/eureka` |
| `LOGGING_LEVEL_ROOT` | Nivel de log (`INFO`, `DEBUG`) | ❌ | `INFO` | `INFO` |

*Registro:* `spring.application.name=cartService` (Eureka).

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
| `product-service` | Validar producto, precio y stock al agregar/actualizar ítems; refrescar snapshot | HTTP/REST (`lb://productService`) | Escrituras: `503`; lecturas: servir snapshot persistido | ✅ Sí (escrituras) |

*Nota:* no hay llamada a `order-service`. En el checkout, el frontend (o el flujo de checkout) toma los ítems de `GET /api/carts` y los envía a `POST /api/orders`; `order-service` valida y recalcula todo en el servidor.

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia del carrito | ✅ Sí | `503`, readiness en `DOWN` |
| Kafka | Mensajería | Consumo de `product-events` (precio/baja de producto) | ❌ No | El carrito opera con el snapshot persistido; el consumer group reanuda al volver |

### 6.3 Eventos publicados/consumidos

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| `product-events` (`PRODUCT_PRICE_CHANGED`) | Consume | Cambio de precio en `product-service` | `{ productId, price, occurredAt }` |
| `product-events` (`PRODUCT_DELETED`) | Consume | Baja de producto en `product-service` | `{ productId, occurredAt }` |

*Sin eventos publicados:* el carrito no genera cambios relevantes para otros servicios; el checkout fluye por `POST /api/orders`.

---

## 7. Cobertura del wireframe

Fuente: `docs/OpenPencil/LaTiendita.fig`, página "WireFrame".

| Pantalla del wireframe | Endpoints / responsabilidades del servicio |
|---|---|
| **Cart Wireframe** — lista de ítems (imagen, nombre, precio unitario, control de cantidad, subtotal por línea, quitar ítem) | `GET /api/carts`, `PATCH /api/carts/items/{productId}`, `DELETE /api/carts/items/{productId}` |
| **Cart Wireframe** — resumen "Subtotal (4 productos) $ 56.279", envío y total | `GET /api/carts` aporta subtotal, contador de productos y total; el costo de envío lo resuelve `shipment-service` en el checkout (fuera de este servicio) |
| **Cart Wireframe** — CTA continuar al checkout y fila de Steps (Carrito → Envío → Pago → Confirmación) | `GET /api/carts` como origen de ítems para `POST /api/orders`; vaciado del carrito (`DELETE /api/carts`) al confirmar el pedido |
| **Header** (todas las pantallas) — zona Actions con ícono/contador de carrito | `GET /api/carts` → campo `itemCount` para el badge del contador |
| **Product Detail** — botón "Agregar al carrito" (nombre, precio, stock, cantidad) | `POST /api/carts/items`: valida existencia, precio y stock contra `product-service` y persiste el ítem |
| **Product Detail** — sección "Reviews", datos del catálogo, pedidos | No aplica: ver `review-service.md`, `product-service` y `order-service` |
