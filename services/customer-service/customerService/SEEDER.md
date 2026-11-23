# Database Seeder — customer-service

Seeder independiente y determinista para la base de datos PostgreSQL de
`customer-service` (`customer`). Genera 1.000 clientes sintéticos y, por defecto,
1.000 eventos `outbox_events` ya marcados como `SENT` para poder comprobar que
toda la cadena de infraestructura funciona sin republicar mensajes.

## Qué siembra

| Tabla | Volumen por defecto | Regla principal |
|---|---:|---|
| `customers` | 1.000 | `authorizer_sub = SEED-SUB-000001..0001000`, `email = seed+000001@example.test..`, `status` `ACTIVE`/`SUSPENDED`, `deleted_at = NULL`, `newsletter_opt_in => newsletter_consent_at` |
| `outbox_events` | 1.000 | `topic = customer-events`, `event_type = CUSTOMER_CREATED`, `status = SENT`, `attempts = 1`, `sent_at NOT NULL`, payload JSON válido con `source = SEED` |
| `seeder_markers` | 1 | `service = customer`, `version = customer-v1` |

Los 2 clientes reales (`ana.prueba@latiendita.test`, `elena.admin@latiendita.test`)
y sus 2 eventos no se tocan: la limpieza y la validación sólo afectan a filas con
el prefijo sintético.

## Identificadores cross-service

No hay FK entre bases de datos. Los otros servicios pueden reconstruir el mismo
UUID sin consultar esta BD:

```
customerKey = "<SEEDER_EXTERNAL_ID_PREFIX>-CUST-" + zeroPad(SEEDER_EXTERNAL_ID_BASE + i + 1, 6)
customerId  = UUID.nameUUIDFromBytes(customerKey.getBytes(UTF_8))
```

Con los valores por defecto (`SEED`, base `9000000`), el primer cliente es
`SEED-CUST-9000001` → `9f09fe28-9e2f-38f0-a93e-7d8a6aeaf6df`.

## Configuración

| Variable | Default | Descripción |
|---|---:|---|
| `SEEDER_ENABLED` | `false` | Activa el `ApplicationRunner` |
| `SEEDER_COUNT` | `1000` | Número de clientes/eventos |
| `SEEDER_BATCH_SIZE` | `500` | Tamaño del batch JDBC |
| `SEEDER_RANDOM_SEED` | `20261122` | Semilla de Datafaker |
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
mvn test "-Dtest=com.latiendita.customerService.seeder.*Test"

# Build del jar
mvn -DskipTests package

# Ejecución contra la BD local del stack
java -jar target/customerService-0.0.1-SNAPSHOT.jar `
  --spring.main.web-application-type=none `
  --spring.profiles.active=local `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/customer `
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

Si el marcador `customer-v1` ya existe, la ejecución se omite antes de limpiar.
Para forzar una regeneración, elimine ese marcador o añada
`--seeder.marker-enabled=false`.

## Verificación en PostgreSQL

```bash
docker exec latiendita-docker-postgres-1 psql -U tienda -d customer -Atc "
SELECT 'customers=' || count(*) FROM customers;
SELECT 'seeded=' || count(*) FROM customers WHERE authorizer_sub LIKE 'SEED-SUB-%';
SELECT 'distinct_sub=' || count(DISTINCT authorizer_sub) FROM customers WHERE authorizer_sub LIKE 'SEED-SUB-%';
SELECT 'distinct_email=' || count(DISTINCT email) FROM customers WHERE email LIKE 'seed+%@example.test';
SELECT 'events_sent=' || count(*) FROM outbox_events WHERE payload LIKE '%\"source\":\"SEED\"%' AND status='SENT' AND sent_at IS NOT NULL;
SELECT 'real_customers=' || count(*) FROM customers WHERE email LIKE '%@latiendita.test';
SELECT 'markers=' || count(*) FROM seeder_markers WHERE service='customer' AND version='customer-v1';
"
```

## Métricas y logs

- Logs: `seed.completed`, `seed.skipped`, `seed.cleaned`, `seed.failed`.
- Métricas Micrometer: `seeder.rows{service=customer,entity=customers|events}`,
  `seeder.runs{service=customer,outcome=skipped|failed}`,
  `seeder.duration{service=customer}`, `seeder.last.success{service=customer}`.

El marcador y la métrica de éxito sólo se escriben después de verificar conteos,
UNIQUE, estados, consentimiento de newsletter y eventos `SENT` directamente en PostgreSQL.
