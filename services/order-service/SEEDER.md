# Database Seeder — order-service

Seeder independiente y determinista para la base de datos PostgreSQL de
`order-service` (`orders`). Genera 1.000 pedidos sintéticos, sus ítems y eventos
de infraestructura `outbox_events` ya marcados como `SENT` para poder comprobar
que la cadena funciona sin republicar mensajes.

## Qué siembra

| Tabla | Volumen por defecto | Regla principal |
|---|---:|---|
| `orders` | 1.000 | `order_number = SEED-ORD-9200000..9200999` (UNIQUE), `customer_id` UUID sintético, estados `CANCELLED`/`PENDING`/`PAID`/`SHIPPED`/`DELIVERED` por `i % 10`, `cancel_reason` en cancelados, `refund_required` en cancelados pares, `total = SUM(subtotal)` |
| `order_items` | 1.999 | FK real `order_id → orders(id)`, `product_id` externo, `quantity >= 1`, `subtotal = unit_price * quantity`, sin productos repetidos por pedido |
| `outbox_events` | 2.500 | `topic = order-events`, `status = SENT`, `attempts = 1`, `sent_at NOT NULL`, payload JSON válido con `source = SEED`; `ORDER_CREATED` + transiciones de estado + `ORDER_CANCELLED` + `REFUND_REQUESTED` |
| `seeder_markers` | 1 | `service = order`, `version = order-v1` |

No hay DELETE masivo: la limpieza sólo afecta a pedidos `SEED-ORD-%`, sus ítems,
sus eventos y el marcador.

## Identificadores cross-service

No hay FK entre bases de datos. Las referencias se reconstruyen sin consultar
otras BDs ni hacer HTTP:

```
customerKey = "<SEEDER_EXTERNAL_ID_PREFIX>-CUST-" + zeroPad(SEEDER_EXTERNAL_ID_BASE + i + 1, 6)
customerId  = UUID.nameUUIDFromBytes(customerKey.getBytes(UTF_8))
productId   = SEEDER_EXTERNAL_ID_BASE + 100000 + i
sku         = "<SEEDER_EXTERNAL_ID_PREFIX>-SKU-" + zeroPad(100000 + i, 6)
```

Con los valores por defecto (`SEED`, base `9000000`): el primer cliente es
`SEED-CUST-9000001` → `9f09fe28-9e2f-38f0-a93e-7d8a6aeaf6df`, y el primer
producto `9100000` / `SEED-SKU-100000` (convención real de product-service).

## Configuración

| Variable | Default | Descripción |
|---|---:|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner` |
| `SEEDER_COUNT` | `1000` | Número de pedidos |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño del batch JDBC |
| `SEEDER_RANDOM_SEED` | `20261122` | Semilla de Datafaker (precios/cantidades) |
| `SEEDER_INCLUDE_INFRA` | `true` | Genera eventos `SENT` |
| `SEEDER_EXTERNAL_ID_PREFIX` | `SEED` | Prefijo de claves sintéticas |
| `SEEDER_EXTERNAL_ID_BASE` | `9000000` | Base numérica de IDs externos |
| `SEEDER_CLEAN_BEFORE_RUN` | `false` | Borra sólo filas sintéticas antes de sembrar |
| `SEEDER_ALLOW_CLEAN` | `false` | Segundo interruptor obligatorio para limpiar |
| `SEEDER_ALLOWED_PROFILES` | `dev,test,local` | Perfiles permitidos |
| `SEEDER_MARKER_ENABLED` | `true` | Habilita el marcador de idempotencia |

Perfiles `prod`, `production` y `prd` están prohibidos aunque se active el seeder.
El perfil `default` tampoco está permitido.

## Comandos reales

```powershell
# Tests (unitarios + Testcontainers PostgreSQL 17)
mvn test "-Dtest=OrderDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest"

# Build del jar
mvn -DskipTests package

# Ejecución contra la BD local del stack
java -jar target/orderService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/orders `
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

Si el marcador `order-v1` ya existe, la ejecución se omite antes de limpiar.
Para forzar una regeneración, elimine ese marcador o añada
`--seeder.marker-enabled=false`.

## Verificación en PostgreSQL

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d orders -Atc "
SELECT 'orders=' || count(*) FROM orders;
SELECT 'seeded=' || count(*) FROM orders WHERE order_number LIKE 'SEED-ORD-%';
SELECT 'distinct_numbers=' || count(DISTINCT order_number) FROM orders;
SELECT 'items=' || count(*) FROM order_items;
SELECT 'orphans=' || count(*) FROM order_items i LEFT JOIN orders o ON o.id=i.order_id WHERE o.id IS NULL;
SELECT 'bad_totals=' || count(*) FROM (
  SELECT o.id FROM orders o JOIN order_items i ON i.order_id=o.id
  WHERE o.order_number LIKE 'SEED-ORD-%'
  GROUP BY o.id, o.total HAVING o.total <> SUM(i.subtotal)) x;
SELECT 'events=' || count(*) FROM outbox_events;
SELECT 'events_sent=' || count(*) FROM outbox_events WHERE status='SENT' AND sent_at IS NOT NULL;
SELECT 'markers=' || count(*) FROM seeder_markers WHERE service='order' AND version='order-v1';
"
```

## Métricas y logs

- Logs: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.
- Métricas Micrometer: `seeder.rows{service=order,entity=orders|items|events}`,
  `seeder.runs{service=order,outcome=skipped|failed}`,
  `seeder.duration{service=order}`, `seeder.last.success{service=order}`.

El marcador y la métrica de éxito sólo se escriben después de verificar conteos,
UNIQUE, FK, totales, estados, reglas de cancelación y eventos `SENT` directamente
en PostgreSQL.
