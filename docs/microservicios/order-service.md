# Microservicio: `order-service`


## 1. Responsabilidad

Gestiona el ciclo de vida de los pedidos: creación del pedido desde el carrito (validando precios contra `product-service`), consulta de los pedidos del cliente, cambio de estado y cancelación, más la publicación de eventos de dominio por Kafka mediante outbox transaccional. Es la única fuente de verdad sobre pedidos; conserva una instantánea congelada de los ítems (título, SKU, precio al momento de comprar) para que un cambio posterior de catálogo no altere pedidos históricos. No gestiona el carrito en sí (es estado del frontend hasta confirmar la compra), el catálogo (lo consulta a `product-service`), los pagos ni reembolsos (los ejecuta `payment-service`, que confirma vía `PAYMENT_APPROVED`), ni reseñas (`review-service`).

---

## 2. Endpoints

Todas las rutas son relativas a la raíz del servicio (ej. `http://orderService:8080`). El gateway enruta `/api/orders/**` hacia este servicio.

> **Auth:** el gateway exige JWT para todo `/api/orders/**` y propaga `X-User-Id`, `X-User-Email`, `X-User-Roles` + `X-Correlation-Id`. El servicio **exige `X-User-Id`** (`CurrentUser.id()` → `401` si falta, porque la petición no pasó por el gateway) y **verifica pertenencia**: sólo el dueño del pedido (`customer_id == X-User-Id`) puede leerlo, cambiar su estado o cancelarlo (`403` si es de otro usuario). Los `401`/`403` por *autenticación/autorización de rol* (p. ej. no tener JWT) los emite el gateway; los de este servicio son por header ausente o pertenencia.

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes (`readiness` incluye `db`) | — | `200` `{ status }` | No |
| `POST` | `/api/orders` | Crea un pedido desde el carrito (valida ítems, precios y total) | `CreateOrderRequest` | `201` OrderResponse + `Location` | Sí |
| `GET` | `/api/orders` | Lista los pedidos del usuario autenticado (paginado) | — | `200` Page\<OrderResponse\> | Sí |
| `GET` | `/api/orders/{orderId}` | Obtiene un pedido con sus ítems (sólo si es del usuario) | — | `200` OrderResponse | Sí (dueño) |
| `POST` | `/api/orders/{orderId}/status` | Cambia el estado del pedido según la máquina de transiciones (`CANCELLED` no está permitido acá) | `UpdateOrderStatusRequest` | `200` OrderResponse | Sí (dueño) |
| `POST` | `/api/orders/{orderId}/cancel` | Cancela un pedido (`PENDING` o `PAID`), con motivo opcional | `CancelOrderRequest` | `200` OrderResponse | Sí (dueño) |

**Formatos de request:**

```jsonc
// POST /api/orders — CreateOrderRequest
{ "items": [ { "productId": 1, "quantity": 2 } ], "total": 199.98 }   // total opcional

// POST /api/orders/{id}/status — UpdateOrderStatusRequest
{ "status": "PAID" }

// POST /api/orders/{id}/cancel — CancelOrderRequest
{ "reason": "Me arrepentí" }                                          // reason opcional, máx. 500
```

### 2.1 Endpoints pendientes

| Método | Ruta | Descripción | Estado |
|---|---|---|---|
| `GET` | `/api/orders/admin` (o `/api/orders?status=&from=&to=`) | Listado **global** de pedidos para la tabla del Admin Pedidos (hoy `GET /api/orders` sólo devuelve los del usuario actual) | `<a implementar>` |
| `GET` | `/api/orders/admin/summary` | KPIs del Admin Resumen: ventas de hoy (monto + cantidad), pedidos pendientes (incl. sin pago acreditado), serie de 7 días (total, pedidos, ticket promedio), últimos pedidos | `<a implementar>` |
| `POST` | `/api/orders/{orderId}/status` con rol `admin` sobre pedidos de terceros | El cambio de estado actual exige ser el dueño del pedido | `<a implementar>` |

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Transición de estado inválida",
  "status": 409,
  "detail": "No se puede pasar de SHIPPED a PAID"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla (`errors` en el body) | `items: must not be empty` |
| `400` | Bad Request | Pedido inválido (`InvalidOrderException`): sin ítems, `productId` repetido, producto sin precio, o `status=CANCELLED` enviado al endpoint de cambio de estado | `No puede repetir el mismo productId en los ítems del pedido` |
| `400` | Bad Request | `TotalMismatchException`: el `total` del request no coincide con el calculado en el servidor | `El total enviado (100.00) no coincide con el total calculado en el servidor (199.98)` |
| `401` | Unauthorized | Falta `X-User-Id` (la petición no pasó por el gateway) | `Falta o es inválido el header 'X-User-Id'; la petición debe pasar por el Gateway` |
| `403` | Forbidden | El pedido pertenece a otro usuario | `No tiene permiso sobre el pedido 42` |
| `404` | Not Found | Pedido inexistente | `Pedido no encontrado con id 42` |
| `404` | Not Found | Producto inexistente al crear el pedido (respuesta `404` de `product-service`) | `Producto no encontrado con id 42` |
| `409` | Conflict | Transición de estado inválida | `No se puede pasar de DELIVERED a PAID` |
| `409` | Conflict | Conflicto de concurrencia por `@Version` (optimistic locking) | `El recurso fue modificado por otra operación; reintente la petición` |
| `409` | Conflict | Violación de restricción única (p. ej. `order_number`) | `Violación de restricción única (posible carrera entre peticiones simultáneas)` |
| `503` | Service Unavailable | `product-service` caído, circuit breaker abierto o error no-404 | `El servicio de productos no está disponible` |
| `500` | Internal Error | Fallo inesperado (respuesta por defecto de Spring, sin handler explícito) | `Unexpected error` |

---

## 4. Modelo de datos (Entidad)

### 4.1 Entidades

#### `OrderEntity` → tabla `orders`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador numérico interno |
| `order_number` | `varchar(36)` | ❌ | ❌ | ✅ | Número visible del pedido (UUID string generado en la creación) |
| `customer_id` | `varchar(36)` | ❌ | ❌ | ❌ | `sub` del token (header `X-User-Id`); sin FK a usuarios |
| `status` | `varchar(20)` | ❌ | ❌ | ❌ | Enum `OrderStatus`: `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| `total` | `numeric(12,2)` | ❌ | ❌ | ❌ | Total calculado en el servidor (suma de subtotales) |
| `cancel_reason` | `varchar(500)` | ❌ | ❌ | ❌ | Motivo de cancelación (opcional) |
| `refund_required` | `boolean` | ❌ | ❌ | ❌ | `true` si se canceló estando `PAID` (dispara `REFUND_REQUESTED`) |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Última modificación (auditoría) |
| `version` | `bigint` | ❌ | ❌ | ❌ | Optimistic locking (`@Version`) |

#### `OrderItemEntity` → tabla `order_items`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `order_id` | `bigint` | ❌ | ❌ | ❌ | FK a `orders.id` (cascade + orphanRemoval) |
| `product_id` | `bigint` | ❌ | ❌ | ❌ | Referencia al producto en `product-service` |
| `product_title` | `varchar(255)` | ❌ | ❌ | ❌ | Instantánea del título al momento de comprar |
| `sku` | `varchar(100)` | ❌ | ❌ | ❌ | Instantánea del SKU al momento de comprar |
| `quantity` | `integer` | ❌ | ❌ | ❌ | Cantidad, `>= 1` |
| `unit_price` | `numeric(12,2)` | ❌ | ❌ | ❌ | Precio unitario congelado (viene de `product-service`, no del cliente) |
| `subtotal` | `numeric(12,2)` | ❌ | ❌ | ❌ | `unit_price * quantity` |

#### `OutboxEvent` → tabla `outbox_events`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `topic` | `varchar(50)` | ❌ | ❌ | ❌ | Topic de destino (p. ej. `order-events`) |
| `aggregate_id` | `varchar(64)` | ❌ | ❌ | ❌ | Key de partición (ID del pedido) |
| `event_type` | `varchar(30)` | ❌ | ❌ | ❌ | Tipo (`ORDER_CREATED`, `ORDER_STATUS_CHANGED`, `ORDER_CANCELLED`, `REFUND_REQUESTED`) |
| `payload` | `text` | ❌ | ❌ | ❌ | JSON serializado del evento |
| `status` | `varchar(20)` | ❌ | ❌ | ❌ | `PENDING` → `SENT` / `FAILED` |
| `attempts` | `integer` | ❌ | ❌ | ❌ | Intentos de publicación (máx. `OUTBOX_RELAY_MAX_ATTEMPTS`) |
| `last_error` | `varchar(500)` | ❌ | ❌ | ❌ | Último error de publicación |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Momento de escritura en la transacción |
| `sent_at` | `timestamptz` | ❌ | ❌ | ❌ | Momento de publicación exitosa |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Order` 1 → N `OrderItem` | Uno-a-muchos | `order_items.order_id` con `CascadeType.ALL` y `orphanRemoval`; se cargan con `@EntityGraph` (`findWithItemsById`, `findByCustomerId`) |
| `Order` N → 1 `Customer` | Referencia lógica | `orders.customer_id` = `X-User-Id` (sin FK: los usuarios viven en `authorizer`, no en esta BD) |
| `OutboxEvent` | Eventos | Se escriben **en la misma transacción** que el cambio de dominio (outbox transaccional) |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Creación desde el carrito | `POST /api/orders` recibe sólo `{ productId, quantity }` por ítem. Debe haber ≥ 1 ítem y `productId` sin repetir. El **precio no se acepta del cliente**: para cada ítem se llama a `GET /api/products/{id}` de `product-service` y se congela `unitPrice`, `productTitle` y `sku` en el ítem |
| Validación de precios (`TotalMismatchException`) | El total se recalcula en el servidor (Σ `unit_price * quantity`). Si el request trae `total` y no coincide → `400`. Si un producto viene sin `price` → `400` (`InvalidOrderException`) |
| Estado inicial | Todo pedido nace en `PENDING` con `orderNumber = UUID` y `createdAt/updatedAt = now` |
| Máquina de transiciones (`OrderStatus.canTransitionTo`) | `PENDING → PAID \| CANCELLED`; `PAID → SHIPPED \| CANCELLED`; `SHIPPED → DELIVERED`; `DELIVERED` y `CANCELLED` son **finales**. Cualquier otra combinación → `409` (`No se puede pasar de X a Y`) |
| `POST /{id}/status` | Rechaza `CANCELLED` explícitamente (`Para cancelar un pedido use POST /api/orders/{orderId}/cancel`); valida la transición con la máquina |
| `POST /{id}/cancel` | Sólo sobre `PENDING` o `PAID` (`cancellable()`); si estaba `PAID` setea `refundRequired=true` y publica además `REFUND_REQUESTED`. Motivo opcional, máx. 500 caracteres |
| Pago aprobado (asíncrono) | `PaymentApprovedConsumer` escucha `payment-events`; sólo actúa si `type == PAYMENT_APPROVED` y el pedido está en `PENDING` → `PAID` (idempotente: un evento repetido se ignora). **No publica** evento de cambio de estado por esta vía |
| Propiedad | Todo acceso a un pedido exige `customerId == X-User-Id`; si no → `403` |
| Concurrencia | `@Version` en `orders`: cambios concurrentes → `409` |
| Outbox transaccional | Los eventos se agregan a `outbox_events` dentro de la misma transacción; `OutboxRelay` (poll cada `1 s`, batch 50, máx. 10 intentos con re-lectura de `FAILED`) los publica a Kafka con key = `aggregateId` |
| Métricas | Micrometer: `latiendita.orders.create` (timer), `latiendita.orders.created`, `latiendita.orders.revenue`, `latiendita.orders.status.changed{from,to}`, `latiendita.orders.cancelled{refund}`, `latiendita.orders.refunds.requested` |

---

## 5. Configuración

### 5.1 Variables de entorno

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de la base de datos | ✅ | — | `jdbc:postgresql://db:5432/orders` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `orders` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `KAFKA_BOOTSTRAP_SERVERS` | Servidores de Kafka | ❌ | `localhost:9092` | `kafka:9092` |
| `OUTBOX_RELAY_POLL_INTERVAL_MS` | Intervalo de polling del relay de outbox | ❌ | `1000` | `1000` |
| `OUTBOX_RELAY_BATCH_SIZE` | Tamaño de lote por corrida del relay | ❌ | `50` | `50` |
| `OUTBOX_RELAY_MAX_ATTEMPTS` | Intentos máximos por evento antes de dejarlo en `FAILED` | ❌ | `10` | `10` |
| `PRODUCT_SERVICE_BASE_URL` | Base URL de `product-service` para validar precios | ❌ | `http://localhost:8080` | `http://productService:8080` |
| `HTTP_CLIENT_CONNECT_TIMEOUT_MS` | Timeout de conexión del RestClient | ❌ | `2000` | `2000` |
| `HTTP_CLIENT_READ_TIMEOUT_MS` | Timeout de lectura del RestClient | ❌ | `3000` | `3000` |
| `RESILIENCE_MAX_ATTEMPTS` | Reintentos del Retry hacia `product-service` | ❌ | `3` | `3` |
| `RESILIENCE_WAIT_DURATION` | Backoff base del retry | ❌ | `200ms` | `200ms` |
| `RESILIENCE_WAIT_DURATION_MULTIPLIER` | Multiplicador del backoff exponencial | ❌ | `2.0` | `2.0` |
| `RESILIENCE_FAILURE_RATE_THRESHOLD` | Umbral de fallos del circuit breaker (%) | ❌ | `50` | `50` |
| `RESILIENCE_SLIDING_WINDOW_SIZE` | Ventana del circuit breaker | ❌ | `10` | `10` |
| `RESILIENCE_MINIMUM_NUMBER_OF_CALLS` | Mínimo de llamadas antes de evaluar el CB | ❌ | `5` | `5` |
| `RESILIENCE_WAIT_DURATION_IN_OPEN_STATE` | Tiempo en estado abierto | ❌ | `10s` | `10s` |
| `EUREKA_URL` | URL del registro Eureka | ❌ | `http://localhost:8761/eureka` | `http://eureka:8761/eureka` |
| `EUREKA_ENABLED` | Habilita el registro en Eureka | ❌ | `true` | `true` |
| `TRACING_SAMPLING_PROBABILITY` | Muestreo de tracing | ❌ | `0.1` | `0.1` |
| `OTLP_TRACING_ENDPOINT` | Endpoint OTLP de trazas | ❌ | `http://localhost:4318/v1/traces` | `http://otel:4318/v1/traces` |

### 5.2 Spring profiles

| Profile | Uso |
|---|---|
| `test` | Tests automatizados con Testcontainers (`TestcontainersConfiguration`) |
| `local` / `k8s` | `<a implementar>` (no hay perfiles declarados en el código; la variación por entorno se hace por variables de entorno) |

### 5.3 Recursos Kubernetes (orientativo)

| Recurso | Request | Limit | Notas |
|---|---|---|---|
| CPU | `250m` | `1000m` | — |
| Memoria | `512Mi` | `768Mi` | JVM con `-XX:MaxRAMPercentage=75` |

> `server.shutdown=graceful` con `spring.lifecycle.timeout-per-shutdown-phase=30s` para drenar requests ante `SIGTERM`; el relay de outbox sigue publicando en vuelo hasta que el pod termina.

---

## 6. Dependencias

### 6.1 Dependencias internas (otros microservicios)

| Servicio | Para qué | Protocolo | Fallback si cae | Crítica |
|---|---|---|---|---|
| `product-service` | Obtener precio, título y SKU de cada ítem al crear el pedido (`ProductClient.findById`) | HTTP/REST (`PRODUCT_SERVICE_BASE_URL`, por defecto `http://localhost:8080`) | Retry (3 intentos, backoff exponencial) + circuit breaker `productService`; si sigue fallando → `503 El servicio de productos no está disponible` | ✅ Sí |
| `payment-service` | Confirmar el pago del pedido (publica `PAYMENT_APPROVED`; este servicio lo consume) | Kafka (`payment-events`) | El pedido permanece en `PENDING` hasta que el evento llegue | ✅ Sí |
| `gateway` | Enruta `/api/orders/**`, valida el JWT y agrega `X-User-Id`/`X-User-Email`/`X-User-Roles`/`X-Correlation-Id` | HTTP/REST (upstream) | Sin gateway no hay identidad → `401` en este servicio | ✅ Sí |

> `X-Correlation-Id` se propaga hacia `product-service` mediante `CorrelationIdInterceptor` en el `RestClient`.

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia principal (`ddl-auto=update`) | ✅ Sí | `readiness` en `DOWN` (el group incluye `db`) |
| Kafka | Mensajería | Publicación de eventos (outbox transaccional) y consumo de `payment-events` | ❌ No | Los eventos quedan en `outbox_events` como `PENDING`/`FAILED` y el relay los reintenta al volver; los pagos aprobados en vuelo se procesan al recuperarse (`auto-offset-reset=earliest`) |
| Eureka | Registro de servicios | Descubrimiento para el gateway (`spring.application.name=orderService`) | ❌ No | `EUREKA_ENABLED=false` para operar sin registro |
| Collector OTLP | Observabilidad | Trazas y métricas (`/actuator/prometheus`, `/actuator/metrics`) | ❌ No | Sólo pierde observabilidad |

### 6.3 Eventos publicados/consumidos

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| `ORDER_CREATED` | Publica (topic `order-events`, vía outbox) | `POST /api/orders` exitoso | `{ orderId, orderNumber, customerId, total, items: [{ productId, quantity, unitPrice, subtotal }], occurredAt }` |
| `ORDER_STATUS_CHANGED` | Publica (topic `order-events`, vía outbox) | `POST /api/orders/{id}/status` con transición válida | `{ orderId, orderNumber, customerId, from, to, occurredAt }` |
| `ORDER_CANCELLED` | Publica (topic `order-events`, vía outbox) | `POST /api/orders/{id}/cancel` sobre `PENDING` o `PAID` | `{ orderId, orderNumber, customerId, reason, refundRequired, occurredAt }` |
| `REFUND_REQUESTED` | Publica (topic `order-events`, vía outbox) | Cancelación de un pedido que estaba `PAID` (sólo si `refundRequired`) | `{ orderId, orderNumber, customerId, amount, occurredAt }` |
| `PAYMENT_APPROVED` | **Consume** (topic `payment-events`, grupo `order-service`) | `payment-service` confirma el cobro → `PaymentApprovedConsumer` pasa el pedido de `PENDING` a `PAID` (idempotente) | `{ type, paymentId, orderId, orderNumber, customerId, amount, currency, provider, providerPaymentId, occurredAt }` |

> Publicaciones: `OutboxWriter.append(...)` en la transacción de dominio + `OutboxRelay` (`@Scheduled`) hacia Kafka. Consumo: `@KafkaListener(topics = "payment-events")`; los mensajes con otro `type` o malformados se descartan con log.

---

## 7. Cobertura del wireframe

Pantalla del wireframe (`docs/OpenPencil/LaTiendita.fig`, página "WireFrame") → endpoints/responsabilidades de `order-service` que la soportan.

| Pantalla | Soporte en este servicio | Estado |
|---|---|---|
| **Cart — lista de ítems** (producto, cantidad, precio unitario, subtotal por línea, botón quitar) | El carrito vive en el frontend; los precios/stock se leen con `GET /api/products/{id}` (de `product-service`, no de este servicio). Al confirmar, `POST /api/orders` congela `unitPrice` y `subtotal` por línea en `order_items` | ✅ Implementado (el pedido) / carrito en cliente |
| **Cart — resumen** ("Subtotal (4 productos)", total, CTA de continuar al checkout) | Calculado en el frontend con los mismos precios; el total se re-valida contra el servidor en `POST /api/orders` (`TotalMismatchException`) | ✅ Implementado |
| **Checkout — pasos** Carrito → Envío → Pago → Confirmación | `POST /api/orders` crea el pedido en `PENDING` al salir del carrito/confirmar envío; el paso **Pago** lo resuelve `payment-service` y el resultado llega por Kafka (`PAYMENT_APPROVED` → `PAID`); la confirmación muestra el pedido con `GET /api/orders/{id}` | ✅ Implementado |
| **Checkout Shipping — resumen** (subtotal, costo de envío, total, "Impuestos incluidos en los precios") | Subtotal y total salen de `POST /api/orders` / `GET /api/orders/{id}`. **Costo de envío no existe** en el modelo ni en el request (el total es sólo Σ subtotales); "Impuestos incluidos" es copy del frontend | ⚠️ `<a implementar>` (campo y cálculo de envío) |
| **Checkout Shipping — CTAs** "Continuar al pago" / "Volver al carrito" | Frontend; el paso de pago dispara `payment-service` y el estado del pedido queda visible vía `GET /api/orders/{id}` | ✅ Implementado |
| **Admin Pedidos — tabla** (Pedido `#1042`, Cliente, Fecha, Estado, Total, Acción) | `orderNumber`, `customerId`, `createdAt`, `status`, `total` existen en `OrderResponse`, pero **no hay endpoint de listado global**: `GET /api/orders` sólo devuelve pedidos del usuario actual. Además `orderNumber` es un UUID string, no un correlativo `#1042` (la UI debe formatearlo o guardar un correlativo) | ⚠️ `<a implementar>` (listado global y/o correlativo visible) |
| **Admin Pedidos — estados mostrados** | Mapeo a `OrderStatus` real: `Pendiente` = `PENDING`, `Pagado` = `PAID`, `Enviado` = `SHIPPED`; además el enum tiene `DELIVERED` (Entregado) y `CANCELLED` (Cancelado) que la UI debe mostrar | ✅ Implementado (mapeo de etiquetas) |
| **Admin Pedidos — acción de cambiar estado** | `POST /api/orders/{id}/status` con la máquina de transiciones | ⚠️ Sólo para pedidos propios; uso admin global `<a implementar>` |
| **Admin Resumen — KPI "Ventas de hoy"** ($ + cantidad de pedidos del día) | Sin endpoint de agregación; existen métricas Micrometer (`latiendita.orders.revenue`, `latiendita.orders.created`) como fuente posible | ⚠️ `<a implementar>` |
| **Admin Resumen — KPI "Pedidos pendientes"** ("4 sin pago acreditado") | Conteo de pedidos en `PENDING` (sin `PAYMENT_APPROVED`); sin endpoint de agregación | ⚠️ `<a implementar>` |
| **Admin Resumen — gráfico "Ventas de los últimos 7 días"** (total, pedidos, ticket promedio) | Serie temporal derivada de `orders.created_at`/`total`; sin endpoint de agregación | ⚠️ `<a implementar>` |
| **Admin Resumen — tabla "Últimos pedidos"** | Reutiliza el listado global de pedidos (ver fila de Admin Pedidos) | ⚠️ `<a implementar>` |
