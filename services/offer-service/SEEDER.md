# Database Seeder — offer-service

Seeder independiente y determinista para la base de datos PostgreSQL de
`offer-service` (`offer`). Genera 1.000 promociones, 1.000 usos de cupón,
20 beneficios y 1.000 eventos de infraestructura (`outbox_events`) marcados como
`SENT` para no republicar mensajes.

## Qué siembra

| Tabla | Volumen por defecto | Regla principal |
|---|---:|---|
| `promotions` | 1.000 | `id` UUID sintético, `code = SEED-PROMO-000001..001000` (UNIQUE), `type` rotado `FEATURED`/`PERCENTAGE`/`FIXED_AMOUNT`, `scope` rotado `PRODUCT`/`CATEGORY`/`ORDER`, ventana válida (`ends_at > starts_at`), `priority 1..10`, `discount_value` sólo en promociones con descuento |
| `coupon_usages` | 1.000 | `id` UUID sintético, `promotion_id` de una promoción **no FEATURED**, `customer_sub = SEED-CUST-9000001..`, `order_id = 9200000+i`, `(promotion_id, order_id)` UNIQUE, FK real a `promotions` |
| `benefits` | 20 | `code = SEED-BENEFIT-000001..000020` (UNIQUE), `sort_order 1..20`, `active = true` |
| `outbox_events` | 1.000 | `topic = promotion-events`, `event_type = PROMOTION_CHANGED`, `status = SENT`, `attempts = 1`, `sent_at NOT NULL`, payload JSON con `source = SEED`. Un evento por promoción, `aggregate_id = promotionId` |
| `seeder_markers` | 1 | `service = offer`, `version = offer-v1` |

### Distribución por defecto (`count=1000`)

| Valor | Cantidad |
|---|---:|
| `FEATURED` / `PERCENTAGE` / `FIXED_AMOUNT` | 334 / 333 / 333 |
| `PRODUCT` / `CATEGORY` / `ORDER` | 334 / 333 / 333 |
| Promociones activas (`i % 10 != 9`) | 900 |
| Usos sobre `FEATURED` | 0 (regla de `CouponService.validate`) |

Los `FEATURED` no llevan `discount_value` (no descuentan, según
`PromotionEvaluationService.discountFor`); los otros llevan porcentaje 5–39 o
monto fijo 5.00–99.99. El payload del outbox replica `PromotionChangedEvent`
(`type`, `promotionId`, `scope`, `targetId`, `active`, `occurredAt`) y añade
`source: SEED` para poder identificarlo y limpiarlo.

No hay DELETE masivo: la limpieza sólo afecta a promociones `SEED-PROMO-%`, usos
`SEED-CUST-%`, beneficios `SEED-BENEFIT-%`, eventos con `"source":"SEED"` y el
marcador.

## Identificadores cross-service

No hay FK entre bases de datos. Las referencias se reconstruyen sin consultar
otras BDs ni hacer HTTP:

```
promotionId = UUID.nameUUIDFromBytes((prefix + "-PROMO-" + zeroPad(base + i + 1, 6)).getBytes(UTF_8))
usageId     = UUID.nameUUIDFromBytes((prefix + "-USAGE-" + zeroPad(base + i + 1, 6)).getBytes(UTF_8))
customerSub = "<prefix>-CUST-" + zeroPad(base + i + 1, 6)
orderId     = 9200000 + i
```

Con los valores por defecto (`SEED`, base `9000000`): la primera promoción es
`SEED-PROMO-000001` con id derivado de `SEED-PROMO-9000001`; su primer uso
referencia al cliente `SEED-CUST-9000001` y al pedido `9200000` (`SEED-ORD-9200000`
en order-service).

## Configuración

| Variable | Default | Descripción |
|---|---:|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner` |
| `SEEDER_COUNT` | `1000` | Número de promociones y usos |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño del batch JDBC |
| `SEEDER_RANDOM_SEED` | `20261122` | Semilla de Datafaker (descuentos) |
| `SEEDER_INCLUDE_INFRA` | `true` | Genera eventos `SENT` en `outbox_events` |
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
# Tests del seeder (unitarios + Testcontainers PostgreSQL 17)
mvn test "-Dtest=OfferDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest"

# Suite completa
mvn test

# Build del jar
mvn -DskipTests package

# Ejecución contra la BD local del stack
java -jar target/offerService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/offer `
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

Si el marcador `offer-v1` ya existe, la ejecución se omite antes de limpiar.
Para forzar una regeneración, elimine ese marcador o añada
`--seeder.marker-enabled=false`.

## Verificación en PostgreSQL

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d offer -Atc "
SELECT 'promotions=' || count(*) FROM promotions;
SELECT 'distinct_codes=' || count(DISTINCT code) FROM promotions WHERE code LIKE 'SEED-PROMO-%';
SELECT 'usages=' || count(*) FROM coupon_usages WHERE customer_sub LIKE 'SEED-CUST-%';
SELECT 'benefits=' || count(*) FROM benefits;
SELECT 'outbox=' || count(*) FROM outbox_events WHERE payload LIKE '%SEED%';
SELECT 'sent=' || count(*) FROM outbox_events WHERE payload LIKE '%SEED%'
  AND status='SENT' AND sent_at IS NOT NULL AND attempts=1;
SELECT 'orphan_usages=' || count(*) FROM coupon_usages u
  LEFT JOIN promotions p ON p.id=u.promotion_id WHERE p.id IS NULL;
SELECT 'featured_usages=' || count(*) FROM coupon_usages u
  JOIN promotions p ON p.id=u.promotion_id
  WHERE u.customer_sub LIKE 'SEED-CUST-%' AND p.type='FEATURED';
SELECT 'markers=' || count(*) FROM seeder_markers
  WHERE service='offer' AND version='offer-v1';
"
```

## Métricas y logs

- Logs: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.
- Métricas Micrometer: `seeder.rows{service=offer,entity=promotions|coupon_usages|benefits|events}`,
  `seeder.runs{service=offer,outcome=skipped|failed}`,
  `seeder.duration{service=offer}`, `seeder.last.success{service=offer}`.

El marcador y la métrica de éxito sólo se escriben después de verificar conteos,
UNIQUE, enums, scopes, ventanas, usos no FEATURED, FK, topics, estados `SENT` y
fechas de envío directamente en PostgreSQL.
