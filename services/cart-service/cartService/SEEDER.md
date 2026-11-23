# Database Seeder — cart-service

Seeder independiente y determinista para la base de datos PostgreSQL de
`cart-service` (`cart`). Genera 1.000 carritos sintéticos y 1.999 ítems.
Esta base **no tiene tablas de infraestructura** (`outbox_events` /
`payment_events`): cart-service sólo consume `product-events`, por lo que
`SEEDER_INCLUDE_INFRA` no produce filas aquí.

## Qué siembra

| Tabla | Volumen por defecto | Regla principal |
|---|---:|---|
| `carts` | 1.000 | `id` UUID determinista, `user_id` = `sub` del cliente sintético (UUID de customer-service), `version = 0`, `updated_at > created_at`, `user_id` UNIQUE |
| `cart_items` | 1.999 | 1–3 ítems por carrito (`i % 3`), `product_id` de product-service, `(cart_id, product_id)` UNIQUE, `quantity 1..10` (≤ 99), `unit_price > 0`, FK real `cart_id → carts(id)`, 100 ítems con `price_changed = true` |
| `seeder_markers` | 1 | `service = cart`, `version = cart-v1` |

No hay DELETE masivo: la limpieza sólo afecta a ítems cuyo `product_title`
empieza con `Producto sintético <prefijo>-SKU-`, a los carritos generados y al
marcador.

## Identificadores cross-service

No hay FK entre bases de datos. Las referencias se reconstruyen sin consultar
otras BDs ni hacer HTTP:

```
cartId    = UUID.nameUUIDFromBytes((prefix + "-CART-" + zeroPad(base + i + 1, 6)).getBytes(UTF_8))
userId    = UUID.nameUUIDFromBytes((prefix + "-CUST-" + zeroPad(base + i + 1, 6)).getBytes(UTF_8))
productId = 9100000 + productIndex     // productIndex = (i * itemsInCart + j) % 1000
sku       = "<prefix>-SKU-" + zeroPad(100000 + productIndex, 6)
```

Con los valores por defecto (`SEED`, base `9000000`): el primer carrito pertenece
al cliente `SEED-CUST-9000001` → `9f09fe28-9e2f-38f0-a93e-7d8a6aeaf6df` y su
primer ítem referencia el producto `9100000` / `SEED-SKU-100000`.

## Configuración

| Variable | Default | Descripción |
|---|---:|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner` |
| `SEEDER_COUNT` | `1000` | Número de carritos |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño del batch JDBC |
| `SEEDER_RANDOM_SEED` | `20261122` | Semilla de Datafaker (precios/cantidades) |
| `SEEDER_CLEAN_BEFORE_RUN` | `false` | Borra sólo filas sintéticas antes de sembrar |
| `SEEDER_ALLOW_CLEAN` | `false` | Segundo interruptor obligatorio para limpiar |
| `SEEDER_ALLOWED_PROFILES` | `dev,test,local` | Perfiles permitidos |
| `SEEDER_EXTERNAL_ID_PREFIX` | `SEED` | Prefijo de claves sintéticas |
| `SEEDER_EXTERNAL_ID_BASE` | `9000000` | Base numérica de IDs externos |
| `SEEDER_MARKER_ENABLED` | `true` | Habilita el marcador de idempotencia |
| `SEEDER_INCLUDE_INFRA` | `true` | Aceptado por uniformidad; sin efecto (no hay outbox) |

Perfiles `prod`, `production` y `prd` están prohibidos aunque se active el seeder.
El perfil `default` tampoco está permitido.

## Comandos reales

```powershell
# Tests del seeder (unitarios + Testcontainers PostgreSQL 17)
mvn test "-Dtest=CartDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest"

# Build del jar
mvn -DskipTests package

# Ejecución contra la BD local del stack
java -jar target/cartService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/cart `
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

Si el marcador `cart-v1` ya existe, la ejecución se omite antes de limpiar.
Para forzar una regeneración, elimine ese marcador o añada
`--seeder.marker-enabled=false`.

## Verificación en PostgreSQL

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d cart -Atc "
SELECT 'carts=' || count(*) FROM carts;
SELECT 'items=' || count(*) FROM cart_items;
SELECT 'distinct_users=' || count(DISTINCT user_id) FROM carts;
SELECT 'distinct_pairs=' || count(DISTINCT cart_id || ':' || product_id) FROM cart_items;
SELECT 'orphans=' || count(*) FROM cart_items i
  LEFT JOIN carts c ON c.id = i.cart_id WHERE c.id IS NULL;
SELECT 'bad_qty=' || count(*) FROM cart_items WHERE quantity < 1 OR quantity > 99;
SELECT 'bad_price=' || count(*) FROM cart_items WHERE unit_price <= 0;
SELECT 'markers=' || count(*) FROM seeder_markers
  WHERE service='cart' AND version='cart-v1';
"
```

## Métricas y logs

- Logs: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.
- Métricas Micrometer: `seeder.rows{service=cart,entity=carts|cart_items}`,
  `seeder.runs{service=cart,outcome=skipped|failed}`,
  `seeder.duration{service=cart}`, `seeder.last.success{service=cart}`.

El marcador y la métrica de éxito sólo se escriben después de verificar conteos,
UNIQUE, FK, cantidades, precios, versiones y banderas `price_changed` directamente
en PostgreSQL.

## Nota sobre la suite existente

Los tests del seeder pasan. La suite completa de `cart-service` tiene 4 fallos
**preexistentes en HEAD** (verificados sin los cambios del seeder) en
`CartApiIntegrationTest`: `ObjectOptimisticLockingFailureException` → HTTP 409
al crear/leer carritos con `@Version Long` inicializado explícitamente. No son
causados por el seeder y no se modificó esa lógica.
