# Database seeding — product-service

Seeder independiente, determinista e idempotente para la BD `product` (PostgreSQL).
No consulta otros microservicios ni otras bases: las referencias cruzadas usan IDs
sintéticos `SEED-*` reservados desde `SEEDER_EXTERNAL_ID_BASE`.

## Qué siembra

| Entidad | Filas por defecto | Claves / relaciones |
|---|---:|---|
| `categories` | 20 | `name` y `slug` únicos, nombres `SEED-CAT-000001 …` |
| `products` | `SEEDER_COUNT` (1000) | `sku` y `slug` únicos, `category_id` → categoría sintética, JSON en `*_json` |

IDs: categorías `base+1..20`, productos `base+100000..base+100000+count-1`.
Con `SEEDER_EXTERNAL_ID_BASE=9000000`, los productos van de `9100001` a `9101000`.
Las secuencias IDENTITY se ajustan con `setval` al final para no colisionar.

No hay tablas de infraestructura (`outbox_events`, `payment_events`) en esta BD;
`SEEDER_INCLUDE_INFRA` se registra en el marcador pero no inserta nada aquí.

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner`. |
| `SEEDER_COUNT` | `1000` | Número de productos. |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño de lote JDBC (y de Hibernate). |
| `SEEDER_RANDOM_SEED` | `12345` | Semilla de Datafaker; misma semilla ⇒ mismo dataset. |
| `SEEDER_CLEAN_BEFORE_RUN` | `false` | Borra **sólo** filas `SEED-%` y los marcadores. |
| `SEEDER_ALLOW_CLEAN` | `false` | Segundo interruptor obligatorio para limpiar. |
| `SEEDER_ALLOWED_PROFILES` | `dev,test,local` | Perfiles autorizados. Nunca `prod`. |
| `SEEDER_INCLUDE_INFRA` | `false` | Reservado para eventos (sin efecto en esta BD). |
| `SEEDER_EXTERNAL_ID_PREFIX` | `SEED` | Prefijo de IDs sintéticos. |
| `SEEDER_EXTERNAL_ID_BASE` | `9000000` | Base numérica de IDs. |
| `SEEDER_MARKER_ENABLED` | `true` | Usa `seeder_markers` para la idempotencia. |

**Guardas:** con perfil `prod`/`production`/`prd` el arranque falla aunque
`SEEDER_ENABLED=true`. Sin perfil activo (perfil `default`) también falla: hay que
pasar explícitamente `SPRING_PROFILES_ACTIVE=local`. La limpieza exige además
`SEEDER_ALLOW_CLEAN=true`.

## Ejecución real verificada

```powershell
mvn -f services/product-service/pom.xml -DskipTests package

java -jar services/product-service/target/productService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/product `
  --spring.datasource.username=tienda `
  --spring.datasource.password=devpassword123 `
  --eureka.client.enabled=false `
  --seeder.enabled=true --seeder.count=1000 --seeder.batch-size=500
```

Segunda ejecución (idempotencia): el mismo comando imprime
`seed.skipped ... reason=already_seeded` y no inserta nada.

También se puede ejecutar dentro del stack Docker:

```bash
docker compose -f runtime/docker/compose.yaml run --rm \
  -e SPRING_PROFILES_ACTIVE=local -e SEEDER_ENABLED=true \
  -e SEEDER_COUNT=1000 product-service
```

## Verificación sobre la BD real

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d product -Atc "
select 'categories', count(*) from categories
union all select 'products', count(*) from products
union all select 'seeded_products', count(*) from products where sku like 'SEED-SKU-%'
union all select 'distinct_sku', count(distinct sku) from products where sku like 'SEED-SKU-%'
union all select 'orphans', count(*) from products p
  left join categories c on c.id=p.category_id
  where p.sku like 'SEED-SKU-%' and c.id is null
union all select 'markers', count(*) from seeder_markers
  where service='product' and version='product-v1';"
```

Resultado real tras la ejecución: `categories=20`, `products=1000`,
`seeded_products=1000`, `distinct_sku=1000`, `orphans=0`, `markers=1`.

## Métricas y logs

- `seeder.rows{service=product,entity=categories|products}` — filas insertadas.
- `seeder.runs{service=product,outcome=skipped|failed}`.
- `seeder.duration{service=product}` — duración en milisegundos.
- `seeder.last.success{service=product}` — `1` sólo tras validar la BD y el marcador.
- Logs estructurados: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.

## Tests

```bash
mvn -f services/product-service/pom.xml \
  "-Dtest=ProductDataGeneratorTest,SeederProfileGuardTest,DatabaseSeederIntegrationTest" test
```

Cubren determinismo, volumen, UNIQUE, relaciones, JSON, guardas de perfil/limpieza,
idempotencia e integridad en PostgreSQL 17 vía Testcontainers (10 tests, 0 fallos).
