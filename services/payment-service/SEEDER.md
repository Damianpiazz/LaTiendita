# Database Seeder — payment-service

Seeder independiente y determinista para la base de datos PostgreSQL de
`payment-service` (`payment`). Genera 1.000 pagos sintéticos, 1.000 notificaciones
de webhook ya procesadas (`payment_events`) y 700 eventos de infraestructura
(`outbox_events`) marcados como `SENT` para no republicar mensajes.

## Qué siembra

| Tabla | Volumen por defecto | Regla principal |
|---|---:|---|
| `payments` | 1.000 | `id = 9400000..9400999`, `order_id = 9200000+i`, `external_reference = SEED-ORD-9200000+i`, `idempotency_key = SEED-IDEM-9400000..` (UNIQUE), `provider_payment_id = SEED-PAY-9400000..` (UNIQUE), `provider = MERCADO_PAGO`, `currency = ARS`, `amount > 0`, estados por `i % 10` |
| `payment_events` | 1.000 | `notification_id = SEED-NOTIF-9400000..` (UNIQUE), `provider = MERCADO_PAGO`, `signature_valid = true`, `status = PROCESSED`, `attempts = 1`, `processed_at NOT NULL`, `last_error = NULL` |
| `outbox_events` | 700 | `topic = payment-events`, `event_type = PAYMENT_APPROVED`/`PAYMENT_REJECTED`/`PAYMENT_CANCELLED`, `status = SENT`, `attempts = 1`, `sent_at NOT NULL`, payload JSON válido con `source = SEED`. No se generan eventos para `PENDING`/`EXPIRED` |
| `seeder_markers` | 1 | `service = payment`, `version = payment-v1` |

### Distribución de estados (`i % 10`, `count=1000`)

| Estado | Cantidad | `paid_at` | Evento outbox |
|---|---:|---|---|
| `APPROVED` | 400 | `updatedAt` | `PAYMENT_APPROVED` |
| `PENDING` | 100 | NULL | — |
| `REJECTED` | 200 | NULL | `PAYMENT_REJECTED` |
| `CANCELLED` | 100 | NULL | `PAYMENT_CANCELLED` |
| `EXPIRED` | 200 | NULL | — |

Los aprobados llevan `payment_method_id = SEED-MP`, `payment_type = credit_card`
y `providerPaymentId`; el resto lleva `status_detail` sintético y sin fecha de pago.

No hay DELETE masivo: la limpieza sólo afecta a pagos `SEED-IDEM-%`, sus
notificaciones `SEED-NOTIF-%`, sus eventos con `"source":"SEED"` y el marcador.

## Identificadores cross-service

No hay FK entre bases de datos. Las referencias se reconstruyen sin consultar
otras BDs ni hacer HTTP:

```
paymentId     = SEEDER_EXTERNAL_ID_BASE + 400000 + i
orderId       = SEEDER_EXTERNAL_ID_BASE + 200000 + i
orderNumber   = "<SEEDER_EXTERNAL_ID_PREFIX>-ORD-" + zeroPad(orderId, 6)
customerKey   = "<SEEDER_EXTERNAL_ID_PREFIX>-CUST-" + zeroPad(SEEDER_EXTERNAL_ID_BASE + i + 1, 6)
customerId    = UUID.nameUUIDFromBytes(customerKey.getBytes(UTF_8))
```

Con los valores por defecto (`SEED`, base `9000000`): el primer pago es
`9400000` para el pedido `9200000` / `SEED-ORD-9200000`, y el cliente
`SEED-CUST-9000001` → `9f09fe28-9e2f-38f0-a93e-7d8a6aeaf6df` (misma convención
que customer-service y order-service).

## Configuración

| Variable | Default | Descripción |
|---|---:|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner` |
| `SEEDER_COUNT` | `1000` | Número de pagos |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño del batch JDBC |
| `SEEDER_RANDOM_SEED` | `20261122` | Semilla de Datafaker (montos) |
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
mvn test "-Dtest=PaymentDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest"

# Build del jar
mvn -DskipTests package

# Ejecución contra la BD local del stack
java -jar target/paymentService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/payment `
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

Si el marcador `payment-v1` ya existe, la ejecución se omite antes de limpiar.
Para forzar una regeneración, elimine ese marcador o añada
`--seeder.marker-enabled=false`.

## Verificación en PostgreSQL

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d payment -Atc "
SELECT 'payments=' || count(*) FROM payments;
SELECT 'seeded=' || count(*) FROM payments WHERE idempotency_key LIKE 'SEED-IDEM-%';
SELECT 'distinct_idem=' || count(DISTINCT idempotency_key) FROM payments WHERE idempotency_key LIKE 'SEED-IDEM-%';
SELECT 'distinct_provider_payment=' || count(DISTINCT provider_payment_id) FROM payments WHERE idempotency_key LIKE 'SEED-IDEM-%';
SELECT 'approved=' || count(*) FROM payments WHERE idempotency_key LIKE 'SEED-IDEM-%' AND status='APPROVED';
SELECT 'events=' || count(*) FROM payment_events WHERE notification_id LIKE 'SEED-NOTIF-%';
SELECT 'processed=' || count(*) FROM payment_events WHERE notification_id LIKE 'SEED-NOTIF-%' AND status='PROCESSED';
SELECT 'outbox=' || count(*) FROM outbox_events WHERE payload LIKE '%\"source\":\"SEED\"%';
SELECT 'sent=' || count(*) FROM outbox_events WHERE payload LIKE '%\"source\":\"SEED\"%' AND status='SENT' AND sent_at IS NOT NULL;
SELECT 'markers=' || count(*) FROM seeder_markers WHERE service='payment' AND version='payment-v1';
"
```

## Métricas y logs

- Logs: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.
- Métricas Micrometer: `seeder.rows{service=payment,entity=payments|payment_events|events}`,
  `seeder.runs{service=payment,outcome=skipped|failed}`,
  `seeder.duration{service=payment}`, `seeder.last.success{service=payment}`.

El marcador y la métrica de éxito sólo se escriben después de verificar conteos,
UNIQUE, estados, montos, fechas de pago, notificaciones `PROCESSED` y eventos
`SENT` directamente en PostgreSQL.
