# Microservicio: `shipment-service`

## 1. Responsabilidad

Cotiza los envíos (costo y fecha estimada de entrega), administra las direcciones de entrega del cliente y gestiona el despacho de los pedidos pagados: al despachar, publica el evento que hace que `order-service` pase el pedido de `Pagado` a `Enviado`. Es la única fuente de verdad sobre el estado del envío de un pedido.

No le pertenece: procesar pagos (`payment-service`), calcular el total del pedido o cambiar su estado por sí solo (`order-service`, salvo el efecto del despacho vía evento), ni administrar el carrito o el catálogo.

> **Estado del servicio:** *scaffold*. Solo existe `ShipmentServiceApplication` y sus tests; no hay controllers, entidades ni configuración de base de datos. Las secciones 2 a 6 son una **propuesta de contrato** diseñada para cumplir el wireframe.

---

## 2. Endpoints

> **Propuesta de contrato; aún no implementado.**

Todas las rutas son relativas a la raíz del servicio (ej. `http://shipmentService:8080`) y se publicarán a través del gateway bajo `/api/shipments/**` (ruta de gateway a agregar).

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes | — | `200` `{ status }` | No |
| `POST` | `/api/shipments/quote` | Cotiza el envío para un subtotal: costo, si es gratis y ventana estimada de entrega | `QuoteRequest` `{ subtotal }` | `200` QuoteResponse | Sí |
| `POST` | `/api/shipments/addresses` | Guarda una dirección de entrega (checkbox "Guardar esta dirección para futuras compras") | `CreateAddressRequest` | `201` AddressResponse | Sí |
| `GET` | `/api/shipments/addresses` | Lista las direcciones guardadas del cliente | — | `200` AddressResponse[] | Sí |
| `PUT` | `/api/shipments/addresses/{id}` | Actualiza una dirección guardada | `UpdateAddressRequest` | `200` AddressResponse | Sí |
| `DELETE` | `/api/shipments/addresses/{id}` | Elimina una dirección guardada | — | `204` | Sí |
| `POST` | `/api/shipments` | Registra el envío de un pedido (paso "Continuar al pago" del checkout) | `CreateShipmentRequest` `{ orderId, addressId?, address?, shippingCost, currency, estimatedFrom, estimatedTo }` | `201` ShipmentResponse (+ header `Location`) | Sí |
| `GET` | `/api/shipments/{id}` | Obtiene un envío por su ID | — | `200` ShipmentResponse | Sí |
| `GET` | `/api/shipments/order/{orderId}` | Envío asociado a un pedido | — | `200` ShipmentResponse | Sí |
| `GET` | `/api/shipments` | Lista de envíos (admin; filtros `status`, `page`, `size`, `sort`) | query | `200` Page&lt;ShipmentResponse&gt; | Sí (admin) |
| `GET` | `/api/shipments/pending-dispatch` | KPI de "pendientes de despacho" | — | `200` `{ count }` | Sí (admin) |
| `POST` | `/api/shipments/{id}/dispatch` | Acción de despacho: marca el envío como despachado (`Enviado`) | — | `200` ShipmentResponse | Sí (admin) |

Notas de identidad y trazabilidad:

- El gateway valida el JWT (Authorizer) y propaga `X-User-Id`, `X-User-Email` y `X-User-Roles`; el servicio leerá la identidad con el patrón `CurrentUser` (sin `X-User-Id` → `401`). El rol `admin` se lee de `X-User-Roles` (propuesto: ningún servicio valida roles hoy; el header ya está propagado).
- `X-Correlation-Id` se propagará de punta a punta (mismo patrón `CorrelationIdFilter` + `CorrelationIdInterceptor` que el resto de los servicios).

Flujo del checkout (paso "Envío"):

1. El frontend llama a `POST /api/shipments/quote` con el subtotal; el servicio responde costo (`0` o `2.499`), `freeShipping` y la ventana estimada (3–5 días hábiles) que se muestra en "Llega en 3–5 días hábiles a tu puerta".
2. El cliente completa el formulario de dirección; si marca la casilla, se persiste con `POST /api/shipments/addresses`.
3. Al "Continuar al pago", `POST /api/shipments` registra el envío del pedido con la dirección elegida (snapshot), el costo cotizado y la ventana estimada, en estado `AWAITING_PAYMENT`.
4. Cuando el pago se aprueba (`PAYMENT_APPROVED` desde `payment-service`), el envío pasa a `PENDING_DISPATCH` (alimenta el KPI "pendientes de despacho").
5. El admin despacha con `POST /api/shipments/{id}/dispatch`: el envío pasa a `DISPATCHED` y se publica `SHIPMENT_DISPATCHED` (outbox) para que `order-service` pase el pedido de `PAID` a `SHIPPED`.

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Envío no encontrado",
  "status": 404,
  "detail": "Envío no encontrado con id 12"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla (subtotal negativo, dirección incompleta) | `subtotal debe ser mayor o igual a 0` |
| `401` | Unauthorized | Falta `X-User-Id` (la petición no pasó por el gateway) | `Falta o es inválido el header 'X-User-Id'; la petición debe pasar por el Gateway` |
| `403` | Forbidden | Dirección o envío perteneciente a otro usuario | `No tiene permiso sobre la dirección 7` |
| `404` | Not Found | Envío, dirección o pedido inexistente | `Envío no encontrado con id 12` |
| `409` | Conflict | Envío ya registrado para el pedido, transición inválida o carrera de concurrencia | `Ya existe un envío para el pedido 42` / `No se puede despachar un envío en estado AWAITING_PAYMENT` |
| `422` | Unprocessable | Entidad válida pero no procesable | *(sin uso previsto)* |
| `500` | Internal Error | Fallo inesperado | `Unexpected error` |
| `503` | Service Unavailable | `order-service` caído (circuito abierto) al validar el pedido | `El servicio de pedidos no está disponible` |

---

## 4. Modelo de datos (Entidad)

> **Propuesto; aún no implementado.**

### 4.1 Entidades

#### `Shipment` → tabla `shipments`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `order_id` | `bigint` | ❌ | ❌ | ✅ | Pedido asociado; un solo envío por pedido |
| `customer_id` | `varchar(36)` | ❌ | ❌ | ❌ | Cliente dueño (`sub` del JWT) |
| `status` | `varchar(20)` | ❌ | ❌ | ❌ | `AWAITING_PAYMENT`, `PENDING_DISPATCH`, `DISPATCHED`, `DELIVERED`, `CANCELLED` |
| `shipping_cost` | `numeric(12,2)` | ❌ | ❌ | ❌ | Costo cotizado (`0` si es gratis) |
| `currency` | `char(3)` | ❌ | ❌ | ❌ | Moneda (`ARS`) |
| `recipient_name` | `varchar(120)` | ❌ | ❌ | ❌ | Snapshot del destinatario |
| `phone` | `varchar(30)` | ❌ | ❌ | ❌ | Snapshot del teléfono |
| `address_line` | `varchar(200)` | ❌ | ❌ | ❌ | Snapshot de la dirección |
| `city` | `varchar(100)` | ❌ | ❌ | ❌ | Snapshot de la ciudad |
| `province` | `varchar(100)` | ❌ | ❌ | ❌ | Snapshot de la provincia |
| `postal_code` | `varchar(10)` | ❌ | ❌ | ❌ | Snapshot del código postal |
| `references` | `varchar(200)` | ❌ | ✅ | ❌ | Referencias del lugar (opcional) |
| `estimated_from` | `date` | ❌ | ❌ | ❌ | Inicio de la ventana (3 días hábiles) |
| `estimated_to` | `date` | ❌ | ❌ | ❌ | Fin de la ventana (5 días hábiles) |
| `dispatched_at` | `timestamptz` | ❌ | ✅ | ❌ | Momento del despacho |
| `delivered_at` | `timestamptz` | ❌ | ✅ | ❌ | Momento de la entrega |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |
| `version` | `bigint` | ❌ | ❌ | ❌ | Optimistic locking |

#### `DeliveryAddress` → tabla `delivery_addresses`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `customer_id` | `varchar(36)` | ❌ | ❌ | ❌ | Cliente dueño |
| `recipient_name` | `varchar(120)` | ❌ | ❌ | ❌ | Nombre y apellido del destinatario |
| `phone` | `varchar(30)` | ❌ | ❌ | ❌ | Teléfono de contacto |
| `address_line` | `varchar(200)` | ❌ | ❌ | ❌ | Dirección (calle y número) |
| `city` | `varchar(100)` | ❌ | ❌ | ❌ | Ciudad |
| `province` | `varchar(100)` | ❌ | ❌ | ❌ | Provincia |
| `postal_code` | `varchar(10)` | ❌ | ❌ | ❌ | Código postal |
| `references` | `varchar(200)` | ❌ | ✅ | ❌ | Referencias (opcional) |
| `is_default` | `boolean` | ❌ | ❌ | ❌ | Dirección por defecto del cliente |
| `created_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de creación (auditoría) |
| `updated_at` | `timestamptz` | ❌ | ❌ | ❌ | Fecha de modificación (auditoría) |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Shipment` N → 1 `Order` | Muchos-a-uno | `order_id`; el pedido reside en `order-service` (sin FK) |
| `Shipment` N → 1 `DeliveryAddress` | Muchos-a-uno | Referencia al momento de crear; luego el envío conserva su propio snapshot (sin FK) |
| `Shipment` N → 1 `Customer` | Muchos-a-uno | `customer_id`; reside en `customer-service` (sin FK) |
| `DeliveryAddress` N → 1 `Customer` | Muchos-a-uno | `customer_id`; direcciones del libro del cliente |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Costo de envío | `shippingCost = 0` si `subtotal >= 50.000` (`SHIPMENT_FREE_SHIPPING_THRESHOLD`); caso contrario `2.499` (`SHIPMENT_FLAT_RATE`). Moneda `ARS` |
| Fecha estimada | Ventana de 3 a 5 días hábiles desde el registro (sábados y domingos excluidos; feriados nacionales **no** contemplados — limitación conocida) |
| Unicidad | Un solo envío por pedido (`order_id` unique) |
| Snapshot de dirección | La dirección se copia al envío al registrarlo; editar o borrar la dirección guardada no altera envíos existentes |
| Estados | `AWAITING_PAYMENT` → `PENDING_DISPATCH` (pago aprobado) → `DISPATCHED` (despacho admin) → `DELIVERED`; `CANCELLED` desde `AWAITING_PAYMENT` o `PENDING_DISPATCH` si el pedido se cancela |
| Despacho | Solo desde `PENDING_DISPATCH`; al despachar se publica `SHIPMENT_DISPATCHED` por outbox y el pedido pasa `PAID` → `SHIPPED` en `order-service` |
| Idempotencia | Reenviar `PAYMENT_APPROVED` no duplica el envío; repetir el despacho responde `200` sin re-publicar el evento |
| Sin pago aprobado | Si `PAYMENT_APPROVED` llega sin envío registrado, se crea con la dirección por defecto del cliente; si no hay ninguna, queda `PENDING_DISPATCH` sin dirección para resolución manual |
| Borrado | Físico; las direcciones del cliente también (sin borrado lógico) |
| Concurrencia | `version` (optimistic locking) → `409` ante escrituras simultáneas |

---

## 5. Configuración

> **Propuesto; aún no implementado.** Hoy `application.properties` solo contiene `spring.application.name=shipmentService`.

### 5.1 Variables de entorno

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de la base de datos | ✅ | — | `jdbc:postgresql://db:5432/shipment` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `shipment` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers de Kafka (outbox + listeners) | ❌ | `localhost:9092` | `kafka:9092` |
| `ORDER_SERVICE_BASE_URL` | URL base de `order-service` | ❌ | `http://localhost:8080` | `http://orderService:8080` |
| `SHIPMENT_FREE_SHIPPING_THRESHOLD` | Umbral de subtotal para envío gratis | ❌ | `50000` | `50000` |
| `SHIPMENT_FLAT_RATE` | Costo fijo de envío | ❌ | `2499` | `2499` |
| `SHIPMENT_ETA_MIN_BUSINESS_DAYS` | Mínimo de días hábiles de entrega | ❌ | `3` | `3` |
| `SHIPMENT_ETA_MAX_BUSINESS_DAYS` | Máximo de días hábiles de entrega | ❌ | `5` | `5` |
| `SHIPMENT_DEFAULT_CURRENCY` | Moneda del costo de envío | ❌ | `ARS` | `ARS` |
| `OUTBOX_RELAY_POLL_INTERVAL_MS` / `OUTBOX_RELAY_BATCH_SIZE` / `OUTBOX_RELAY_MAX_ATTEMPTS` | Relay del outbox → Kafka | ❌ | `1000` / `50` / `10` | `1000` |
| `RESILIENCE_MAX_ATTEMPTS`, `RESILIENCE_FAILURE_RATE_THRESHOLD`, … | Circuit breaker y retry hacia `order-service` | ❌ | `3`, `50`, … | *(ver `ResilienceProperties`)* |
| `EUREKA_URL` / `EUREKA_ENABLED` | Registro en Eureka (`spring.application.name=shipmentService`) | ❌ | `http://localhost:8761/eureka` / `true` | — |
| `LOGGING_LEVEL_ROOT` | Nivel de log | ❌ | `INFO` | `INFO` |

Nota: no requiere `JWT_SECRET`; la validación del token la hace el gateway (patrón del repo).

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

> **Propuesto; aún no implementado.**

### 6.1 Dependencias internas (otros microservicios)

| Servicio | Para qué | Protocolo | Fallback si cae | Crítica |
|---|---|---|---|---|
| `order-service` | Validar que el pedido existe y está `PENDING` al registrar el envío (`OrderClient`, patrón de `payment-service`) | HTTP/REST (`ORDER_SERVICE_BASE_URL`) | Retry + circuit breaker Resilience4j → `503` | ✅ Sí |

**Decisión sobre las direcciones:** las direcciones de entrega se guardan en **este servicio** (`delivery_addresses`), no en `customer-service`. Justificación: (1) `customer-service` es hoy un scaffold sin API, y bloquear el paso "Envío" del checkout detrás de un servicio no implementado no es aceptable; (2) la dirección de entrega es un dato logístico —de él dependen el costo, la ventana estimada y el despacho—; (3) el envío de todos modos debe persistir su propio snapshot para no reescribir el historial cuando la dirección cambie o se borre. Cuando `customer-service` madure como fuente de verdad del perfil, el libro de direcciones puede consolidarse allí copiando solo el snapshot al crear el envío (migración sin cambio de contrato para el frontend).

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia de `shipments` y `delivery_addresses` | ✅ Sí | `503`, readiness en `DOWN` |
| Kafka | Mensajería | Eventos de dominio (vía outbox transaccional) + consumo de `payment-events`/`order-events` | ❌ No | El outbox retiene eventos y el relay los reintenta al volver; los listeners reintentan mensajes |

### 6.3 Eventos publicados/consumidos

Publicados en `shipment-events` con key `orderId`, mediante outbox transaccional (`OutboxWriter` + `OutboxRelay`), mismo patrón que `payment-service`.

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| `PAYMENT_APPROVED` | Consume | Pago aprobado (topic `payment-events`, desde `payment-service`) | Envío → `PENDING_DISPATCH` (o lo crea) — `{ paymentId, orderId, customerId, amount, … }` |
| `ORDER_CANCELLED` | Consume | Pedido cancelado (topic `order-events`, desde `order-service`) | Envío → `CANCELLED` — `{ orderId, orderNumber, customerId, reason, refundRequired, occurredAt }` |
| `SHIPMENT_DISPATCHED` | Publica | Acción de despacho (`POST /api/shipments/{id}/dispatch`) | `{ type, shipmentId, orderId, customerId, dispatchedAt, estimatedTo, occurredAt }` — consumidor propuesto en `order-service`: `PAID` → `SHIPPED` |
| `SHIPMENT_DELIVERED` | Publica | Confirmación de entrega | `{ type, shipmentId, orderId, customerId, deliveredAt, occurredAt }` |
| `SHIPMENT_CANCELLED` | Publica | Cancelación de envío | `{ type, shipmentId, orderId, customerId, occurredAt }` |

---

## 7. Cobertura del wireframe

| Pantalla del wireframe | Endpoints / responsabilidades del servicio |
|---|---|
| **Checkout "Envío"** (Checkout Shipping Wireframe) — método "Envío a domicilio", descripción "Llega en 3–5 días hábiles a tu puerta", precio "$ 2.499" y nota "Gratis desde $ 50.000" | `POST /api/shipments/quote` aplica la regla (gratis desde $ 50.000, si no $ 2.499) y devuelve la ventana de 3–5 días hábiles que alimenta la descripción y la fila "Envío a domicilio" del resumen |
| **Checkout "Envío"** — formulario de dirección (nombre y apellido, teléfono, dirección, ciudad, provincia, código postal, referencias) y checkbox "Guardar esta dirección para futuras compras" | `POST /api/shipments/addresses` (con el checkbox marcado), `GET`/`PUT`/`DELETE /api/shipments/addresses` para reutilizar direcciones en compras futuras; `POST /api/shipments` registra el envío con la dirección elegida al continuar al pago |
| **Checkout "Envío"** — resumen con fila "Envío a domicilio" y total | `QuoteResponse.shippingCost` se muestra en la fila; el total es responsabilidad del frontend/`order-service` (ver nota de integración abajo) |
| **Admin Pedidos** — estado `Enviado` del pedido y acción de despacho | `POST /api/shipments/{id}/dispatch` marca el envío `DISPATCHED` y publica `SHIPMENT_DISPATCHED`; consumidor propuesto en `order-service` pasa el pedido `PAID` → `SHIPPED` (`Enviado`) |
| **Admin Resumen** — KPI "Pedidos pendientes: 12 pendientes de despacho" | `GET /api/shipments/pending-dispatch` devuelve el conteo de envíos en `PENDING_DISPATCH` (pedidos pagados sin despachar) |
| **Admin Pedidos** — listado y detalle de envíos | `GET /api/shipments` (filtros por estado, paginado) y `GET /api/shipments/{id}` |

Notas de integración (gaps a resolver fuera de este servicio):

1. **Ruta de gateway:** agregar `Path=/api/shipments/**` hacia `lb://shipmentService` en `gateway/application.properties`.
2. **Consumidor en `order-service`:** implementar el listener de `SHIPMENT_DISPATCHED` que pase `PAID` → `SHIPPED` (hoy solo existe `PaymentApprovedConsumer`).
3. **Total del checkout:** `order-service` calcula `total` solo con los ítems y rechaza cualquier desvío (`TotalMismatchException`); para que el resumen incluya el costo de envío en el total cobrado, `CreateOrderRequest` deberá aceptar `shippingCost`. Es un cambio en `order-service`, fuera del alcance de este servicio.
