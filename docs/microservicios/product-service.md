# Microservicio: `product-service`


## 1. Responsabilidad

Gestiona el catálogo de la tienda: productos y categorías (ABM, búsqueda, filtrado, paginación y ordenamiento) y el servicio de las imágenes de producto a través de un proxy de Azure Blob Storage. Es la única fuente de verdad sobre datos de catálogo; otros servicios (p. ej. `order-service`) la consultan por HTTP pero nunca almacenan copias propias más allá de la instantánea congelada en los ítems de un pedido. No gestiona reseñas ni valoraciones (las maneja `review-service`), ni carrito/pedidos (`order-service`), ni usuarios/autenticación (`authorizer` + gateway), ni pagos (`payment-service`).

---

## 2. Endpoints

Todas las rutas son relativas a la raíz del servicio (ej. `http://productService:8080`). El gateway enruta `/api/products/**` y `/api/categories/**` hacia este servicio.

> **Auth:** el servicio NO valida JWT ni roles; la identidad llega en headers `X-User-Id`, `X-User-Email`, `X-User-Roles` inyectados por el gateway. Los `GET` son públicos y las operaciones de escritura requieren rol `admin`, **pero esa validación la hace únicamente el gateway** (`SecurityConfig`), por lo que este servicio responde `400`/`404` y nunca `401`/`403` por auth (salvo acceso directo sin gateway, que no está soportado). El header `X-Correlation-Id` se propaga en cada request (`CorrelationIdFilter`).

| Método | Ruta | Descripción | Request | Response | Auth |
|---|---|---|---|---|---|
| `GET` | `/actuator/health` | Health check para probes de Kubernetes (`readiness` incluye `db`) | — | `200` `{ status }` | No |
| `GET` | `/api/products` | Listado paginado de productos con filtros opcionales `q` (búsqueda por título), `categoryId` y `sort` | — | `200` Page\<ProductDto\> | No |
| `GET` | `/api/products/{productId}` | Obtiene un producto por su ID | — | `200` ProductDto | No |
| `GET` | `/api/products/{productId}/image` | Sirve la imagen del producto: si `image_url` es `http(s)://` redirige (`302`); si es clave de blob, proxya los bytes con `Cache-Control: public, max-age=3600` | — | `200` binario / `302` / `404` | No |
| `POST` | `/api/products` | Crea un producto (valida SKU duplicado y existencia de la categoría) | `ProductDto` | `201` ProductDto + `Location` | Sí (admin, vía gateway) |
| `PUT` | `/api/products` | Actualiza un producto (el ID viene en el body) | `ProductDto` | `200` ProductDto | Sí (admin, vía gateway) |
| `PUT` | `/api/products/{productId}` | Actualiza un producto (el ID de la ruta tiene prioridad) | `ProductDto` | `200` ProductDto | Sí (admin, vía gateway) |
| `DELETE` | `/api/products/{productId}` | Elimina un producto (borrado físico) | — | `204` | Sí (admin, vía gateway) |
| `GET` | `/api/categories` | Listado paginado de categorías (`Pageable`, con `sort` validado) | — | `200` Page\<CategoryDto\> | No |
| `GET` | `/api/categories/paging` | Listado paginado con `page` (default `0`) y `size` (default `10`) | — | `200` Page\<CategoryDto\> | No |
| `GET` | `/api/categories/paging-and-sorting` | Listado paginado y ordenado con `pageNo` (default `0`), `pageSize` (default `10`) y `sortBy` (default `categoryId`) | — | `200` Page\<CategoryDto\> | No |
| `GET` | `/api/categories/{categoryId}` | Obtiene una categoría por su ID | — | `200` CategoryDto | No |
| `POST` | `/api/categories` | Crea una categoría (valida nombre duplicado) | `CategoryDto` | `201` CategoryDto + `Location` | Sí (admin, vía gateway) |
| `PUT` | `/api/categories` | Actualiza una categoría (el ID viene en el body) | `CategoryDto` | `200` CategoryDto | Sí (admin, vía gateway) |
| `PUT` | `/api/categories/{categoryId}` | Actualiza una categoría (el ID de la ruta tiene prioridad) | `CategoryDto` | `200` CategoryDto | Sí (admin, vía gateway) |
| `DELETE` | `/api/categories/{categoryId}` | Elimina una categoría (borrado físico) | — | `204` | Sí (admin, vía gateway) |

**Comportamiento de `sort`:** formato `propiedad` o `propiedad,asc|desc`. Propiedades permitidas en productos: `id`, `title`, `imageUrl`, `sku`, `price`, `quantity`, `categoryId`; en categorías: `id`, `name`, `description`. Cualquier otra (incluidos `-id` o `campo:1`) responde `400` vía `SortValidator` **antes** de llegar a JPA.

**Filtros combinables de `GET /api/products`:** `q` + `categoryId`, sólo `q`, sólo `categoryId`, o ninguno (queries derivadas separadas, sin inferencia de tipos ante `null`).

### 2.1 Endpoints pendientes

| Método | Ruta | Descripción | Estado |
|---|---|---|---|
| `POST` | `/api/products/{productId}/images` | Subida de imágenes desde el ABM de administración (hoy sólo existen el seed y el proxy `GET`) | `<a implementar>` |
| `GET` | `/api/products?featured=true` (o `/api/products/featured`) | Productos destacados para la sección "Featured" del Home (no existe flag `featured` en la entidad) | `<a implementar>` |
| `GET` | `/api/admin/products?quantityBelow=5` | Productos con stock bajo (KPI "Stock bajo" del Admin Resumen) | `<a implementar>` |

---

## 3. Errores

Formato de respuesta de error: **ProblemDetail (RFC 7807)**, emitido por `GlobalExceptionHandler` (mismo formato en todos los servicios):

```json
{
  "type": "about:blank",
  "title": "Producto no encontrado",
  "status": 404,
  "detail": "Producto no encontrado con id 42"
}
```

| Código | Error | Cuándo ocurre | Ejemplo de mensaje |
|---|---|---|---|
| `400` | Bad Request | Validación de campos falla (`errors` en el body) | `title: must not be blank` |
| `400` | Bad Request | `sort` con propiedad no permitida o formato inválido (`Sort inválido`) | `Propiedad de sort no soportada: 'nombre'. Use 'propiedad' o 'propiedad,asc\|desc'...` |
| `400` | Bad Request | Uso inválido de la API de Spring Data (`Solicitud inválida`) | `Unsupported property ...` |
| `404` | Not Found | Recurso inexistente (`Producto no encontrado` / `Categoría no encontrada`) | `Producto no encontrado con id 42` |
| `404` | Not Found | `GET /api/products/{id}/image` sin `image_url` o blob inexistente | *(body vacío)* |
| `409` | Conflict | SKU duplicado (`Recurso duplicado`) | `El SKU 'SKU-001' ya está en uso` |
| `409` | Conflict | Categoría duplicada (`Recurso duplicado`) | `La categoría 'Tecnología' ya existe` |
| `409` | Conflict | Conflicto de concurrencia por `@Version` (optimistic locking) | `El recurso fue modificado por otra operación; reintente la petición` |
| `409` | Conflict | Violación de restricción única (`DataIntegrityViolationException`) | `Violación de restricción única (posible carrera entre peticiones simultáneas)` |
| `500` | Internal Error | Fallo inesperado (respuesta por defecto de Spring, sin handler explícito) | `Unexpected error` |

> `401`/`403` **no se emiten desde este servicio**: las rutas autenticadas o restringidas a `admin` devuelven `401`/`403` sólo desde el gateway.

---

## 4. Modelo de datos (Entidad)

### 4.1 Entidades

#### `ProductEntity` → tabla `products`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `title` | `varchar(255)` | ❌ | ❌ | ❌ | Título del producto (no es `@NotBlank`) |
| `image_url` | `varchar(500)` | ❌ | ❌ | ❌ | Referencia de imagen: clave de blob (`productos/<SKU>.<ext>` o `productos/<uuid>.<ext>`) o URL `http(s)` externa |
| `sku` | `varchar(100)` | ❌ | ❌ | ✅ | Código único de producto (`@NotBlank`) |
| `price` | `numeric(12,2)` | ❌ | ❌ | ❌ | Precio unitario, `>= 0`, hasta 2 decimales |
| `quantity` | `integer` | ❌ | ❌ | ❌ | Stock disponible, `>= 0` |
| `category_id` | `bigint` | ❌ | ❌ | ❌ | Referencia a `categories.id` (sin FK en JPA; se valida existencia en servicio) |
| `version` | `bigint` | ❌ | ❌ | ❌ | Optimistic locking (`@Version`) |

#### `CategoryEntity` → tabla `categories`

| Campo | Tipo | PK | Nullable | Unique | Descripción |
|---|---|---|---|---|---|
| `id` | `bigserial` | ✅ | ❌ | ✅ | Identificador único |
| `name` | `varchar(100)` | ❌ | ❌ | ✅ | Nombre único de la categoría (`@NotBlank`) |
| `description` | `varchar(500)` | ❌ | ❌ | ❌ | Descripción de la categoría |
| `version` | `bigint` | ❌ | ❌ | ❌ | Optimistic locking (`@Version`) |

### 4.2 Relaciones

| Relación | Tipo | Descripción |
|---|---|---|
| `Category` 1 → N `Product` | Uno-a-muchos (logical) | `products.category_id` referencia `categories.id`; la consistencia la garantiza el servicio (`validateCategory`), no una FK en la BD |

### 4.3 Reglas de negocio

| Regla | Detalle |
|---|---|
| Unicidad | `products.sku` y `categories.name` no pueden repetirse (chequeo previo `existsBySku` / `existsByName` + constraint de BD) |
| Referencia de categoría | Un producto con `categoryId != null` sólo se crea/actualiza si la categoría existe; si es `null` está permitido (producto sin categoría) |
| Borrado | Físico (`delete`) en ambos modelos; no hay borrado lógico ni protección de referencias hacia productos al borrar una categoría |
| Concurrencia | Ambas entidades usan `@Version`: actualizaciones concurrentes devuelven `409` |
| Ordenamiento | `sort` validado contra allowlist (`SortValidator`) antes de tocar JPA |
| Imágenes | `image_url` guarda la CLAVE del blob (no la URL absoluta), de modo que el storage pueda cambiarse sin migrar el dato; el `GET .../image` resuelve clave → bytes |
| Seed | `CatalogSeedRunner` (`app.seed.enabled=true`) carga `seed/categories.json`, `seed/products.json` y sube `seed/images/<SKU>.*` (jpg/jpeg/png/webp/gif) al storage con clave `productos/<SKU>.<ext>`, idempotente: existentes se omiten y las imágenes se re-subben con `overwrite=true`. Best-effort: un storage caído no detiene el arranque |

---

## 5. Configuración

### 5.1 Variables de entorno

| Variable | Descripción | Requerida | Default | Ejemplo |
|---|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | ❌ | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC URL de la base de datos | ✅ | — | `jdbc:postgresql://db:5432/product` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | ✅ | — | `product` |
| `SPRING_DATASOURCE_PASSWORD` | Password de la base de datos | ✅ | — | *(secret)* |
| `EUREKA_URL` | URL del registro Eureka | ❌ | `http://localhost:8761/eureka` | `http://eureka:8761/eureka` |
| `EUREKA_ENABLED` | Habilita el registro en Eureka | ❌ | `true` | `true` |
| `APP_SEED_ENABLED` | Activa el seed de categorías/productos/imágenes | ❌ | `false` | `true` |
| `APP_SEED_LOCATION` | Directorio externo con los JSON de seed (vacío = classpath embebido) | ❌ | *(vacío)* | `/data/seed` |
| `AZURE_STORAGE_ENDPOINT` | Endpoint de Azure Blob Storage (Azurite en local) | ❌ | `http://localhost:10000/devstoreaccount1` | `http://azurite:10000/devstoreaccount1` |
| `AZURE_STORAGE_ACCOUNT` | Cuenta de storage | ❌ | `devstoreaccount1` | `devstoreaccount1` |
| `AZURE_STORAGE_KEY` | Key de la cuenta; **vacío ⇒ storage deshabilitado** (permite arrancar/testear sin credenciales) | ✅ (en k8s) | *(vacío)* | *(secret)* |
| `AZURE_STORAGE_CONTAINER` | Container de imágenes | ❌ | `productos` | `productos` |
| `TRACING_SAMPLING_PROBABILITY` | Muestreo de tracing | ❌ | `0.1` | `0.1` |
| `OTLP_TRACING_ENDPOINT` | Endpoint OTLP de trazas | ❌ | `http://localhost:4318/v1/traces` | `http://otel:4318/v1/traces` |

### 5.2 Spring profiles

| Profile | Uso |
|---|---|
| `test` | Tests automatizados con Testcontainers (`TestcontainersConfiguration`) |
| `local` / `k8s` | `<a implementar>` (no hay perfiles declarados en el código; la variación por entorno se hace por variables de entorno) |

### 5.3 Recursos Kubernetes (orientativo)

| Recurso | Request | Limit | Notas |
|---|---|---|---|
| CPU | `250m` | `1000m` | — |
| Memoria | `512Mi` | `768Mi` | JVM con `-XX:MaxRAMPercentage=75` |

> El servicio tiene `server.shutdown=graceful` con `spring.lifecycle.timeout-per-shutdown-phase=30s` para drenar requests ante `SIGTERM`.

---

## 6. Dependencias

### 6.1 Dependencias internas (otros microservicios)

| Servicio | Para qué | Protocolo | Fallback si cae | Crítica |
|---|---|---|---|---|
| `gateway` | Enruta `/api/products/**` y `/api/categories/**`, valida el JWT, inyecta `X-User-Id`/`X-User-Email`/`X-User-Roles` y propaga `X-Correlation-Id` | HTTP/REST (upstream; este servicio no lo llama) | El servicio no está expuesto directamente fuera del gateway | ✅ Sí |
| `authorizer` | Emite el JWT que el gateway valida (issuer/firma) | HTTP/REST (sólo lo consume el gateway) | Login/signup caídos → `401` en el gateway | ✅ Sí |

*Este servicio no realiza llamadas salientes a otros microservicios; es una dependencia de `order-service`, que consulta `GET /api/products/{id}` con circuit breaker + retry.*

### 6.2 Dependencias externas

| Dependencia | Tipo | Para qué | Crítica | Comportamiento si cae |
|---|---|---|---|---|
| PostgreSQL | Base de datos | Persistencia principal (`ddl-auto=update`) | ✅ Sí | `readiness` en `DOWN` (el group incluye `db`) |
| Azure Blob Storage (Azurite en local) | Almacenamiento de objetos | Imágenes de producto (`ImageStorageService`) | ❌ No | Arranque y lecturas continúan: `@PostConstruct` best-effort, `download` devuelve `404` y el seed loguea el error sin detenerse |
| Eureka | Registro de servicios | Descubrimiento para el gateway (`spring.application.name=productService`) | ❌ No | `EUREKA_ENABLED=false` para operar sin registro |
| Collector OTLP | Observabilidad | Trazas y métricas (`/actuator/prometheus`, `/actuator/metrics`) | ❌ No | Sólo pierde observabilidad |

### 6.3 Eventos publicados/consumidos

| Evento | Dirección | Trigger | Payload |
|---|---|---|---|
| — | — | *Este servicio no publica ni consume eventos de Kafka; no tiene configuración de Kafka.* | — |

---

## 7. Cobertura del wireframe

Pantalla del wireframe (`docs/OpenPencil/LaTiendita.fig`, página "WireFrame") → endpoints/responsabilidades de `product-service` que la soportan.

| Pantalla | Soporte en este servicio | Estado |
|---|---|---|
| **Home — sección "Categories"** (grilla de categorías) | `GET /api/categories` (paginado) alimenta las cards de categorías | ✅ Implementado |
| **Home — sección "Featured"** (grilla de productos destacados con imagen, nombre, precio y valoración) | Imagen → `GET /api/products/{id}/image`; nombre/precio → `GET /api/products`. **Destacado** (flag `featured`) y **valoración** no existen acá: la valoración la aporta `review-service` | ⚠️ Destacado: `<a implementar>`; valoración: fuera de este servicio |
| **Categories (listado)** — título, toolbar de búsqueda/filtros, grilla de cards | `GET /api/categories` (+ `sort` validado); búsqueda/filtros de categorías más allá del paginado: `<a implementar>` | ⚠️ Parcial |
| **Category "Tecnología" (listado de productos)** — breadcrumb, toolbar de filtros y orden, grilla, paginación | `GET /api/products?categoryId={id}&q={texto}&page=&size=&sort=` (filtros combinables, paginación `Page`, sort con allowlist). El breadcrumb resuelve el nombre con `GET /api/categories/{id}` | ✅ Implementado |
| **Product Detail — galería** (imagen principal + thumbnails) | Imagen principal → `GET /api/products/{id}/image` (un `image_url` por producto). **Galería múltiple / thumbnails**: no hay entidad de imágenes ni endpoint de subida (sólo el seed) | ⚠️ `<a implementar>` (galería y subida) |
| **Product Detail — bloque de info** (nombre, precio, stock/disponibilidad, botón "Agregar al carrito") | `GET /api/products/{id}` → `title`, `price`, `quantity` (disponibilidad = `quantity > 0`). La creación del carrito es responsabilidad del frontend | ✅ Implementado |
| **Product Detail — pestañas de detalles/especificaciones** | `GET /api/products/{id}` sólo expone `title`, `sku`, `price`, `quantity`, `categoryId`, `imageUrl`. **`description` y especificaciones no existen en `ProductEntity`** | ⚠️ `<a implementar>` (campo `description`/specs) |
| **Product Detail — sección de reseñas** | Fuera de alcance: la maneja `review-service` | ➖ Otro servicio |
| **Product Detail — sección "Related"** (productos de la misma categoría) | `GET /api/products?categoryId={misma}&page=&size=` (misma query del listado de categoría) | ✅ Implementado |
| **Admin Productos** — ABM (`+ Nuevo producto`, editar, eliminar) | `POST /api/products`, `PUT /api/products[/{id}]`, `DELETE /api/products/{id}`, `GET /api/products/{id}` | ✅ Implementado |
| **Admin Productos** — campos: nombre, precio, stock, categoría, SKU | `title`, `price`, `quantity`, `categoryId` (valida existencia), `sku` (único) | ✅ Implementado |
| **Admin Productos** — campos: descripción, imágenes (subida), estado | Sin campos ni endpoints en el modelo actual (sólo `image_url` resuelto por seed/proxy) | ⚠️ `<a implementar>` |
| **Admin Categorías** — ABM de categorías | `POST /api/categories`, `PUT /api/categories[/{id}]`, `DELETE /api/categories/{id}`, `GET /api/categories[/{id}]`; campos `name` + `description` | ✅ Implementado |
| **Admin Resumen** — KPI "Stock bajo" (productos con menos de 5 unidades) | Los datos crudos existen (`GET /api/products` con filtro de cantidad), pero no hay endpoint de agregación ni filtro `quantityBelow` | ⚠️ `<a implementar>` |
