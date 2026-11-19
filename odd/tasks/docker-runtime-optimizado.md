# Optimizar runtime Docker (compose, Dockerfiles, túnel público)

Objetivo: dejar `runtime/docker` como un stack ligero, rápido y con caché de build, con publicación automática vía ngrok del frontend externo y del gateway para webhooks.
Branch: `feat/completar-microservicios` | Delivery: `single-pr` | Verificación: `docker compose config --quiet`, build de imagen compartida, smoke tests de red.

## Specs

### S1 — Dockerfiles y caché
Petición literal del usuario: "Quiero implementar los dockerfile y compose de las formas lo mas optimas y optimizadas posibles que sea rapidas y no pesen muchos, que eviten lo que no sirva. que tenga buen cache. usa las skills de docker que hay en el .agents"

Alcance autorizado: `services/**` (Dockerfile compartido, `.dockerignore`, retiro de duplicados) y `runtime/docker/**`.
Criterios: un único Dockerfile parametrizado para los 10 servicios Spring Boot 4.1.1 / Java 17; capa de dependencias Maven separada del código fuente; imagen runtime JRE mínima con usuario no-root; `.dockerignore` que excluya `target`, `.git`, `.next`, `node_modules`, tests y docs; ningún `apt-get` en runtime; puerto correcto por servicio (8080, gateway 8081, eureka 8761).
Checks: `docker compose config --quiet`; inspección del Dockerfile; build de al menos dos módulos (uno anidado) con caché reutilizable.

### S2 — Compose con infraestructura completa
Petición literal del usuario: "Recorda que uso atuhorizer para la parte de usuarios permisos y roles, una bdd por microservicio, kafka, etc ..., alguna forma de tener object storage como azurite, etc... no uses floci por el momento. Quiero que la implementacion sea en runtime/docker."

Alcance autorizado: `runtime/docker/compose.yaml`, `runtime/docker/.env.example`, `runtime/docker/postgres/**`, `.gitignore`.
Criterios: Authorizer `quay.io/authorizer/authorizer:2.4.4` conservado y configurable por entorno; PostgreSQL con una base por microservicio (`product`, `orders`, `payment`, `shipment`, `customer`, `cart`, `offer`, `review`) más la BD de Authorizer; Kafka KRaft `apache/kafka:4.3.2` sin ZooKeeper; Azurite `mcr.microsoft.com/azure-storage/azurite` en `:10000` para `product-service`; Eureka `:8761`; gateway `:8081`; los 8 microservicios de negocio; healthchecks en infraestructura y `depends_on` con `condition: service_healthy`; volúmenes nombrados; tags fijos; sin Docker socket; secretos solo por `.env` (gitignored). No se usa floci.
Checks: `docker compose config --quiet`; verificación de que cada servicio resuelve `postgres`, `kafka`, `azurite`, `authorizer`, `eureka-server` y `gateway` por nombre de red.

### S3 — ngrok para frontend externo y webhooks
Peticiones literales del usuario: "Quiero que al levantar el compose ngrok comporta de forna publica los puertos del frontend y si es necesario el backend de microservicios para poder recibir webhooks o mostrarle a algun cliente el sistema en desarrollo." y "el frontend va a estar por fuera de docker asi que busca una manera para poder compartir su puerto de forma publica de forma automatica"

Alcance autorizado: `runtime/docker/ngrok/**`, servicio `ngrok` en `runtime/docker/compose.yaml`, variables en `.env.example`.
Criterios: ngrok oficial pinneado; endpoint HTTP al frontend del host mediante `host.docker.internal:3000`; endpoint HTTP al gateway `:8081` para `/api/webhooks/mercado-pago`; token por `NGROK_AUTHTOKEN` nunca escrito en archivos versionados; dominio reservado opcional por variable; URL pública visible en `docker compose logs`; el túnel arranca con el compose.
Checks: `docker compose config --quiet`; validación de la sintaxis del config v3 (`version: 3`, `endpoints`); comprobación de que el comando del contenedor usa `start --all`.

### S4 — README y verificación
Petición literal del usuario: "Actualizame el readme luego de esto"

Alcance autorizado: `README.md`.
Criterios: reemplazar las secciones obsoletas de "Infraestructura de datos", "Backends" y "Emulador Azure" por el flujo real de `runtime/docker`; documentar arranque, túnel público, variables, puertos, bases, topics y limitación de URLs dinámicas de ngrok para webhooks; no dejar afirmaciones de que el compose solo tiene Authorizer.
Checks: lectura estructural del README actualizado; `docker compose config --quiet` final.

## Tasks

- T1 — S1 — inline — Dockerfile compartido + `.dockerignore` + borrado de duplicados.
- T2 — S2 — inline — Compose completo, init SQL de bases, `.env.example`, `.gitignore`.
- T3 — S3 — inline — Config ngrok + servicio en Compose + variables.
- T4 — S4 — inline — README y verificación final.

Work unit commit: `d573266` — `refactor(docker): optimizar el stack con Dockerfile compartido y tuneles ngrok` (19 archivos, +503/-233). Estrategia `single-pr`: un único work unit lógico; el usuario no pidió PR, no se encadena.

Route declaration: directo inline. No hay writer reason (una sola unidad coherente de runtime, sin edit surfaces paralelos) y el contexto del mapa cabe en la sesión. Test-first: no aplica RED preexistente para archivos de configuración declarativa; se sustituye por validación estructural y smoke checks.

Forecast de líneas autoras: ~540 (Dockerfile 60, compose 300, ngrok 25, SQL 25, env 60, README 70). Estrategia `single-pr`: un único work unit lógico, sin PRs encadenados (el usuario no pidió PR).

## Log

L1 (verbatim):
Quiero implementar los dockerfile y compose de las formas lo mas optimas y optimizadas posibles que sea rapidas y no pesen muchos, que eviten lo que no sirva. que tenga buen cache. usa las skills de docker que hay en el .agents
Quiero que al levantar el compose ngrok comporta de forna publica los puertos del frontend y si es necesario el backend de microservicios para poder recibir webhooks o mostrarle a algun cliente el sistema en desarrollo.
Recorda que uso atuhorizer para la parte de usuarios permisos y roles, una bdd por microservicio, kafka, etc ..., alguna forma de tener object storage como azurite, etc... no uses floci por el momento. Quiero que la implementacion sea en runtime/docker. Actualizame el readme luego de esto

el frontend va a estar por fuera de docker asi que busca una manera para poder compartir su puerto de forma publica de forma automatica

L2 — 2026-11-19 — Mapa del repo: compose actual solo tiene Authorizer; 5 Dockerfiles duplicados y 5 servicios sin Dockerfile; gateway escucha 8081 pero su Dockerfile expone 8080; `payment-service` es el único que recibe webhooks (`POST /api/webhooks/mercado-pago`); `product-service` usa Azure Blob en `:10000`; el frontend corre fuera de Docker en `:3000` con `NEXT_PUBLIC_API_URL` apuntando por defecto a `:8080`. Se decide shared Dockerfile parametrizado, un cluster PostgreSQL con 8 bases separadas, Kafka KRaft oficial, Azurite directo (no floci) y ngrok con túnel frontend + gateway.

L3 — 2026-11-19 — Decisión: ngrok se publica en el compose por defecto. El token se lee de `NGROK_AUTHTOKEN` en `.env`; el dominio reservado es opcional. Como el frontend es externo, el túnel al frontend usa `host.docker.internal:3000`. El frontend debe apuntar su API al túnel público del gateway (no a `localhost`) cuando se muestre a un cliente; se documenta en README.

L4 — 2026-11-19 — Verificación observada (T1-T4): `docker compose -f runtime/docker/compose.yaml config --quiet` OK; builds de `eureka-server`, `product-service` y `cart-service/cartService` (anidado) OK con caché Maven reutilizable; config ngrok v3 parseado (sin token solo `ERR_NGROK_4018`, esperado); `postgres` y `kafka` healthy; las 8 bases listadas; `azurite` healthy tras ajustar healthcheck; `eureka-server` healthy con `/actuator/health` 200; stack detenido con `docker compose down` (sin `-v`, volúmenes conservados). Todas las tareas cerradas; pendiente commit del work unit y verificación nativa RDD/independiente.
