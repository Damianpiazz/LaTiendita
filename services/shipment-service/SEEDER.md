# Database Seeder — shipment-service

Seeder independiente y determinista para la base de datos PostgreSQL de
`shipment-service` (`shipments`, `delivery_addresses`). Genera 1.000 envíos
sintéticos, una dirección de entrega por cliente y eventos de infraestructura
`outbox_events` ya marcados como `SENT` para comprobar la cadena sin republicar
mensajes.

## Qué siembra

| Tabla | Volumen por defecto | Regla principal |
|---|---:|---|
| `shipments` | 1.000 | `order_id = 9200000..9200999` (UNIQUE), `customer_id` sintético, estados `AWAITING_PAYMENT`/`PENDING_DISPATCH`/`DISPATCHED`/`DELIVERED`/`CANCELLED` por `i % 5`, `dispatched_at` y `delivered_at` coherentes con el estado, `version = 0` |
| `delivery_addresses` | 1.000 | Una dirección por `customer_id`, `is_default = true`, teléfono y destinatario sintéticos |
| `outbox_events` | 800 | `topic = shipment-events`, `status = SENT`, `attempts = 1`, `sent_at NOT NULL`, payload JSON válido con `source = SEED`; `SHIPMENT_DISPATCHED` (1 por despachado), `SHIPMENT_DELIVERED` (1 por entregado) y `SHIPMENT_CANCELLED` (1 por cancelado) |
| `seeder_markers` | 1 | `service = shipment`, `version = shipment-v1` |

No hay DELETE masivo: la limpieza sólo afecta a filas `SEED-CUST-%`, sus eventos
y el marcador. El `order_id` es UNIQUE, por eso los envíos sintéticos usan la
misma base numérica que `order-service` y no colisionan con datos reales.

## Identificadores cross-service

No hay FK entre bases de datos. Las referencias se reconstruyen sin consultar
otras BDs ni hacer HTTP:

```
customerId  = "<SEEDER_EXTERNAL_ID_PREFIX>-CUST-" + zeroPad(SEEDER_EXTERNAL_ID_BASE + i + 1, 6)
orderId     = SEEDER_EXTERNAL_ID_BASE + 200000 + i
shipmentId  = SEEDER_EXTERNAL_ID_BASE + 500000 + i
addressId   = SEEDER_EXTERNAL_ID_BASE + 600000 + i
```

Con los valores por defecto (`SEED`, base `9000000`): el primer envío usa
`orderId = 9200000`, `shipmentId = 9500000`, `addressId = 9600000` y
`customerId = SEED-CUST-9000001`. La clave del cliente coincide con la de
`order-service` y `customer-service`, así que los datos sintéticos se pueden
cruzar entre bases sin llamadas externas.

## Configuración

| Variable | Default | Descripción |
|---|---:|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner` |
| `SEEDER_COUNT` | `1000` | Número de envíos y direcciones |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño del batch JDBC |
| `SEEDER_RANDOM_SEED` | `20261122` | Semilla de `java.util.Random` (costos y teléfonos) |
| `SEEDER_INCLUDE_INFRA` | `true` | Genera eventos `SENT` |
| `SEEDER_EXTERNAL_ID_PREFIX` | `SEED` | Prefijo de claves sintéticas |
| `SEEDER_EXTERNAL_ID_BASE` | `9000000` | Base numérica de IDs externos |
| `SEEDER_CLEAN_BEFORE_RUN` | `false` | Borra sólo filas sintéticas antes de sembrar |
| `SEEDER_ALLOW_CLEAN` | `false` | Segundo interruptor obligatorio para limpiar |
| `SEEDER_ALLOWED_PROFILES` | `dev,test,local` | Perfiles permitidos |
| `SEEDER_MARKER_ENABLED` | `true` | Habilita el marcador de idempotencia |

Perfiles `prod`, `production` y `prd` están prohibidos aunque se active el seeder.
El perfil `default` tampoco está permitido: hay que fijar explícitamente
`SPRING_PROFILES_ACTIVE=local`.

## Comandos reales

```powershell
# Tests (unitarios + Testcontainers PostgreSQL 17; requiere Docker corriendo)
mvn test "-Dtest=ShipmentDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest"

# Build del jar
mvn -DskipTests package

# Ejecución contra la BD local del stack
java -jar target/shipmentService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/shipment `
  --spring.datasource.username=tienda `
  --spring.datasource.password=devpassword123 `
  --spring.kafka.listener.auto-startup=false `
  --eureka.client.enabled=false `
  --seeder.enabled=true `
  --seeder.count=1000 `
  --seeder.batch-size=500
```

La segunda ejecución con el mismo `version` devuelve
`seed.skipped ... reason=already_seeded` y no modifica datos.

Para regenerar sólo las filas sintéticas (nunca las reales):

```
--seeder.clean-before-run=true --seeder.allow-clean=true
```

Si el marcador `shipment-v1` ya existe, la ejecución se omite antes de limpiar.
Para forzar una regeneración, elimine ese marcador o añada
`--seeder.marker-enabled=false`.

## Verificación en PostgreSQL

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d shipment -Atc "
SELECT 'shipments=' || count(*) FROM shipments;
SELECT 'seeded=' || count(*) FROM shipments WHERE customer_id LIKE 'SEED-CUST-%';
SELECT 'distinct_orders=' || count(DISTINCT order_id) FROM shipments WHERE customer_id LIKE 'SEED-CUST-%';
SELECT 'addresses=' || count(*) FROM delivery_addresses WHERE customer_id LIKE 'SEED-CUST-%';
SELECT 'events=' || count(*) FROM outbox_events WHERE topic='shipment-events';
SELECT 'events_sent=' || count(*) FROM outbox_events WHERE status='SENT' AND sent_at IS NOT NULL;
SELECT 'markers=' || count(*) FROM seeder_markers WHERE service='shipment' AND version='shipment-v1';
"
```

## Métricas y logs

- Logs: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.
- Métricas Micrometer: `seeder.rows{service=shipment,entity=shipments|addresses|events}`,
  `seeder.runs{service=shipment,outcome=skipped|failed}`,
  `seeder.duration{service=shipment}`, `seeder.last.success{service=shipment}`.

El marcador y la métrica de éxito sólo se escriben después de verificar conteos,
UNIQUE, estados, rangos de fecha, direcciones y eventos `SENT` directamente en
PostgreSQL.
