# Microservicio: `payment-service`

## 1. Responsabilidad

Gestiona el ciclo de vida de los pagos de los pedidos: crea la intención de pago contra el proveedor (Mercado Pago), aplica las confirmaciones recibidas por webhook firmado y publica los eventos de estado que permiten a `order-service` avanzar el pedido de `Pendiente` a `Pagado`. Es la única fuente de verdad sobre el estado del pago; nunca modifica el pedido directamente, solo lo notifica.

No le pertenece: calcular el total del pedido (carrito e ítems), cambiar el estado del pedido (`order-service`) ni persistir datos del cliente (`customer-service`).

---

## 2. Endpoints

Todas las rutas son relativas a la raíz del servicio (ej. `http://paymentService:8080`) y se publican a través del gateway bajo `/api/payments/**` y `/api/webhooks/**`.

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes | — | `200` `{ status }` | No |
| `POST` | `/api/payments` | Crea el pago de un pedido: genera la preferencia en el proveedor y devuelve la `checkoutUrl` | `CreatePaymentRequest` | `201` PaymentResponse (+ header `Location`) | Sí |
| `GET` | `/api/payments/{paymentId}` | Obtiene un pago por su ID | — | `200` PaymentResponse | Sí |
| `GET` | `/api/payments` | Pagos del cliente autenticado (paginado: `page`, `size`, `sort`) | — | `200` Page&lt;PaymentResponse&gt; | Sí |
| `GET` | `/api/payments/order/{orderId}` | Pago asociado a un pedido | — | `200` PaymentResponse | Sí |
| `GET` | `/api/payments/reference/{externalReference}` | Resuelve el pago por `orderNumber`; lo usan las *back_urls* de Mercado Pago (`?external_reference=<orderNumber>`) | — | `200` PaymentResponse | Sí |
| `POST` | `/api/webhooks/mercado-pago` | Notificación de Mercado Pago (webhook/IPN) | `WebhookNotification` (body) + headers `x-signature`, `x-request-id` + query `data.id`, `type` | `200` (vacío) | No (firma HMAC-SHA256) |

Notas de identidad y trazabilidad:

- El gateway valida el JWT (Authorizer) y propaga `X-User-Id`, `X-User-Email` y `X-User-Roles`; este servicio lee la identidad con el patrón `CurrentUser` (sin `X-User-Id` → `401`). Los headers de identidad se pisan siempre con los claims del token.
- `X-Correlation-Id` se propaga de punta a punta (`CorrelationIdFilter` + `CorrelationIdInterceptor`).
- `/api/webhooks/**` está excluido del *rate limiting* del gateway (Mercado Pago no lleva JWT).

Flujo del checkout (paso "Pago"):

1. `POST /api/payments` valida contra `order-service` que el pedido exista, pertenezca al cliente y esté `PENDING`, crea el registro en `PENDING` con `idempotencyKey = orderNumber` y llama al proveedor para crear la preferencia (`checkoutUrl`).
2. El cliente paga fuera de casa y Mercado Pago notifica a `POST /api/webhooks/mercado-pago`.
3. El webhook valida la firma (`WebhookSignatureValidator`, HMAC-SHA256 sobre `id:[data.id];request-id:[x-request-id];ts:[ts];`), registra la notificación en el inbox idempotente (`PaymentEventEntity`) y, si corresponde, consulta el pago al proveedor (`MercadoPagoAdapter.getPayment`).
4. `MercadoPagoStatusMapper` traduce el estado del proveedor a `PaymentStatus`, se valida la transición y el monto, y se publica el evento correspondiente en `payment-events` (outbox transaccional).

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Pago no encontrado",
  "status": 404,
  "detail": "Pago no encontrado con id 42"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla (o monto del proveedor inconsistente) | `El monto informado por el proveedor (100.00) no coincide con el monto registrado (150.00)` |
| `401` | Unauthorized | Falta `X-User-Id` (la petición no pasó por el gateway) | `Falta o es inválido el header 'X-User-Id'; la petición debe pasar por el Gateway` |
| `403` | Forbidden | El pago pertenece a otro cliente | `No tiene permiso sobre el pago 7` |
| `404` | Not Found | Pago o pedido inexistente | `Pago no encontrado con id 42` / `Pedido no encontrado con id 42` |
| `409` | Conflict | Pedido ya pagado, transición inválida o carrera de concurrencia | `El pedido 42 ya tiene un pago aprobado` / `No se puede pasar de APPROVED a REJECTED` |
| `422` | Unprocessable | Entidad válida pero no procesable | *(sin uso actual)* |
| `500` | Internal Error | Fallo inesperado | `Unexpected error` |
| `503` | Service Unavailable | Proveedor de pagos u `order-service` caído (circuito abierto) | `El servicio de pedidos no está disponible` |

Detalle de los `409`: `DuplicatePaymentException` (pedido ya `PAID`), `InvalidStateTransitionException` (violación de `PaymentStatus.canTransitionTo`), `OptimisticLockingFailureException` (campo `version`) y `DataIntegrityViolationException` (únicas de `payments`).

---

## 4. Modelo de datos (Entidad)

### 4.1 Entidades

#### `Payment` → tabla `payments`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `order_id` | `bigint` | ❌ | ❌ | ❌ | Pedido asociado (referencia lógica a `order-service`) |
| `customer_id` | `varchar(36)` | ❌ | ❌ | ❌ | Cliente dueño (`sub` del JWT) |
| `provider` | `varchar(20)` | ❌ | ❌ | ❌ | Proveedor (`MERCADO_PAGO`); único junto a `provider_payment_id` |
| `status` | `varchar(20)` | ❌ | ❌ | ❌ | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `EXPIRED` |
| `amount` | `numeric(12,2)` | ❌ | ❌ | ❌ | Monto cobrado (=`order.total`) |
| `currency` | `char(3)` | ❌ | ❌ | ❌ | Moneda (`ARS`) |
| `preference_id` | `varchar(64)` | ❌ | ✅ | ❌ | ID de la preferencia en el proveedor |
| `checkout_url` | `varchar(500)` | ❌ | ✅ | ❌ | URL de pago devuelta por el proveedor |
| `external_reference` | `varchar(36)` | ❌ | ✅ | ❌ | `orderNumber`; identificador propio en las *back_urls* |
| `idempotency_key` | `varchar(64)` | ❌ | ❌ | ✅ | = `orderNumber`; garantiza un solo pago por pedido |
| `provider_payment_id` | `varchar(64)` | ❌ | ✅ | ❌ | ID del pago en el proveedor; único con `provider` |
| `payment_method_id` | `varchar(40)` | ❌ | ✅ | ❌ | Medio de pago (ej. `visa`) |
| `payment_type` | `varchar(30)` | ❌ | ✅ | ❌ | Tipo (ej. `credit_card`) |
| `status_detail` | `varchar(100)` | ❌ | ✅ | ❌ | Detalle crudo del proveedor (`accredited`, `cc_rejected`, …) |
| `paid_at` | `timestamptz` | ❌ | ✅ | ❌ | Se setea solo al pasar a `APPROVED` |
| `expires_at` | `timestamptz` | ❌ | ✅ | ❌ | Vencimiento de la preferencia |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |
| `version` | `bigint` | ❌ | ❌ | ❌ | Optimistic locking |

#### `PaymentEvent` → tabla `payment_events` (inbox idempotente de webhooks)

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `provider` | `varchar(20)` | ❌ | ❌ | ❌ | Proveedor; único junto a `notification_id` |
| `notification_id` | `varchar(128)` | ❌ | ❌ | ❌ | ID de la notificación (o `type:resourceId` si no viene) |
| `resource_type` | `varchar(20)` | ❌ | ❌ | ❌ | Tipo de recurso notificado (`payment`) |
| `resource_id` | `varchar(64)` | ❌ | ❌ | ❌ | ID del pago en el proveedor (`data.id`) |
| `action` | `varchar(50)` | ❌ | ✅ | ❌ | Acción reportada por el proveedor |
| `signature_valid` | `boolean` | ❌ | ❌ | ❌ | Resultado de validar `x-signature` |
| `status` | `varchar(20)` | ❌ | ❌ | ❌ | `RECEIVED`, `PROCESSED`, `IGNORED`, `FAILED` |
| `attempts` | `int` | ❌ | ❌ | ❌ | Reintentos de procesamiento |
| `last_error` | `varchar(500)` | ❌ | ✅ | ❌ | Último error registrado |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de recepción |
| `processed_at` | `timestamptz` | ❌ | ✅ | ❌ | Fecha de resolución |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Payment` N → 1 `Order` | Muchos-a-uno | `order_id`; el pedido reside en `order-service` (sin FK, integridad vía `OrderClient`) |
| `PaymentEvent` N → 1 `Payment` | Muchos-a-uno | Referencia lógica por `(provider, resource_id)`; sin FK (el evento llega antes de resolver el pago) |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Unicidad por pedido | `idempotency_key` = `orderNumber` (unique): un solo pago activo por pedido |
| Unicidad por proveedor | `(provider, provider_payment_id)` unique: evita aplicar dos veces la misma confirmación |
| Transiciones de estado | `PENDING` → cualquier estado; `REJECTED`/`CANCELLED`/`EXPIRED` → solo `APPROVED`; `APPROVED` es final (`PaymentStatus.canTransitionTo`, con `rank` para ordenar) |
| Idempotencia de webhooks | Notificaciones repetidas se descartan por el inbox `payment_events` (`PROCESSED`/`IGNORED`); los `FAILED` se reintentan |
| Monto | El `transaction_amount` del proveedor debe coincidir con `amount`; si difiere → `400` y no se aplica |
| Firma | Sin `MERCADO_PAGO_WEBHOOK_SECRET` o con firma inválida, la notificación se registra como `IGNORED` y se descarta |
| Borrado | Físico (no hay borrado lógico); los pagos se conservan por auditoría |
| Concurrencia | `version` (optimistic locking) → `409` ante escrituras simultáneas |
| Invariantes | `created_at <= updated_at`; `paid_at` solo cuando `status = APPROVED` |

---

## 5. Configuración

### 5.1 Variables de entorno

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de la base de datos | ✅ | — | `jdbc:postgresql://db:5432/payment` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `payment` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers de Kafka para el outbox | ❌ | `localhost:9092` | `kafka:9092` |
| `ORDER_SERVICE_BASE_URL` | URL base de `order-service` | ❌ | `http://localhost:8080` | `http://orderService:8080` |
| `PAYMENT_PROVIDER` | Proveedor activo | ❌ | `MERCADO_PAGO` | `MERCADO_PAGO` |
| `PAYMENT_DEFAULT_CURRENCY` | Moneda de los pagos | ❌ | `ARS` | `ARS` |
| `MERCADO_PAGO_ACCESS_TOKEN` | Token de API del proveedor | ✅ | *(vacío → `503` al crear preferencia)* | *(secret)* |
| `MERCADO_PAGO_WEBHOOK_SECRET` | Secreto para validar `x-signature` | ✅ | *(vacío → webhooks descartados)* | *(secret)* |
| `MERCADO_PAGO_NOTIFICATION_URL` | URL pública de notificación | ✅ | — | `https://api.tienda.com/api/webhooks/mercado-pago` |
| `MERCADO_PAGO_BACK_URL_SUCCESS` | URL de retorno (aprobado) | ❌ | `https://tienda.com/checkout/success` | *(front)* |
| `MERCADO_PAGO_BACK_URL_FAILURE` | URL de retorno (rechazado) | ❌ | `https://tienda.com/checkout/failure` | *(front)* |
| `MERCADO_PAGO_BACK_URL_PENDING` | URL de retorno (pendiente) | ❌ | `https://tienda.com/checkout/pending` | *(front)* |
| `MERCADO_PAGO_STATEMENT_DESCRIPTOR` | Descriptor en el resumen de tarjeta | ❌ | *(vacío)* | `LATIENDITA` |
| `OUTBOX_RELAY_POLL_INTERVAL_MS` / `OUTBOX_RELAY_BATCH_SIZE` / `OUTBOX_RELAY_MAX_ATTEMPTS` | Relay del outbox → Kafka | ❌ | `1000` / `50` / `10` | `1000` |
| `RESILIENCE_MAX_ATTEMPTS`, `RESILIENCE_FAILURE_RATE_THRESHOLD`, … | Circuit breaker y retry hacia `order-service` | ❌ | `3`, `50`, … | *(ver `ResilienceProperties`)* |
| `EUREKA_URL` / `EUREKA_ENABLED` | Registro en Eureka | ❌ | `http://localhost:8761/eureka` / `true` | — |
| `LOGGING_LEVEL_ROOT` | Nivel de log | ❌ | `INFO` | `INFO` |

Nota: este servicio **no** recibe `JWT_SECRET`; la validación del token la hace el gateway (patrón del repo). Los datos de trazabilidad (`TRACING_SAMPLING_PROBABILITY`, `OTLP_TRACING_ENDPOINT`) y el shutdown gracioso (`server.shutdown=graceful`) están en `application.properties`.

### 5.2 Spring profiles

| Profile | Uso |
|---|---|
| `local` | Desarrollo con `docker compose` |
| `test` | Tests automatizados (Testcontainers, `TestcontainersConfiguration`) |
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
| `order-service` | Validar que el pedido existe, pertenece al cliente y está `PENDING` antes de cobrar (`OrderClient.findById`) | HTTP/REST (`ORDER_SERVICE_BASE_URL`) | Retry + circuit breaker Resilience4j (`orderService`) → `503` | ✅ Sí (en la creación del pago) |

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia de `payments` y `payment_events` | ✅ Sí | `503`, readiness en `DOWN` |
| Kafka | Mensajería | Eventos de dominio (vía outbox transaccional) | ❌ No | El outbox retiene eventos y el relay los reintenta al volver |
| API de Mercado Pago (SDK) | Terceros | Crear preferencias y consultar pagos | ✅ Sí (creación) | `503` con retry; los webhooks entrantes siguen registrándose en el inbox |

### 6.3 Eventos publicados/consumidos

Todos se publican en el topic `payment-events` con key `orderId`, mediante outbox transaccional (`OutboxWriter` + `OutboxRelay`).

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| `PAYMENT_CREATED` | Publica | Alta de un pago (`POST /api/payments`) | `{ type, paymentId, orderId, orderNumber, customerId, amount, currency, occurredAt }` |
| `PAYMENT_APPROVED` | Publica | Webhook confirma el pago (transición a `APPROVED`) | `{ type, paymentId, orderId, orderNumber, customerId, amount, currency, provider, providerPaymentId, occurredAt }` |
| `PAYMENT_REJECTED` | Publica | Proveedor rechaza el pago | `{ type, paymentId, orderId, customerId, statusDetail, occurredAt }` |
| `PAYMENT_CANCELLED` | Publica | Proveedor cancela el pago | `{ type, paymentId, orderId, customerId, occurredAt }` |
| — | Consume | — | Este servicio no consume eventos hoy |

Consumidor existente: `order-service` (`PaymentApprovedConsumer`) reacciona a `PAYMENT_APPROVED` y pasa el pedido de `PENDING` a `PAID` (idempotente). `PAYMENT_REJECTED` y `PAYMENT_CANCELLED` aún no tienen consumidor.

---

## 7. Cobertura del wireframe

| Pantalla del wireframe | Endpoints / responsabilidades del servicio |
|---|---|
| **Checkout — paso "Pago"** (tras "Continuar al pago" del resumen de envío) | `POST /api/payments` crea el pago del pedido y devuelve `checkoutUrl` para redirigir al proveedor; `GET /api/payments/reference/{orderNumber}` resuelve el pago en las páginas de retorno; `POST /api/webhooks/mercado-pago` confirma la aprobación y publica `PAYMENT_APPROVED` |
| **Checkout — estados del pago y confirmación hacia el pedido** | Transiciones de `PaymentStatus` y mapeo del proveedor con `MercadoPagoStatusMapper` (`approved`, `rejected`, `cancelled`, `expired`, `pending/authorized/in_process/in_mediation`); eventos `PAYMENT_APPROVED` / `PAYMENT_REJECTED` / `PAYMENT_CANCELLED` en `payment-events` |
| **Admin Pagos — tabla/lista de pagos con estados** | `GET /api/payments` devuelve `Page<PaymentResponse>` con `status`, `amount` y `paidAt`. **Gap:** hoy el listado se filtra por el cliente autenticado (`CurrentUser`); falta el endpoint de listado para admin (filtrado por `X-User-Roles: admin`) |
| **Admin Pagos — KPI "Pagos a acreditar: $86.400 · 3 pagos en revisión"** | **Gap:** no existe endpoint agregado. Se propone `GET /api/payments/admin/summary` (sumatoria de `APPROVED` pendientes de acreditar + conteo de `PENDING` como "en revisión") |
| **Admin Resumen — KPI "Pagos a acreditar" y "Pedidos pendientes — 4 sin pago acreditado"** | Pagos a acreditar: mismo endpoint admin propuesto. Pedidos sin acreditar: corresponde a pedidos en `PENDING` en `order-service`; este servicio aporta el estado real del pago que los explica |
| **Admin Pedidos — estados del pedido derivados del pago (`Pendiente` → `Pagado`)** | `PAYMENT_APPROVED` → `PaymentApprovedConsumer` (order-service) cambia `PENDING` → `PAID`; `PAYMENT_REJECTED`/`PAYMENT_CANCELLED` quedan publicados para los efectos aún no implementados (notificación/reintento) |

Nota de integración: el monto cobrado es `order.total`, que hoy suma solo los ítems del pedido (ver nota de integración en `shipment-service.md`).
