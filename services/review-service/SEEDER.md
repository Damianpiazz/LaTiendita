# Database Seeder — review-service

Seeder independiente y determinista para la base de datos PostgreSQL de
`review-service` (`review`). Genera 1.000 reseñas, 120 votos útiles, 436
agregados `product_ratings` y 600 eventos de infraestructura (`outbox_events`)
marcados como `SENT` para no republicar mensajes.

## Qué siembra

| Tabla | Volumen por defecto | Regla principal |
|---|---:|---|
| `reviews` | 1.000 | `id` UUID sintético, `customer_id` UUID derivado del cliente sintético, `product_id`/`order_id` reconstruidos de order-service, `rating = 1 + (i % 5)`, estados `approved`/`pending`/`rejected`, motivo obligatorio en rechazadas |
| `review_votes` | 120 | Un voto para cada 5ª reseña aprobada; `(review_id, customer_id)` UNIQUE; votante distinto del autor; `helpful_votes` de la reseña igual al número real de votos |
| `product_ratings` | 436 | Un agregado por producto con al menos una reseña aprobada; `average` con 2 decimales, `review_count`, `distribution` JSONB con claves `"1".."5"` |
| `outbox_events` | 600 | Un `REVIEW_APPROVED` por reseña aprobada, `topic = review-events`, `status = SENT`, `attempts = 1`, `sent_at NOT NULL`, payload con `source = SEED` |
| `seeder_markers` | 1 | `service = review`, `version = review-v1` |

### Distribución por defecto (`count=1000`)

| Valor | Cantidad |
|---|---:|
| `approved` / `pending` / `rejected` | 600 / 200 / 200 |
| Ratings 1 / 2 / 3 / 4 / 5 | 200 / 200 / 200 / 200 / 200 |
| Reseñas con un voto útil | 120 |
| Productos con agregado | 436 |
| Eventos `REVIEW_APPROVED` | 600 |

## Coherencia con order-service

`ReviewService.create` sólo acepta pedidos con estado `PAID`, `SHIPPED` o
`DELIVERED`, y el pedido debe contener el producto reseñado. En order-service,
con `count=1000`, eso significa `order_index % 10 >= 4`; además cada pedido
tiene `1 + (order_index % 3)` ítems con productos
`(order_index + j) % 1000`.

El seeder replica exactamente ese convenio:

```
pedido elegible  = i tal que i % 10 >= 4
producto         = 9100000 + ((i + j) % 1000), j < 1 + (i % 3)
customerId       = UUID.nameUUIDFromBytes((prefix + "-CUST-" + zeroPad(base + i + 1, 6)).getBytes(UTF_8))
orderId          = 9200000 + i
reviewId         = UUID.nameUUIDFromBytes((prefix + "-REVIEW-" + zeroPad(base + n + 1, 6)).getBytes(UTF_8))
voterId          = UUID.nameUUIDFromBytes((prefix + "-VOTER-" + zeroPad(base + 1000000 + n + 1, 6)).getBytes(UTF_8))
```

donde `n` es el índice de la reseña (0..999) y `i` el índice del pedido elegible.
La primera reseña usa el pedido `9200004`, el producto `9100004` y el cliente
`UUID` derivado de `SEED-CUST-9000005`.

No hay FK entre bases de datos: las referencias se reconstruyen sin consultar
otras BDs ni hacer HTTP.

## Configuración

| Variable | Default | Descripción |
|---|---:|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner` |
| `SEEDER_COUNT` | `1000` | Número de reseñas |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño del batch JDBC |
| `SEEDER_RANDOM_SEED` | `20261122` | Semilla de Datafaker (título y cuerpo) |
| `SEEDER_INCLUDE_INFRA` | `true` | Genera eventos `SENT` en `outbox_events` |
| `SEEDER_EXTERNAL_ID_PREFIX` | `SEED` | Prefijo de claves sintéticas |
| `SEEDER_EXTERNAL_ID_BASE` | `9000000` | Base numérica de IDs externos |
| `SEEDER_CLEAN_BEFORE_RUN` | `false` | Borra sólo filas sintéticas antes de sembrar |
| `SEEDER_ALLOW_CLEAN` | `false` | Segundo interruptor obligatorio para limpiar |
| `SEEDER_ALLOWED_PROFILES` | `dev,test,local` | Perfiles permitidos |
| `SEEDER_MARKER_ENABLED` | `true` | Habilita el marcador de idempotencia |

Perfiles `prod`, `production` y `prd` están prohibidos aunque se active el seeder.
El perfil `default` tampoco está permitido.

No hay DELETE masivo: la limpieza afecta sólo a reseñas con
`order_id 9200000..9200999`, sus votos, agregados con `product_id
9100000..9100999`, eventos con `"source":"SEED"` y el marcador.

## Comandos reales

```powershell
# Tests del seeder (unitarios + Testcontainers PostgreSQL 17)
mvn test "-Dtest=ReviewDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest"

# Suite completa
mvn test

# Build del jar
mvn -DskipTests package

# Ejecución contra la BD local del stack
java -jar target/reviewService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/review `
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

Si el marcador `review-v1` ya existe, la ejecución se omite antes de limpiar.
Para forzar una regeneración, elimine ese marcador o añada
`--seeder.marker-enabled=false`.

## Verificación en PostgreSQL

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d review -Atc "
SELECT 'reviews=' || count(*) FROM reviews;
SELECT 'approved=' || count(*) FROM reviews WHERE status='approved';
SELECT 'pending=' || count(*) FROM reviews WHERE status='pending';
SELECT 'rejected=' || count(*) FROM reviews WHERE status='rejected';
SELECT 'votes=' || count(*) FROM review_votes;
SELECT 'self_votes=' || count(*) FROM review_votes v
  JOIN reviews r ON r.id=v.review_id WHERE r.customer_id=v.customer_id;
SELECT 'product_ratings=' || count(*) FROM product_ratings
  WHERE product_id BETWEEN 9100000 AND 9100999;
SELECT 'outbox=' || count(*) FROM outbox_events WHERE payload LIKE '%SEED%';
SELECT 'sent=' || count(*) FROM outbox_events WHERE payload LIKE '%SEED%'
  AND status='SENT' AND sent_at IS NOT NULL AND attempts=1;
SELECT 'markers=' || count(*) FROM seeder_markers
  WHERE service='review' AND version='review-v1';
"
```

## Métricas y logs

- Logs: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.
- Métricas Micrometer: `seeder.rows{service=review,entity=reviews|review_votes|product_ratings|events}`,
  `seeder.runs{service=review,outcome=skipped|failed}`,
  `seeder.duration{service=review}`, `seeder.last.success{service=review}`.

El marcador y la métrica de éxito sólo se escriben después de verificar conteos,
UNIQUE, estados, ratings, motivos de moderación, votos, agregados JSONB, topics,
estados `SENT` y fechas de envío directamente en PostgreSQL.
