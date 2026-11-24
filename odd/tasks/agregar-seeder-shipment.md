# Agregar seeder a shipment-service

Objective: completar el único servicio con base de datos sin seeder, usando el patrón de order-service. Branch: main (según preferencia del usuario). Entrega: sin commits, sin push. Test runner: `./mvnw verify` en `services/shipment-service` (Docker requerido para Testcontainers).

## Specs

S1. "Podrias terminar los seeders en los services? Que tenga un formato parecido a los que ya hay" — alcance confirmado: "los que falten". Único servicio con BD y sin seeder: `services/shipment-service`; no crear seeders para `gateway` ni `eureka-server` (sin base de datos). Los 7 seeders existentes quedan intactos.

S2. Formato canónico de `order-service`: `DatabaseSeederRunner` (`ApplicationRunner`, `Ordered`, `@ConditionalOnProperty(prefix="seeder", name="enabled", havingValue="true")`), `DatabaseSeederService.seed()`, `SeedExecution`, `SeederProperties` (11 claves `seeder.*`), `SeederProfileGuard` (`validateClean()`), `OrderDataGenerator` equivalente, `SeederJdbcRepository` con tabla `seeder_markers`, `SeederValidator`, `SeederMetrics`. Idempotencia por marcador `shipment-v1`, datos sintéticos con prefijo `SEED-`, eventos outbox pre-marcados `SENT` y sin llamadas externas.

S3. Incluir tests (generador, guard de perfiles, integración con Testcontainers PostgreSQL 17) y `services/shipment-service/SEEDER.md` con el mismo formato que `services/order-service/SEEDER.md`. No cambiar `runtime/docker/compose.yaml`: el seeder se activa con `docker compose run -e SEEDER_ENABLED=true`.

S4. "No, dejar sin commitear" — no ejecutar `git add`, `git commit` ni push. Los 342 cambios previos del reordenamiento de carpetas quedan fuera de este trabajo.

## Tasks

T1 — S1-S2 — inline — completado — seeder principal en `services/shipment-service/src/main/java/com/latiendita/shipmentService/seeder/` (runner, service, generator, repository, validator, metrics, guard, properties, execution y 4 modelos) más 11 claves `seeder.*` en `application.properties`. Sin commit.
T2 — S3 — inline — completado — 4 tests en `services/shipment-service/src/test/java/com/latiendita/shipmentService/seeder/`. RED observado por símbolos ausentes; 9/9 tests no-Docker en verde. Sin commit.
T3 — S3-S4 — inline — pendiente (Needs your decision: Docker no está corriendo) — `services/shipment-service/SEEDER.md` escrito y leído; falta ejecutar `DatabaseSeederIntegrationTest` con Testcontainers y el `./mvnw verify` completo. Sin commit.

## Log

L1 — 2026-11-24 — Pedido original verbatim: "Podrias terminar los seeders en los services? Que tenga un formato parecido a los que ya hay"

L2 — 2026-11-24 — Respuesta al alcance: "los que falten".

L3 — 2026-11-24 — Respuesta sobre historial: "No, dejar sin commitear".

L4 — 2026-11-24 — Mapa read-only de `gentle-ai-explore`: los 7 seeders existentes están completos; `order-service` es el patrón moderno; `shipment-service` usa JPA/Hibernate con `ddl-auto=update`, tablas `shipments`, `delivery_addresses`, `outbox_events`, `shipments.order_id` UNIQUE, `@Version` NOT NULL, sin FK cross-service. El repositorio no tiene `SEEDER_*` en compose y usa un único `Dockerfile.spring` parametrizado.

L5 — 2026-11-24 — Decisión de implementación: `customerId` se guarda como clave literal `<PREFIX>-CUST-<base+i+1>` en vez de UUID derivado, para que el SQL de validación y limpieza (`customer_id LIKE 'SEED-CUST-%'`) sea coherente con el prefijo aislable. El test `shouldDeriveCrossServiceIdsFromPrefixAndBase` se ajustó a esa clave. `reference_notes` mapea la columna real del entity (`references` es palabra reservada en PostgreSQL). Costos y teléfonos usan `java.util.Random` para no agregar Datafaker al pom.

L6 — 2026-11-24 — Verificación: baseline `.\mvnw -q -DskipTests compile` exit=0; tests nuevos RED por `cannot find symbol` antes de la implementación. Tras implementar, `.\mvnw -q test "-Dtest=ShipmentDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest"` → 10 tests, 0 failures, 1 error: `IllegalStateException: Could not find a valid Docker environment` en `DatabaseSeederIntegrationTest`. `docker info` confirma que el daemon no responde en `npipe:////./pipe/dockerDesktopLinuxEngine`. Suite unitaria ampliada (7 clases, sin Testcontainers) exit=0.

L7 — 2026-11-24 — Estado de historial: `git status --short` = 2 M, 309 D, 36 ??; `git diff --cached --stat` vacío. Sin `git add`, sin `git commit`, sin push. El error de Docker es ambiental y no cambia el código; queda registrado como único bloqueo para cerrar T3.

L8 — 2026-11-24 — Usuario: "Lo de docker lo probamos despues". T3 queda pendiente por decisión explícita del usuario; no se corrieron verificaciones adicionales ni se hicieron cambios de código.
