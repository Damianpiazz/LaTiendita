# Implementar microservicios scaffold
Objective: implement the four Spring Boot scaffold microservices (cart, customer, offer, shipment) matching the working services' patterns, with dependencies, production code and tests; scalable, low-coupling design.
Branch: `feat/implementar-microservicios-scaffold` (from `main`).
Delivery: `auto-chain` with `feature-branch-chain` slicing (no PR opened without explicit request). Runner: `mvn verify` per service (wrapper if present). Seeders excluded; `review-service` out of scope.

## Specs

S1 — Alcance global y restricciones, en palabras del usuario (literal): "Quiero que empiezes a implementar los microservicios, instalando dependencias, implementando codigo que respete como esta implementado ahora. testeando de la misma manera, codigo escalable, con buenas practicas y diseño. que no tengo mucho acoplamiento. no tengas en cuenta los seeders. utiliza las skills necesarias. separa la carga de trabajo como quieras"
   - Cuatro scaffolds a implementar: `cart-service`, `customer-service`, `offer-service`, `shipment-service`. `review-service` queda fuera por decisión previa del usuario: "por ahora lo de la review no te fijes".
   - Criterio transversal aplicable a S2–S5: mismo Boot/Java y mismas dependencias BOM-manejadas que los servicios ya implementados; estructura controller → request/response `record` → service → repository → entity → `GlobalExceptionHandler` → config; tests con el mismo set-up (Testcontainers, `eureka.client.enabled=false`); clientes HTTP detrás de interfaces, como `ProductClient` en `order-service`, sin acoplamiento directo entre servicios; DTOs según §4.4 de cada documento.
   - Verificación: cada servicio compila y pasa su suite (`mvn verify`); `git diff --check` limpio; `gentle-ai review assess` por work-unit.

S2 — `cart-service` (`docs/microservicios/cart-service.md`): alta de ítems con `productId`/`quantity`, actualización y borrado por `productId`, vaciado del carrito y `GET /api/carts`. Identidad por header `X-User-Id` (un carrito por usuario). Entidades `Cart` (`UUID`, `@Version`) y `CartItem` (`product_id` `bigint`, `unit_price` snapshot, constraint `(cart_id, product_id)`). DTOs `CartDto` (`id`, `items[]`, `subtotal`, `itemCount`, `total`; ítems con `productTitle`, `imageUrl`, `lineSubtotal`, `priceChanged`), `AddCartItemRequest`, `UpdateCartItemRequest`. Título/precio/stock se obtienen de `product-service` vía cliente HTTP, nunca del cliente. Regla `Quantity must be between 1 and 99` y validación contra stock. Sin seeders.

S3 — `customer-service` (`docs/microservicios/customer-service.md`): alta idempotente en primer login (clave `X-User-Id`), `GET /api/customers/me`, `PATCH /api/customers/me`, `PUT /api/customers/me/newsletter`, listado paginado de Admin con filtros por nombre/email/estado y detalle por id. Entidad `Customer` con `authorizer_sub`, `email`, `status`, consentimiento de newsletter y borrado lógico; **sin direcciones** (viven en `shipment-service` / `delivery_addresses`). DTOs de §4.4: `CustomerDto`, `CustomerSummaryDto`, `CreateCustomerRequest`, `UpdateCustomerRequest`, `NewsletterRequest`, `NewsletterDto`. Sin seeders.

S4 — `offer-service` (`docs/microservicios/offer-service.md`): `GET /api/benefits`, `GET /api/promotions/active`, `POST /api/promotions/evaluate`, `GET /api/coupons/{code}` y CRUD admin de promociones. Entidades `Benefit`, `Promotion`, `Coupon`; reglas de `type` (`PERCENTAGE`, `FIXED_AMOUNT`, `FEATURED`), `scope`, `priority`, `stackable` y vigencia. DTOs de §4.4: `BenefitDto`, `PromotionDto`, `EvaluationDto`, `EvaluateRequest`, `CouponDto`, `CreatePromotionRequest`, `UpdatePromotionRequest`. **Sin `FREE_SHIPPING` / `freeShipping`** en ningún DTO ni configuración; `productId`/`categoryId` numéricos. Sin seeders.

S5 — `shipment-service` (`docs/microservicios/shipment-service.md`): `POST /api/shipments/quote` (tarifa plana `2.499`, `subtotal` informativo), CRUD de `delivery_addresses` (fuente única de direcciones), alta/consulta de envíos y despacho. Entidades `Shipment` (con snapshot de dirección y `@Version`) y `DeliveryAddress`. DTOs de §4.4: `QuoteRequest`, `QuoteResponse`, `AddressResponse`, `CreateAddressRequest`, `UpdateAddressRequest`, `CreateShipmentRequest`, `ShipmentResponse`. Ventana estimada de 3–5 días hábiles; sin `freeShipping`. Outbox `SHIPMENT_DISPATCHED`/`SHIPMENT_DELIVERED`/`SHIPMENT_CANCELLED` y consumers de `payment-events`/`order-events` (§6.3). Sin seeders.

S5b — Mensajería (decisión del usuario 2026-11-19: "API + Kafka/outbox junto"): los 4 servicios incluyen Kafka/outbox según §6.3 de su doc. `cart-service` consume `PRODUCT_PRICE_CHANGED`/`PRODUCT_DELETED` de `product-events` (sin publica); `customer-service` publica `CUSTOMER_CREATED` y consume `USER_REGISTERED`; `offer-service` publica `PROMOTION_CHANGED` y consume `ORDER_CREATED`; `shipment-service` publica 3 eventos y consume `PAYMENT_APPROVED`/`ORDER_CANCELLED`. Patrón idéntico al de `payment-service`: `OutboxEvent`+`OutboxStatus`+`OutboxEventRepository`+`OutboxRelay` (`@Scheduled`, `KafkaTemplate.send(...).get(5s)`), consumers `@KafkaListener` con @Transactional e idempotencia. **Fuera de los 4 scaffolds, a cargo del padre (T5):** consumidor de `SHIPMENT_DISPATCHED` en `order-service` (`PAID` → `SHIPPED`). Sin seeders.

S6 — Cableado compartido: rutas del gateway y registro en Eureka acordes a los servicios existentes, y actualización del estado en `README.md` al pasar de *scaffold* a *implementado*. El hermano de archivos compartidos lo edita el padre, no los writers. Sin seeders.

S7 — Limitación ambiental conocida (no blocking): Java 17 está disponible, pero el daemon de Docker no está corriendo, así que los tests `@SpringBootTest` + `@Import(TestcontainersConfiguration.class)` NO pueden ejecutarse localmente. Deben escribirse y compilar igual (corren en CI con Docker); la verificación local se limita a compilación (`mvn -DskipTests package`) y tests unitarios Mockito puros (`mvn test -Dtest=<UnitTest>`). Reportar como skip ambiental, nunca como fallo del código.

## Tasks

- T1 — S1, S2, S5b, S7 — `cart-service` — delegado (writer) — pending
- T2 — S1, S3, S5b, S7 — `customer-service` — delegado (writer) — pending
- T3 — S1, S4, S5b, S7 — `offer-service` — delegado (writer) — pending
- T4 — S1, S5, S5b, S7 — `shipment-service` — delegado (writer) — pending
- T5 — S1, S5b, S6 — consumidor `SHIPMENT_DISPATCHED` en `order-service` + gateway/Eureka/README — inline (padre) — pending

Route evidence: mapa de patrones delegado a `gentle-ai-explore` (presupuesto inline excedido). Writers por servicio porque hay 4 unidades independientes con superficies de edición disjuntas, lanzadas juntas (Writer trigger a); cada una es sustancialmente más pesada que arrancar un subagent. El padre posee git, el consumidor en `order-service` y los archivos compartidos (T5), con un seam check al final. Delivery forecast: ~1200-1600 líneas autorizadas → `auto-chain` con `feature-branch-chain`; se registrarán límites de slice en el Log.

## Log

L1 — 2026-11-19 — Petición original del usuario (literal): "Quiero que empiezes a implementar los microservicios, instalando dependencias, implementando codigo que respete como esta implementado ahora. testeando de la misma manera, codigo escalable, con buenas practicas y diseño. que no tengo mucho acoplamiento. no tengas en cuenta los seeders. utiliza las skills necesarias. separa la carga de trabajo como quieras"

L2 — 2026-11-19 — Decisiones heredadas de la auditoría previa (documentadas en `docs/microservicios/`): sin modo envío gratis; IDs numéricos normales; `order_number` correlativo numérico; `delivery_addresses` en `shipment-service` como fuente única de direcciones; `review-service` fuera de alcance. Los DTOs de §4.4 ya están documentados en los 7 servicios (review excluido).

L3 — 2026-11-19 — Rama creada desde `main`: `feat/implementar-microservicios-scaffold`. El working tree arrastra sin commitear los 7 documentos de microservicios modificados por la auditoría/DTOs; se conservan y se incluirán en el primer work-unit que corresponda.

L4 — 2026-11-19 — Skills cargadas: `java-springboot` y `work-unit-commits` (más `cognitive-doc-design`/`documentation` si se toca documentación). Registry local en `.atl/skill-registry.md`.

L5 — 2026-11-19 — Exploración de patrones completada por `gentle-ai-explore`: Boot 4.1.1 / Java 17, POMs independientes con `spring-boot-starter-webmvc` (Boot 4), Testcontainers `@ServiceConnection`, gateway `spring.cloud.gateway.server.webmvc.routes[n]` con `lb://<spring.application.name>`, y los 4 scaffolds en Boot 4.1.2 con dependencias mínimas. Handoff recibido.

L6 — 2026-11-19 — Decisión del usuario (pregunta de alcance): "API + Kafka/outbox junto". Agrega S5b. Incluye consumidor `SHIPMENT_DISPATCHED` en `order-service` (T5, padre) y dependencias Kafka/outbox en los 4 servicios.

L7 — 2026-11-19 — Sonda de entorno: Java 17.0.21 disponible; Docker daemon NO responde (`npipe:////./pipe/dockerDesktopLinuxEngine`). Aplica S7: tests Spring se escriben pero no ejecutan localmente; verificación local = compilación + tests unitarios Mockito.

L8 — 2026-11-19 — Se lanzan writers para T1 y T2 en paralelo (superficies disjuntas: un directorio de servicio cada uno; límite de 2 background tasks). Skills por writer: `java-springboot`, `work-unit-commits`. El padre ejecuta T5 y posee git. T3 y T4 se lanzan al liberarse el cupo.

L9 — 2026-11-19 — T5 COMPLETADO (padre, inline, verificado): RED observado en `ShipmentDispatchedConsumerTest` (clase inexistente) → implementado `ShipmentDispatchedEvent` + `ShipmentDispatchedConsumer` → GREEN. Patrón idéntico a `PaymentApprovedConsumer`: `@KafkaListener` topic `shipment-events`, lee `type`, descarta malformados, idempotente (solo `PAID` → `SHIPPED`), lanza `IllegalStateException` si el pedido no existe para reintento de Kafka. Rutas gateway [5]–[8] agregadas (carts, customers, offers, shipments) con `lb://<service>`. README actualizado (diagrama, tabla de estado, mensajería, árbol, próximos pasos, nota Docker). Verificación: `order-service` `mvnw -q -DskipTests package` EXIT 0; `mvnw -q test -Dtest=ShipmentDispatchedConsumerTest,PaymentApprovedConsumerTest,OrderServiceTest,ProductClientTest` EXIT 0; `git diff --check` EXIT 0 (solo warnings LF→CRLF de archivos que los writers están tocando). Sin commit aún.

L10 — 2026-11-19 — Nota de superficie: los writers están modificando sus directorios en el mismo worktree (superficies disjuntas por directorio de servicio). `git status` ya muestra archivos nuevos en cart/customer mientras T5 se verificaba; sin solapamiento con order/gateway/README.
