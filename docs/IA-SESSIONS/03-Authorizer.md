# API Gateway para roles y permisos

**Session ID:** ses_e5c325e21ffeAPRoJ5wwURioqO
**Created:** 3/11/2026, 20:45:24
**Updated:** 3/11/2026, 21:23:28

---

## User

Si por ejemplo en un sistema de microservicios no quiero a ponerme a configurar o a desarrollar todo el sistema de roles y permisos que podria utilizar para que se haga cargo de eso. voy a usar apigateway

---

## Assistant

_Thinking:_

The user asks in Spanish: in a microservices system, they don't want to build roles/permissions themselves — what could they use so it takes care of that, using an API Gateway. They mention "voy a usar apigateway" — which API Gateway? AWS API Gateway likely. Question: which solution handles RBAC/authorization for them.

This is a conceptual/architecture question. Persona: Senior Architect, concepts > code, Spanish (Rioplatense voseo). Keep short-ish but this genuinely requires some explanation. No file changes needed. Plan mode — just discuss.

Should I check skills? Available skills: none really match (springboot-security maybe if Spring, but not indicated). No mandatory skill.

Maybe I should verify the user's context — which API Gateway (AWS? Kong? etc.)? The persona says ask at most one question at a time, and stop. But I can give a useful answer with alternatives and then ask one clarifying question... Actually rule: "Do not present option menus, exhaustive lists, or multiple approaches unless there is a real fork with meaningful tradeoffs." Here there IS a real fork: which gateway/cloud. Hmm.

The user says "voy a usar apigateway" — likely AWS API Gateway (or maybe Google/Azure). The key architectural answer: the gateway handles authentication (JWT validation) and coarse authorization (route/role-based), but roles/permissions management requires an IdP: Keycloak (open source, realm-based RBAC), Auth0, Okta, Cognito, Authelia, Ory Keto (permissions as a service/ReBAC/Zanzibar), Casbin. If they use AWS: Cognito + IAM/JWT authorizer in API Gateway; fine-grained permissions with a policy engine (OPA/Cedar) or externalized service.

Concept: API Gateway is the enforcement point (policy enforcement point), but you still need a policy decision point — IdP issuing tokens. Keycloak is the classic "let it take care of it" answer: user federation, roles, clients, groups, token exchange, fine-grained authz services. Or Auth0/Okta if you want SaaS.

Answer plan (Spanish, Rioplatense):
- Concept: gateway = punto de enforcement, no decide nada por sí solo. Necesitás un IdP que emita JWT con roles/permisos.
- Opción open source auto-hospedada: Keycloak (lo más completo: realms, roles, groups, RBAC fino, token exchange, fine-grained authz). Alternativas más ligeras: Ory/Kratos+Keto, Casbin/OPA para permisos muy personalizados (Zanzibar-style con Keto).
- Si AWS: Cognito + Lambda/HTTP API JWT authorizer; para permisos finos, Cedar/OPA.
- Gateway: AWS API Gateway no "entiende" roles salvo por authorizers; podés mapear claims a rutas.
- Recommendation: Keycloak + JWT authorizer en el gateway; microservicios validan el token (o el gateway lo hace) — la validación centralizada en el gateway, pero cada servicio debe verificar o confiar en el gateway.

Tradeoffs: SaaS (Auth0/Okta) = menos mantenimiento pero vendor lock-in/costo; Keycloak = control total pero es un servicio más que operar; OPA/Keto = potencia pero más complejidad.

One question at the end: which gateway/cloud? Actually the rule says ask at most one question and stop. I could ask "¿Qué API Gateway exactamente?" That's a real fork. But I can also answer generically. Given they said "apigateway" (probably AWS), maybe answer with the general pattern + note AWS specifics briefly, then ask one question to pin the stack.

Keep response concise per length contract but this needs substance. Medium length, structured.

No tools needed really. Should I verify claims? The technical claims about Keycloak are well-known. The persona says verify technical claims before stating them — for library docs use context7. This is general architecture knowledge; maybe quick check not needed. I think it's fine — these are stable, well-known facts. I could do a websearch for current state... Not necessary; risk of being wrong is low. But "Never agree with user claims without verification" — the user isn't making a factual claim, just asking.

Engram: session start protocol — should call mem_current_project first before memory access. Do I need memory here? The first message references... no project. This is a general question, not project work. The protocol says at session start when runtime supplies workspace, resolve project before initial memory access. It's mandatory-ish. But is it needed for a pure Q&A? The protocol says "SESSION START & PROJECT DETECTION PROTOCOL (mandatory)". Hmm, I don't have a cwd parameter schema shown for mem_current_project in the tools list... it's in engram tools. I could call mem_current_project then skip searches. Honestly for a conceptual question with no project context, doing one cheap call is fine but unnecessary noise. The instructions say mandatory at session start. I'll skip heavy memory work; actually let me just call mem_current_project to comply, then answer. Hmm — but tools via execute. It's a cheap call. Let's do it to follow protocol, then compose answer.

Actually, also "WHEN TO SEARCH MEMORY: user's FIRST message references the project, a feature, or a problem" — no project reference here. Just resolve project at start. I'll do one call.

Wait — plan mode: read-only, mem_current_project is read-only, fine.

Let me call it.

**Tool: execute**

**Input:**
```json
{
  "code": "return await tools.engram.mem_current_project();"
}
```

{
  "available_projects": null,
  "cwd": "C:\\Users\\damian\\Repositorios\\LaTiendita",
  "project": "latiendita",
  "project_path": "C:\\Users\\damian\\Repositorios\\LaTiendita",
  "project_source": "git_remote"
}

---

## Assistant

_Thinking:_

Project resolved: latiendita. No ambiguity. Now answer the conceptual question in Spanish (Rioplatense, voseo), concise but substantive. One question at most at the end.

Key points:
1. Concept: API Gateway = policy enforcement point, no es un sistema de roles. Necesitás un IdP que decida y emita tokens.
2. La solución "que se haga cargo": Keycloak (self-hosted, open source) — realms, roles, grupos, RBAC fino, clients, token exchange. Es el estándar de facto en microservicios.
3. SaaS: Auth0/Okta/Cognito — menos ops, costo/vendor lock-in.
4. Si el gateway es AWS API Gateway: JWT authorizer con Cognito/Keycloak (custom authorizer); API Gateway no evalúa permisos finos por ruta más allá del claim.
5. Permisos muy finos/dinámicos (recurso-usuario): Ory Keto / Casbin / OPA — pero eso ya es el sistema que no querés construir... Actually Keycloak fine-grained authz covers much of that.
6. Validación: o el gateway valida y los servicios confían en headers (riesgo: service-to-service), o cada servicio valida el JWT (recomendado: validación de firma/claims en cada servicio o al menos internamente).

Recommendation: Keycloak + authorizer en el gateway.

Tradeoffs table? Keep short. Then one question: ¿qué gateway/nube? Actually I can ask: "¿Qué API Gateway vas a usar (AWS, Kong, otro)?" — one question.

Format: short answer first, expand. Use minimal structure.

Buena pregunta, pero aclaro el concepto primero porque ahí está la trampa: **el API Gateway no gestiona roles y permisos, solo los aplica**. Es un *policy enforcement point*: valida el token, mira los claims y deja pasar o no. Quien define, administra y emite esos claims es otra pieza — un **Identity Provider (IdP)**. Así que "que se haga cargo" significa elegir un IdP maduro en vez de construir tu propio servicio de usuarios y permisos.

La opción más común para microservicios:

- **Keycloak** (open source, self-hosted): es el estándar de facto. Te da realms, usuarios, grupos, roles jerárquicos, RBAC fino por recurso, clientes OIDC, y emite JWTs que tu gateway valida. No escribís una línea de lógica de autorización.
- **Auth0 / Okta / Cognito** (SaaS): lo mismo pero gestionado. Menos operación, pero costo creciente y vendor lock-in.
- **OPA / Casbin / Ory Keto**: solo si necesitás permisos dinámicos muy finos (tipo "este usuario puede editar *este* documento") que Keycloak no cubra bien. Eso ya es un nivel de complejidad extra.

El flujo queda: usuario → Keycloak (login) → JWT con roles → **API Gateway valida firma y claims** (con su authorizer) → microservicios. Detalle que muchos ignoran: si los servicios hablan entre sí o acceden directamente a datos, deberían validar el JWT también, o al menos verificar un claim de identidad — no confíes ciegamente en que el gateway filtró todo.

Dicho esto, un detalle cambia el setup concreto: **¿qué API Gateway vas a usar?** Si es AWS API Gateway el authorizer es JWT/Cognito o Lambda custom; si es Kong o otro, la integración con Keycloak es distinta.

---

## User

Voy a usar api gateway con spring cloud o un nginx que me recomendas, todos mis micrtoservicios van a ser con spring boot

---

## Assistant

_Thinking:_

The user asks: API Gateway with Spring Cloud (Spring Cloud Gateway) vs Nginx, all microservices in Spring Boot. What do I recommend?

This is a conceptual/architecture question. Persona: Senior Architect, Spanish Rioplatense, concepts > code. Should I check skills? Available skills: java-springboot — "Write and review application code in an existing Spring Boot 3/4 project..." This is a recommendation question about architecture, not writing code. springboot-security — reviewing authn/authz... The question is about gateway choice + roles/permissions. Hmm, the skill says "Use when reviewing Spring Security authn/authz, validation, CSRF, secrets, headers, or rate limiting in Java Spring Boot services." This is more of an architecture recommendation, not a code review. I think the core answer here is Spring Cloud Gateway vs Nginx. But the contextual skill loading is MANDATORY: "does this request match any skill?" — The request involves Spring Boot microservices and an API gateway with auth. It's arguably a match for java-springboot (configuring Spring Cloud Gateway is application code in a Spring Boot project) and springboot-security (authn/authz at gateway level).

The rule says: self-check before every response, if yes, read the matching SKILL.md BEFORE generating reply. To be safe, I should load at least java-springboot since the recommendation will involve Spring Cloud Gateway configuration. But this is Plan mode — discussing a recommendation, no code writing yet. Still, the skill instructions say to read matching skills. Let me be pragmatic: the answer is an architecture recommendation (Spring Cloud Gateway vs Nginx). The java-springboot skill is about writing/reviewing application code — I'm not writing code yet. springboot-security is about reviewing security in Spring Boot services — the auth topic is relevant but I'm not reviewing code.

Hmm. The mandate is strong: "If yes, read the matching SKILL.md (using your agent's read mechanism) BEFORE generating your reply. This is a blocking requirement." Does the request match? "Voy a usar api gateway con spring cloud o un nginx que me recomendas, todos mis microservicios van a ser con spring boot" — this is a technology choice question. It matches java-springboot's description loosely ("Use when adding or changing features" — no, it's for writing and reviewing application code). I'd say the closest match is java-springboot since Spring Cloud Gateway IS a Spring Boot app and my recommendation will likely include config snippets (though in plan mode, no code changes).

To be safe and compliant, I'll load java-springboot skill. It's one call. Actually maybe both java-springboot and springboot-security? The security skill is for reviewing services — the JWT validation at gateway is security-relevant. But loading two skills for a recommendation question seems heavy. Let me load java-springboot only — the gateway itself is Spring Boot app code.

Actually, wait — should I check context7 for Spring Cloud Gateway docs? The recommendation is well-established: Spring Cloud Gateway vs Nginx. For library docs, context7 is preferred over web search "when the user asks about a library, framework...". The question is a choice between two. My knowledge: Spring Cloud Gateway is reactive (Spring WebFlux), integrates with Spring Cloud Discovery (Eureka), Spring Security OAuth2 resource server for JWT validation, rate limiting via Redis, predicates/filters. Nginx is a general reverse proxy — mature, high performance, but for routing/auth logic you'd write Lua or use njs, or external auth. For a Spring Boot shop: Spring Cloud Gateway gives Java-native development, same stack, discovery integration, easy JWT validation with spring-boot-starter-oauth2-resource-server.

Recommendation: Spring Cloud Gateway. Reasons:
1. Consistencia de stack — tu equipo ya sabe Java/Spring; no necesitás Lua ni ops de Nginx.
2. Integración nativa: service discovery (Eureka/Consul), load balancing, resilience4j (rate limiting, retries, circuit breaker), OAuth2 Resource Server (validación JWT declarativa), routing por predicados/filtros.
3. Autorización por claim/rol se declara en configuración Java — mismo modelo mental que el resto de tus microservicios.
4. Nginx sigue siendo útil como proxy estático al borde (TLS termination, servir estáticos), pero como "gateway de lógica" obliga a Lua/njs o a un backend extra.

Tradeoffs to mention honestly:
- Spring Cloud Gateway es reactivo (WebFlux) — distinto modelo de programación (Reactor), aunque para un gateway no es gran problema porque es I/O puro.
- Nginx es más performante en proxy puro y más maduro para TLS/statics, pero cada lógica de auth/routing es config o código Lua = otro lenguaje/otro mantenimiento.

Structure of answer: recommendation first (Spring Cloud Gateway), why, when Nginx still has a place (TLS termination al borde), the stack proposal: Nginx (borde, TLS) → Spring Cloud Gateway (auth, routing, rate limit) → microservicios, con Keycloak como IdP (from previous turn).

Should I read the skill? The persona says self-check. This is a tech recommendation, no code. I'll load java-springboot to be safe — it's blocking requirement if it matches. Let me read it.

Also plan mode: no file changes. This is pure discussion. One question at most if needed — I don't think I need to ask; I can give recommendation. Previous turn I asked which gateway — they answered. Now give the recommendation.

Let me load the skill first.

**Tool: skill**

**Input:**
```json
{
  "id": "java-springboot"
}
```

<skill_content name="java-springboot">
# Skill: java-springboot

# Spring Boot application development

Opinionated rules plus a router to deeper references. Code samples in the references were compiled and run on Spring Boot 4.1.1 (Spring Framework 7) and, where they differ, on Boot 3.5.16 (Framework 6.2). Boot 3 vs 4 differences are marked "Boot 3" / "Boot 4" in each reference.

## Before writing code

1. **Detect, do not assume:** read `pom.xml`/`build.gradle(.kts)` for the Spring Boot version, Java version, starters in use (`spring-boot-starter-web` is Boot 3, `spring-boot-starter-webmvc` is Boot 4), persistence (JPA, JDBC), the migration tool, the test setup, and the package layout and naming conventions of the existing code. Match them.
2. **Never upgrade Spring Boot, Java or dependencies implicitly.** A Boot 3 -> 4 move is a deliberate task (`spring-boot-4-migration`).
3. **Ask before adding infrastructure:** a broker (Kafka, RabbitMQ), a cache provider, a new database, or a scheduler/async setup. Add only what the user requested, with the Boot-managed version (no explicit version for BOM-managed artifacts, never `latest`).
4. **Verify every change** with the project's own build: `./mvnw verify` or `./gradlew build` (plain `mvn`/`gradle` if no wrapper). Integration tests need Docker when they use Testcontainers.

## Router

| Task | Read |
| --- | --- |
| Repositories, projections, pagination, N+1, `@Transactional`, auditing, Flyway/Liquibase, Spring Data JDBC | `references/data.md` |
| Controllers, validation, `ProblemDetail`, `RestClient`, `@HttpExchange`, API versioning, OpenAPI, JSpecify | `references/rest-api.md` |
| Kafka/AMQP, `@Cacheable`/Caffeine, `@Scheduled`, `@Async`, virtual threads, outbox | `references/messaging-caching.md` |
| `@ConfigurationProperties`, profiles, secrets, config import, Docker Compose / Testcontainers at dev time | `references/configuration.md` |
| AOT, CDS/AppCDS, lazy initialization, native image | `references/aot-and-startup.md` |

If these standalone skills are installed, also use them for their topic; otherwise rely on the official docs: `java-hibernate-jpa` (JPA mapping), `spring-boot-testing` (tests), `spring-security` (authentication, authorization, CSRF/CORS), `spring-observability` (metrics, tracing, Actuator, logs), `create-spring-boot-java-project` (project defaults, Boot 3 vs 4 build changes), `java-add-graalvm-native-image-support` (native executable).

## Core rules

- **Structure and layers:** package by feature; constructor injection; controllers return DTOs/records, never entities; services own the transaction.
- **Validation and errors:** Bean Validation on request records with `@Valid`; one `@RestControllerAdvice` returning `ProblemDetail`. JSpecify annotations document nullability only and do not validate.
- **Jackson:** Boot 4 uses Jackson 3 (`tools.jackson.databind.*`, `JsonMapper`; annotations stay in `com.fasterxml.jackson.annotation`); Boot 3 uses Jackson 2 (`com.fasterxml.jackson.databind.*`, `ObjectMapper`).
- **Slice-test gotcha:** put `@EnableJpaAuditing`, `@EnableCaching`, `@EnableAsync`, `@EnableScheduling` on dedicated `@Configuration` classes, not on the `@SpringBootApplication` class, or slices such as `@DataJpaTest` fail to start (for example, no `CacheManager`). Slices do not scan `@Configuration` classes: `@Import` the one a test needs.
- **Proxied beans:** `@Cacheable`, `@Async` and `@Transactional` work through proxies: they do nothing on self-invocation or on `private`/`final` methods; test them through the injected bean.

## Checklist

- [ ] Versions, build tool and conventions detected; starters match the Boot major
- [ ] No new infrastructure or dependency without the user's agreement
- [ ] `./mvnw verify` / `./gradlew build` passes; secrets absent from code, properties and logs

See also: Spring Boot reference <https://docs.spring.io/spring-boot/reference/>, Spring Framework reference <https://docs.spring.io/spring-framework/reference/>, Spring Data <https://docs.spring.io/spring-data/commons/reference/>.

Base directory for this skill: C:\Users\damian\Repositorios\LaTiendita\.agents\skills\java-springboot
Relative paths in this skill (e.g., scripts/, reference/) are relative to this base directory.
Note: file list is sampled.

<skill_files>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\java-springboot\references\aot-and-startup.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\java-springboot\references\configuration.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\java-springboot\references\data.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\java-springboot\references\messaging-caching.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\java-springboot\references\rest-api.md</file>
</skill_files>
</skill_content>

---

## Assistant

_Thinking:_

The skill is loaded. It's about writing Spring Boot application code — this question is a technology recommendation (Spring Cloud Gateway vs Nginx), no code to write. The skill doesn't directly change my answer much, but confirms conventions (Boot 3 vs 4 starters etc.) if code snippets come up later.

Now answer: recommend Spring Cloud Gateway for their case. Key reasoning:

1. Stack consistency: all services are Spring Boot; gateway as Spring Boot app = same language, same tooling, same team skills. Nginx would require Lua/njs for auth logic = a second "language" and maintenance burden.

2. Native integrations that matter for their use case (roles/permissions from previous turn):
   - spring-boot-starter-oauth2-resource-server: JWT validation + role/authority mapping declaratively (`.hasRole("ADMIN")` or reactive equivalent) — this is exactly "que se haga cargo" of applying roles at the edge.
   - Service discovery (Eureka/Consul) + load balancing.
   - Resilience4j: rate limiting, retries, circuit breakers.
   - Predicates/filters for routing: path, header, claim-based routing.

3. When Nginx still wins: TLS termination, static content, raw performance proxying, or if the team doesn't know Java/reactive. But for logic, Nginx = Lua/OpenResty = another stack.

4. Honest tradeoff: Spring Cloud Gateway is reactive (WebFlux/Reactor) — different programming model (non-blocking). For a gateway it's fine because it's I/O-bound, but your team should know Reactor basics. Also it's a JVM service to operate (memory, deployment) vs Nginx which is lightweight.

5. Recommended architecture (from previous turn): Nginx or the cloud LB at the edge for TLS → Spring Cloud Gateway (validates JWT from Keycloak, enforces roles, rate limiting, routing) → microservicios. Both can coexist; they're not mutually exclusive.

Also note Boot 3 vs 4: Spring Cloud Gateway versions tie to Spring Cloud release train; Boot 4 support — Spring Cloud 2025.x supports Boot 3.4/3.5... Actually, Boot 4.0 with Spring Cloud — I should be careful about version claims. Spring Cloud Gateway works with Spring Boot 3.x via Spring Cloud 2023.x/2024.x/2025.x. Boot 4 support in Spring Cloud 2026.x? I'm not certain. Better to say "ojo con la matriz de compatibilidad entre Spring Boot y Spring Cloud (release train)" without claiming specifics. The skill mentions Boot 4.1.1 exists — so Boot 4 is out. I'll mention compatibility matrix as a caveat to verify, without specific claims.

Should I verify with context7? The recommendation doesn't hinge on a specific API detail. The compatibility caveat is generic advice ("check the release train matrix"). I think it's fine without a docs lookup. Keep answer concise per response-length contract. Use Spanish, Rioplatense, warm but direct.

Structure:
- Recommendation: Spring Cloud Gateway, con Nginx opcional al borde.
- Why (concepts): consistency + declarative auth.
- Tradeoff honesto: reactive model.
- Stack proposal (short).
- Maybe end without question? Rules: at most one question at a time; not required. I could ask if they want me to plan the gateway project structure — but we're in Plan mode, so discussing the plan is the point. Actually, a natural next question: "¿ya tenés Keycloak/IdP definido?" or ask about discovery (Eureka vs static routes). That could be useful to continue planning. One question: ¿usan discovery service (Eureka/Consul) o rutas estáticas? That's a real fork for the plan.

Keep it short-medium.

**Recomendación: Spring Cloud Gateway**, y el porqué va más allá de "es de Spring":

1. **Consistencia de stack** — tu equipo ya escribe Java. Con Nginx, cada lógica de auth o routing se resuelve en Lua/njs (OpenResty) o en un backend extra: es otro lenguaje, otro mantenimiento, otra curva. Con Spring Cloud Gateway la autorización se declara igual que en cualquier microservicio.

2. **La validación de roles te la resuelve en configuración** — con `spring-boot-starter-oauth2-resource-server` validás el JWT (firma, issuer) y mapeás roles/authorities del token de Keycloak de forma declarativa. Es literalmente el "que se haga cargo" que buscabas: sin escribir código de autorización, solo decir qué ruta requiere qué rol.

3. **Integración nativa con el ecosistema** — discovery (Eureka/Consul) + load balancing, rate limiting y circuit breakers con Resilience4j, routing por predicados (path, header, claim del token). Todo en la misma JVM, sin glue externo.

**El tradeoff honesto:** Spring Cloud Gateway es reactivo (WebFlux/Reactor), un modelo distinto al MVC que probablemente usás en tus servicios. Para un gateway es correcto porque es I/O puro, pero tu gente tiene que aprender lo básico de Reactor. Y es un servicio JVM más para operar, contra Nginx que es ultraliviano.

**Dónde sigue teniendo sentido Nginx:** al borde, para TLS y estáticos. No son excluyentes — el patrón común es:

```
Nginx/LB (TLS) → Spring Cloud Gateway (JWT, roles, rate limit) → microservicios
```

Nginx no desaparece, pero baja a proxy de borde y deja de tener lógica.

Ojo con un detalle: las versiones de Spring Cloud Gateway se amarran a una release train de Spring Cloud, que a su vez es compatibles con un rango de Spring Boot — verificá la matriz de compatibilidad antes de fijar versiones, sobre todo si pensás en Boot 4.

Para seguir armando el plan: **¿van a usar un discovery service (Eureka/Consul) o rutas estáticas apuntando a los servicios?**

---

## User

voy a usar Eureka

---

## Assistant

_Thinking:_

User says they'll use Eureka. Plan mode — we're discussing architecture. Continue the plan discussion. Now with Eureka chosen, I can outline the stack/plan. What remains to decide? Keycloak vs other IdP (previous discussion assumed Keycloak), Redis for rate limiting? etc.

The persona says ask at most one question at a time. Keep response short. Let me give the resulting stack summary and ask the next key question — probably the IdP (Keycloak?) since that was the original topic.

I could check memory... not needed. No code changes. Keep concise.

Perfecto, con Eureka el stack queda así:

```
Nginx/LB (TLS, borde)
   ↓
Spring Cloud Gateway (JWT + roles declarativos, rate limit, routing por servicio)
   ↓  descubrimiento vía Eureka
Eureka Server ← microservicios Spring Boot (se registran solos)
```

Detalles a tener en cuenta con Eureka:

- **El gateway también se registra o consulta Eureka** para resolver `lb://nombre-del-servicio` en las rutas — no URLs hardcodeadas.
- **Eureka no es seguro por defecto**: sin auth, cualquier instancia que se registre pasa por ahí. En un entorno más allá de dev, protegelo (Spring Security en el server de Eureka + credenciales en los clientes) o al menos acotalo a la red interna.
- **Condiciones de registro**: Eureka tiene enfoque de "eviction" (instancias muertas tardan en salir, ~30s+ de caché). Está pensado para resiliencia eventual, no para descubrimiento instantáneo — para un gateway eso es aceptable, pero es bueno que lo sepas.

Falta definir la pieza que inició toda la conversación: **¿qué IdP van a usar para emitir los JWTs con roles — Keycloak self-hosted, o algo gestionado (Auth0, Cognito)?**

---

## User

Entonces que podria utilizar para roles y permisos, cuantas de usuarios administradores y clientes y no tener que desarrollar yo esa parte, keycloack esta descartado dame opciones

---

## Assistant

_Thinking:_

The user asks: options for roles/

---

## User

Entonces que podria utilizar para roles y permisos, cuantas de usuarios administradores y clientes y no tener que desarrollar yo esa parte, keycloack esta descartado dame opciones  ademas tiene que poder ser desplegable en docker o poder tener sdk o algo yo en local

---

## Assistant

_Thinking:_

The user wants alternatives to Keycloak for roles/permissions, user management (admins and clients), without developing it themselves, deployable in Docker or with an SDK, usable locally. Stack: Spring Boot microservices, Spring Cloud Gateway with Eureka.

Options to consider:
1. **Auth0** — SaaS, has free tier, SDKs, roles, organizations, local dev via tenant. No Docker (it's SaaS) but works locally.
2. **Okta** — SaaS, free developer edition, Spring Security OAuth2 integration official.
3. **Cognito (AWS)** — SaaS, Docker n/a, has SDK.
4. **Authentik** — open source, self-hosted, Docker, has roles, flows, provider (OIDC), SCIM. Very popular Keycloak alternative.
5. **Casdoor** — open source, Go, Docker, OIDC/OAuth2, roles, SAML, has SDKs, UI for user management.
6. **SuperTokens** — open source, Docker, auth, has roles... actually SuperTokens core + license. Roles feature exists (RBAC) — yes, SuperTokens has user roles / RBAC.
7. **Zitadel** — open source (SaaS too), Go, Docker, OIDC, roles/grants, built-in user management UI.
8. **Ory Kratos/Kong Hydra** — more DIY-ish, less "admin UI included".
9. **Logto** — open source, Docker, OIDC, RBAC, has admin console. Quite polished.
10. **Dex** — no UI really, mostly for connecting to LDAP/OIDC providers; not a full user management solution.
11. **Gluu / OpenIAM** — heavy.
12. **Spring Authorization Server** — but that would be developing it themselves, which they don't want. Excluded.
13. **Apereo CAS** — complex.
14. **Keycloak alternatives on Docker**: Authentik, Zitadel, Casdoor, Logto, Ory, SuperTokens.

Also "cuantas de usuarios administradores y clientes" — probably "cuantos" = how many users, admins and clients (customers) they can manage. So the solution must manage user roles (admin vs customer) and scale.

Constraints:
- Not Keycloak
- Deployable in Docker OR has SDK, works locally
- Don't develop it themselves
- Roles + permissions + user management (admins and customers)
- Java/Spring Boot ecosystem — Spring Security has first-class support for OAuth2/OIDC resource servers, so anything OIDC-compliant works: the gateway validates JWT, roles in claims (realm_access.roles or custom claims). Note: different providers put roles in different claims — need role mapping in Spring Security (JwtAuthenticationConverter).

Top recommendations:
1. **Authentik** — Docker, self-hosted, user management UI, roles/groups, OIDC provider, free community. Node-based (Python actually — authentik is Python/Django + React frontend). It's heavier but works. Good for local dev with docker compose.
2. **Zitadel** — Go, Docker, OIDC, roles with instance-level grants, nice admin console, free tier (25 users?) — actually Zitadel cloud free up to 25 users... self-hosted unlimited? License: Apache 2.0 / Zitadel has its own license with restrictions? Zitadel is Apache 2.0? Zitadel license: it's "Apache 2.0" for most... Actually Zitadel v2 license changed — Zitadel is under Apache License 2.0? Let me recall: Zitadel was Apache 2.0, then they introduced a custom license? I believe Zitadel is Apache 2.0 still... Hmm, I'm not 100% sure. Careful with license claims.
3. **Casdoor** — Go, Docker, user management, roles, has Java SDK? Casdoor has SDKs for various languages including Java I think. It's from casbin.
4. **Logto** — Node, Docker, OSS (Apache 2.0 for community), RBAC, admin console, OIDC. SaaS option too.
5. **SuperTokens** — Docker, has user roles (RBAC) feature — but RBAC might be under their business license? SuperTokens core is open source (Apache 2.0) but some features (multi tenancy, MFA...) are under enterprise license? Actually SuperTokens: core is open source, features like email/password, passwordless, session are open source; RBAC... I believe user roles are in the open source core. Not sure — mark uncertainty.
6. **SaaS options**: Auth0 (free up to 25k MAU? — pricing changes: free tier 10k MAU... Auth0 changed pricing recently, migration to Okta Workforce...), Okta (free developer, 100 MAU? Okta free is 100 monthly active users), Cognito (free up to 50k MAU? — actually Cognito free tier: 50k MAU free... pricing changed to per MAU tiers). Don't quote exact numbers — they change; just mention free tiers exist and to check current pricing.

Since this is a recommendation with a real fork (self-hosted OSS vs SaaS), presenting options is legitimate. But persona rules: "Do not present option menus, exhaustive lists, or multiple approaches unless there is a real fork with meaningful tradeoffs." There IS a real fork here: user explicitly asked "dame opciones" — so give options. They explicitly requested options.

Constraints say Docker or SDK, local. So prioritize Docker-deployable OSS: Authentik, Zitadel, Casdoor, Logto. SaaS: Auth0/Okta/Cognito work locally too (SDK/OIDC against cloud), but "desplegable en docker" suggests self-host preference.

Recommendation: Authentik or Zitadel as closest Keycloak replacements (admin UI, users, roles, groups, OIDC). For Spring: any OIDC provider works with `spring-boot-starter-oauth2-resource-server`; gateway validates JWT; roles come as claims; map with JwtAuthenticationConverter.

Should I verify with web search? Pricing and license details change. My knowledge: I should be careful. The question: "cuantas de usuarios administradores y clientes" — maybe they're asking "cuántos usuarios... aguanta" (how many users can it handle)? "cuantas de usuarios administradores y clientes" — likely "cuántos usuarios (administradores y clientes)". So they want a system that can host many users with roles admin/customer. All these handle that fine.

Plan mode: just discussion, no files. Should I use websearch to verify current state of these tools (2026)? The date is Nov 2026. My training may be stale. Pricing/licenses could have changed. A quick websearch could help verify. But the persona says verify technical claims before stating them. Key claims: Authentik/Zitadel/Casdoor/Logto are Docker-deployable, OIDC, have user/role management — these are stable facts. License specifics I'll keep vague or verify.

Let me do a quick search to check current info? Web search costs time; maybe one search for "Keycloak alternatives2026 open source docker" could surface current landscape. I think a couple of targeted searches would be prudent given claims about licenses/pricing. Let me do one or two searches.

Actually, let me think about what matters for the user's decision:
1. Self-hosted Docker vs SaaS — they said "desplegable en docker o poder tener sdk o algo yo en local" — so either Docker self-host or SDK that works locally. SaaS options also work locally (just HTTP calls), but Docker deploy seems preferred.
2. Not Keycloak.
3. Roles for admins and customers, user management included.
4. Spring Boot integration.

Top3 self-hosted Docker options: Authentik, Zitadel, Casdoor, Logto. I'd rank: Authentik (most complete admin UI, Python/Django, OIDC provider built in), Zitadel (Go, single binary-ish, nice, OIDC, roles/grants model), Logto (Node, modern, RBAC, management API), Casdoor (Go, from Casbin team, has SDKs including Java?, lots of protocols).

SaaS: Auth0, Okta, Cognito — no Docker but SDKs, free tiers.

I should verify a few facts with web search. Let me run searches in parallel: one for Authentik/Zitadel/Logto current status, maybe one for "Auth0 free tier 2026". Keep it light — maybe 2 searches.

Then give recommendation: for their case (Spring Boot, self-host, local dev): **Authentik** as first choice or **Zitadel**. With Spring: OIDC discovery, resource server in gateway.

Also mention: regardless of choice, the pattern is the same — the IdP exposes OIDC/OAuth2, issues JWT with roles in claims, Spring Cloud Gateway validates with `oauth2ResourceServer(jwt)` + JwtAuthenticationConverter mapping the roles claim (e.g., `realm_access.roles` for Keycloak, custom claim for others).

Role mapping detail is valuable: each provider puts roles in different claims; in Spring Security you configure a JwtAuthenticationConverter to extract them.

Let me do the web searches to verify current landscape/pricing/licenses.

Search1: "Authentik vs Zitadel vs Logto 2026 self-hosted identity provider docker"
Search 2: "Auth0 free tier pricing 2026" — maybe skip; mention pricing changes generally.

Actually the user asked specifically for options with Docker/SDK local. SaaS is secondary. Let me search for current state of OSS alternatives.

I'll run two searches in parallel.

**Tool: websearch**

**Input:**
```json
{
  "query": "mejores alternativas Keycloak 2026 open source identity provider docker Authentik Zitadel Logto Casdoor"
}
```

## [Casdoor vs Keycloak vs authentik vs Zitadel (2026)](https://www.casdoor.com/blog/casdoor-vs-keycloak-vs-authentik-vs-zitadel)
Published: 2026-10-08T23:38:23.000Z

## The table

| | Casdoor | Keycloak | authentik | Zitadel |
| --- | --- | --- | --- | --- |
| License | Apache-2.0 | Apache-2.0 | MIT, enterprise features under a separate license | AGPL-3.0 |
| Written in | Go and React | Java | Python and TypeScript | Go |
| GitHub stars (Oct 2026) | 14.5k | 37.2k | 25.9k | 15.3k |
| OIDC and OAuth 2.0 | Yes | Yes | Yes | Yes |
| SAML | Yes | Yes | Yes | Yes |
| CAS | Yes | No | No | No |
| Serves LDAP to apps | Yes | No (federates from LDAP) | Yes, through an outpost | No |
| Serves RADIUS | Yes | No | Yes, through an outpost | No |
| Authorization model | Casbin: ACL, RBAC, ABAC, custom | Authorization Services (policies, UMA) | Policies on applications and flows | Roles per project |
| Multi-tenancy | Organizations | Realms | Single tenant | Instances and organizations |
| Managed cloud from the maintainers | Casdoor Cloud, flat price per instance | None | None | Zitadel Cloud, priced by daily active users |

...

authentik shines at putting SSO in front of things that have none. Its outposts act as a proxy, LDAP server or RADIUS server, and its visual flow designer makes custom login and enrollment flows approachable.

...

It is single-tenant, has no hosted version, and some features sit in an enterprise edition priced per internal user.

Choose authentik for internal SSO across self-hosted apps, especially behind a reverse proxy.

...

## Our recommendation

Start from your hardest requirement:

1. Legacy protocols (CAS, LDAP, RADIUS) or fine-grained authorization: Casdoor.
2. Red Hat support or the largest community: Keycloak.
3. SSO in front of self-hosted apps without login: authentik.
4. Per-customer tenants with delegated admins: Zitadel or Casdoor organizations.

All four are free to try. Casdoor's Docker quick start takes a few minutes, or skip the setup with Casdoor Cloud.

## [Best Open Source Identity Providers for SSO & CIAM (2026)](https://www.authgear.com/post/best-open-source-identity-providers/)
Published: 2026-10-23T00:00:00.000Z

## Which open source identity provider should you pick?

- Keycloak if you need workforce SSO with LDAP, Active Directory or Kerberos, and you have a team that is comfortable running a Java service.
- Authgear if you’re building customer-facing or frontline apps and want passkeys, magic links and SMS or WhatsApp OTP built in, with the option of having it run for you.
- Authentik if you want SSO for internal tools, including older apps that need LDAP, RADIUS or a proxy in front.
- Zitadel if you’re building B2B SaaS with many customer organisations and can work with AGPL-3.0.
- Ory if you want a headless, API-first identity stack and plan to build your own login screens.
- Authelia if you want a small, fast login portal in front of a reverse proxy for self-hosted services.
- SuperTokens if you want auth embedded in your own app code, with SDKs for your frontend and backend.
- Logto if you want a modern TypeScript stack with pre-built sign-in, organisations and RBAC.

...

| | Licence | Stack | OIDC provider | SAML | Passkeys | Vendor-managed cloud | Best for |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Keycloak | Apache-2.0 | Java | Yes | Yes | Yes | No (third parties only) | Workforce SSO, enterprise federation |
| Authgear | Apache-2.0 | Go | Yes | Yes | Yes | Yes, plus private cloud | Customer and frontline login |
| Authentik | MIT core, enterprise features under a separate licence | Python, Go | Yes | Yes | Yes | No | Internal tools, older apps, homelabs |
| Zitadel | AGPL-3.0 (some parts Apache-2.0 or MIT) | Go, Next.js login | Yes | Yes | Yes | Yes | Multi-tenant B2B SaaS |
| Ory (Kratos, Hydra) | Apache-2.0, with enterprise features under the Ory Enterprise License | Go | Yes (Hydra) | Enterprise licence or Ory Network | Yes | Yes (Ory Network) | Headless, API-first identity |
| Authelia | Apache-2.0 | Go | Yes (beta) | No (planned) | Yes | No | Reverse-proxy login for self-hosted services |
| SuperTokens | Apache-2.0 core, `ee/` folder under a separate licence | Java core, Node.js / Python / Go SDKs | Managed service only | As a client, via multi-tenancy (paid) | Yes | Yes | Auth embedded in your own app |
| Logto | MPL-2.0 | TypeScript (Node.js) | Yes | Yes (3 SAML apps in the free edition) | Yes | Yes | Developer-friendly CIAM for SaaS |

...

## Authentik

A self-hosted identity provider built around flows: you chain stages such as identification, password and MFA in a visual editor.

- Licence: MIT for the core. Code under `authentik/enterprise/` uses a separate licence.
- Stack: Python (Django) with Go outposts. Needs only PostgreSQL since 2025.10. Current release 2026.8.
- Protocols: OIDC, OAuth 2.0 and SAML 2.0, plus LDAP, RADIUS, SCIM and proxy providers.
- Login UI: Flows and stages, with CSS customisation.
- Passkeys and MFA: WebAuthn passkeys, TOTP, static codes, Duo, SMS and email codes.
- Hosting: Self-host only; there is no hosted version.

### Where it wins

- Quick to start with Docker Compose or Helm.
- One tool covers OIDC, SAML, LDAP, RADIUS and a proxy for apps with no SSO of their own.
- Visual flows make login journeys easy to follow.

### Watch-outs

- Google Workspace and Microsoft Entra ID integrations, mTLS, privileged access management and enhanced audit logging need Enterprise ($5 per internal user a month, $0.02 per external user a month).
- Smaller enterprise ecosystem than Keycloak.

Best for: small and mid-sized teams, internal tools and homelabs, and company-wide SSO where older apps need LDAP, RADIUS or a proxy.

## Zitadel

An identity platform built for multi-tenant SaaS, with an event-sourced core.

- Licence: AGPL-3.0 since v3 in 2025, when it moved from Apache-2.0. The `proto/` folder and docs are Apache-2.0; the login app and client packages are MIT. A commercial licence is available.
- Stack: Go and PostgreSQL. The new Login V2 is a Next.js app. Current release 4.20.
- Protocols: OpenID Certified OIDC, SAML 2.0 and a SCIM 2.0 server.
- Login UI: Hosted login pages, with Login V2 as the customisable option.
- Passkeys and MFA: Passkeys (FIDO2 / WebAuthn), TOTP, U2F, and email and SMS OTP.
- Hosting: Self-host, or Zitadel Cloud in US, EU, Australian and Swiss regions. Both run the same codebase.

...

## Logto

A developer-focused identity platform for SaaS and AI apps.

- Licence: MPL-2.0.
- Stack: TypeScript on Node.js, with PostgreSQL. Current release 1.45.
- Protocols: OIDC, OAuth 2.1 and SAML. The free self-hosted edition includes three SAML applications.
- Login UI: Pre-built sign-in experience with customisable branding and SDKs for 30+ frameworks. “Bring your UI” is a paid self-hosted feature.
- Passkeys and MFA: Passkeys for sign-in and as a second factor, plus TOTP.
- Hosting: Self-host, or Logto Cloud in US, EU and Australian regions.

...

- Organisations, RBAC, enterprise SSO and just-in-time provisioning included.

## [Feature comparison | Casdoor](https://casdoor.ai/docs/comparison/feature-comparison/)

## Overview​

| | Casdoor | Keycloak | ZITADEL | authentik |
| --- | --- | --- | --- | --- |
| Language | Go, React UI | Java (Quarkus) | Go, TypeScript login UI | Python and Go |
| License | Apache-2.0 | Apache-2.0 | AGPL-3.0 | MIT core, enterprise features under a commercial license |
| Databases | MySQL, MariaDB, PostgreSQL, SQL Server, Oracle, SQLite, TiDB, CockroachDB | PostgreSQL, MySQL, MariaDB, SQL Server, Oracle | PostgreSQL | PostgreSQL |
| Multi-tenancy | Organizations, each with its own users, applications, providers, and roles | Realms, plus Organizations inside a realm | Instances, and organizations inside an instance | Brands for separate branding; separate tenants are an alpha enterprise feature |

## Protocols​

| | Casdoor | Keycloak | ZITADEL | authentik |
| --- | --- | --- | --- | --- |
| OAuth 2.0 / OIDC provider | Yes | Yes | Yes | Yes |
| SAML 2.0 identity provider | Yes | Yes | Yes | Yes |
| Sign in through external SAML / OIDC providers | Yes | Yes | Yes | Yes |
| CAS server | Built in | Community extension | No | No |
| LDAP server (applications bind to the IdP) | Built in | No | No | LDAP outpost |
| RADIUS server | Built in | Community extension | No | RADIUS outpost |
| MCP server and OAuth 2.1 for MCP | Built in | No built-in MCP server | No built-in MCP server | No built-in MCP server |

...

| | Casdoor | Keycloak | ZITADEL | authentik |
| --- | --- | --- | --- | --- |
| Kubernetes | Helm chart, operator | Operator | Helm chart | Helm chart |
| Horizontal scaling | Multiple replicas, with Redis for sessions | Clustering with Infinispan, multi-site | Stateless replicas | Multiple server and worker replicas |
| Infrastructure as code | Terraform provider, Kubernetes resources | Terraform provider | Terraform provider | Terraform provider |
| Declarative config file | Init data (JSON or YAML, also as Helm values), applied again when it changes | Realm import (JSON) at startup | Setup steps for the first instance only | Blueprints (YAML), applied again when they change |
| Audit log | Records of every API call, with retention settings and webhooks | Login and admin events | Event-sourced: every change is stored as an event | Events, with notification rules |

## [Is there something simpler than Keycloak? | loginos](https://loginos.io/answers/keycloak-alternatives)
Published: 2026-10-14T07:20:15.000Z

- Zitadel is written in Go and stores its data in PostgreSQL. It is OpenID Connect certified and supports SAML 2.0, passkeys, LDAP as a login source and SCIM 2.0, with organizations built in for B2B apps. It is licensed under AGPL-3.0, and Zitadel Cloud runs the same code as a hosted service.
- authentik is written mostly in Python. It speaks OAuth 2.0 and OIDC, SAML, LDAP and RADIUS, and its proxy provider can put a login in front of apps that have none. The company behind it offers no hosted version.
- Authelia is a Go server that adds single sign-on and two-factor sign-in to apps behind a reverse proxy such as NGINX, Traefik, Caddy or HAProxy. It is also an OpenID Connect provider. Users come from a YAML file or from LDAP, and SAML is on its roadmap, not shipped.
- Logto is written in TypeScript and runs on Node.js with PostgreSQL. It covers OIDC, OAuth 2.1 and SAML, with organizations and role-based access control. Logto Cloud is the hosted version, and the open-source edition has a single admin account.
- Ory is a set of Go services: Kratos for accounts and sessions, Hydra for OAuth 2.0 and OIDC, Keto for permissions and Polis for SAML. Ory is headless, so you build the login screens yourself. Ory Network is the hosted version.
- Dex is a small Go service that issues OIDC tokens and passes sign-ins on to identity sources you already have, through connectors for LDAP, SAML 2.0, GitHub and other OIDC providers. It is a CNCF sandbox project, and its docs include a Kubernetes guide.
- Casdoor, from the Casbin community, is a Go server with a React admin console. It supports OAuth 2.0, OIDC, SAML 2.0, CAS, LDAP, SCIM 2.0, WebAuthn and TOTP.

| Project | Self-hosted | Written in | Protocols | Good for |
| --- | --- | --- | --- | --- |
| Keycloak | Yes, or managed by third parties | Java | OIDC, SAML, LDAP federation | Everything, if you can run it |
| Zitadel | Yes, or Zitadel Cloud | Go | OIDC, SAML, LDAP | B2B apps with customer organizations |
| authentik | Yes, self-hosted only | Python | OIDC, SAML, LDAP, RADIUS | Many self-hosted apps behind one login |
| Authelia | Yes | Go | OIDC, LDAP for users, SAML planned | Apps behind a reverse proxy |
| Logto | Yes, or Logto Cloud | TypeScript | OIDC, SAML | SaaS apps with organizations and roles |
| Ory | Yes, or Ory Network | Go | OIDC, SAML through Polis | Teams that want APIs and their own UI |
| Dex | Yes | Go | OIDC, with LDAP and SAML as sources | Bridging existing directories to OIDC |
| Casdoor | Yes | Go | OIDC, SAML, LDAP, CAS | Many protocols in one server with a web UI |
| loginos | No, hosted only | Not open source | OIDC | Products on many domains, nothing to run |

## [Open-Source Identity Servers Compared](https://www.casdoor.com/blog/open-source-identity-server)
Published: 2026-11-01T16:38:10.000Z

## The shortlist

| Project | License | Protocols | Managed option |
| --- | --- | --- | --- |
| Keycloak | Apache-2.0 | OIDC, SAML 2.0 | Third-party only |
| Casdoor | Apache-2.0 | OIDC, SAML 2.0, CAS, SCIM, LDAP | Yes, from $9 / month |
| authentik | MIT (core) | OIDC, SAML 2.0, LDAP, RADIUS | Yes |
| Zitadel | AGPL-3.0 | OIDC, SAML 2.0 | Yes |
| Ory Kratos | Apache-2.0 | OIDC (via Ory Hydra for OAuth 2.0) | Yes (Ory Network) |
| Logto | MPL-2.0 | OIDC, SAML 2.0 | Yes |

Licenses matter: AGPL-3.0 (Zitadel) has obligations a company may not want if it modifies and distributes the software, and MPL-2.0 (Logto) is file-level copyleft. Apache-2.0 and MIT are the permissive options most enterprises prefer.

### Keycloak

The reference open-source identity server. It has the largest ecosystem, the longest track record and integrations for almost everything. It is also the heaviest to operate: a Java service with its own database schema, a regular upgrade cadence and a configuration model that rewards experience.

Choose it when you need maximum protocol coverage, a large community and you have platform engineers to run it.

### Casdoor

Casdoor is Apache-2.0 and covers OIDC, SAML 2.0, CAS, SCIM and LDAP, plus 75+ social providers, MFA and passkeys. It is lighter than Keycloak to deploy (a Go binary and a database), and it has a managed cloud from $9 per month with unlimited MAU if you would rather not operate it.

Choose it when you want the protocols without a Java stack, or when the per-user pricing of commercial CIAM is the problem.

### authentik

authentik's core is MIT-licensed, with an enterprise directory under a separate license. It covers OIDC, SAML, LDAP and RADIUS, and it is popular for homelab and self-hosted application estates because of its proxy and application catalog features.

Choose it when you want a self-hosted identity provider with strong "connect my apps" ergonomics.

### Zitadel

Zitadel is AGPL-3.0 and focuses on multi-tenancy and a modern API-first design. Its cloud free tier is measured in daily active users (100), which is much smaller than MAU-based allowances elsewhere.

Choose it when its multi-tenant model fits and the AGPL license is acceptable.

...

Logto is MPL-2.0 and positioned as a developer-friendly CIAM product with a managed cloud.

## [Keycloak vs Authentik vs Zitadel (2026) · ShipGarden](https://www.shipgarden.com/gallery/keycloak-vs-authentik-vs-zitadel-self-hosted-sso-2026)
Published: 2026-09-04T15:16:35.000Z

## Quick answer (2026)

If you need a self-hosted identity provider and you do not want a licence conversation later, run Keycloak. It is Apache-2.0 across the whole tree, one `LICENSE.txt` at the root, and there is no directory anywhere in the repository carrying different terms. Every feature it has, you may run in production. The bill is paid in operational weight instead.

If you want the fastest thing to operate and you are comfortable reading a copyleft licence carefully, run Zitadel. We measured its published image at 50.7 MiB compressed across 3 layers, roughly a fifth of Keycloak, and it wants nothing but PostgreSQL. The core is AGPL-3.0-only, and the parts you actually write code against are deliberately not.

Run authentik when its administrative model fits how you think, and go in knowing that a named set of features sits behind a commercial licence that lives inside the same repository you cloned. That set is smaller and more specific than most write-ups suggest, and we corrected our own first reading of it below.

...

| Project | GitHub reports | What the file actually says |
| --- | --- | --- |
| Keycloak | `Apache-2.0` | One `LICENSE.txt`, Apache-2.0, no carve-outs |
| authentik | `NOASSERTION` | MIT, with `authentik/enterprise/` under a separate commercial licence |
| Zitadel | `AGPL-3.0` | AGPL-3.0-only, with MIT and Apache-2.0 carve-outs |

...

1.

## [Keycloak Alternatives: Stack, Security Gate and Recommendation](https://hackmd.io/@sBpHjmN5SYCSYba1ULKYGA/SylwAnJTfg)

1. No candidate clears the security gate more convincingly than Keycloak 26.8.1. Keycloak had a very heavy 2026 advisory load, but every issue found is tied to a fixed release, and 26.8.1 (13 Oct 2026) is the only line that carries all fixes. Treat Keycloak as the benchmark, not the problem.
2. Preferred alternative: authentik, conditionally. Current release 2026.8.5 (21 Oct 2026), a documented security process (advance notice, GHSA plus CVE page, backports to two supported branches), and a simpler runtime (server, worker, PostgreSQL; Redis removed in 2025.10). Condition: the security content of 2026.8.0 to 2026.8.5 could not be retrieved, so confirm it before production.
3. Second: ZITADEL, conditionally. Clear fixed-version ranges for each 2026 CVE, single database (PostgreSQL), Go runtime. Conditions: AGPL-3.0 obligations, the official latest release number is unverified (v4.19.4 was seen only in a third-party fork), and one Low SSRF fix was not backported to the v3 line.
4. Third: Logto, for a narrower use case. Current release v1.45.0 (13 Oct 2026), prompt fixes (SSRF fixed in 1.43.0, MFA step-up bypass fixed in 1.41.0). It is customer-identity (CIAM) oriented, not a workforce IdP; some features are now licence-gated on self-hosted.
5. Flagged, do not adopt without vendor confirmation: Casdoor (several critical SAML and token flaws with no fixed version published; vendor reported non-responsive in two advisories) and SuperTokens Core (CVE-2026-37171 cross-tenant access, versions 6.0.0 to 11.4.0, no fix identified). Ory is good engineering but is a toolkit, not a Keycloak replacement, and its open-source container dependency findings are unresolved in a public issue.
6. Runtime context. None of the candidates runs PHP in its production runtime. The PHP "PHP2Shell" critical RCEs (21 Oct 2026) matter only for PHP relying-party apps, SDKs and CMS sites that sit behind the IdP. Node.js, Go, Java and Python all shipped 2026 security releases that the relevant images must include (section 4.2).

...

| Product | What it is | Full Keycloak replacement? |
| --- | --- | --- |
| Keycloak (baseline) | Java IAM server; OIDC, OAuth 2.0, SAML, LDAP/Kerberos federation, fine-grained admin, UMA, token exchange, SCIM (supported and on by default in 26.8) | n/a |
| authentik | Flow-based IdP with providers for OIDC, SAML, LDAP, RADIUS, SCIM and proxy outposts | Yes for most workforce and self-hosted SSO; WS-Federation is Enterprise-only |
| ZITADEL | Event-sourced multi-tenant IAM (Go), organisations, OIDC, SAML, LDAP as an upstream IdP | Largely yes for SaaS and B2B; LDAP/Kerberos federation depth not verified |
| Logto | CIAM / auth infrastructure for SaaS and AI apps; OIDC, OAuth 2.1, SAML, organisations, RBAC | Partly; narrower, developer-led |
| Casdoor | Go-based IAM/SSO with SAML and OAuth | Feature-wise broad; security gate fails (below) |
| Ory Kratos + Hydra (+ Keto, Oathkeeper, Polis) | Headless identity, OAuth2/OIDC server, permissions, proxy, SAML bridge | No: composable building blocks, no bundled admin product |
| SuperTokens | SDK-driven auth (sessions, passwordless, social) with a Java Core service | No: embedded app-auth, not a central IdP |

...

1. Deploy each in Docker Compose behind a TLS reverse proxy (Caddy or Traefik, Let's Encrypt), pinned by image digest.

...

Database: PostgreSQL 16 or 18 with a named volume, `pg_dump` and WAL-based point-in-time backups, restore drill to a clean host, record RTO and RPO.
3. Secrets: Docker/Kubernetes secrets or Vault; rotate ZITADEL masterkey handling, authentik secret key, Logto signing keys; confirm none appear in env dumps or logs.
4. OIDC integration: a sample app (Node and a PHP app) using authorization code with PKCE; test refresh rotation, logout and token introspection.

...

MFA and sessions: TOTP and WebAuthn enrolment, step-up, session revocation, idle and absolute

...

6. RBAC: roles and groups mapped to application claims; delegated-admin tests that replay the 2026 escalation classes (group-role mapping, cross-org reads).

...

Scanning: Trivy and Grype on images; OWASP Dependency-Check for Java and npm; `govulncheck` for Go binaries; `pip-audit` for Python; OSV-Scanner across lockfiles.

## [Best Self-Hosted Authentication & SSO in 2026 | selfhosting.sh](https://selfhosting.sh/best/authentication/)
Published: 2026-02-20T00:00:00.000Z

## Quick Picks

| Use Case | Best Choice | Why |
| --- | --- | --- |
| Best overall for homelabs | Authentik | Full identity provider with clean UI and reasonable resource usage |
| Best lightweight SSO | Authelia | Minimal resources, works as a reverse proxy auth layer |
| Best for enterprise | Keycloak | Industry standard, protocol support, federation |
| Best modern alternative | Zitadel | Cloud-native, event-sourced, built-in OIDC/SAML |

**Tool: websearch**

**Input:**
```json
{
  "query": "Authentik vs Zitadel vs Logto self-hosted OIDC 2026 comparison license"
}
```

## [Zitadel vs authentik: License, Pricing and Features Compared (2026)](https://www.casdoor.com/blog/zitadel-vs-authentik)
Published: 2026-10-25T17:18:28.000Z

| | Zitadel | authentik |
| --- | --- | --- |
| License | AGPL-3.0 (since v3, 2025); the login app and client libraries are MIT | MIT, except the `enterprise` directory, which needs a paid license in production |
| Written in | Go, with a separate Next.js login app | Python (Django), behind a Go web server |
| Database | PostgreSQL 14 to 18 only; Redis optional as a cache | PostgreSQL only; Redis was removed in 2025.10 |
| Managed cloud | Zitadel Cloud | None: "We do not currently provide a hosted version" |
| Paid plans | Cloud Pro $100/month with 25,000 DAU; Enterprise custom, cloud or self-hosted | Enterprise $5 per user and $0.02 per external user per month, billed yearly; Enterprise Plus from $20k a year |
| OIDC / OAuth 2.0 / SAML IdP | Yes | Yes |
| LDAP | As an upstream identity provider | Serves an LDAP directory of authentik users through an outpost |
| RADIUS, proxy authentication, remote desktop | Not in the docs | Yes, through outposts |
| SCIM | SCIM 2.0 server for inbound user provisioning (preview) | Outbound SCIM provider, plus an inbound SCIM source |
| Multi-tenancy | Instances and organizations, a core feature | Brands per domain; full tenancy is an Enterprise feature in alpha |
| Customization | Actions v2: webhooks called at request, response, function and event points | Flows and stages, plus Python expression policies |
| Audit | Event-sourced: every change is stored as an event | Event log, kept 365 days by default |
| GitHub stars (October 2026) | About 15,400 | About 26,100 |

...

This is often the deciding factor.

- Zitadel moved from Apache-2.0 to AGPL-3.0 with version 3 (announced March 2025, released May 2025). If you modify Zitadel and let users interact with it over a network, you must offer them your modified source. The login app and the client packages are MIT, so customizing the login UI doesn't trigger that. Zitadel sells a commercial license with Enterprise.
- authentik's core is MIT, which is about as permissive as it gets. Code in the `authentik/enterprise/` directory is under authentik's Enterprise license: free for development and testing, but production use needs a subscription.

Teams that can't accept AGPL usually rule out Zitadel here; teams that can't run code under a commercial license usually just leave authentik's enterprise features off.

...

Zitadel Cloud is free for 100 daily active users and US$100 per month on Pro with 25,000 DAU included; Enterprise is custom-priced and can run on your own infrastructure. The fine print matters: Zitadel adds up the daily active user count of every day in the month. So 25,000 DAU is not 25,000 people: 1,000 employees who sign in every workday already use about 22,000. Zitadel doesn't publish a price for going over on its pricing page. Self-hosting the AGPL code is free.

authentik has no hosted version. The open-source edition is free, with community support only. Enterprise is $5 per user per month and $0.02 per external user per month, billed yearly, and adds features such as WS-Federation, Chrome device trust, mutual TLS and enhanced audit logging; Enterprise Plus starts at $20,000 a year.

## [Logto vs Authentik vs Zitadel (2026) — OSSAlt Guides | OSSAlt](https://ossalt.com/guides/logto-vs-authentik-vs-zitadel-2026)
Published: 2026-03-08T00:00:00.000Z

| Feature | Logto | Authentik | Zitadel |
| --- | --- | --- | --- |
| GitHub Stars | 12K+ | 20K+ | 13K+ |
| Primary use case | SaaS apps | Enterprise SSO | B2B cloud apps |
| Developer experience | Excellent | Good | Good |
| SAML | Yes | Yes | Yes |
| OIDC/OAuth2 | Yes | Yes | Yes |
| LDAP | Limited | Yes | Yes |
| RADIUS | No | Yes | No |
| Application proxy | No | Yes | No |
| Organizations/tenancy | Yes | Limited | Yes |
| Pre-built UI | Yes | Yes | Yes |
| Self-hosting | Easy | Medium | Medium |
| License | MPL-2.0 | MIT | Apache 2.0 |

## [Logto vs ZITADEL: Customer Identity and Auth Platforms Comparison (2026)](https://faun.dev/toolbox/logto-vs-zitadel/)

| Criterion | Logto Rate Logto | ZITADEL Rate ZITADEL |
| --- | --- | --- |
| About | Logto is an open source auth server for SaaS and AI apps, built on OpenID Connect and OAuth 2.1, that you can run yourself for free or use as Logto Cloud with 50,000 free monthly active users. | ZITADEL is an open source identity platform in Go built around organizations for B2B SaaS, with an event-sourced audit trail, the same code for cloud and self-hosting, and AGPL-3.0 since 2025. |
| Description | The hosted Logto Cloud is free up to 50,000 monthly active users. The Pro plan starts at 24 dollars a month per tenant with no user cap, but it meters access tokens past the first 50,000 (8 cents per 100) and sells RBAC, organizations, MFA and enterprise SSO as separate add-ons, so the real bill depends on which of those you switch on. Self-hosting the MPL-2.0 code costs nothing in licences; paid self-hosted plans start at 199 dollars a month, billed yearly, for commercial features and email support. You get prebuilt sign-in pages, SDKs for more than 30 frameworks, social and enterprise connectors, passkeys, machine-to-machine apps and an organizations model for B2B products. It fits a tea… The hosted Logto Cloud is free up to 50,000 monthly active users. The Pro plan starts at 24 dollars a month per tenant with no user cap, but it meters access tokens past the first 50,000 (8 cents per 100) and sells RBAC, organizations, MFA and enterprise SSO as separate add-ons, so the real bill depends on which of those you switch on. Self-hosting the MPL-2.0 code costs nothing in licences; paid self-hosted plans start at 199 dollars a month, billed yearly, for commercial features and email support. You get prebuilt sign-in pages, SDKs for more than 30 frameworks, social and enterprise connectors, passkeys, machine-to-machine apps and an organizations model for B2B products. It fits a team that wants a standards-based OIDC provider it can later move in-house. The token meter and the add-on list take some modelling before a cloud bill is predictable. Logto is built by Silverhand Inc. and has been developed in the open on GitHub since 2021, with a monthly release rhythm on the 1.x line. more less | Most identity servers add tenants later; ZITADEL starts with them. An instance holds organizations, each with its own identity providers, branding, domains and users, and organizations can grant each other access to projects, which suits a SaaS company whose customers bring their own SSO. Underneath, every change is written as an immutable event in PostgreSQL, so the audit trail is the data model rather than a log bolted on. It covers the usual customer login surface: passwords, passkeys, OTP by app, email or SMS, social and enterprise identity providers, LDAP, device authorization, token exchange and impersonation. It is a certified OpenID Connect provider, speaks SAML 2.0, exposes a SCIM… Most identity servers add tenants later; ZITADEL starts with them. An instance holds organizations, each with its own identity providers, branding, domains and users, and organizations can grant each other access to projects, which suits a SaaS company whose customers bring their own SSO. Underneath, every change is written as an immutable event in PostgreSQL, so the audit trail is the data model rather than a log bolted on. It covers the usual customer login surface: passwords, passkeys, OTP by app, email or SMS, social and enterprise identity providers, LDAP, device authorization, token exchange and impersonation. It is a certified OpenID Connect provider, speaks SAML 2.0, exposes a SCIM 2.0 server, and runs custom logic through Actions v2 webhooks. There is a Terraform provider and example apps for more than twenty frameworks. Self-hosting needs PostgreSQL 14 or later; CockroachDB support was dropped in v3. The repository opened in March 2020 and v1.0 shipped in April 2021. On 2 May 2025, with v3.0.0, the main code moved from Apache-2.0 to AGPL-3.0, with a commercial licence on request; v4.17.3 came out on 4 September 2026. ZITADEL Cloud charges by daily active users: 100 DAU free, and Pro at 100 dollars a month for 25,000 DAU (September 2026). more less |
| General | | |
| Ease of use How fast you get productive: setup, defaults, learning curve. | 78 One docker compose command starts a local server with its admin console, and the prebuilt sign-in pages work before you write any UI. The OIDC concepts behind API resources, scopes and organization roles still take a read before production. | 68 The cloud console and quickstarts get an app signing in fast, and the hosted login UI works out of the box. The instance, organization and project hierarchy is the price of its B2B strength: it has to be understood before roles and grants make sense, and self-hosting adds PostgreSQL and upgrades to run. |
| Maturity & stability Battle-tested, stable releases, few breaking changes. | 72 Developed on GitHub since June 2021 and shipping a minor 1.x release every month (1.43.0 on 31 August 2026) from a venture-backed company, Silverhand Inc. No ownership change or repositioning, and the product is younger than the long-standing identity servers. | 72 Public since March 2020, v1.0 on 20 April 2021, v4.0 on 31 July 2025 and v4.17.3 on 4 September 2026, with patches every one to three weeks and the v3 line still maintained. Four major versions in five years, the switch to AGPL-3.0 in May 2025 and the removal of CockroachDB support in v3 mean upgrades need reading. |
| Capabilities | | |
| Integrations What it plugs into, and which standards and formats it speaks. | 82 Speaks OpenID Connect, OAuth 2.1 and SAML, ships SDKs for more than 30 frameworks, and connects to social and enterprise identity providers through a connector catalogue. Machine-to-machine apps and Model Context Protocol servers are documented use cases. | 80 Certified OpenID Connect, OAuth 2.0, SAML 2.0, a SCIM 2.0 server, LDAP as an identity source, Actions v2 webhooks, a Terraform provider and example apps for more than twenty frameworks from Angular to Rust. |
| Login methods Passwords, social login, passkeys, magic links, OTP and MFA available out of the box. | 84 Passwords, email and SMS passcodes, social login, enterprise SSO over SAML and OIDC, and passkeys are built in, with TOTP, WebAuthn and backup codes for MFA. On the cloud, MFA is part of a paid add-on. | 80 Passwords, passkeys, OTP by authenticator app, email or SMS, U2F keys, social and enterprise identity providers, LDAP, device authorization and impersonation. Magic links are not listed among the documented methods. |
| B2B and multi-tenancy Organizations, per-tenant SSO and SCIM, invitations, roles and a self-serve admin portal. | 80 Organizations come with their own roles, member invitations and just-in-time provisioning tied to enterprise SSO connections, which is the model a B2B SaaS needs. On Logto Cloud the organizations and SSO pieces are billed as add-ons. | 90 Organizations are the core of the model: unlimited organizations, each with its own identity providers, domains, branding and roles, cross-organization project grants, a SCIM 2.0 server and a management console for delegated administration. |
| Community & ecosystem | | |
| Documentation Quality and depth of docs, guides and examples. | 80 Framework quickstarts, an OpenAPI reference for the management API and a separate auth wiki that explains the protocols in plain terms. Some pages mix cloud and self-hosted instructions, so you check which edition a feature applies to. | 76 Versioned documentation (v4.15 to v4.17 online in September 2026) with concept pages on the organization model, guides, a full API reference and example apps per framework. Operating a large self-hosted install is covered by deployment pages rather than an operations guide. |
| Community & support Size of the community and how easy it is to get help. | 54 About 14,600 GitHub stars and an active Discord where the team answers. Free help stops there: email support starts with the paid self-hosted plan at 199 dollars a month, billed yearly. | 70 About 15,000 GitHub stars, around 260 contributors, GitHub Discussions and a Discord of roughly 4,700 members in September 2026, where the team answers. Commercial support comes with Pro and Enterprise. |
| Operations | | |
| Portability Whether you can self-host it, export your data, and leave without a rewrite. | 86 The same MPL-2.0 code runs on Logto Cloud or on your own servers with PostgreSQL, so moving in-house does not change the product. Apps talk standard OIDC, which keeps the exit cost to configuration. | 86 The same code runs the cloud and a self-hosted install on your own PostgreSQL, user import accepts password hashes in argon2, bcrypt, scrypt, pbkdf2 and several legacy formats, and data leaves through the APIs and event stream. AGPL-3.0 means modifications offered over a network must be shared, which some companies' legal teams read carefully. |
| Security | | |
| Attack protection Bot detection, brute-force and credential-stuffing defence, breached password checks. | 68 CAPTCHA through reCAPTCHA Enterprise or Cloudflare Turnstile, identifier lockout after failed attempts, a blocklist and a password policy that rejects passwords found in Have I Been Pwned. Adaptive, risk-based sign-in checks are not part of the documented set. | 52 A lockout policy caps password and OTP attempts and a complexity policy sets password rules. Bot detection, CAPTCHA and breached-password checks are not documented, so they need a WAF or custom Actions. |
| Cost | | |
| Cost of ownership Total cost to run it: license or subscription, hosting, and the time it takes to operate. | 76 Free up to 50,000 monthly active users in the cloud, and free to self-host apart from a Node.js service and a PostgreSQL database to run. On Pro, RBAC, organizations, MFA and enterprise SSO are each separate add-ons on top of the 24-dollar base. | 76 Free to self-host apart from running PostgreSQL and the service; the cloud's free tier stops at 100 daily active users and Pro is 100 dollars a month for 25,000 DAU at September 2026 prices, which is inexpensive for a busy B2B app. |
| Pricing transparency How clear and predictable the pricing is. Free and open source counts as fully transparent. | 74 The base price, the token overage rate (8 cents per 100 tokens past 50,000) and each add-on price are on the public page. Enterprise and private cloud are quoted, and the token meter needs an estimate of your traffic. | 68 Free and Pro prices are on the pricing page at September 2026, but the overage rate beyond 25,000 DAU is not shown and Enterprise is quoted. Counting daily rather than monthly actives also makes comparison with MAU-priced services harder.

## [Keycloak vs Authentik vs Zitadel (2026) · ShipGarden](https://www.shipgarden.com/gallery/keycloak-vs-authentik-vs-zitadel-self-hosted-sso-2026)
Published: 2026-09-04T15:16:35.000Z

| Project | GitHub reports | What the file actually says |
| --- | --- | --- |
| Keycloak | `Apache-2.0` | One `LICENSE.txt`, Apache-2.0, no carve-outs |
| authentik | `NOASSERTION` | MIT, with `authentik/enterprise/` under a separate commercial licence |
| Zitadel | `AGPL-3.0` | AGPL-3.0-only, with MIT and Apache-2.0 carve-outs |

## [authentik vs Logto: Customer Identity and Auth Platforms Comparison (2026)](https://faun.dev/toolbox/authentik-vs-logto/)

| Ease of use How fast you get productive: setup, defaults, learning curve. ... | 78 One docker compose command starts a local server with its admin console, and the prebuilt sign-in pages work before you write any UI. The OIDC concepts behind API resources, scopes and organization roles still take a read before production. |
| Maturity & stability Battle-tested, stable releases, few breaking changes. ... |
| Capabilities | | |
| Integrations What it plugs into, and which standards and formats it speaks.

## [Best Open Source Identity Providers for SSO & CIAM (2026)](https://www.authgear.com/post/best-open-source-identity-providers/)
Published: 2026-10-23T00:00:00.000Z

| | Licence | Stack | OIDC provider | SAML | Passkeys | Vendor-managed cloud | Best for |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Keycloak | Apache-2.0 | Java | Yes | Yes | Yes | No (third parties only) | Workforce SSO, enterprise federation |
| Authgear | Apache-2.0 | Go | Yes | Yes | Yes | Yes, plus private cloud | Customer and frontline login |
| Authentik | MIT core, enterprise features under a separate licence | Python, Go | Yes | Yes | Yes | No | Internal tools, older apps, homelabs |
| Zitadel | AGPL-3.0 (some parts Apache-2.0 or MIT) | Go, Next.js login | Yes | Yes | Yes | Yes | Multi-tenant B2B SaaS |
| Ory (Kratos, Hydra) | Apache-2.0, with enterprise features under the Ory Enterprise License | Go | Yes (Hydra) | Enterprise licence or Ory Network | Yes | Yes (Ory Network) | Headless, API-first identity |
| Authelia | Apache-2.0 | Go | Yes (beta) | No (planned) | Yes | No | Reverse-proxy login for self-hosted services |
| SuperTokens | Apache-2.0 core, `ee/` folder under a separate licence | Java core, Node.js / Python / Go SDKs | Managed service only | As a client, via multi-tenancy (paid) | Yes | Yes | Auth embedded in your own app |
| Logto | MPL-2.0 | TypeScript (Node.js) | Yes | Yes (3 SAML apps in the free edition) | Yes | Yes | Developer-friendly CIAM for SaaS |

...

## Authentik

A self-hosted identity provider built around flows: you chain stages such as identification, password and MFA in a visual editor.

- Licence: MIT for the core. Code under `authentik/enterprise/` uses a separate licence.
- Stack: Python (Django) with Go outposts. Needs only PostgreSQL since 2025.10. Current release 2026.8.
- Protocols: OIDC, OAuth 2.0 and SAML 2.0, plus LDAP, RADIUS, SCIM and proxy providers.
- Login UI: Flows and stages, with CSS customisation.
- Passkeys and MFA: WebAuthn passkeys, TOTP, static codes, Duo, SMS and email codes.
- Hosting: Self-host only; there is no hosted version.

### Where it wins

- Quick to start with Docker Compose or Helm.
- One tool covers OIDC, SAML, LDAP, RADIUS and a proxy for apps with no SSO of their own.
- Visual flows make login journeys easy to follow.

### Watch-outs

- Google Workspace and Microsoft Entra ID integrations, mTLS, privileged access management and enhanced audit logging need Enterprise ($5 per internal user a month, $0.02 per external user a month).
- Smaller enterprise ecosystem than Keycloak.

Best for: small and mid-sized teams, internal tools and homelabs, and company-wide SSO where older apps need LDAP, RADIUS or a proxy.

## Zitadel

An identity platform built for multi-tenant SaaS, with an event-sourced core.

- Licence: AGPL-3.0 since v3 in 2025, when it moved from Apache-2.0. The `proto/` folder and docs are Apache-2.0; the login app and client packages are MIT. A commercial licence is available.
- Stack: Go and PostgreSQL. The new Login V2 is a Next.js app. Current release 4.20.
- Protocols: OpenID Certified OIDC, SAML 2.0 and a SCIM 2.0 server.
- Login UI: Hosted login pages, with Login V2 as the customisable option.
- Passkeys and MFA: Passkeys (FIDO2 / WebAuthn), TOTP, U2F, and email and SMS OTP.
- Hosting: Self-host, or Zitadel Cloud in US, EU, Australian and Swiss regions. Both run the same codebase.

### Where it wins

- Organisations, projects and roles fit B2B SaaS that signs up many customer companies.
- A managed cloud with regional choice, plus self-hosting from one codebase.

### Watch-outs

- AGPL-3.0 can create obligations if you modify Zitadel and offer it over a network. Zitadel itself recommends legal advice.
- Smaller community than Keycloak.

Best for: B2B SaaS teams that need multi-tenancy and are comfortable with AGPL or a commercial licence.

...

## Logto

A developer-focused identity platform for SaaS and AI apps.

- Licence: MPL-2.0.
- Stack: TypeScript on Node.js, with PostgreSQL. Current release 1.45.
- Protocols: OIDC, OAuth 2.1 and SAML. The free self-hosted edition includes three SAML applications.
- Login UI: Pre-built sign-in experience with customisable branding and SDKs for 30+ frameworks. “Bring your UI” is a paid self-hosted feature.
- Passkeys and MFA: Passkeys for sign-in and as a second factor, plus TOTP.
- Hosting: Self-host, or Logto Cloud in US, EU and Australian regions.

### Where it wins

- Quick to start, with a clean admin console.
- Organisations, RBAC, enterprise SSO and just-in-time provisioning included.

### Watch-outs

- Paid self-hosted plans (from $249 a month) add more SAML apps, IdP-initiated SSO, removal of Logto branding and your own UI.
- Younger project with a smaller enterprise track record.

Best for: SaaS teams on a TypeScript stack who want CIAM with organisations out of the box.

## [開源 Authorization Server 比較 | OAuth 2.0](https://oauth2.tw/2/open-source/)

Implicit | 移除 ROPC | DCR |
| --- | --- | --- | ... | --- | --- |
| Keycloak | ✅ | — | — | — | ✅ |
|
...
| ⚠️ 企業版 |
| Ory Hydra | ✅ | 建議 | 標記不安全 | deprecated | ✅ |
|
...
| ✅ 內建 | ✅ 已移除 | ✅ 已移除 | ✅ |
| node-

## [Open Source Auth Alternatives to WorkOS | The Open Source Drop](https://www.opensourcedrop.com/alternative-to/workos)

| | Authentik | ZITADEL | Logto |
| --- | --- | --- | --- |
| Overlap | 70% | 70% | 65% |
| Migration | moderate | moderate | moderate |
| License | MIT (enterprise/ directory proprietary) | GNU ... Public License v3.0 | Mozilla Public License 2.0 |

---

## Assistant

_Thinking:_

I have good current data. Now compose the answer in Spanish (Rioplatense, voseo), concise but with a real fork. The user asked for options: roles/permissions, admin users + client users, don't develop it, Docker or SDK, local use, Keycloak discarded.

Options to present (real fork with tradeoffs — they explicitly asked for options):

1. **Authentik** — MIT core, Docker Compose, self-hosted only, PostgreSQL only since 2025.10, admin console, OIDC, roles/groups, users. Best fit for "que se haga cargo" with admin + client users. Enterprise dir needs license in production for specific features (WS-Federation, mTLS, device trust, enhanced audit) — core features (OIDC, roles, flows) are MIT/free.
2. **Zitadel** — Go + PostgreSQL, Docker, OIDC certified, organizations, roles per project, event-sourced audit. AGPL-3.0 — caveat for companies. Cloud free tier 100 DAU (small). Self-host free.
3. **Logto** — TypeScript/Node + PostgreSQL, Docker, RBAC, organizations, admin console, MPL-2.0. Free self-hosted edition has limitations (some features license-gated: more SAML apps, custom UI, branding removal paid). Note: single admin account in OSS edition? One source said "the open-source edition has a single admin account" — hmm, that seems odd; might be about... Actually it says "Logto... the open-source edition has a single admin account." That's a notable limitation but I'm not sure it's accurate/current. I'll mention limitations exist but be careful. Actually I'd rather not state uncertain claims. The search result did say it; I can mention "edicón OSS con funciones bajo licencia (SAML extra, UI propia)".
4. **Casdoor** — Apache-2.0, Go, light, lots of protocols, but security advisories flagged (no fixed versions for critical SAML/token flaws, vendor non-responsive) — recommend against per the security gate source. I should mention it but with warning, or drop it. Given security gate failed, I'd mention briefly with the caveat.
5. **SuperTokens** — SDK-driven, embedded auth in app code — but they don't want to develop; it's more embedded than central IdP; also CVE flagged without fix. Probably skip or mention briefly.
6. **Authelia** — lightweight reverse proxy auth, OIDC beta, YAML users — too limited for user management UI (no admin console for users/roles really). Skip or brief mention.
7. **SaaS: Auth0 / Okta / Cognito** — no Docker but SDKs, work locally, free tiers. Since they said "o poder tener sdk o algo yo en local" — SaaS also qualifies (works locally over internet). Worth mentioning as the zero-ops option.

Also note the integration pattern: any OIDC provider works with Spring Cloud Gateway via oauth2ResourceServer + JwtAuthenticationConverter to map roles claim → authorities. Roles (ADMIN, CLIENTE) defined in IdP admin console, not in code.

Recommendation: Authentik as first pick for their case (self-host, Docker, admin console, no license conversation for core, MIT), Zitadel if they want multi-tenancy/B2B and can accept AGPL, Logto if they prefer developer-friendly/modern and Node stack. SaaS (Auth0/Okta/Cognito) if they prefer zero ops.

Security note: Casdoor flagged in2026 security gate — mention to avoid? The hackmd source is less authoritative but it's a real signal. I'll mention briefly as "evitar por ahora".

Keep response reasonably concise: a compact comparison + recommendation + one question (maybe: SaaS vs self-hosted? or which one to plan with?). Since plan mode, next step is deciding so we can plan the setup. Ask one question: which to go with, recommending Authentik.

Format: short intro, table or bullet list of3-4 options with tradeoffs, recommendation, integration note (one line), one question at end. Actually rules say don't present exhaustive lists unless real fork — user asked "dame opciones", so options are requested. Keep to 3 strong + 1 SaaS mention.

Length: medium. Spanish.

Buena data — verifiqué el estado actual de las alternativas (noviembre 2026). Estas son las opciones que cumplen tus requisitos (roles + usuarios admin/clientes sin desarrollarlo, Docker o SDK, uso local, sin Keycloak):

**Self-hosted en Docker:**

| | **Authentik** | **Zitadel** | **Logto** |
|---|---|---|---|
| Stack | Python + Go, solo PostgreSQL | Go + PostgreSQL | Node.js + PostgreSQL |
| Licencia | MIT (core) — funciones enterprise en carpeta aparte con licencia | **AGPL-3.0** desde v3 (ojo con esto) | MPL-2.0 |
| Roles/usuarios | Consola admin completa, flows visuales, grupos/roles | Organizaciones, roles por proyecto, auditoría event-sourced | RBAC + organizaciones, consola moderna |
| Docker | `docker compose` de un comando | Un contenedor + PostgreSQL | Un contenedor + PostgreSQL |

- **Authentik** → mi primera recomendación. Core MIT, admin console lista, OIDC completo, y las funciones enterprise que son pagas (mTLS, WS-Federation) no las necesitás para tu caso. Es el más parecido a "instalás y te maneja los usuarios y roles".
- **Zitadel** → excelente técnica (OpenID certified, liviano, ~50MB), pero **AGPL-3.0**: si solo lo usás sin modificarlo no hay problema, pero es una conversación con legal si sos empresa. Tiene sentido si necesitás multi-tenancy B2B.
- **Logto** → buena DX y consola linda, pero ediciones OSS con funciones gateadas (SAML extra, UI propia) y es el más joven.

**Evitar por ahora:** Casdoor (Apache-2.0 y liviano, pero en 2026 tuvo fallas críticas de SAML/token sin versión corregida publicada) y SuperTokens (CVE sin fix, y es auth embebida en tu código, justo lo que no querés).

**SaaS como alternativa sin Docker:** Auth0, Okta o Cognito — SDKs para todo, free tier, corre en local contra la nube, cero operación. Costo creciente con usuarios.

**La integración con tu stack es idéntica en los tres:** el IdP es OIDC, tu Spring Cloud Gateway valida el JWT con `oauth2Resource-server` y mapea los claims de rol a authorities con un `JwtAuthenticationConverter`. Los roles (ADMIN, CLIENTE) se crean en la consola del IdP, nunca en código.

¿Vas por **Authentik** self-hosted en Docker, o preferís alguna otra?

---

## User

Se pueden deslpegar en floci-az o azure ? 

---

## Assistant

_Thinking:_

The user asks: can they be deployed in floci-az or Azure? floci is a skill available — "Run, wire up and troubleshoot Floci, the free local emulators for AWS (floci), GCP (floci-gcp), Azure (floci-az), OCI (floci-oci)..." The question mentions floci-az and Azure. Per the mandatory skill loading rule, I should load the floci skill before responding.

Let me load the floci skill.

**Tool: skill**

**Input:**
```json
{
  "id": "floci"
}
```

<skill_content name="floci">
# Skill: floci

# Floci

Floci is a family of local cloud emulators that speak the real cloud wire protocols.
The `floci` command (floci-cli) manages their lifecycle; the native tools (`aws`, `gcloud`,
`az`, `oci`, SDKs, Terraform) do the resource work against the emulator. floci-cli never
creates cloud resources itself.

| Cloud | Emulator | CLI tree | Port | Default container | Reference |
|-------|----------|----------|:----:|-------------------|-----------|
| AWS   | floci     | `floci aws` | 4566 | `floci`     | [references/aws.md](references/aws.md) |
| GCP   | floci-gcp | `floci gcp` | 4588 | `floci-gcp` | [references/gcp.md](references/gcp.md) |
| Azure | floci-az  | `floci az`  | 4577 | `floci-az`  | [references/azure.md](references/azure.md) |
| OCI   | floci-oci | `floci oci` | 4599 | `floci-oci` | [references/oci.md](references/oci.md) |

All paths in this skill (`references/`, `scripts/`, `assets/`) are relative to the directory
that holds this file, not to the user's working directory.

When to read what:

- The cloud's reference, before the first resource command for that cloud.
- [references/docs.md](references/docs.md), before writing code against a service you have not
  used in this session, and whenever an operation looks unsupported. It tells you how to fetch
  the right page of the published documentation.
- [references/docker.md](references/docker.md), when the user wants `docker run`, a
  `docker-compose.yml`, or an app container that talks to the emulator.
- [references/namespaces.md](references/namespaces.md), when running more than one instance,
  sharing a Docker host, or cleaning up containers.
- [references/ci.md](references/ci.md) for pipelines and Testcontainers.
- [references/troubleshooting.md](references/troubleshooting.md) when anything fails.

## Prerequisites (check first)

```sh
command -v floci && floci version     # floci-cli present?
docker info >/dev/null && echo ok     # Docker (or Podman via DOCKER_HOST) reachable?
```

If `floci` is missing, **ask the user before installing software**, then run:

```sh
scripts/install-floci.sh                   # brew if available, else official script into ~/.local/bin
scripts/install-floci.sh --version 0.4.0   # pin a version (uses the install script)
```

- Windows: `iwr https://floci.io/install.ps1 | iex` (per-user, no admin). The helper scripts
  here are bash; on Windows run the `floci` commands they wrap directly.
- No native binary for the platform: download `floci.jar` from the latest release and run
  `java -jar floci.jar` (needs Java 25+).
- Do not use `sudo` or edit shell profiles (`~/.bashrc`, `~/.zshrc`) without asking; if the
  install dir is not on `PATH`, export it for the current session only.
- Updates: `floci update --check` exits 0 when up to date, 1 when an update exists and 2 when
  the check itself failed. Run `floci update` only on exit 1 (Homebrew installs:
  `brew upgrade floci`).
- The user does not want the CLI at all: the emulators are plain container images, see
  [references/docker.md](references/docker.md).

floci-cli does not install Docker. If the daemon is missing, tell the user; do not install
Docker yourself. The native cloud CLIs (`aws`, `gcloud`, `az`, `oci`) are only needed for the
clouds the user works with; suggest installing them, do not install them unasked.

## Hard rules

1. **Never touch real cloud.** Use only the fake credentials the emulators accept
   (`test`/`test` for AWS, the dev identities `floci az setup` and `floci oci setup` create).
   Never read, export or reuse the user's real cloud credentials or profiles.
2. **Verify the endpoint before every mutating command.** It must point at the emulator
   (`localhost`, `127.0.0.1`, `localhost.floci.io`, or a Compose service name). If a command
   might reach public cloud (see each reference's "leaks" section), stop and fix the wiring.
3. **Always name the tree explicitly**: `floci aws start`, not `floci start`. Bare commands
   follow `floci config default-product`, which the user may have changed.
4. **Parse with `-o json`**, never scrape text output. Use `-q` to silence noise in scripts.
5. **Do not remove containers, volumes or snapshots you did not create** in this session.
   Scope any cleanup to a namespace (see namespaces.md) and confirm with the user first.
6. **State is ephemeral** unless the instance was started with `--persist <dir>` or a profile
   with `persistDir`. Both `start` on a stopped container and `restart` remove the container
   and create a new one: `restart` keeps the settings (port, image, services, persist dir,
   namespace) but not in-memory state, and on Azure the new container has a new certificate,
   so `floci az setup` must be run again. `start` on a running container fails. Say so before
   recreating anything.
7. **Never add `-q` to `env`**: it suppresses the exports themselves.
8. **Do not guess what an emulator supports.** Check `floci <tree> services -o json` and the
   published docs ([references/docs.md](references/docs.md)) instead of answering from memory.
9. **With a profile, start and wait before `env`.** Until the profile's container is running,
   `env` and `status` fall back to the default port and describe the default instance. Trust
   `"state": "running"` in `status -o json`, not `reachable`.

## Lifecycle (same for every tree)

```sh
floci <tree> start --detach          # pull if needed, start, return immediately
floci <tree> wait --timeout 2m       # block until healthy (exit 0 = ready)
eval "$(floci <tree> env -o text)"   # export SDK/CLI wiring into this shell
# ... run aws / gcloud / az / oci / tests ...
floci <tree> stop --remove           # when done, only if you started it
```

`-o text` keeps the exports correct even when a profile sets `output: json`. In other shells:
`floci <tree> env --shell fish | source`, or
`floci <tree> env --shell powershell | Invoke-Expression`.

Or use the helper, which starts the container only when none exists, then waits and prints
the exports:

```sh
exports="$(scripts/ensure-running.sh <aws|gcp|az|oci> [profile])" && eval "$exports"
```

Keep the two steps: `eval "$(...)"` in one go succeeds even when the helper fails, and the
commands after it would run with missing or stale wiring. The helper exits 3 without
touching anything when the container exists but is stopped, because starting it replaces
it (rule 6). Tell the user, and re-run with `FLOCI_REPLACE_STOPPED=1` once they agree.

One-time per instance, before using the native CLI:
- Azure: `floci az setup` (trusts the emulator cert, registers an az cloud, logs in).
- OCI: `floci oci setup` (writes a throwaway key and a `[FLOCI]` profile in `~/.oci/config`).

`eval` only affects the current shell. If each tool call runs in a fresh shell, prefix every
command: `eval "$(floci aws env -o text)" && aws s3 ls`.

The examples are written for bash. In zsh (the macOS default) an unquoted variable is not
split into words, so `flags="--profile aws-b"; floci aws status $flags` passes one argument
and fails. Write the flags out, or use an array: `flags=(--profile aws-b)` and
`"${flags[@]}"`.

## Inspecting state

| Need | Command |
|------|---------|
| Is it running and healthy? | `floci <tree> status -o json` |
| Which services are enabled? | `floci <tree> services -o json` (Azure: returns an empty list, see azure.md) |
| CLI and server versions | `floci <tree> version -o json` (Azure: read the version from `status -o json`) |
| Recent server logs | `floci <tree> logs --tail 100` or `--since 5m` |
| Environment problems | `floci <tree> doctor -o json` (an array of checks with `status` `ok`, `warn` or `fail`; see troubleshooting.md before acting on a `fix`) |
| Effective config | `floci <tree> config show --profile <name>` (the namespace row appears only with a profile that yields one) |
| All instances | `floci <tree> config profile list -o json` (profiles are shared by all trees; `image` tells the cloud). Also `config profile show`, `edit` and `delete <name>` |
| Any command's flags | `floci <tree> <command> --help` |

## Several instances

An instance is a profile: its container name is its identity, its port must be unique, and
its Docker resource namespace keeps the containers it launches apart from other instances.
Pass the same `--profile <name>` to every command for that instance. Details and the
namespace derivation rules are in [references/namespaces.md](references/namespaces.md).

```sh
floci aws config profile create aws-b --container floci-b --port 14566 --persist ~/.floci/data/aws-b
floci aws start --profile aws-b          # waits up to 30s; or --detach, then wait
eval "$(floci aws env -o text --profile aws-b)"
```

floci-cli keeps its own files under `~/.floci` (profiles in `~/.floci/profiles/`, per-instance
state in `~/.floci/<product>/<container>/`), so instance data goes there too, as
`~/.floci/data/<name>`.

## Choosing the right integration

- **Ad-hoc commands or a dev server**: the CLI lifecycle above.
- **The user asks for `docker run` or a Compose file, or the app itself runs in a container**:
  [references/docker.md](references/docker.md). Start from the templates in
  `assets/compose/` instead of writing YAML from memory.
- **Integration tests in code**: a Testcontainers module where one exists (AWS in Java, Node,
  Python, Go and .NET; Azure in Java only; none for GCP or OCI, see ci.md). Otherwise start
  the emulator with the CLI or Compose before the tests.

## When something is not supported

Coverage differs per service: some are full engines, some are in-process, some answer with
canned responses. Before writing code against a service, check that it is enabled
(`floci <tree> services -o json`) and read its page in the published docs
([references/docs.md](references/docs.md)). If an operation is missing, say so plainly,
suggest a workaround, and point the user to the emulator's issue tracker
(`https://github.com/floci-io/<floci|floci-gcp|floci-az|floci-oci>/issues`) instead of
retrying variations.

Base directory for this skill: C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci
Relative paths in this skill (e.g., scripts/, reference/) are relative to this base directory.
Note: file list is sampled.

<skill_files>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\assets\compose\aws.yml</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\assets\compose\azure.yml</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\assets\compose\gcp.yml</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\assets\compose\multi-cloud.yml</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\assets\compose\oci.yml</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\references\aws.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\references\azure.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\references\ci.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\references\docker.md</file>
<file>C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\references\docs.md</file>
</skill_files>
</skill_content>

---

## Assistant

_Thinking:_

The question: "Se pueden desplegar en floci-az o azure?" — can Authentik/Zitadel/Logto be deployed in floci-az (Azure emulator) or Azure?

Key points:
1. **floci-az** is an Azure resource emulator — it emulates Azure services (resource manager, etc.) on localhost. It's not a general container runtime for arbitrary apps. Actually, floci-az might emulate Container Instances? I shouldn't guess — rule 8: "Do not guess what an emulator supports. Check `floci <tree> services -o json`."

But the real answer conceptually: Authentik/Zitadel/Logto are just Docker containers (plus PostgreSQL). They don't need Azure at all — they run with Docker locally. floci-az emulates Azure services for testing cloud code; you wouldn't deploy an IdP *into* the emulator unless the emulator supports Azure Container Instances/Container Apps emulation. And even then, the purpose of floci is testing your cloud infrastructure code locally, not hosting apps.

On real Azure: yes — any Docker image can run on Azure Container Apps, AKS, Container Instances, or a VM with Docker. Also Azure has its own managed IdP: **Microsoft Entra ID (formerly Azure AD)** — which could replace Authentik entirely if they're on Azure, with App registrations, app roles, and JWT validation in Spring Cloud Gateway. That's the "don't develop it" option on Azure. Entra ID Free tier includes app roles / token issuance. Actually — Entra ID allows creating app roles and issuing tokens to users; free tier covers this for up to... App registrations are free; token issuance for own apps is free (no cost for authentication of users? The free tier includes 50k MAU... actually Microsoft Entra ID Free: no charge for workforce tokens? Microsoft changed licensing: Entra ID Free includes unlimited...? Careful. Microsoft pricing: Microsoft Entra ID Free is free, and app tokens... There was a 2023 licensing change where they started charging per app token issued ($0.0032) beyond free tier? Hmm. In 2023 Microsoft announced per-app token issuance charges starting March 2024, then reversed/revised for free tier? Let me not quote pricing specifics I'm unsure about. I know: Microsoft planned "per-app token" pricing, community pushback, they revised — apps using free tenant... I'd better verify if I want to mention cost.

Let me check what services floci-az supports — run `floci az services -o json` if floci is installed. The skill says check services before answering. Also read references/azure.md maybe. Let me check if floci CLI is available first.

Actually, the question is simpler than I might be making it. The user asks whether the IdPs can be deployed in floci-az or Azure. The honest answer:

1. **floci-az**: it's an emulator of Azure services for local dev/test — it's not a hosting platform for your apps. Whether it emulates Container Instances/Container Apps determines if you could run containers in it. Check services list.

2. **Azure (real)**: yes — Container Apps/AKS/ACI/VM can run those Docker images. But note the flow: in the emulator, your Spring Cloud Gateway would need to reach the IdP — typically the IdP runs as a plain container on Docker alongside (or as a Docker Compose service), independent of Azure emulation.

3. **Alternative on Azure**: Entra ID as the managed IdP (no deployment at all) — app roles for ADMIN/CLIENTE.

Let me check floci az services. First verify floci is installed: `command -v floci && floci version`. Per skill, check prerequisites. Let me run that plus services in one shell call. Also read references/azure.md for what's emulated.

Let me do: shell `floci az services -o json` (and version). If floci missing, adapt. Note Windows PowerShell environment. Commands run with powershell. `command -v` is bash. Use `Get-Command floci`.

Also read the azure.md reference (I should read it before first resource command — here I'm answering conceptual questions about Azure support; reading it helps).

Let me run in parallel: read azure.md, and shell check for floci.

**Tool: read**

**Input:**
```json
{
  "path": "C:\\Users\\damian\\Repositorios\\LaTiendita\\.agents\\skills\\floci\\references\\azure.md"
}
```

Read file C:\Users\damian\Repositorios\LaTiendita\.agents\skills\floci\references\azure.md, lines 1-124
1: # Azure (floci-az)
2: 
3: Port 4577, container `floci-az`, tree `floci az`, image `floci/floci-az`. Dev storage
4: account: `devstoreaccount1`.
5: 
6: `floci az start` turns TLS on (`FLOCI_AZ_TLS_ENABLED=true`), which serves HTTP and HTTPS on
7: the same port; the `az` CLI needs HTTPS. The image's own default is TLS off, so an instance
8: started with `docker run` or Compose needs that variable before `floci az setup` can work.
9: 
10: ## Two planes, two wirings
11: 
12: - **Data plane (storage SDKs, `az storage ...`)**: a connection string is enough.
13: - **Control plane (`az group create`, ARM, Entra login, Terraform azurerm)**: `az` must be told
14:   about the emulator, otherwise it talks to public Azure. That is what `floci az setup` does.
15: 
16: ## One-time setup per instance
17: 
18: ```sh
19: floci az start
20: floci az setup                 # per-instance isolated az config; ~/.azure untouched
21: eval "$(floci az env -o text)"
22: az group create -n my-rg -l westeurope
23: ```
24: 
25: `floci az setup`:
26: 1. fetches the emulator CA and writes a CA bundle (public roots + emulator cert);
27: 2. registers an az cloud named after the container, pointing ARM and Entra at
28:    `https://localhost:<port>`, and makes it active;
29: 3. disables `core.instance_discovery` (otherwise login fails with `invalid_instance`);
30: 4. logs in as a service principal: tenant `00000000-0000-0000-0000-000000000002`,
31:    subscription `00000000-0000-0000-0000-000000000001`. The secret is never checked.
32: 
33: State lives in `~/.floci/az/<container>/`. Use `--profile <name>` for other instances,
34: `--global` only if the user wants their default `~/.azure` switched (`--reset --global` switches
35: it back), `--reset` to delete the isolated config.
36: 
37: ## What `floci az env` exports
38: 
39: Default (connection string mode): `AZURE_STORAGE_CONNECTION_STRING`. It contains `;`, and the
40: output is already quoted, so always `eval "$(floci az env -o text)"` with the double quotes.
41: 
42: The emulator does not check SharedKey signatures, so the connection string works for normal
43: data-plane calls. Blob SAS tokens are checked against the emulator's real account key: when
44: the user needs SAS, take the key from the floci-az docs
45: (`https://floci.io/floci-az/services/blob/`) rather than from the exported string.
46: 
47: `--format sdk-vars` instead exports `AZURE_STORAGE_ACCOUNT`, `AZURE_STORAGE_KEY` and per-service
48: endpoints (`AZURE_STORAGE_BLOB_ENDPOINT`, `..._QUEUE_...`, `..._TABLE_...`,
49: `AZURE_FUNCTIONS_ENDPOINT`, `AZURE_APP_CONFIGURATION_ENDPOINT`, `AZURE_KEY_VAULT_ENDPOINT`).
50: Limit with `--service blob,queue`.
51: 
52: After setup, both modes also export `AZURE_CONFIG_DIR` (the isolated az config) and
53: `REQUESTS_CA_BUNDLE` (the CA bundle). These only apply in the current shell. After
54: `floci az setup --global` the output unsets `AZURE_CONFIG_DIR` instead, because the user's
55: default `~/.azure` is then the one pointing at the emulator.
56: 
57: ## Leaks to public Azure
58: 
59: - Control-plane commands (`az group`, `az resource`, anything ARM or login) run without
60:   `AZURE_CONFIG_DIR` from `floci az env` use the user's real config and real cloud. Data-plane
61:   `az storage` commands with the connection string go to the emulator either way, but still
62:   read and touch the user's `~/.azure`; run `floci az setup` first to keep `az` fully isolated. Check with `az cloud show --query name`: it must be the Floci cloud
63:   (`floci-az` by default), not `AzureCloud`.
64: - If the container was recreated without `--persist`, its certificate changed. `floci az env`
65:   warns on stderr; re-run `floci az setup`.
66: - Containers started by an older floci-cli may have TLS off. `floci az restart` once, then
67:   `floci az setup` again.
68: 
69: ## Endpoint layout
70: 
71: All services share port 4577, routed by path, with the account as prefix:
72: Blob `/{account}/`, Queue `/{account}-queue/`, Table `/{account}-table/`,
73: Functions `/{account}-functions/`, App Config `/{account}-appconfig/`,
74: Cosmos `/{account}-cosmos/`, Key Vault `/{account}-keyvault/`,
75: Service Bus `/{account}-servicebus/`. ARM resources use the normal `/subscriptions/...` paths.
76: Other services have their own prefixes; read the service page for the exact URL shape.
77: 
78: AMQP (Event Hubs, Service Bus) and Kafka (Event Hubs) do not go through 4577. They are served
79: by sidecar containers the emulator starts, which bind their own ports on the host;
80: `floci az start` publishes only 4577. Read
81: `https://floci.io/floci-az/services/event-hub/` and `.../services/service-bus/` for the ports
82: and connection strings before writing a messaging client.
83: 
84: ## Coverage
85: 
86: `floci az services -o json` returns an empty list and `floci az version -o json` reports the
87: server as unavailable, both with exit 0: floci-az does not serve the endpoint they read. That
88: does not mean nothing is enabled. Take the version from `floci az status -o json`, and
89: coverage from the docs. `floci az wait --service <name>` times out for the same reason; use
90: plain `wait`. Read the service page:
91: `https://floci.io/floci-az/services/<slug>/` (30 pages, slugs such as `blob`, `queue`,
92: `table`, `cosmos`, `key-vault`, `service-bus`, `event-hub`, `functions`, `aks`, `sql`,
93: `postgresql`, `entra`, `arm`; see [docs.md](docs.md)).
94: 
95: Rough guide: Blob (incl. ADLS Gen2), Queue, Table, App Configuration, Cosmos DB SQL API, Key
96: Vault and ARM resource groups are in-process with full CRUD. Functions, the database engines
97: (PostgreSQL, MySQL, MariaDB, managed SQL, the other Cosmos APIs), AKS, Container Apps,
98: Container Instances, Redis, ACR, and the Event Hubs and Service Bus brokers run real
99: containers and need the Docker socket. Some services are control plane only (Policy) or do
100: not deliver anything (Communication Email). The page says which.
101: 
102: Docs: https://floci.io/floci-az/
103: 
104: ## Recipes
105: 
106: ```sh
107: # Data plane
108: az storage container create --name uploads
109: az storage blob upload --container-name uploads --name hello.txt --data "hello"
110: 
111: # Control plane (after setup)
112: az group create -n my-rg -l westeurope
113: az group list -o table
114: ```
115: 
116: For Key Vault, Cosmos and the other data planes, read the service page first
117: (`https://floci.io/floci-az/services/<slug>/`): their URL shapes differ from public Azure.
118: 
119: ```python
120: import os
121: from azure.storage.blob import BlobServiceClient
122: svc = BlobServiceClient.from_connection_string(os.environ["AZURE_STORAGE_CONNECTION_STRING"])
123: svc.create_container("uploads")
124: ```

**Tool: shell**

**Input:**
```json
{
  "command": "if (Get-Command floci -ErrorAction SilentlyContinue) { floci version; floci az services -o json } else { \"floci no instalado\" }",
  "timeout": 60000
}
```

floci no instalado


---

## Assistant

_Thinking:_

Key findings:
- floci is not installed on this machine. So floci-az isn't available locally right now (would need installation — ask before installing).
- floci-az does emulate Azure services: notably **Container Apps, Container Instances, AKS** run real containers via Docker socket. And **Entra** is a documented service slug (`entra`)! So the emulator has an Entra service (probably emulating Microsoft Entra ID for token validation?). Let me check what floci-az entra does — docs.md says to read the published page: https://floci.io/floci-az/services/entra/ and maybe container-apps page.

The question: "Se pueden desplegar en floci-az o azure?" — can Authentik/Zitadel/Logto (the IdPs) be deployed in floci-az or Azure?

Answers:
1. **En Azure (real)**: yes — they're standard Docker images + PostgreSQL. Azure Container Apps, AKS, ACI, or a VM with Docker Compose. Note: Azure Database for PostgreSQL as the DB. Also mention: on Azure you might consider Microsoft Entra ID as the managed IdP instead of self-hosting.

2. **En floci-az**: it's an emulator of Azure services — Container Apps/Container Instances/AKS run real containers with the Docker socket, so in principle you could deploy them there... BUT: purpose of floci is local dev/test of your cloud infrastructure code, state is ephemeral (unless --persist), and it's heavy (many real containers: IdP + PostgreSQL). More importantly: your Spring Cloud Gateway in local dev needs to reach the IdP — simpler to run the IdP as a plain Docker container (compose) next to your services rather than "inside" the emulated Azure.

Also worth checking: what does floci-az's `entra` service do? If it emulates Entra ID (OIDC token issuance/validation), that could be relevant: the gateway could validate tokens against the emulator's Entra. But does it support app roles / user login? I must not guess — check the docs page: https://floci.io/floci-az/services/entra/. Let me fetch it. Also maybe check container-apps page to confirm Container Apps runs real containers: docs already say "Container Apps ... run real containers and need the Docker socket" — confirmed in azure.md line 97-99.

Let me fetch the entra service page to give accurate info.

Also worth checking container-apps service page? azure.md already confirms it runs real containers. That's enough.

Let me fetch https://floci.io/floci-az/services/entra/ via webfetch.

**Tool: webfetch**

**Input:**
```json
{
  "url": "https://floci.io/floci-az/services/entra/",
  "format": "markdown"
}
```

[ Skip to content](#microsoft-entra-id)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ")

Floci-AZ

Microsoft Entra ID

Initializing search

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- [ Getting Started](../../getting-started/quick-start/)

- [ Configuration](../../configuration/docker-compose/)

- [ Web Console](../../ui/)

- [ Services](../)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ") Floci-AZ

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- Getting Started

  Getting Started

  - [ Quick Start](../../getting-started/quick-start/)

  - [ Installation](../../getting-started/installation/)

  - [ Azure CLI & SDK Setup](../../getting-started/azure-setup/)

  - [ Terraform / OpenTofu](../../terraform/)

- Configuration

  Configuration

  - [ Docker Compose](../../configuration/docker-compose/)

  - [ Docker Images](../../configuration/docker-images/)

  - [ Ports Reference](../../configuration/ports/)

  - [ Emulator Endpoints](../../configuration/endpoints/)

  - [ Storage Modes](../../configuration/storage/)

  - Advanced

    Advanced

    - [ application.yml Reference](../../configuration/advanced/application-yml/)

- Web Console

  Web Console

  - [ Overview](../../ui/)

  - [ Console Contract v2](../../ui/console-contract/)

- Services

  Services

  - [ Overview](../)

  - [ Azure Resource Manager](../arm/)

  - [ Blob Storage](../blob/)

  - [ Queue Storage](../queue/)

  - [ Table Storage](../table/)

  - [ Azure Functions](../functions/)

  - [ App Configuration](../app-config/)

  - [ Azure SignalR Service](../signalr/)

  - [ Key Vault](../key-vault/)

  - Microsoft Entra ID [ Microsoft Entra ID](./)

    Table of contents

    - [ Features](#features)

    - [ Endpoints](#endpoints)

    - [ Discovery document](#discovery-document)

    - [ Default tenant & dev credentials](#default-tenant-dev-credentials)

    - [ Interactive sign-in (authorization code \+ PKCE)](#interactive-sign-in-authorization-code-pkce)

    - [ Refreshing a session](#refreshing-a-session)

    - [ Acquiring a token](#acquiring-a-token)

    - [ Validating a token](#validating-a-token)

    - [ Client credential validation](#client-credential-validation)

    - [ Configuration](#configuration)

    - [ UserInfo](#userinfo)

    - [ Startup directory seed file](#startup-directory-seed-file)

  - [ Microsoft Graph](../graph/)

  - [ Event Hubs](../event-hub/)

  - [ Service Bus](../service-bus/)

  - [ Cosmos DB](../cosmos/)

  - [ Azure SQL Database](../sql/)

  - [ Azure Database for PostgreSQL](../postgresql/)

  - [ Azure Database for MySQL](../mysql/)

  - [ Azure Database for MariaDB](../mariadb/)

  - [ Azure Kubernetes Service](../aks/)

  - [ Azure Container Apps](../container-apps/)

  - [ API Management](../apim/)

  - [ Virtual Network](../network/)

  - [ Virtual Machines](../vm/)

  - [ Azure Cache for Redis](../redis/)

  - [ Azure Container Registry](../acr/)

  - [ Azure Container Instances](../aci/)

  - [ Event Grid](../event-grid/)

  - [ Azure Monitor / Log Analytics](../monitor/)

  - [ Communication Services Email](../email/)

  - [ Managed Identity](../managed-identity/)

  - [ Azure Policy](../policy/)

  - [ Azure Resource Graph](../resourcegraph/)

  - [ Azure Management Groups](../managementgroups/)

  - [ ARM Template Deployments](../deployments/)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

Table of contents

- [ Features](#features)

- [ Endpoints](#endpoints)

- [ Discovery document](#discovery-document)

- [ Default tenant & dev credentials](#default-tenant-dev-credentials)

- [ Interactive sign-in (authorization code \+ PKCE)](#interactive-sign-in-authorization-code-pkce)

- [ Refreshing a session](#refreshing-a-session)

- [ Acquiring a token](#acquiring-a-token)

- [ Validating a token](#validating-a-token)

- [ Client credential validation](#client-credential-validation)

- [ Configuration](#configuration)

- [ UserInfo](#userinfo)

- [ Startup directory seed file](#startup-directory-seed-file)

# Microsoft Entra ID

A local **OpenID Connect provider** that issues real **RS256-signed** JWTs with a published discovery document and JWKS. This replaces the previous static, unsigned-token stub so apps that *acquire* and *validate* Entra tokens can work fully offline.

> **Phase 1** delivered the OIDC foundation and the two non-interactive grants (**client credentials** and **resource-owner password / ROPC**). **Phase 2** ([\#120](https://github.com/floci-io/floci-az/issues/120)) adds the **authorization code \+ PKCE** grant for interactive (browser SPA) sign-in, plus a narrow Microsoft Graph slice for group membership. App registrations and their client secrets are managed through that slice too: see [`services/graph.md`](../graph/). Full Graph CRUD is not implemented.

## Features

- **Signed tokens**: RS256 JWTs with a stable signing key persisted across restarts; app-only tokens carry `idtyp=app`, user access tokens name the user (`upn` and `unique_name` in v1.0, `preferred_username` in v2.0), and every token carries a unique `uti`, matching real Entra

- **Discovery**: `/.well-known/openid-configuration` derived from the request base URL. It advertises only what the emulator serves, see [Discovery document](#discovery-document)

- **JWKS**: `/discovery/v2.0/keys` exposing the public signing key (`kty`, `use`, `alg`, `kid`, `n`, `e`) plus the self-signed cert chain (`x5c`, `x5t`)

- **Grants**: `client_credentials`, `password` (ROPC), and `authorization_code` (auth code \+ PKCE), v1.0 and v2.0 token shapes; `refresh_token` at the v2.0 token endpoint

- **Azure-shaped errors**: token errors return `error`, `error_description` (with the `AADSTS` code), `error_codes`, `trace_id`, `correlation_id`, `timestamp`, and `error_uri`

- **Dev seed**: a default tenant, a well-known dev app registration, a dev user and a dev group

- **Permissive client authentication by default**: client secrets are not checked, so any `client_id` and `client_secret` get a token and `ClientSecretCredential` works with zero setup. Set `validate-client-credentials: true` to check them against the directory (see [Client credential validation](#client-credential-validation))

## Endpoints

All endpoints are tenant-rooted at the base URL (port `4577`). `{tenant}` may be a tenant id or `common` / `organizations` / `consumers`.

| Path | Purpose |
| --- | --- |
| `GET /{tenant}/oauth2/v2.0/authorize` | Authorize endpoint (auth code \+ PKCE) |
| `POST /{tenant}/oauth2/v2.0/token` | Token endpoint (v2.0) |
| `POST /{tenant}/oauth2/token` | Token endpoint (v1.0) |
| `GET /{tenant}/v2.0/.well-known/openid-configuration` | OpenID discovery |
| `GET /{tenant}/.well-known/openid-configuration` | OpenID discovery |
| `GET /oidc/userinfo`, `POST /oidc/userinfo` | Scoped profile for a locally signed delegated access token |
| `GET /{tenant}/discovery/v2.0/keys` | JWKS |
| `GET /common/userrealm/{username}?api-version=1.0` | Home realm discovery: always `account_type: Managed`, so MSAL username-password sign-in goes straight to the password grant |

## Discovery document

`GET /{tenant}/v2.0/.well-known/openid-configuration` lists only the grants, endpoints and response types the emulator implements, so a client that configures itself from discovery fails early with a clear message instead of calling a route that does not exist:

| Field | Value |
| --- | --- |
| `issuer` | The configured `issuer`, or `{base}/{tenant}/v2.0` when unset (`common` stays `common`) |
| `authorization_endpoint`, `token_endpoint`, `jwks_uri` | the routes in the table above |
| `grant_types_supported` | `authorization_code`, `client_credentials`, `password`, `refresh_token` |
| `response_types_supported` | `code` |
| `response_modes_supported` | `query`, `fragment` |
| `scopes_supported` | `openid`, `profile`, `email`, `offline_access` |
| `end_session_endpoint` | `{base}/{tenant}/oauth2/v2.0/logout` |

There is no `device_authorization_endpoint` and the device code grant is not listed, because it is not implemented yet. msal-python's `initiate_device_flow` therefore stops with "You need to provide device authorization endpoint" before sending any request. `end_session_endpoint` is listed even though logout is not served yet, because MSAL.js refuses to log out without it.

When `FLOCI_AZ_SERVICES_ENTRA_ISSUER` is set, discovery and issued tokens use that same issuer, regardless of the hostname used to fetch discovery. This supports clients reaching Floci-AZ through different host and Docker addresses. Endpoint URLs still use the request base URL so the caller can reach them. Without an override, discovery keeps its request-derived issuer.

## Default tenant & dev credentials

| Value | Default |
| --- | --- |
| Tenant id | `00000000-0000-0000-0000-000000000002` |
| Client id | `11111111-1111-1111-1111-111111111111` |
| Client secret | `floci-az-dev-secret` (checked only when `validate-client-credentials` is on) |
| Dev user (UPN) | `dev-user@floci-az.local` |
| Dev group | `floci-az dev group` (the dev user is a direct member) |

## Interactive sign-in (authorization code \+ PKCE)

There is no real interactive consent screen: `GET /{tenant}/oauth2/v2.0/authorize` auto-approves against the seeded dev user (or a different seeded user, selected via `login_hint`) and redirects straight back to `redirect_uri` with a `code`: no login form, nothing to click through. This keeps the flow fully scriptable while still exercising the real auth-code+PKCE wire protocol that MSAL (`@azure/msal-browser`, `@azure/msal-react`, `@azure/msal-node`) speaks.

```
GET /{tenant}/oauth2/v2.0/authorize
    ?client_id=11111111-1111-1111-1111-111111111111
    &redirect_uri=https://app.local/callback
    &response_type=code
    &response_mode=query          # or "fragment"
    &scope=openid profile
    &state=...
    &nonce=...
    &code_challenge=...           # PKCE S256 challenge
    &code_challenge_method=S256
    &login_hint=dev-user@floci-az.local   # optional; defaults to the seeded dev user
```

`302`s to `{redirect_uri}?code=...&state=...` (or `#code=...&state=...` for `response_mode=fragment`). The issued code is bound to the tenant, `client_id`, and `redirect_uri` from this request: redemption must repeat the same `client_id`/`redirect_uri` and happen against the same tenant, or it fails with `invalid_grant`. Redeem the code with `grant_type=authorization_code`:

```
curl -s http://localhost:4577/00000000-0000-0000-0000-000000000002/oauth2/v2.0/token \
  -d grant_type=authorization_code \
  -d client_id=11111111-1111-1111-1111-111111111111 \
  -d redirect_uri=https://app.local/callback \
  -d code=<code from the redirect> \
  -d code_verifier=<the PKCE verifier for the challenge you sent>
```

The response carries an `access_token`, plus an `id_token` when `openid` was requested. The ID token's `aud` is always the client id (per OIDC, regardless of v1.0/v2.0), and it echoes the `nonce` from the `/authorize` request: MSAL rejects an ID token whose nonce doesn't match. PKCE verification (`S256` or `plain`) is skipped when `/authorize` was called without a `code_challenge`: `client_id` is still required and bound to the code, so an omitted challenge no longer hands a usable code to a caller who doesn't know the client. There is no app-registration redirect-URI allow-list yet, so any `client_id` is accepted at `/authorize`, matching this phase's permissive client validation elsewhere (`client_credentials` is likewise accepted without strict validation).

## Refreshing a session

Include `offline_access` in the original authorization request to receive an opaque `refresh_token` from the v2.0 code exchange. A request without this scope does not receive one. Renew the session without another authorization request:

```
curl -s http://localhost:4577/00000000-0000-0000-0000-000000000002/oauth2/v2.0/token \
  -d grant_type=refresh_token \
  -d client_id=11111111-1111-1111-1111-111111111111 \
  --data-urlencode 'refresh_token=<refresh token from the code exchange>'
```

Send the client's secret too when the session used a confidential client and credential validation is enabled. HTTP Basic and form credentials follow the same policy as other grants. A code exchange that supplies a secret or assertion, or uses a registered `web.redirectUris` entry, binds a confidential session. Public-client sessions need no secret.

The response contains a fresh access token and replacement refresh token. `openid` permits a fresh ID token for the same user, without repeating the authorization request's nonce. MSAL's `client_info=1` is supported for account caching. An optional `scope` selects the access token's scopes, including another resource such as Graph after an ARM sign-in. The emulator does not model consent, so additional scopes are accepted for the same client and user session. Omitting `scope` uses the original authorization scopes, even after a replacement was issued for a different resource. A narrowed access token does not remove those default scopes.

Refresh tokens last 90 days by default, and a replacement starts a new 90-day lifetime. A redirect URI registered under the application's `spa.redirectUris` instead creates a 24-hour session; all replacements retain that original deadline. Replacement does not revoke the old token, matching [Microsoft's refresh-token contract](https://learn.microsoft.com/en-us/entra/identity-platform/refresh-tokens).

Tokens remain bound to the client and resolved tenant that authorized them, and renewal resolves the same user in that directory. Invalid, revoked, expired or incorrectly bound tokens return `invalid_grant`. Expiry uses AADSTS700082, or AADSTS700084 for the fixed SPA lifetime. Refresh state uses the existing Entra storage mode and persists only token digests. Issuance removes expired records at most once a minute, including on the first issuance after restart. Emulator reset clears all refresh state. Cross-tenant consent, B2B sessions, password-change revocation and Graph's `revokeSignInSessions` action are not modelled. The v1.0 refresh grant is not implemented.

**Group membership** for the signed-in user is *not* embedded in the token (no `groups` claim, matching real Entra's default behavior): call the Graph `getMemberGroups` endpoint instead, see [`services/graph.md`](../graph/).

## Acquiring a token

Pythoncurl

```
from azure.identity import ClientSecretCredential

cred = ClientSecretCredential(
    tenant_id="00000000-0000-0000-0000-000000000002",
    client_id="11111111-1111-1111-1111-111111111111",
    client_secret="floci-az-dev-secret",  # any value works unless validate-client-credentials is on
    authority="http://localhost:4577",
)
token = cred.get_token("api://resource/.default")
print(token.token)  # RS256-signed JWT
```

```
curl -s http://localhost:4577/00000000-0000-0000-0000-000000000002/oauth2/v2.0/token \
  -d grant_type=client_credentials \
  -d client_id=11111111-1111-1111-1111-111111111111 \
  -d client_secret=floci-az-dev-secret \
  -d scope=api://resource/.default
```

## Validating a token

Fetch the JWKS and validate the signature, `iss`, `aud`, and `exp`:

```
import jwt
from jwt import PyJWKClient

jwks = PyJWKClient("http://localhost:4577/00000000-0000-0000-0000-000000000002/discovery/v2.0/keys")
signing_key = jwks.get_signing_key_from_jwt(token)
claims = jwt.decode(token, signing_key.key, algorithms=["RS256"], audience="api://resource")
```

## Client credential validation

By default the token endpoint accepts any client: an unregistered `client_id` and a wrong secret both get a token, and the first unregistered `client_id` is logged once at `WARN`. With `validate-client-credentials: true` the client is checked against the directory instead:

| Request | Response |
| --- | --- |
| a `client_id` not registered in the tenant | `400` `unauthorized_client`, `AADSTS700016` |
| a secret that matches none of the app's secrets, or one before its `startDateTime` | `401` `invalid_client`, `AADSTS7000215` |
| a secret that matches but is past its `endDateTime` | `401` `invalid_client`, `AADSTS7000222` |
| `client_credentials` with neither a secret nor a `client_assertion` | `401` `invalid_client`, `AADSTS7000218` |
| no `client_id` at all | `400` `invalid_request`, `AADSTS900144` |

The secret may be sent in the form (`client_secret_post`) or in an HTTP Basic header (`client_secret_basic`). The `password` and `authorization_code` grants need no secret, only a registered `client_id`. A `client_assertion` is accepted without verification, and the `password` grant does not check the user's password. The seeded dev app (`11111111-...` with `floci-az-dev-secret`) is registered, so it keeps working. Secrets are stored as salted PBKDF2-HMAC-SHA256 hashes, never in plaintext.

Tenants: `common`, `organizations` and `consumers` act in the default tenant. Any other tenant id is accepted as it is and served from the default tenant's directory, so the dev app authenticates under it too.

## Configuration

```
floci-az:
  services:
    entra:
      enabled: true                 # local OIDC provider (default on)
      default-tenant-id: "00000000-0000-0000-0000-000000000002"
      # issuer:                     # optional override; default {baseUrl}/{tenant}/v2.0
      token-lifetime-seconds: 3599
      validate-tokens: false        # true: ARM requests must carry a valid token (same as parity-overrides.strict-tokens)
      validate-client-credentials: false   # true: reject unregistered clients and wrong secrets
      # signing-key-path:           # optional; default {storage.persistent-path}/entra
```

| Setting | Env var | Default |
| --- | --- | --- |
| `enabled` | `FLOCI_AZ_SERVICES_ENTRA_ENABLED` | `true` |
| `default-tenant-id` | `FLOCI_AZ_SERVICES_ENTRA_DEFAULT_TENANT_ID` | `00000000-0000-0000-0000-000000000002` |
| `issuer` | `FLOCI_AZ_SERVICES_ENTRA_ISSUER` | *(derived from request)* |
| `token-lifetime-seconds` | `FLOCI_AZ_SERVICES_ENTRA_TOKEN_LIFETIME_SECONDS` | `3599` |
| `validate-tokens` | `FLOCI_AZ_SERVICES_ENTRA_VALIDATE_TOKENS` | `false` |
| `validate-client-credentials` | `FLOCI_AZ_SERVICES_ENTRA_VALIDATE_CLIENT_CREDENTIALS` | `false` |
| `signing-key-path` | `FLOCI_AZ_SERVICES_ENTRA_SIGNING_KEY_PATH` | `{storage.persistent-path}/entra` |

> `validate-tokens: true` turns on [token verification](../arm/#token-verification) for ARM requests. Data planes still accept any Bearer token whatever its value.

> **Keep `enabled: true` unless you have a reason not to.** The OAuth2 token endpoint (`/{tenant}/oauth2/v2.0/token`) is served by this service. Disabling Entra makes that endpoint `404`, which breaks the ARM/Terraform and SDK sign-in handshakes that authenticate through it.

## UserInfo

Discovery advertises the request base URL plus `/oidc/userinfo`. Use GET or POST with an authorization-code access token as `Authorization: Bearer <token>`. The endpoint verifies the local RSA signature, issuer, expiry, not-before time and audience before reading the selected user from the directory. `openid` is required; `profile` permits `name`, while `email` permits `email` when present. `sub` matches the ID token from the same code exchange. `preferred_username` remains in the ID token and is not returned by UserInfo.

The local authorization-code access token retains these OIDC scopes in `scp`; token response scope formatting is unchanged. UserInfo accepts the request cloud's Graph endpoint audience, Graph's application ID and the client audience used by existing local v2 delegated tokens. App-only tokens and tokens for other API audiences are rejected. This endpoint serves directory-backed code-flow users; synthetic password-grant identities are not directory provisioning. Invalid tokens return a Bearer 401, and missing `openid` permission returns a Bearer 403\. It does not accept external Microsoft tokens.

## Startup directory seed file

Set `FLOCI_AZ_SERVICES_ENTRA_SEED_FILE` (configuration key `floci-az.services.entra.seed-file`) to a JSON file readable by the Floci-AZ process. This adds local applications, users, groups and direct memberships without editing the persisted store. Mount the file into the container and use its container path in the environment variable.

```
{
  "applications": [{
    "id": "aaaaaaaa-0000-0000-0000-000000000001",
    "appId": "aaaaaaaa-1111-1111-1111-111111111111",
    "displayName": "Local app",
    "clientSecret": "local-development-only"
  }],
  "users": [{
    "id": "bbbbbbbb-0000-0000-0000-000000000001",
    "userPrincipalName": "reader@example.test",
    "displayName": "Local Reader",
    "mail": "reader@example.test"
  }],
  "groups": [{
    "id": "cccccccc-0000-0000-0000-000000000001",
    "displayName": "Readers",
    "securityEnabled": true,
    "members": ["bbbbbbbb-0000-0000-0000-000000000001"]
  }]
}
```

The arrays are optional. All object ids are required and unique within the file; use letters, digits, periods, underscores or hyphens. Applications require `appId`; users require `userPrincipalName`; groups require `displayName`. Application/user `displayName` defaults to appId/UPN, `mail` and `clientSecret` are optional, and `securityEnabled` defaults to true. The top-level `tenantId` defaults to `default-tenant-id`. Memberships must reference users or groups in that tenant, either already present or included in the file. Configure the tenant resolver appropriately when seeding a directory other than the default.

The whole file is parsed and validated before configured objects are written. Duplicate ids, alternate-key conflicts, unknown fields and unresolved members fail startup. No file is read when the setting is absent; the built-in dev seed remains available. Seeding creates missing objects and merges memberships; it does not overwrite existing profiles, rotate their secrets or delete objects or memberships omitted on later starts. Repeat starts do not duplicate links. Explicit reset also reapplies the seed.

Client secrets are hashed by the normal directory writer before persistence. Protect the seed file itself because its optional `clientSecret` values are plaintext. To check seeded clients, enable `FLOCI_AZ_SERVICES_ENTRA_VALIDATE_CLIENT_CREDENTIALS`. Existing `login_hint` selects seeded users for authorization-code sign-in, and Graph membership reads use the same records. This is a startup seed contract, not a directory CRUD or interactive account-picker API.

Made with [ Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

---

## Assistant

_Thinking:_

Now I have solid facts:

**floci-az:**
- It's an Azure *emulator*, not a hosting platform per se. But it DOES emulate Container Apps, Container Instances, AKS — "run real containers and need the Docker socket". So technically you could deploy containers through emulated Container Apps/ACI.
- It has an **Entra ID emulation service**: a local OIDC provider issuing RS256-signed JWTs with discovery, JWKS, grants (client_credentials, password/ROPC, auth code + PKCE), seed file with users/groups/apps. BUT: does it support app roles? The docs mention groups (no `groups` claim in token by default; call Graph getMemberRoles), no mention of app roles/roles claim. So it's not a full roles/permissions management — it's a dev/test token issuer. No admin console for user management CRUD (seed file only, Graph slice partial).
- floci is not installed on this machine.

So the answer:

1. **Authentik/Zitadel/Logto in floci-az?** — Not really their place. floci-az emulates Azure services (including Container Apps/ACI running real containers), so you *could* technically deploy a container via emulated Container Apps... but wait — "Container Apps... run real containers and need the Docker socket". So yes, plausible. But the caveats: ephemeral state, emulator is for testing your infrastructure code, not hosting your dev stack; heavy. Also purpose: your gateway needs an IdP reachable locally — simpler: run the IdP as a plain Docker container (compose) alongside. That's what floci itself suggests for app containers (references/docker.md — app containers that talk to the emulator).

Actually, be careful with rule 8: "Do not guess what an emulator supports." azure.md explicitly says Container Apps and Container Instances run real containers and need the Docker socket. So deploying Authentik there is likely possible but I haven't verified the exact Container Apps data plane behavior (creating container apps, pulling images, ingress). I should hedge and suggest checking the container-apps service page. Let me maybe fetch the container-apps page quickly to be accurate? The user's question is fairly direct: "se pueden desplegar en floci-az o azure?" — I should answer accurately. Let me fetch the container-apps page.

2. **Azure real**: yes — Container Apps / ACI / AKS / VM with Docker. Standard images + Azure Database for PostgreSQL. AND the important architectural note: if you're going to Azure, the natural option is **Microsoft Entra ID (Azure AD)** as managed IdP — no deployment at all: app registrations + app roles (ADMIN, CLIENTE), and Spring Cloud Gateway validates with `spring-boot-starter-oauth2-resource-server` using Entra's discovery/JWKS. That's the ultimate "que se haga cargo". Free tier covers it (I should be careful about pricing claims — Entra ID Free includes... app roles and SSO; token issuance pricing: Microsoft announced per-app token charges but for free tier... I recall they backtracked: "Microsoft will not charge for apps using... " hmm. In 2024 Microsoft revised per-app token pricing so that only tenants wanting premium features pay? Actually: Microsoft said per-app token issuance charges would apply from March 2024, then after pushback clarified that most free customers (using Azure AD Free / SSO) won't be charged — apps using "Azure AD Free" for workforce SSO... The final state: pricing applies to tokens issued via "app tokens" beyond free tier, free 100k? I don't remember exactly. Better: don't quote numbers; say "tier gratuito cubre SSO/roles para tus propias apps" with a caveat to verify pricing. Or just avoid pricing entirely.

Also relevant: **floci-az's Entra emulator** — for local dev against Azure-style tokens: your Spring Cloud Gateway can validate real RS256 JWTs from the emulator's local OIDC provider. But roles management: the emulator has groups/seed, no app-roles claim mentioned... So for *local dev with roles*, the emulator isn't a full replacement for the IdP — unless you add roles via... not documented. Keep it factual: it issues tokens with seeded users/groups, but group claims aren't embedded (call Graph), no full admin console. So it's for infra/dev testing, not for managing admin/customer roles.

So the structure of the answer:

**En Azure (real): sí.** Son imágenes Docker estándar → Container Apps, ACI, AKS o un VM con Docker. PostgreSQL: Azure Database for PostgreSQL. Y si vas a Azure, considerá **Microsoft Entra ID** como IdP gestionado — no desplegás nada, app registrations + roles de app, mismo flujo JWT en el gateway. (Mention as the "no te ocupes de nada" path.)

**En floci-az: con matices.**
- floci-az emula servicios de Azure; Container Apps/ACI/AKS corren contenedores reales vía Docker socket — técnicamente posible desplegarlos ahí, pero el emulador es para probar tu infraestructura, no para hospedar tu stack; estado efímero (regla 6: restart borra estado sin --persist). Verificar en la página de container-apps antes de intentar.
- Lo que SÍ te da floci-az para este caso: el servicio **Entra emulado** — un proveedor OIDC local que emite JWT RS256 reales con discovery/JWKS, usuarios y grupos por seed file. Tu Spring Cloud Gateway puede validar tokens contra él localmente. PERO: es para desarrollo/pruebas de validación de tokens; no tiene consola admin para gestionar roles ADMIN/CLIENTE como sí lo tiene Authentik (los usuarios/grupos se cargan por seed file; los grupos no vienen en el claim, hay que llamar a Graph).
- Y aparte: **floci no está instalado** en esta máquina.

Also worth noting: the IdPs themselves don't need Azure at all — plain Docker Compose locally. floci is for emulating Azure services, not needed to run Authentik.

Recommendation for local: `docker compose` con Authentik (or the IdP chosen) + PostgreSQL, corriendo en paralelo a tus servicios Spring; floci-az solo si además querés emular Azure (por ejemplo, si el producto usa storage/queue de Azure). En producción Azure: Container Apps o Entra ID gestionado.

One question at the end? Maybe: "¿tu sistema usa servicios de Azure además (storage, etc.) o floci-az lo usarías solo para el IdP?" — helps decide. Actually good single question.

Let me also quickly check the container-apps page to be accurate about deploying arbitrary containers? Rule: do not guess. azure.md says container apps run real containers. Whether you can deploy arbitrary images (Authentik) — plausible but unverified; I'll phrase carefully with "verificá la página de container-apps". Actually, let me fetch it — one fetch, more accuracy. It's worth it since the user directly asks "se pueden desplegar".

Let me fetch https://floci.io/floci-az/services/container-apps/.

**Tool: webfetch**

**Input:**
```json
{
  "format": "markdown",
  "url": "https://floci.io/floci-az/services/container-apps/"
}
```

[ Skip to content](#azure-container-apps)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ")

Floci-AZ

Azure Container Apps

Initializing search

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- [ Getting Started](../../getting-started/quick-start/)

- [ Configuration](../../configuration/docker-compose/)

- [ Web Console](../../ui/)

- [ Services](../)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

[ ![logo](../../assets/floci-header.svg)](../.. "Floci-AZ") Floci-AZ

[](https://github.com/floci-io/floci-az "Go to repository")

floci-io/floci-az

- [ Home](../..)

- Getting Started

  Getting Started

  - [ Quick Start](../../getting-started/quick-start/)

  - [ Installation](../../getting-started/installation/)

  - [ Azure CLI & SDK Setup](../../getting-started/azure-setup/)

  - [ Terraform / OpenTofu](../../terraform/)

- Configuration

  Configuration

  - [ Docker Compose](../../configuration/docker-compose/)

  - [ Docker Images](../../configuration/docker-images/)

  - [ Ports Reference](../../configuration/ports/)

  - [ Emulator Endpoints](../../configuration/endpoints/)

  - [ Storage Modes](../../configuration/storage/)

  - Advanced

    Advanced

    - [ application.yml Reference](../../configuration/advanced/application-yml/)

- Web Console

  Web Console

  - [ Overview](../../ui/)

  - [ Console Contract v2](../../ui/console-contract/)

- Services

  Services

  - [ Overview](../)

  - [ Azure Resource Manager](../arm/)

  - [ Blob Storage](../blob/)

  - [ Queue Storage](../queue/)

  - [ Table Storage](../table/)

  - [ Azure Functions](../functions/)

  - [ App Configuration](../app-config/)

  - [ Azure SignalR Service](../signalr/)

  - [ Key Vault](../key-vault/)

  - [ Microsoft Entra ID](../entra/)

  - [ Microsoft Graph](../graph/)

  - [ Event Hubs](../event-hub/)

  - [ Service Bus](../service-bus/)

  - [ Cosmos DB](../cosmos/)

  - [ Azure SQL Database](../sql/)

  - [ Azure Database for PostgreSQL](../postgresql/)

  - [ Azure Database for MySQL](../mysql/)

  - [ Azure Database for MariaDB](../mariadb/)

  - [ Azure Kubernetes Service](../aks/)

  - Azure Container Apps [ Azure Container Apps](./)

    Table of contents

    - [ Features](#features)

    - [ ARM endpoints](#arm-endpoints)

    - [ Example](#example)

    - [ Configuration](#configuration)

  - [ API Management](../apim/)

  - [ Virtual Network](../network/)

  - [ Virtual Machines](../vm/)

  - [ Azure Cache for Redis](../redis/)

  - [ Azure Container Registry](../acr/)

  - [ Azure Container Instances](../aci/)

  - [ Event Grid](../event-grid/)

  - [ Azure Monitor / Log Analytics](../monitor/)

  - [ Communication Services Email](../email/)

  - [ Managed Identity](../managed-identity/)

  - [ Azure Policy](../policy/)

  - [ Azure Resource Graph](../resourcegraph/)

  - [ Azure Management Groups](../managementgroups/)

  - [ ARM Template Deployments](../deployments/)

- [ Contributing](../../contributing/)

- [ Changelog](../../changelog/)

Table of contents

- [ Features](#features)

- [ ARM endpoints](#arm-endpoints)

- [ Example](#example)

- [ Configuration](#configuration)

# Azure Container Apps

Compatible with Azure Resource Manager clients using `Microsoft.App/managedEnvironments` and `Microsoft.App/containerApps`.

Real mode runs each active revision replica as Docker containers. Mocked mode keeps full ARM and revision state without starting Docker.

## Features

- Managed Environment create, get, update, delete, and list

- Container App create, get, update, delete, and list

- Versioned templates with Single and Multiple active revision modes

- Revision list, get, activate, deactivate, and restart

- Container commands, arguments, environment variables, and secret references

- External and internal HTTP ingress forwarded to running revision containers

- Weighted revision traffic and round-robin replica selection with unhealthy replica failover

- `minReplicas` and `maxReplicas` validation; local replicas start at `minReplicas`

- Scale-to-zero apps start one replica on first ingress request when `maxReplicas` permits

- Mocked mode for Docker-free tests

Scale rules beyond minimum/maximum replicas are retained in ARM responses but are not evaluated locally. Internal ingress accepts only transport peers whose source IP belongs to a Docker IPAM subnet used by a running Container App replica. Forwarded headers cannot make a public caller internal.

## ARM endpoints

```
PUT    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
GET    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
PATCH  /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
DELETE /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments/{name}
GET    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/managedEnvironments
GET    /subscriptions/{sub}/providers/Microsoft.App/managedEnvironments
POST   .../managedEnvironments/{name}/checkNameAvailability

PUT    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
GET    /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
PATCH  /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
DELETE /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.App/containerApps/{name}
POST   .../containerApps/{name}/listSecrets
GET    .../containerApps/{name}/revisions
GET    .../containerApps/{name}/revisions/{revision}
POST   .../containerApps/{name}/revisions/{revision}/activate
POST   .../containerApps/{name}/revisions/{revision}/deactivate
POST   .../containerApps/{name}/revisions/{revision}/restart
```

## Example

Create an environment:

```
curl -X PUT 'http://localhost:4577/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/managedEnvironments/local?api-version=2025-07-01' \
  -H 'Content-Type: application/json' \
  -d '{"location":"eastus","properties":{}}'
```

Create an externally accessible app:

```
curl -X PUT 'http://localhost:4577/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/containerApps/hello?api-version=2025-07-01' \
  -H 'Content-Type: application/json' \
  -d '{
    "location":"eastus",
    "properties":{
      "environmentId":"/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/managedEnvironments/local",
      "configuration":{
        "activeRevisionsMode":"Single",
        "secrets":[{"name":"token","value":"local-secret"}],
        "ingress":{"external":true,"targetPort":80}
      },
      "template":{
        "revisionSuffix":"v1",
        "containers":[{
          "name":"web",
          "image":"nginx:alpine",
          "env":[{"name":"TOKEN","secretRef":"token"}]
        }],
        "scale":{"minReplicas":1,"maxReplicas":3}
      }
    }
  }'
```

The response returns a globally unique value in `properties.configuration.ingress.fqdn`. Route that hostname to floci-az, then call it through port 4577:

```
FQDN=$(curl -s 'http://localhost:4577/subscriptions/dev/resourceGroups/apps/providers/Microsoft.App/containerApps/hello?api-version=2025-07-01' | jq -r '.properties.configuration.ingress.fqdn')
curl -H "Host: $FQDN" http://localhost:4577/
```

## Configuration

```
floci-az:
  services:
    container-apps:
      enabled: true
      mocked: true
      dns-suffix: azurecontainerapps.io
      ingress-timeout-seconds: 60
```

| Environment variable | Default | Description |
| --- | --- | --- |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_ENABLED` | `true` | Enables `Microsoft.App` routing |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_MOCKED` | `true` | Keeps ARM state without Docker containers |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_DNS_SUFFIX` | `azurecontainerapps.io` | Suffix returned in environment and app FQDNs |
| `FLOCI_AZ_SERVICES_CONTAINER_APPS_INGRESS_TIMEOUT_SECONDS` | `60` | Backend connect/request timeout |

Set `mocked: false` to enable real mode, which requires access to the Docker daemon. Template containers in one replica share the leader container's network namespace, so sidecars can communicate over `localhost`. The leader receives the dynamic host-port binding for the shared ingress target port. A replica becomes healthy only after that port accepts TCP connections. Requests still enter floci-az on port 4577\.

Made with [ Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)

---

## User

Mi eccomerce no va a ser muy complejo. es para tener en cuenta, puede que hayan admin que puedan editar productos pero no categorias y ese tipo de casos de uso pueden suceder

---

## Assistant

_Thinking:_

The user says: "Mi ecommerce no va a ser muy complejo. Es para tener en cuenta, puede que hayan admin que puedan editar productos pero no categorías y ese tipo de casos de uso pueden suceder."

So they're saying: my e-commerce won't be very complex, but keep in mind there can be admins who can edit products but not categories — that kind of use case can happen.

They didn't directly answer my pending question (Authentik vs other), and I had a pending floci/Azure answer that I was composing... Wait — let me re-read the conversation. My previous turn was the fetch of container-apps page, but I never delivered the final answer about floci-az/Azure deployment! Actually looking at the structure: my last assistant message was just tool calls (webfetch), and now the user has sent a new message. So the floci/Azure answer was never delivered. Hmm — the user moved on. Now they're refining requirements: fine-grained permissions at resource/field level — admins who can edit products but not categories.

This is a classic case: coarse-grained RBAC (route/role level) vs resource-level permissions ("admin that can edit products but not categories" is actually still RBAC — role-level: PRODUCT_EDITOR role vs CATEGORY_MANAGER role). Wait, let's think:

- "Admin que puede editar productos pero no categorías" — this is still expressible as roles: role "product-editor" and permission "product:edit", "category:edit". At the gateway level, you can enforce path/method-based permissions: `PUT /products/**` requires PRODUCT_EDIT, `PUT /categories/**` requires CATEGORY_EDIT. This is coarse-grained RBAC at the API surface — works perfectly with gateway + JWT roles.

- The harder case would be: "user X can edit product 123 but not product 456" (resource/instance-level) — that's ABAC/ReBAC, needs enforcement inside the service, not at the gateway.

So the key teaching point: the gateway can enforce *what endpoints* a role can hit (admin editar productos pero no categorías = fine, es un rol por recurso). If someday you need per-instance permissions ("solo sus productos"), that lives inside the microservice, not the gateway.

How do the IdP options handle it? Authentik: permissions via roles + per-application permissions... In Authentik you create roles like "editor-productos" and assign permissions. Actually Authentik has RBAC on its own objects; for tokens, you put users in groups and map groups to claims. The standard pattern: groups (e.g., `admin-productos`, `admin-categorias`) → group claim in JWT → Spring maps groups to authorities → gateway routes enforce `hasAuthority("PRODUCTOS_EDIT")` or per-path rules.

Also Entra ID has app roles — same model.

So my response should:
1. Confirm this use case is exactly what role-per-resource RBAC covers — and it's handled in the IdP + gateway config, no code.
2. Explain the split: gateway enforces endpoint-level (ruta/método × rol); service-level enforces instance-level if ever needed (keep it simple now, YAGNI).
3. Concrete example with Authentik groups/claims + Spring Cloud Gateway `AuthorizationFilter`/security config path rules.
4. Keep it short. And maybe re-ask/continue the pending decision? The previous turn's floci answer was never delivered — should I include it? The user changed topic to permissions granularity. But I owed them the floci/Azure answer. Hmm. The user's new message says "es para tener en cuenta" — they're adding a requirement to the plan. The floci question remains unanswered. I should probably answer briefly both: the floci/Azure answer (short) and the permissions case. But response-length contract says start with minimum useful response. The user's current question is about permissions granularity.

Actually, since I never delivered the floci answer, leaving it hanging is bad. But answering it fully now would bury the new question. Let me answer the new question primarily, and close the floci thread concisely at the end (few lines: yes in Azure (Container Apps/ACI), floci-az can emulate Container Apps but it's mocked by default and it's an emulator for testing not hosting; Entra emulator exists for token validation local; floci not installed here).

Wait — but mixing topics... The user asked two things in sequence; both deserve answers. Short version of floci:- Azure real: sí, son imágenes Docker estándar (Container Apps, ACI, AKS) o Entra ID gestionado como alternativa sin desplegar nada.
- floci-az: puede correr contenedores reales vía Container Apps pero en modo mocked por defecto y pensado para probar infra, no hospedar el stack; además tiene un emulador de Entra ID que emite JWTs RS256 reales — sirve para probar la validación del gateway en local, pero no tiene consola para gestionar roles; y floci no está instalado en tu máquina.

Then the main answer: permissions model.

Main content — the permissions case:

"Ceditar productos pero no categorías" es RBAC normal, no necesita permisos complejos:
- Definís en el IdP grupos/roles: `productos-editor`, `categorias-editor` (o un rol `admin` con permisos granulares).
- El JWT viaja con esos roles/groups como claims.
- El gateway aplica reglas por ruta/método: `PUT /productos/** → requiere PRODUCTOS_EDIT`, `PUT /categorias/** → requiere CATEGORIAS_EDIT`.

No código: solo configuración en Authentik (grupos) + reglas en el gateway.

Lo que NO resuelve el gateway (y no necesitás todavía): permisos por instancia ("este admin edita solo estos productos") — eso es a nivel de servicio, se hace dentro del microservicio con la authoridad extraída del token. Si algún día aparece, ya lo agregás. No lo anticipes.

Key concept: separation of concerns — gateway = coarse (endpoint × role), service = fine (instance × ownership).

Example config sketch (conceptual, maybe a small snippet of routes security config):

```yaml
- id: productos
  predicates: Path=/api/productos/**
  filters...```
and in SecurityWebFilterChain:
```java
.pathMatchers(HttpMethod.PUT, "/api/productos/**").hasAuthority("PRODUCTOS_EDIT")
```
In Spring Cloud Gateway you typically do auth in a global filter or via Spring Security on the gateway itself (it's a WebFlux app; you can add spring-security with oauth2 resource server and authorization rules by path). Keep snippet minimal or skip code — persona: CONCEPTS > CODE. A tiny illustrative config is fine.

Also mention: Authentik handles this well — users in groups, group claim → roles. In Entra: app roles. Either way same pattern.

And the "future" note: if permissions multiply (matrix of role × action), consider a permissions claim list or later OPA/Cedar — but don't now.

Question at end: one question. The pending decision (Authentik?) still stands. Maybe combine: "Si te sirve este modelo, cerramos la decisión: ¿Authentik?" Actually previous question was exactly that and user didn't answer — they added info instead. I can ask again gently as the single question, now with this use case as context: with this requirement, Authentik (or Entra if Azure) both cover it. Maybe the better question: confirm going with Authentik to close the plan.

Hmm, but careful: should I ask a question at all? Persona: ask at most one at a time; ending with a question is natural to advance the plan. I'll ask: "¿Cerramos con Authentik?" — or actually maybe the real fork now: gateway-level path rules fine. Let me just close with the Authentik decision question.

Length: keep compact. Include floci closure briefly. Actually order: first close the floci thread briefly (since it was pending), then permissions? Or respond to newest first? Newest first is better UX: answer their point, then "y dejando cerrado lo de floci/Azure..." Hmm, that's odd ordering too. I'll do: main answer (permissions), then a short "lo otro — floci/Azure — resumido:", then one question.

Actually wait — is it appropriate to dump both? Response length contract: minimum useful. Both are owed. Keep floci part to 3-4 lines.

Let me draft in Spanish Rioplatense:

---

Ese caso de uso no es complejo — es exactamente **RBAC por recurso**, y no requiere que desarrolles nada. La clave es dónde se aplica cada capa:

**Lo que resuelve tu gateway (nivel endpoint × rol):**
"Admin que edita productos pero no categorías" = dos permisos en el IdP:
- En Authentik: creás grupos `productos-editor` y `categorias-editor`, asignás usuarios. El JWT sale con el claim de grupos.
- En el gateway, reglas por ruta/método:

```java
.pathMatchers(HttpMethod.PUT, "/api/productos/**").hasAuthority("PRODUCTOS_EDIT")
.pathMatchers(HttpMethod.PUT, "/api/categorias/**").hasAuthority("CAT_EDIT")
```
(o en un filtro del gateway). Cero código de autorización en tus microservicios para esto.

**Lo que NO necesita el gateway (y no anticipes):**
"Este admin solo edita *estos* productos" — permisos por instancia. Eso es otra capa: se resuelve dentro del microservicio leyendo las authorities del token. Si algún día aparece, se agrega ahí; meterlo en el gateway es un error de arquitectura porque el gateway no conoce tus datos.

Regla para no sobre-diseñar: mientras el permiso sea "qué endpoint puede tocar", es configuración del IdP + gateway. Cuando el permiso diga "de qué fila", es código del servicio. Tu e-commerce vive cómodo en la primera.

Y cerrando lo de antes (floci/Azure): [3 lines]

Una cosa: floci no está instalado en tu máquina.

Pregunta: ¿Cerramos con Authentik?

---

Wait — on Authentik: does it put groups in JWT claims? In Authentik, OIDC tokens include... Authentik's default `scope_to_claims`... Authentik tokens include `groups` claim? By default authentik's OIDC provider includes `groups` in the access token? I believe Authentik has property mappings for OIDC access tokens where you can add `groups` claim via a Scope mapping. There's a default scope mapping `authentik default oauth2: ...` — property mappings allow custom claims (e.g., `groups`). So: you configure a property mapping to emit groups claim. That's configuration in the admin UI, not code. I should phrase it as "configurás un mapping de claims para que lleve los grupos" without asserting defaults.

Also mapping groups → Spring authorities: JwtAuthenticationConverter with a custom converter — that IS a bit of code (small, in the gateway). Or use scope/roles claim mapped via `JwtGrantedAuthoritiesConverter` if the claim is `scope`/`roles`. Authentik default: roles from... To keep honest: you emit a claim with permissions/roles and map it once in the gateway (a few lines, done once). Let me say: "el gateway mapea ese claim a authorities (unas pocas líneas, una vez)".

Alternatively Entra: app roles → claim `roles`, mapped easily. But we're heading to Authentik choice.

Also worth noting: alternatives like Authentik have per-... fine.

Structure okay. The code snippet: person says concepts > code; a3-line snippet illustrating "solo configuración" is materially helpful. Keep.

Also — should I verify Authentik claim mapping claims? I've hedged appropriately ("configurás un mapping"), which is accurate for Authentik (property mappings exist and it's a UI config). Fine.

Floci part: 
- Azure real: sí — Container Apps / ACI / AKS con cualquier imagen Docker + Azure Database for PostgreSQL; o directamente Microsoft Entra ID como IdP gestionado (ni desplegás IdP).
- floci-az: puede levantar contenedores reales en Container Apps pero **en modo mocked por defecto** (estado ARM sin Docker) — está pensado para probar tu infraestructura, no para hospedar el stack; y tiene un emulador de Entra ID (JWT RS256 locales con discovery/JWKS) que sirve para probar la validación del gateway en local, pero con usuarios/grupos cargados por seed file y sin consola para gestionar roles. Además floci no está instalado en tu máquina.

Hmm, that's still long. Compress to ~4 lines.

One question at end: Authentik confirmation.

Also should I do memory save? Protocol: proactive save after decisions. The user expressed a requirement (use case: admin edit products but not categories). Is that a decision? It's a constraint learned — "User preference or constraint learned" → save. Also the project context: architecture discussions (gateway, Eureka, Keycloak discarded, Authentik candidate). Previous turns made decisions: Spring Cloud Gateway + Eureka choice, Keycloak discarded. Those were decisions I should have saved. Let me save now — a couple of mem_save calls:1. topic architecture/stack: "Spring Cloud Gateway + Eureka + Nginx edge; Keycloak descartado" (type: decision/architecture)
2. requirement: permission granularity (type: preference or discovery?) — "User preference/constraint": use cases like role-per-resource admin. Could combine into one save.

Do it before final answer (delivery guarantee: save, then final message with no tool calls after).

Session id: The protocol says use the authoritative session ID bound by the OpenCode plugin — "Do not invent or override this value." So call mem_save... but what do I pass for session_id? The plugin binds it automatically? "For attributed Engram writes (mem_save...), the OpenCode plugin binds session_id to the server-confirmed identity... Do not invent or override this value." That suggests the harness injects it — but the tool schema likely requires session_id? Earlier engram description: "session_id: The active session ID created at the start (required...)". Confusing. The plugin binding note says the adapter binds it — maybe I still need to pass something? Safer: call mem_save without session_id (adapter fills it) — but schema may require it... I don't know the exact schema since tools are in catalog. Let me search for mem_save signature via execute search? The instructions say mem_save is core, always available. I'll call tools.engram["mem_save"]... I need exact path. The catalog shows engram tools as tools.engram.mem_save presumably. From the shown subset: tools.engram.mem_doctor, mem_list_projects, mem_pin, mem_session_end, mem_session_start, mem_unpin. mem_save not shown but likely tools.engram.mem_save.

Given "OpenCode plugin binds session_id... do not invent": I'll call without session_id and see. Or include project "latiendita"? Read tools use project. Writes use session_id... plugin binds it. Let me just call mem_save with title/type/scope/topic_key/content and no session_id.

Two saves? Decision (stack) + requirement (permissions use case). Different topics → different topic_keys: `architecture/microservices-stack` and `requirements/authz-granularity`. Or one save covering the session's architecture decisions so far? The stack decision happened across turns and wasn't saved. Save both now — actually maybe combine into one save with topic architecture/microservices-stack including permission requirement? Different topics must not overwrite. Use two saves in one execute call (parallel Promise.all).

Save 1 (decision, architecture):
title: "Elegido Spring Cloud Gateway + Eureka para LaTiendita"
topic_key: architecture/gateway-stack
content: What: Stack de API Gateway definido: Nginx al borde (TLS), Spring Cloud Gateway con oauth2ResourceServer (JWT/roles), Eureka para discovery entre microservicios Spring Boot. Keycloak descartado por el usuario. Why: pedido del usuario de no desarrollar roles/permisos; quiere IdP externo. Where: decisión de arquitectura, sin archivos aún (modo plan). Learned: ...

Save 2 (preference/constraint):
title: "Requisito authz: roles por recurso en e-commerce"
topic_key: requirements/authz-granularity
content: What: Requisito — admins que pueden editar productos pero no categorías (RBAC por recurso a nivel endpoint). E-commerce no complejo. Where: plan de autorización con IdP (candidato Authentik) + reglas por ruta en gateway. Learned: permisos por instancia quedarían dentro del microservicio, no en el gateway.

Also candidate: Authentik pending confirmation.

Let me do the saves, then final answer.

Wait — also the floci info worth saving? Not critical. Skip.

Now, check skill loading requirement for this reply: any skill match? The reply discusses authorization architecture — springboot-security skill: "Use when reviewing Spring Security authn/authz..." I'm giving an authz recommendation with a tiny snippet. The mandatory self-check: does the request match? It's about roles/permissions design for their system → springboot-security is relevant ("authn/authz... best practices"). To be safe, should I load it? The previous turn I loaded java-springboot and floci. For this response about authorization patterns... The springboot-security skill would inform best practices (e.g., use method security, pathMatchers). Given the blocking requirement, loading it is cheap. Let me load it before composing.

Hmm, but the response is conceptual (no code review). The skill trigger: "Use when reviewing Spring Security authn/authz, validation, CSRF, secrets, headers, or rate limiting in Java Spring Boot services." Not a review. Borderline. The previous mandatory check in earlier turns — I loaded java-springboot for the gateway topic. For consistency and safety, load springboot-security? It says blocking requirement if "this request matches any skill". The request: "admin puede editar productos pero no categorías" — an authorization design question involving Spring. I'll load it — cheap and might contain exactly the right guidance.

Plan:1. load springboot-security skill
2. mem_save ×2 (execute)
3. final answer

Can I do 1 and 2 in parallel? skill tool and execute are independent — yes, parallel.

**Tool: skill**

**Input:**
```json
{
  "id": "springboot-security"
}
```

<skill_content name="springboot-security">
# Skill: springboot-security

# Spring Boot Security Review

Use when adding auth, handling input, creating endpoints, or dealing with secrets.

## When to Activate

- Adding authentication (JWT, OAuth2, session-based)
- Implementing authorization (@PreAuthorize, role-based access)
- Validating user input (Bean Validation, custom validators)
- Configuring CORS, CSRF, or security headers
- Managing secrets (Vault, environment variables)
- Adding rate limiting or brute-force protection
- Scanning dependencies for CVEs

## Authentication

- Prefer stateless JWT or opaque tokens with revocation list
- Use `httpOnly`, `Secure`, `SameSite=Strict` cookies for sessions
- Validate tokens with `OncePerRequestFilter` or resource server

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      Authentication auth = jwtService.authenticate(token);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    chain.doFilter(request, response);
  }
}
```

## Authorization

- Enable method security: `@EnableMethodSecurity`
- Use `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("@authz.canEdit(#id)")`
- Deny by default; expose only required scopes

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users")
  public List<UserDto> listUsers() {
    return userService.findAll();
  }

  @PreAuthorize("@authz.isOwner(#id, authentication)")
  @DeleteMapping("/users/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

## Input Validation

- Use Bean Validation with `@Valid` on controllers
- Apply constraints on DTOs: `@NotBlank`, `@Email`, `@Size`, custom validators
- Sanitize any HTML with a whitelist before rendering

```java
// BAD: No validation
@PostMapping("/users")
public User createUser(@RequestBody UserDto dto) {
  return userService.create(dto);
}

// GOOD: Validated DTO
public record CreateUserDto(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email String email,
    @NotNull @Min(0) @Max(150) Integer age
) {}

@PostMapping("/users")
public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto dto) {
  return ResponseEntity.status(HttpStatus.CREATED)
      .body(userService.create(dto));
}
```

## SQL Injection Prevention

- Use Spring Data repositories or parameterized queries
- For native queries, use `:param` bindings; never concatenate strings

```java
// BAD: String concatenation in native query
@Query(value = "SELECT * FROM users WHERE name = '" + name + "'", nativeQuery = true)

// GOOD: Parameterized native query
@Query(value = "SELECT * FROM users WHERE name = :name", nativeQuery = true)
List<User> findByName(@Param("name") String name);

// GOOD: Spring Data derived query (auto-parameterized)
List<User> findByEmailAndActiveTrue(String email);
```

## Password Encoding

- Always hash passwords with BCrypt or Argon2 — never store plaintext
- Use `PasswordEncoder` bean, not manual hashing

```java
@Bean
public PasswordEncoder passwordEncoder() {
  return new BCryptPasswordEncoder(12); // cost factor 12
}

// In service
public User register(CreateUserDto dto) {
  String hashedPassword = passwordEncoder.encode(dto.password());
  return userRepository.save(new User(dto.email(), hashedPassword));
}
```

## CSRF Protection

- For browser session apps, keep CSRF enabled; include token in forms/headers
- For pure APIs with Bearer tokens, disable CSRF and rely on stateless auth

```java
http
  .csrf(csrf -> csrf.disable())
  .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

## Secrets Management

- No secrets in source; load from env or vault
- Keep `application.yml` free of credentials; use placeholders
- Rotate tokens and DB credentials regularly

```yaml
# BAD: Hardcoded in application.yml
spring:
  datasource:
    password: mySecretPassword123

# GOOD: Environment variable placeholder
spring:
  datasource:
    password: ${DB_PASSWORD}

# GOOD: Spring Cloud Vault integration
spring:
  cloud:
    vault:
      uri: https://vault.example.com
      token: ${VAULT_TOKEN}
```

## Security Headers

```java
http
  .headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
      .policyDirectives("default-src 'self'"))
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
    .xssProtection(Customizer.withDefaults())
    .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));
```

## CORS Configuration

- Configure CORS at the security filter level, not per-controller
- Restrict allowed origins — never use `*` in production

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration config = new CorsConfiguration();
  config.setAllowedOrigins(List.of("https://app.example.com"));
  config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
  config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
  config.setAllowCredentials(true);
  config.setMaxAge(3600L);

  UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/api/**", config);
  return source;
}

// In SecurityFilterChain:
http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
```

## Rate Limiting

- Apply Bucket4j or gateway-level limits on expensive endpoints
- Log and alert on bursts; return 429 with retry hints

```java
// Using Bucket4j for per-endpoint rate limiting
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  private Bucket createBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
        .build();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String clientIp = request.getRemoteAddr();
    Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
    }
  }
}
```

## Dependency Security

- Run OWASP Dependency Check / Snyk in CI
- Keep Spring Boot and Spring Security on supported versions
- Fail builds on known CVEs

## Logging and PII

- Never log secrets, tokens, passwords, or full PAN data
- Redact sensitive fields; use structured JSON logging

## File Uploads

- Validate size, content type, and extension
- Store outside web root; scan if required

## Checklist Before Release

- [ ] Auth tokens validated and expired correctly
- [ ] Authorization guards on every sensitive path
- [ ] All inputs validated and sanitized
- [ ] No string-concatenated SQL
- [ ] CSRF posture correct for app type
- [ ] Secrets externalized; none committed
- [ ] Security headers configured
- [ ] Rate limiting on APIs
- [ ] Dependencies scanned and up to date
- [ ] Logs free of sensitive data

**Remember**: Deny by default, validate inputs, least privilege, and secure-by-configuration first.

Base directory for this skill: C:\Users\damian\Repositorios\LaTiendita\.agents\skills\springboot-security
Relative paths in this skill (e.g., scripts/, reference/) are relative to this base directory.
Note: file list is sampled.

<skill_files>
</skill_files>
</skill_content>

**Tool: execute**

**Input:**
```json
{
  "code": "const results = await Promise.allSettled([\n  tools.engram.mem_save({\n    title: \"Definido stack de API Gateway: Spring Cloud Gateway + Eureka\",\n    type: \"decision\",\n    scope: \"project\",\n    topic_key: \"architecture/gateway-stack\",\n    content: \"What: Stack de gateway definido para LaTiendita: Nginx al borde (TLS/estáticos), Spring Cloud Gateway (Spring Boot) con validación JWT + roles vía oauth2ResourceServer, Eureka Server para discovery, y las rutas resolven lb://servicio. Keycloak fue descartado por el usuario; candidato a IdP: Authentik self-hosted en Docker (pendiente de confirmar). Why: el usuario no quiere desarrollar el sistema de roles/permisos; todos los microservicios son Spring Boot y quiere stack homogéneo. Where: discusión de arquitectura en modo plan (sin archivos modificados). Learned: el usuario evaluó Nginx como gateway y floci-az/Azure para despliegue; floci no está instalado en la máquina, floci-az emula Entra ID (OIDC local RS256) y Container Apps en modo mocked por defecto.\"\n  }),\n  tools.engram.mem_save({\n    title: \"Requisito authz: RBAC por recurso a nivel endpoint en e-commerce\",\n    type: \"preference\",\n    scope: \"project\",\n    topic_key: \"requirements/authz-granularity\",\n    content: \"What: Requisito de autorización del usuario — casos como 'admin que puede editar productos pero no categorías'; e-commerce no complejo pero con matices de permisos. Why: definir el nivel de granularidad que el IdP + gateway deben cubrir sin sobre-diseñar. Where: plan de autorización (IdP candidato Authentik + reglas por ruta/método en Spring Cloud Gateway). Learned: acordado conceptualmente que permisos por endpoint (ruta x rol) los resuelve el gateway con claims del JWT; permisos por instancia ('solo estos productos') pertenecen al microservicio, no al gateway — no anticiparlos.\"\n  })\n]);\nreturn results.map(r => r.status === \"fulfilled\" ? r.value : String(r.reason));"
}
```

[
  {
    "id": 18,
    "judgment_required": false,
    "project": "latiendita",
    "project_path": "c:\\users\\damian\\repositorios\\latiendita",
    "project_source": "session",
    "result": "Memory saved: \"Definido stack de API Gateway: Spring Cloud Gateway + Eureka\" (decision)",
    "review_after": "2027-05-04 00:03:18",
    "state": "active",
    "sync_id": "obs-57b9d4d6e1b8237b",
    "truncation": {
      "original_bytes": 773,
      "limit_bytes": 50000,
      "truncated": false
    }
  },
  {
    "id": 19,
    "judgment_required": false,
    "project": "latiendita",
    "project_path": "c:\\users\\damian\\repositorios\\latiendita",
    "project_source": "session",
    "result": "Memory saved: \"Requisito authz: RBAC por recurso a nivel endpoint en e-commerce\" (preference)",
    "review_after": "2027-02-04 00:03:18",
    "state": "active",
    "sync_id": "obs-64f66a6267ae0c09",
    "truncation": {
      "original_bytes": 595,
      "limit_bytes": 50000,
      "truncated": false
    }
  }
]

---

## Assistant

_Thinking:_

Now compose the final answer:

1. Main: the "admin edita productos pero no categorías" case — it's RBAC per resource at endpoint level. Two layers:
   - Gateway: endpoint × role (config, no code).
   - Service: instance-level (don't anticipate).
2. Close the floci/Azure thread briefly (it was never delivered).
3. One question at the end (confirm Authentik).

Skill guidance applied: deny by default, least privilege, `@PreAuthorize` for method-level in services if needed, resource server. Keep the snippet small — the gateway path rules.

I must not present menus. Keep it tight. Spanish, Rioplatense.

Tranquilo — ese caso **no es un caso complejo**, es exactamente lo que resuelve un RBAC normal, y la clave es saber en qué capa vive cada permiso:

**Capa 1 — "qué endpoint puede tocar" → el gateway (configuración, cero código de negocio)**

"Admin que edita productos pero no categorías" significa dos permisos en el IdP (en Authentik: grupos o roles como `productos-editor`, `categorias-editor`), el JWT viaja con esos claims, y el gateway aplica:

```java
.pathMatchers(HttpMethod.PUT, "/api/productos/**").hasAuthority("PRODUCTOS_EDIT")
.pathMatchers(HttpMethod.PUT, "/api/categorias/**").hasAuthority("CAT_EDIT")
```

Deny by default: lo que no está declarado, no pasa. Eso es todo el código de autorización a nivel gateway — se escribe una vez.

**Capa 2 — "de qué fila" → el microservicio (y no la anticípes)**

Si algún día aparece *"este admin solo edita estos productos"*, eso no lo resuelve el gateway porque no conoce tus datos: se resuelve dentro del servicio con `@PreAuthorize` o una query acotada, usando las mismas authorities del token. Para tu e-commerce probablemente nunca haga falta.

La regla para no sobre-diseñar: **si el permiso dice "qué endpoint", es config del IdP + gateway. Si dice "de qué fila", es código del servicio.** Vos pediste no desarrollar la parte de roles — y no la vas a desarrollar: la parte tuya es declarar la matriz endpoint × rol, que es inevitable en cualquier solución (también lo harías con Auth0 o Entra).

---

Y cerrando lo que te quedó sin respuesta de **floci-az / Azure**: sí, en Azure real se despliegan como cualquier imagen Docker (Container Apps, ACI o AKS) con Azure Database for PostgreSQL — o directamente usás **Microsoft Entra ID** como IdP gestionado y no desplegás nada. En floci-az, Container Apps existe pero corre en **modo mocked por defecto** (solo estado ARM, sin contenedores) y está pensado para probar infraestructura, no para hospedar tu stack; su servicio de Entra emulado te sirve para probar la validación JWT del gateway en local, pero no tiene consola de usuarios/roles. Además, **floci no está instalado en tu máquina** — si lo querés, se instala con un comando (te aviso antes).

Con esto el modelo de permisos queda cubierto. **¿Cerramos la decisión con Authentik self-hosted en Docker?**

---

## User

Es liviano ? no tengo muchos recursos asi que necesito algo relativamente liviano

---

## Assistant

_Thinking:_

The user asks: is Authentik lightweight? They don't have many resources and need something relatively lightweight.

I need to verify. From my earlier search results:
- ShipGarden: Zitadel image ~50.7 MiB compressed across 3 layers, roughly a fifth of Keycloak, needs only PostgreSQL.
- Authentik: Python (Django) + Go web server + worker. Since 2025.10 needs only PostgreSQL (Redis removed). But authentik typically runs multiple containers: server, worker, and PostgreSQL. Resource usage — authentik docs state minimum RAM requirements: historically 2 CPU cores and 2GB RAM recommended (docs said "2 CPU cores and 2GB RAM" minimum for server+worker). Let me recall: authentik docs say recommended minimum 2 cores / 2GB RAM. Yes — authentik documentation states "authentik needs a minimum of 2 CPU cores and 2GB of RAM" or similar.

So the honest answer: Authentik is NOT the lightest. It's Python/Django + worker — noticeably heavier than Zitadel (Go, ~50MB image, single binary + PostgreSQL).

Given the constraint "liviano, pocos recursos", the better pick shifts to **Zitadel**: Go, single container + PostgreSQL, ~50MB image (per ShipGarden measurement), event-sourced, low memory footprint (probably ~100-200MB RAM).

But tradeoffs: AGPL-3.0 (user already saw this caveat — self-host without modification is fine, just don't modify and distribute over network... actually AGPL network clause: if you modify and let users interact over network, you must offer source. Running unmodified = fine. But many companies are wary). Zitadel's model: organizations/projects/roles — fine for admin vs customer roles.

Other lightweight options:
- **Logto**: Node + PostgreSQL — moderate (Node heavier than Go but lighter than Python+worker?). Logto runs server + (maybe one service). Memory moderate.
- **Authelia**: very lightweight (Go, ~50-100MB), but users from YAML file or LDAP — no admin console for user management; user management is YAML files. That contradicts "no quiero desarrollar, quiero consola"... but actually YAML config = no development either, but managing users in YAML files is meh. OIDC provider is beta per one source.
- **Casdoor**: Go, light — but security concerns flagged.
- **Dex**: no user store UI.

Also worth considering: what does the whole stack need? They run: Eureka server, Spring Cloud Gateway, N microservices (Spring Boot JVMs — each ~200-500MB RAM), plus DBs. On a machine with few resources, the JVM services will dominate. The IdP choice matters but Authentik's server+worker (Python) can eat ~1.5-2GB.

Recommendation shift: if resources are tight → **Zitadel** (lightest full-featured: Go, single container + PG) if AGPL ok; or if AGPL is a blocker → Logto? Or actually reconsider: maybe **Authelia** if they accept file-based users? Not great for "admin console to manage users".

Wait — also consider: does the user really need heavy self-hosted IdP? They could also consider that on local dev, Zitadel with PostgreSQL — PG itself needs RAM too. Casdoor supports SQLite! Casdoor can run with SQLite — zero DB. That's very light. But security flag... The hackmd source flagged Casdoor security gate failure (critical SAML/token flaws, no fixed version, vendor non-responsive). SAML flaws matter less if they only use OIDC? Still, vendor non-responsive is a red flag.

Also Keycloak discarded (heavy anyway).

Let me verify authentik's memory requirements with a quick search to be accurate — I should verify claims (persona rule). Search: "authentik minimum requirements RAM CPU2026" and "zitadel memory usage requirements".

Also check Logto resource usage maybe.

Let me do 2 parallel searches:
1. "authentik server worker minimum RAM requirements"
2. "zitadel memory usage RAM requirements self-hosted"

Then answer: recommendation based on constraints. Question rule: one question max. Probably end with confirming Zitadel? But AGPL caveat — ask if AGPL is acceptable? That's the fork.

Let me search.

**Tool: websearch**

**Input:**
```json
{
  "query": "authentik server worker minimum RAM CPU requirements self-hosted documentation"
}
```

## [Docker Compose installation | authentik](https://docs.goauthentik.io/install-config/install/docker-compose/)

# Docker Compose installation

This installation method is for test setups and small-scale production setups.

## Requirements​

- A host with at least 2 CPU cores and 2 GB of RAM
- Podman or Docker Compose (Compose v2, see instructions for upgrade)

...

By default, the authentik Docker Compose file mounts the Docker socket to the authentik worker container:

...

This is used for automatic deployment and management of authentik Outposts.

...

After you have downloaded the `docker-compose.yml` file, generated a password and a secret key, and optionally configured your global email, run these commands to retrieve and install the current version of authentik:

docker compose pull 

docker compose up -d

## [Worker | authentik](https://docs.goauthentik.io/worker/)

# Worker

The authentik worker runs background tasks. The worker also watches for blueprints and certificates that are added to the file system. It runs in a separate container from the server to handle these tasks.

## How it works​

authentik tasks are stored and managed using its PostgreSQL database (installed with authentik). When authentik needs to run a background task, the following happens inside a PostgreSQL transaction:

- a row is inserted in a dedicated PostgreSQL table, containing all the relevant information needed to run the task.
- at the end of the transaction, a PostgreSQL trigger executes a `NOTIFY` command to send a notification to workers that a new task is available.

...

All tasks have a time limit. If running a task takes longer than that limit, the task is canceled and marked as failed. The default time limit is configurable with `AUTHENTIK_WORKER__TASK_DEFAULT_TIME_LIMIT`. Some tasks override that time limit for specific purposes, like synchronization.

### Scaling​

How many workers are needed will depend on what tasks are expected to run. The number of tasks that can concurrently run is calculated as follows:

- worker replicas (1 for Docker Compose, defaults to 1 for the Helm chart but can be configured) multiplied by `AUTHENTIK_WORKER__PROCESSES` multiplied by `AUTHENTIK_WORKER__THREADS`

For example, let's say an LDAP source is configured with 1000 users and 200 groups. The LDAP source syncs the users first, then the groups, and finally memberships. All those steps are done by splitting the objects to synchronize into pages, of size `AUTHENTIK_LDAP__PAGE_SIZE`. Let's say that setting is 50. That means there are `1000 / 50 = 20` pages of users, `200 / 50 = 4` pages of groups. We won't worry about the number of membership pages, because those are usually smaller than the previous ones.

This means that in this scenario, the maximum number of concurrent tasks will be 20, plus 1 as there is a "meta" task watching over the synchronization and managing the steps so they are executed in order. Thus, for the synchronization to run as fast as possible, there needs to be 21 available workers when it starts. However, other tasks might also be running at the same time, or might get created while the synchronization is running. Thus, we recommend having more workers than necessary to keep a buffer for those tasks.

On Kubernetes, you can autoscale worker replicas with KEDA based on the number of queued tasks.

## [authentik Self-Hosted: Docker Compose, Requirements - Server Room](https://serverroom.net/use-cases/authentik-self-hosted)

To run authentik self-hosted you need a Linux host with at least 2 CPU cores and 2 GB of RAM, Docker with Compose v2, the project's own compose.yml, and an .env file holding a database password and a secret key. The stack is three containers: server, worker and PostgreSQL. The part that needs care is the first visit: whoever opens /if/flow/initial-setup/ first sets the akadmin password, so that visit happens through an SSH tunnel before the instance has a public name. Below: the official figures and their scope, the install with both web ports held on loopback, the reverse proxy headers authentik requires, and what to back up.

At a glance

- Docs minimum for Docker Compose: 2 CPU cores, 2 GB RAM
- Three containers: server, worker, PostgreSQL 16
- Ports 9000 (HTTP) and 9443 (HTTPS) bound to 127.0.0.1
- akadmin claimed over an SSH tunnel, then proxied
- Back up the database plus ./data, ./certs, ./custom-templates

...

The official Compose file starts three services:

| Container | Job | Persistent path |
| --- | --- | --- |
| server | Core API, flow execution, SSO endpoints, and the embedded outpost for proxy providers | ./data (uploaded icons, flow backgrounds, CSV reports), ./custom-templates |
| worker | Background tasks: email, event notifications, outpost deployment through the Docker socket | ./data, ./certs, ./custom-templates |
| postgresql | All configuration and user data, except uploaded files | named volume `database` |

...

## Requirements and sizing

The installation guide asks for a host with at least 2 CPU cores and 2 GB of RAM, plus Docker Compose v2 or Podman. That page covers the Compose method, which the docs describe as suited to test setups and small-scale production; they also cover a Kubernetes install and a high-availability guide. The docs give no per-user or per-login figure, so anything above the minimum is your own estimate.

Our reading: memory goes first, because PostgreSQL caches its working set in RAM and the Compose file sets `shm_size: 512mb` on both the server and the worker, a ceiling on their shared memory rather than a fixed reservation. Cores matter when many logins land at once. Disk needs room for the images, the database, and the dumps you retain, one of them taken before every upgrade.

On the dedicated server range, a Moonshot cartridge with 8 cores and 8 to 32 GB clears the minimum several times over for a team-sized directory and keeps the identity service off the machines it protects. A racked single-socket server suits an instance that also hosts the database for other apps, and it adds a full out-of-band console. Racked machines are handed over in about 30 minutes once payment is approved.

## [Authentik SSO Self-Hosted: Worth It for a Household? | Self Host Life](https://selfhostlife.com/authentik-sso-self-hosted/)
Published: 2026-09-10T00:00:00.000Z

## What Does It Take to Run?

A host with at least 2 CPU cores and 2 GB of RAM, which is the requirement on authentik’s Docker Compose installation page (checked 10 September 2026). That floor is a real constraint on the class of machine this site usually writes about: a five-dollar VPS with 1 GB does not clear it. The server image publishes only `linux/amd64` and `linux/arm64`, checked against the container registry on 10 September 2026, so a Raspberry Pi works while a 32-bit Raspberry Pi OS has no image to pull.

...

### How much memory does authentik need?

The project’s Docker Compose installation page asks for a host with at least 2 CPU cores and 2 GB of RAM, checked 10 September 2026. That is the documented minimum for the whole stack rather than a measured idle figure, and it is the number to plan against instead of a RAM reading from a blog post, which was taken on somebody else’s hardware.

## [Authentik Hosting: Docker Setup and Real Cost | Use Apify](https://use-apify.com/docs/self-hosted/identity/authentik)

What authentik hosting is not: a managed option. Authentik Security publishes no cloud edition. Its pricing page says, plainly, "We do not currently provide a hosted version of authentik" (goauthentik.io/pricing, checked 2026-09-09). Hosting it means three containers on a Linux box: `server`, `worker`, and PostgreSQL. Redis is no longer one of them. It was removed in release 2025.10. The published minimum is 2 CPU cores and 2 GB of RAM.

...

## Prerequisites​

- A VPS with at least 2 CPU cores and 2 GB of RAM. authentik's install docs state that minimum; 4 GB gives you room for the apps you will put behind it.
- Docker Engine 25+ and Docker Compose V2.
- A domain with an A record pointing at the VPS (`auth.yourdomain.com`).
- About 30 minutes.

...

## How much RAM does authentik need?​

authentik's install docs give a floor of 2 CPU cores and 2 GB of RAM.

We measured this stack at 610 MB idle and 925 MB peak on 2026-05-02, on local Docker with 4 vCPU and 8 GB. That run used 2024.12.3, when Redis was still in the stack, so read it as history rather than a current figure. We have not re-measured 2026.8.1 and will not publish a number we did not run.

| Service (measured 2026-05-02 on 2024.12.3) | Idle | Peak |
| --- | --- | --- |
| server | 250 MB | 400 MB |
| worker | 200 MB | 300 MB |
| postgresql 16 | 100 MB | 150 MB |
| redis (removed in 2025.10) | 50 MB | 60 MB |
| caddy | 10 MB | 15 MB |
| Total | 610 MB | 925 MB |

What provably changed since: one fewer container, and PostgreSQL carrying roughly 50% more connections than before. Expect the database's share to grow and Redis's 50 MB to disappear.

Size for 4 GB when authentik is the only tenant. Size for 8 GB when it shares the box with something substantial — Coolify running your app deployments, or an analytics stack like Plausible. The apps that most benefit from sitting behind authentik, a self-hosted wiki like Outline among them, are exactly the ones you were going to co-locate anyway.

...

What is the difference between the authentik server and worker containers?

They run the same image with different commands. The server handles API requests, flow executions and SSO requests, and it also contains the embedded outpost that serves proxy providers. The worker executes background work: sending email, the event notification system, and everything listed on the System Tasks page. Tasks are queued in a PostgreSQL table and workers are notified through a PostgreSQL NOTIFY trigger. If the worker is down, logins still succeed but email, notifications and scheduled tasks stop.

## [Install Authentik with Docker Compose | Secure Apps](https://perlod.com/tutorials/install-authentik-with-docker-compose/)
Published: 2026-06-12T00:00:00.000Z

## Requirements to Install Authentik with Docker Compose

To install Authentik with Docker Compose, make sure your server has:

- At least 2 CPU cores and 2 GB of RAM.
- Ubuntu 22.04 or newer is running.
- Docker and Docker Compose v2 are installed.
- A domain name pointing to your server, which is needed for reverse proxy and SSL.
- Ports 80 and 443 are open in your firewall.

A fresh Linux VPS is the perfect place to run this. You get a dedicated IP, full root access, and full control over what runs on the machine.

## [How to Run Authentik in Docker for Identity Provider](https://oneuptime.com/blog/post/2026-02-08-how-to-run-authentik-in-docker-for-identity-provider/view)
Published: 2026-02-08T00:00:00.000Z

## Prerequisites

Authentik requires:

- Docker or Podman with Docker Compose V2
- At least 2 CPU cores and 2 GB of RAM
- A domain name (for production deployments)
...
worker:
    image: ${AUTHENTIK_IMAGE:-ghcr.io

## [authentik on VPS: Minimum RAM, CPU Requirements & Cost](https://runonvps.com/apps/authentik/)

## Minimum and recommended VPS specs

Badges are attached to each value. An estimate is never presented as an official project requirement.

authentik hardware requirements

| Resource | Minimum | Recommended |
| --- | --- | --- |
| RAM | 2 GB Official | 4 GB Estimated |
| CPU | 2 CPU cores Official | 2 CPU cores Estimated |
| Disk | 10 GB Estimated | Not stated |

Disk figures describe the application footprint recorded in the dataset. Personal media, documents, backups and other user data can require substantially more storage.

...

This is our sizing suggestion, derived from authentik’s recommended RAM of 4 GB. It is not an official requirement and it does not guarantee that every workload, plug-in set or media library will fit comfortably.

...

### What are the minimum VPS requirements for authentik?

In the RunOnVPS dataset, authentik needs at least RAM 2 GB, CPU 2 CPU cores, disk 10 GB. Some of these figures are estimated rather than officially published — the badge next to each value in the table above shows its basis. Our suggested starting tier, based on the recommended RAM, is the 4 GB RAM tier.

...

## Important notes

Official Docker Compose docs (checked 2026-10-31) require a host with at least 2 CPU cores and 2 GB of RAM: min CPU/RAM are official, no fixed disk figure so min disk is an estimate. Official install downloads docker-compose.yml (server + worker + PostgreSQL + Redis), listens internally on 9000/9443, initial setup at /if/flow/initial-setup/; PG_PASS and AUTHENTIK_SECRET_KEY are generated into .env. GitHub stars 26182 on 2026-10-31. License: core under MIT per repo LICENSE, enterprise directory under a separate license.

**Tool: websearch**

**Input:**
```json
{
  "query": "Zitadel self-hosted memory usage RAM requirements lightweight container"
}
```

## [Self-Hosting ZITADEL: Operational Overhead & Resource Guide](https://help.zitadel.com/self-hosting-zitadel-overhead-resources)
Published: 2026-06-15T00:00:00.000Z

### Infrastructure Footprint

Compute

Extremely lightweight. Containers require 512MB–1GB RAM minimum. While they can run on less than 1 CPU core, 2–4 vCPUs are recommended to absorb short CPU spikes during password hashing.

Database

Backed by PostgreSQL (versions 14 to 18 supported). ZITADEL uses an event-driven, relational architecture, eliminating the need for complex external session caches for basic horizontal scaling. 

Caching (Optional)

Redis can be plugged in to speed up lookups of frequently accessed objects under high loads.

## [ZITADEL Production Setup | ZITADEL Docs](https://zitadel.com/docs/self-hosting/manage/production)

We strongly advise implementing rate limiting and bot detection. Even with robust cryptography, reducing request volume helps protect against brute-force and denial-of-service attacks.

### General resource usage Copy Anchor Link

ZITADEL itself requires approximately 512MB of RAM and can operate with less than one CPU core. The database component, under typical conditions, utilizes about one CPU core per 100 requests per second (req/s) and 4GB of RAM per core, which includes some caching.

Password hashing

Be aware of CPU spikes when hashing passwords. We recommend to have 4 CPU cores available for this purpose.

### Production HA cluster Copy Anchor Link

For a minimal high-availability setup, we recommend a cluster of 3 nodes, each with 4 CPU cores and 16GB of memory.

If you exclude non-essential services like log collection and metrics, you can reduce the resources to approximately 4 CPU cores and 8GB of memory per node.

## [Deploy ZITADEL | ZITADEL Docs](https://zitadel.com/docs/self-hosting/deploy/overview)

## Prerequisites Copy Anchor Link

- For test environments, ZITADEL does not need many resources, 1 CPU and 512MB memory are more than enough. (With more CPU, the password hashing might be faster)
- A PostgreSQL instance. Make sure to check the database configuration options in our Production Guide.

## [What are ZITADEL Minimum Self-Hosted Specs?](https://help.zitadel.com/what-are-zitadel-minimum-self-hosted-specs)

# What are ZITADEL Minimum Self-Hosted Specs?

## Looking to define minimum specifications needed for Self-Hosting a ZITADEL instance?

When self-hosting ZITADEL, it can consume around 512MB RAM and run with less than 1 CPU core. The database component, under typical conditions, utilizes about one CPU core per 100 requests per second (req/s) and 4GB of RAM per core, which includes some caching.

Be aware of CPU spikes when hashing passwords. We recommend to have 4 CPU cores available for this purpose.

## [Set up ZITADEL with Docker Compose | ZITADEL Docs](https://zitadel.com/docs/self-hosting/deploy/compose)

## Prerequisites Copy Anchor Link

- Docker Engine 24+ with the Compose plugin (`docker compose`)
- A machine with at least 2 GB RAM

See Requirements for supported database, cache, and proxy versions.
...
Next.js) → PostgreSQL. All routing, including gRPC

## [Where Should You Host ZITADEL? A Guide to Cloud-Native Identity Infrastructure | ZITADEL](https://zitadel.com/blog/where-should-you-host-zitadel)
Published: 2026-01-15T00:00:00.000Z

## Full Self-Hosting: The "Keycloak Alternative" for Kubernetes

This is where things get interesting. I see a lot of users running ZITADEL in homelabs or on bare metal in private data centers. I love this use case because ZITADEL's architecture makes it incredibly efficient compared to legacy IdPs. But I have one Golden Rule:

⚠️ Docker Compose is for Development (and homelabs), Not Production.

We provide a docker-compose.yaml to help you try ZITADEL locally. It is great for spinning up an instance on your laptop to test things like OIDC flows. However, I cannot recommend it for production. It doesn't handle zero-downtime updates, it doesn't self-heal, and it lacks the resilience required for an identity system in a production setting.

### The "Minimum Viable ZITADEL" Stack

If you want to self-host in production (whether in a homelab or an air-gapped data center), you need a stack that provides resilience.

Here is the what I recommend:

1. Orchestrator: Kubernetes. 

- It doesn't have to be massive. A lightweight distro like K3s or K0s works perfectly for smaller setups. It gives you the rolling updates and service discovery we rely on.
- Pro Tip: Use our official Helm Charts to streamline the deployment.
2. Database: PostgreSQL (v14-17). 

- If you are on bare metal, use a robust operator like CloudNativePG (CNPG) to manage replication and backups.
3. Networking: A Reverse Proxy with HTTP/2 & TLS. 

- ZITADEL uses gRPC heavily. Your proxy should support HTTP/2 and end-to-end TLS.
- Traefik, NGINX, or Caddy are all excellent choices.

System Specs (Minimum):

- CPU: 1 vCPU (I recommend 2-4 vCPUs to handle password hashing spikes without latency).
- RAM: 512MB (Give it 1GB+ if you want breathing room).

My Verdict: Self-hosting gives you ultimate control and a modern Keycloak alternative, but it comes with the "You Run It" price tag. Ensure you have the Ops skills to manage a stateful database in Kubernetes.

...

| --- | --- | --- | --- |
| ZITADEL Cloud | Startups, SaaS, Teams who want speed | Zero maintenance, High Availability & Managed Security, free tier | Less control over the infrastructure and update cycles |
| Public Cloud (Managed Services) | Enterprise, Compliance-heavy industries | Data Sovereignty, Low Latency, leverages managed DB/K8s for stability | Requires DevOps resources and knowledge |
| Self-Hosted | Homelabs, Air-gapped, Private Metal | Ultimate control, low memory footprint, lowest software cost | High operational overhead (You own the DB) |

## [[Bug]: zitadel-login memory leak and zitadel high memory usage · Issue #10562 · zitadel/zitadel](https://github.com/zitadel/zitadel/issues/10562)
Published: 2025-08-25T00:00:00.000Z

### Environment

Self-hosted

### Version

v4.0.2

### Database

PostgreSQL

### Database Version

15.10

### Describe the problem caused by this bug

When there are repeated login requests, the memory usage in zitadel-login increases until it reaches its limit and crashes due to OOM.

Zitadel itself also starts at 370 MiB which is quite a lot of memory usage for a Go application and increases to 380 MiB during the same time, but at least it decreases again by itself compared to zitadel-login that only keeps increasing and never runs any GC.

...

**conblem** commented on 2026-03-10T09:55:43Z:

> Hello everyone using the case shared by @xmarcelet-saas and @dataviruset, I was able to identify two memory leaks.
> Big thanks for providing such in depth information everyone!
> 
> One is related to our cache handling in the login, and the other is tied to HTTP/2 and gRPC. The cache issue will be resolved by the next16 upgrade (https://github.com/zitadel/zitadel/pull/11629). That same upgrade also bundles a fix from the Next.js team addressing another memory leak, though I'm not certain whether this one was already present in Next 15 (https://github.com/vercel/next.js/pull/85529).
> 
> As for the HTTP/2 leak, I'm currently working on a fix that involves migrating our login API calls to the backend from gRPC to buf connect over HTTP/1.1.
> 
> Once both PRs are merged, I'll go ahead and close this issue. If you encounter any further memory leaks, please open a new issue so we can track them properly.

## [Self-host Zitadel on a VPS with Docker · SSD Nodes](https://www.ssdnodes.com/learn/self-host-zitadel-on-a-vps)
Published: 2026-10-29T22:48:24.000Z

Zitadel itself needs about 512MB of RAM. Set up Postgres, the masterkey, TLS, SMTP and backups on one VPS, and learn what an upgrade does to your database.

## What you need to self-host Zitadel on a VPS

To self-host Zitadel on a VPS you need a Docker host, a public DNS name pointing at it, PostgreSQL, and enough CPU and RAM for both. Zitadel's production guide puts the application itself at about 512 MB of RAM and under one CPU core. The database and password hashing are what grow. Zitadel is an identity provider. It issues tokens over OIDC (OpenID Connect) and SAML (security assertion markup language) so your other services stop keeping their own user lists. The install is a `curl` and a `docker compose up`. The parts that decide whether it survives are the masterkey, the database user, SMTP (simple mail transfer protocol), the backup, and the first upgrade.

Everything below assumes Ubuntu 24.04, Docker Engine 24 or newer with the Compose plugin, and a name like `auth.example.com` already resolving to the server.

## How much VPS does Zitadel need?

The Compose quickstart in Zitadel's docs asks for 2 GB of RAM. That number is for the quickstart. Zitadel's production guide publishes different figures.

Chart Zitadel's own published sizing guidance, October 2026

| config | cpu_cores | ram_gb |
| --- | --- | --- |
| Zitadel itself, under 1 core | 0.5 | 0.5 |
| HA node, reduced setup | 4 | 8 |
| HA node, logs and metrics on | 4 | 16 |

The data behind this chart

```
[
  {
    "config": "Zitadel itself, under 1 core",
    "cpu_cores": 0.5,
    "ram_gb": 0.5
  },
  {
    "config": "HA node, reduced setup",
    "cpu_cores": 4,
    "ram_gb": 8
  },
  {
    "config": "HA node, logs and metrics on",
    "cpu_cores": 4,
    "ram_gb": 16
  }
]
```

Those are published recommendations, not measurements from a running box. Read them as the shape of the problem. The Zitadel process itself is small: the guide puts it at about 0.5 GB of RAM and says it can run on less than one CPU core. The cores are for password hashing, which is deliberately slow, so a burst of logins arrives as a CPU spike, and the guide recommends keeping 4 CPU cores available for it. PostgreSQL is the other half of the bill: the same guide budgets about one core per 100 requests per second and 4 GB of RAM per core.

The two larger rows are not single-box numbers. For a minimal high-availability setup the guide recommends a cluster of three nodes, each with 4 cores and 16 GB, and says you can reduce that to about 4 cores and 8 GB per node if you leave out log collection and metrics. It names no sizing for a single node.

So a 2 GB VPS will start this stack, and it is well below what the guide budgets for the database alone once real traffic arrives. Login is the service every other service depends on. When it is down, nothing that trusts it lets anyone in. Deciding that the guide's database and hashing budget is more than you want to spend on authentication is a reasonable call, and it is far cheaper to make now than after a migration. The comparison of Keycloak, Authentik and Zitadel covers what each one costs in memory and in operational work, and a self-hosted Authentik server is the usual answer on a smaller box.

...

### How much RAM and CPU does a self-hosted Zitadel need?

Zitadel's production guide says Zitadel itself needs about 0.5 GB of RAM and can run on less than one CPU core, and it recommends keeping 4 cores available for password hashing. PostgreSQL is budgeted separately, at roughly one core per 100 requests per second and 4 GB of RAM per core. The larger figures in the guide are per node in a three-node high-availability cluster: 4 cores and 16 GB each, or about 4 cores and 8 GB without log collection and metrics. The Compose quickstart asks for at least 2 GB, which is enough to try it and leaves little room for the database a system other services depend on will need.

---

## Assistant

_Thinking:_

Now I have verified numbers:

**Authentik:**
- Official minimum: 2 CPU cores + 2 GB RAM (whole stack: server, worker, PostgreSQL — 3 containers)
- Measured (2024.12.3, with Redis): 610 MB idle / 925 MB peak; Redis since removed in 2025.10
- Recommended sizing: 4GB when alone**Zitadel:**
- Official: Zitadel itself ~512MB RAM, <1 CPU core (2-4 recommended for password hashing spikes)
- PostgreSQL separate (small at this scale)
- Compose quickstart asks2GB machine
- ~50MB compressed image (ShipGarden)
- Single container + PostgreSQL
- Caveat: past memory leaks in login (fixed in 2026), event-sourced Go.

So the honest answer: Authentik is NOT the lightest — 3 containers, Python/Django, official floor 2 cores/2GB. Zitadel is lighter: 1 container + PG, ~512MB for the app itself.

Also worth noting their full stack: Eureka + Gateway + N Spring Boot microservices — JVM services will be the bigger RAM consumers (each Spring Boot ~200-400MB+). If resources are tight, the IdP isn't even the biggest consumer — count all those JVMs. Good architectural teaching point: "antes de elegir el IdP por ahorro, contá tus JVMs: Eureka + gateway + microservicios cada uno 200-400MB..." — that's the real RAM budget.

Recommendation shift: **Zitadel** for lightness, with AGPL caveat (unmodified self-host = no obligations to share source; don't modify and distribute). Or if AGPL is a problem → Authentik (heavier) or Logto (Node, moderate). Also mention: if still too much, the ultra-light option is Authelia but users managed in YAML — not aligned with wanting console.

Wait — also consider: user management console matters to them ("no querés desarrollar eso"). Zitadel has full admin console + user/role management. Good.

Also DB note: Zitadel needs PostgreSQL — but wait, they'll have DBs for their e-commerce anyway (MySQL/Postgres), so PG adds another DB process. Authentik also needs PG. Casdoor supports SQLite (zero DB) — very light, but security concerns flagged. I'll keep Casdoor out (already flagged).

Comparison numbers to present:
- Authentik: 3 contenedores (server, worker, PostgreSQL), mínimo oficial 2 cores/2GB, ~600-900MB RAM real en reposo... Actually measured 610MB idle was with Redis on older version; current ~560-610. Say "~600 MB en reposo, piso oficial de 2 GB para el host".
- Zitadel: 1 contenedor + PostgreSQL, la app ~512MB, puede correr con <1 core. Imagen ~50MB.

Plus the bigger point: their own stack will eat more RAM:
- Eureka Server: ~300-500MB
- Spring Cloud Gateway: ~300-500MB (reactive, can tune low)
- each microservice: ~250-500MB
- PostgreSQL/MySQL: ~200MB-1GB dependingSo 4-6 services≈ easily 2-4GB. If they have "pocos recursos" (e.g., 8GB laptop), they should consider: number of microservices (maybe fewer services for a simple e-commerce!), JVM tuning (-Xmx), and run DB + IdP lean.

Teaching moment (persona cares about fundamentals): a "simple e-commerce" with Eureka + gateway + N microservices is itself heavy — maybe question whether they need many microservices. But careful not to derail; mention briefly.

Format: short answer first: "Authentik no es el más liviano — verifiqué los números". Then Zitadel numbers. Then recommendation + the stack RAM reality check. One question at end: switch to Zitadel (accepting AGPL)?Let me structure:

1. Verificado: Authentik no es liviano — docs: mínimo 2 cores/2GB, 3 contenedores (server, worker, PG), medido ~610MB reposo/~925 pico.
2. Zitadel sí es liviano: app ~512MB, <1 core, 1 contenedor + PG, imagen ~50MB. Tradeoff: AGPL (usarlo sin modificar = sin obligaciones; modificarlo y publicarlo por red = otra historia). Ya viste ese caveat.
3. Reality check: tu propio stack de Spring va a consumir más que el IdP: Eureka + gateway + microservicios JVM (~250-500MB c/u). Si el recurso es ajustado, la palanca más grande es cuántos microservicios levantás, no cuál IdP elegís.
4. Pregunta: ¿pasamos a Zitadel considerando que es AGPL (usarlo tal cual no tiene drama) o preferís mantener Authentik?

One question only. Good.

Keep it concise.

Verifiqué los números oficiales, porque acá importa el dato duro: **Authentik NO es liviano**.

| | Authentik | Zitadel |
|---|---|---|
| Contenedores | 3: server + worker + PostgreSQL | 1 + PostgreSQL |
| Mínimo oficial | **2 cores / 2 GB RAM** (docs de instalación) | la app: **~512 MB RAM, <1 core** |
| Medido en reposo | ~610 MB (pico ~925 MB) | ~370-500 MB en el proceso principal |
| Peso de imagen | Pesada (Python/Django + worker) | ~50 MB comprimida (Go) |

Authentik es Python con dos procesos y presupuesta para hosts de 2 GB mínimo — en una máquina justa es el más pesado de la lista. **Zitadel es el liviano**: un binario Go + PostgreSQL, ~512 MB para la app y puede correr con menos de un core (conviene 2-4 para los picos de hashing de contraseñas en login).

Su tradeoff sigue siendo el **AGPL-3.0**: usarlo *tal cual*, sin modificarlo, no te obliga a nada; el problema es modificarlo y exponerlo por red. Si solo lo desplegás y configurás, es transparente. Si el AGPL no te gusta, la alternativa liviana queda descartada y volvés a Authentik (el precio en RAM es real).

**Pero hay algo más importante que el IdP** — y te lo digo porque es fácil distraerse optimizando lo chico: tu propio stack va a consumir más que cualquier IdP:

- Eureka Server: ~300-500 MB
- Spring Cloud Gateway: ~300-400 MB
- **Cada microservicio Spring Boot: ~250-500 MB** (es JVM, no hay atajo)

Con gateway + Eureka + 3-4 servicios ya hablamos de 1.5-2 GB solo en Java, más las bases de datos. Si el recurso es ajustado, la palanca más grande no es qué IdP elegís: es **cuántos microservicios levantás** (para un e-commerce chico, 3-4 bien acotados con `-Xmx` acotado rinden más que 8 servicios de dogmática).

Con esto la recomendación cambia: **Zitadel en vez de Authentik**, si el AGPL te resulta aceptable. ¿Vamos con Zitadel?

---

## User

si quiero usar authorizer

---

## Assistant

_Thinking:_

The user says "si quiero usar authorizer" — a bit ambiguous. "si quiero usar authorizer" — "yes, I want to use authorizer". Hmm. What is "authorizer"?Possibilities:
1. They mean confirming Zitadel? "si" = yes... "quiero usar authorizer" — maybe they're saying yes but they want to use "Authorizer" — there's a product called **Authorizer** (authorizer.dev) — an open-source authentication/authorization server, GraphQL-based, self-hostable with Docker. Yes! Authorizer is an open-source alternative to Auth0/Cognito: single container, has DB... Authorizer (authorizer.dev) is open source (MIT?), Go + React? Actually Authorizer is written in Go with a React admin dashboard, single Docker container, supports SQL databases (Postgres, MySQL, SQLite...), GraphQL API, JWT, roles? It has RBAC? Authorizer has "roles" I think — it added roles/permissions? Let me recall: Authorizer.dev — "Open source Auth0/Cognito alternative", features: JWT, social logins, magic links, multi-tenancy (multiple apps?), admin panel, GraphQL API. Roles: Authorizer added RBAC in later versions? Not sure.So the user might be saying: "sí, quiero usar Authorizer" — the product. That's a plausible reading: lowercase "authorizer" as a product name.

2. Or they're responding to my question about Zitadel and saying "sí" + something about using an "authorizer" component in Spring Cloud Gateway (the authorizer concept — e.g., Spring Security's AuthorizationManager, or AWS API Gateway authorizers, or a custom Authorizer class)? In Spring Cloud Gateway context, "authorizer" could mean... hmm. Actually in Auth0/Amplify terminology there's "authorizer". In Kong there's... In AWS API Gateway there's Cognito User Pool authorizer / Lambda authorizer.3. Could be a typo/voice input of "authorization"? "si quiero usar authorization" — doesn't parse well.

Most likely: they're approving Zitadel... no wait. Let me re-read: my question was "¿Vamos con Zitadel?" Answer: "si quiero usar authorizer" = "Sí, quiero usar Authorizer" — i.e., yes (acknowledging), but I want to use Authorizer (the product). OR "sí" + explanation of why: they want to use an authorizer pattern.Given ambiguity, the persona rule: "Ask at most one question at a time. After asking it, STOP and wait." and "Clarify ambiguous instructions". This is genuinely ambiguous. But maybe I can first verify what Authorizer (the product) is — it's a real product: https://authorizer.dev — open source Auth0 alternative, single container, GraphQL, JWT. If that's what they mean, I should evaluate it against their requirements (roles/permissions for admin vs customers, Docker, local, light).

Let me search to verify Authorizer's current state (2026): features (RBAC?), license, resource usage, maintenance status. That way I can give a useful answer either way: "Si te referís al producto Authorizer..." and evaluate. If they meant something else, they'll correct.Best approach: quickly research Authorizer (authorizer.dev) — is it maintained? Does it support roles/permissions? Resource usage? Then answer: yes/no it fits, compare with Zitadel (lightness), and if it fits, fine.

Actually the safest interpretation: "Sí, quiero usar Authorizer" — they read somewhere about Authorizer as a lightweight Auth0 alternative and want it. Let me verify facts:

Search: "Authorizer.dev open source auth docker roles RBAC 2026" and maybe GitHub repo status (authorizerdev/authorizer).

Let me do 2 parallel searches.

**Tool: websearch**

**Input:**
```json
{
  "query": "Authorizer.dev open source auth server docker RBAC roles features 2026"
}
```

## [Authorizer | Self-hosted auth, fine-grained authorization & permission-aware AI](https://authorizer.dev/)

# Own your identity layer

The open-source auth platform you run yourself—authentication, fine-grained authorization, and permission-aware AI, with every user in your own database.

```
$ docker run -p 8080:8080 -u root \
    -v authorizer_data:/authorizer/data \
    quay.io/authorizer/authorizer \
    --database-type=sqlite \
    --database-url=/authorizer/data/data.db \
    --client-id=123456 \
    --client-secret=secret \
    --admin-secret=admin \
    --jwt-type=HS256 \
    --jwt-secret=test \
    --encryption-key=test-encryption-key
```

✓ Auth server live at http://localhost:8080

✓ Dashboard at http://localhost:8080/dashboard

✓ OIDC config at http://localhost:8080/.well-known/openid-configuration

OpenFGA engine embedded · 13+ databases · OAuth2/OIDC

Runs on

- Kubernetes·
- Docker·
- Helm·
- Railway·
- PostgreSQL·
- MongoDB

...

SECURE SESSION MANAGEMENT

Auth with best services baked in. Secure Session management implemented with HTTP only cookies. Authorization Code flow implemented for mobile based auth.

- OAuth 2.0 (RFC 6749) ↗
- PKCE (RFC 7636) ↗
- JWT (RFC 7519) ↗

AUTH RECIPES

Multiple auth recipes supported out of the box: social login, email and password, magic link, and more.

CONNECT TO YOUR DATABASE

It supports 13+ databases including major SQL, NoSQL and GraphDBs

INTEGRATE OR IMPLEMENT

Built-in universal login page, plus APIs and SDKs so you can build custom UI in JavaScript, React, or any framework.

Role Based Access Control

Define the roles and authorize your APIs with role based session tokens

FINE-GRAINED AUTHORIZATION (FGA)

Relationship-based access control with an embedded OpenFGA (Google Zanzibar) engine—check_permissions and list_permissions, in-process, no extra service to run.

- OpenFGA ↗
- Zanzibar paper ↗

PERMISSION-AWARE AI & RAG

Build AI assistants and RAG pipelines that respect who can see what—pre-filter retrieval with the user’s own token so the model never reads what it shouldn’t.

...

DEPLOY ANYWHERE

Deploy authentication and authorization anywhere you need: Railway, Heroku, Render, Docker, Kubernetes, and more.

- Docker ↗
- Kubernetes ↗
- Helm ↗

...

### Permissions are relationships, not roles

Grant access by writing facts, not code. Ask Authorizer for the user's allow-list, then hand it to your vector store as a filter.

// 1. Ask as the user — subject pinned from their JWT

list_permissions(relation: "can_view",

object_type: "document")

// alice → ["onboarding_guide", "tech_stack"]

// 2. Pre-filter the vector search — forbidden chunks

// are never scored

match(source ∈ allowed)

Same `check_permissions` / `list_permissions` helpers ship in the Go, JavaScript, and Python SDKs.

...

What is Authorizer?

Authorizer is an open-source (Apache-2.0) authentication and authorization server you deploy on your own infrastructure. It supports OAuth2 and OpenID Connect, social logins, magic links, email/password, WebAuthn passkeys, multi-factor authentication, SAML 2.0 and OIDC enterprise SSO, SCIM 2.0 provisioning, webhooks, role-based access control, and fine-grained authorization—while storing users in a database you control.

...

Does Authorizer support fine-grained authorization (FGA)?

Yes. Authorizer ships with an embedded OpenFGA engine—the open-source implementation of Google's Zanzibar relationship-based access control (ReBAC)—in the same Go binary that handles login. You model permissions as relationships between users, teams, and resources, then call check_permissions and list_permissions to enforce them. This goes beyond role-based access control (RBAC) to answer document-, object-, and record-level questions like "can this user view this file?" with no extra service to deploy.

## [Introduction | Authorizer](https://docs.authorizer.dev/)

Authorizer is an open-source authentication and authorization solution for your applications. Bring your database and have complete control over user information. You can self-host Authorizer instances and connect to any supported database.

### Features​

- Sign-in / Sign-up with email ID and password
- Secure session management with HTTP-only cookies
- Email verification
- OAuth2 and OpenID Connect compatible APIs
- APIs to update profile securely
- Forgot password flow using email
- Social logins (Google, GitHub, Facebook, LinkedIn, Apple, Discord, Twitter, Twitch, Roblox, Microsoft)
- Role-based access management and fine-grained authorization (FGA) via an embedded OpenFGA (Zanzibar ReBAC) engine
- Password-less login with magic link
- WebAuthn / passkey registration and login
- Multi-factor authentication: TOTP, email OTP, SMS OTP, and passkey as a second factor
- SMS OTP via Twilio
- Enterprise SSO — SAML 2.0 as Service Provider and Identity Provider, OIDC federation, verified email domains, and home realm discovery (SSO guide)
- SCIM 2.0 user and group provisioning (SCIM)
- Organizations and multi-tenancy with org-scoped admin roles
- Machine-to-machine auth (`client_credentials`) and secretless workload identity — RFC 7523 client assertions, SPIFFE JWT-SVIDs, Kubernetes TokenReview (Workload Identity)
- Agent delegation via RFC 8693 token exchange (Token Exchange)
- Email templating and webhooks
- Rate limiting, security hardening, and Prometheus metrics (Metrics & Monitoring)
- GraphQL, REST, and gRPC APIs
- MCP server for AI agents

...

Or with Docker:

```bash
docker run -p 8080:8080 quay.io/authorizer/authorizer:latest \  --database-type=sqlite \  --database-url=test.db \  --url=http://localhost:8080 \  --jwt-type=HS256 \  --jwt-secret=test \  --encryption-key=test-encryption-key \  --admin-secret=admin \  --client-id=123456 \  --client-secret=secret

## [authorizerdev/authorizer](https://github.com/authorizerdev/authorizer/)

**Authorizer** is an open-source authentication and authorization server you can self-host. Connect any supported database (13+ backends including Postgres, MySQL, SQLite, SQL Server, YugaByte, MariaDB, Cassandra, ScyllaDB, MongoDB, ArangoDB, DynamoDB, and Couchbase) and run OAuth2/OIDC, social login, MFA, magic links, RBAC, webhooks, and email templates from one place.

...

#### We offer the following functionality

- ✅ Sign-in / Sign-up with email ID and password
- ✅ Secure session management
- ✅ Email verification
- ✅ OAuth2 and OpenID Connect compatible APIs
- ✅ APIs to update profile securely
- ✅ Forgot password flow using email
- ✅ Social logins (Google, GitHub, Facebook, LinkedIn, Apple, Discord, Twitter, Twitch, Roblox, Microsoft)
- ✅ Role-based access management
- ✅ Fine-grained authorization (ReBAC via embedded OpenFGA)
- ✅ Password-less login with magic link
- ✅ TOTP-based multi-factor authentication
- ✅ SMS OTP via Twilio
- ✅ Email templating
- ✅ Webhooks
- ✅ GraphQL, REST, and gRPC APIs (all transports share the same service layer)
- ✅ Admin API — user management, webhooks, email templates, audit logs, and FGA model/tuples over all three transports
- ✅ Rate limiting and security hardening (CSRF, CORS, HSTS, CSP, trusted proxies)
- ✅ Prometheus metrics and health/readiness endpoints

...

### Run with Docker

The default image runs as **non-root** (UID `65532`). Writable mounts (SQLite under `/authorizer/data`, etc.) are usually **root-owned**, so pick one of:

1. **Run as root for that container** (simplest for local SQLite + volumes):

   ```sh
   docker run -p 8080:8080 -u root \
     -v authorizer_data:/authorizer/data \
     quay.io/authorizer/authorizer \
     --database-type=sqlite \
     --database-url=/authorizer/data/data.db \
     --client-id=123456 \
     --client-secret=secret \
     --admin-secret=admin \
     --jwt-type=HS256 \
     --jwt-secret=test
   ```

2. **Keep non-root** and make the mount writable by `65532` (good for production-style bind mounts):

   ```sh
   mkdir -p ./data && sudo chown -R 65532:65532 ./data
   docker run -p 8080:8080 \
     -v "$(pwd)/data:/authorizer/data" \
     quay.io/authorizer/authorizer \
     --database-type=sqlite \
     --database-url=/authorizer/data/data.db \
     ...
   ```

3. **Build from source** with the root target (no `-u` at run time):

   ```sh
   docker build --target final-root -t authorizer:root .
   docker run -p 8080:8080 -v authorizer_data:/authorizer/data authorizer:root \
     --database-type=sqlite --database-url=/authorizer/data/data.db ...
   ```

- Port **8080** serves the app and GraphQL; use `-p 8080:8080` to expose it.
- Volume `authorizer_data` persists the SQLite DB; use a named volume or a host path (e.g. `-v $(pwd)/data:/authorizer/data`).
- All config is passed as CLI arguments (the image uses `ENTRYPOINT ["./authorizer"]` so args after the image name go to the binary). See MIGRATION.md for the full list of flags.

## [Getting Started | Authorizer](https://docs.authorizer.dev/getting-started/)

## Option B: Docker​

```bash
docker run -p 8080:8080 quay.io/authorizer/authorizer:latest \  --database-type=sqlite \  --database-url=test.db \  --url=http://localhost:8080 \  --jwt-type=HS256 \  --jwt-secret=test \  --encryption-key=test-encryption-key \  --admin-secret=admin \  --client-id=123456 \  --client-secret=secret
```

With PostgreSQL:

```bash
docker run -p 8080:8080 quay.io/authorizer/authorizer:latest \  --database-type=postgres \  --database-url="postgres://user:pass@host:5432/authorizer" \  --url=http://localhost:8080 \  --jwt-type=HS256 \  --jwt-secret=test \  --encryption-key=test-encryption-key \  --admin-secret=admin \  --client-id=123456 \  --client-secret=secret
```

See Docker deployment for Docker Compose examples.

## [Authorization (FGA) | Authorizer](https://docs.authorizer.dev/core/authorization)

# Authorization (Fine-Grained)

Authorizer ships a built-in fine-grained authorization (FGA) engine powered by an embedded OpenFGA instance — the open-source implementation of Google's Zanzibar relationship-based access control (ReBAC) model. Instead of static `role → permission` tables, you describe your domain as types and relations, grant access with relationship tuples, and ask the engine `Check(user, relation, object)` at request time.

FGA is opt-in and runs in-process — there is no extra service to deploy and no network hop on a check.

...

Nothing to configure.
- NoSQL main database (MongoDB, DynamoDB, …): OpenFGA can't use these, so FGA is disabled unless you

...

## 2. The authorization model​

The model is your permission rulebook. It declares the object types you protect, the relations on each type (`owner`, `editor`, `viewer`, `can_view`…), and how those relations are computed from one another. You write it once; access is then granted with data (tuples), not by editing the model.

It is expressed in OpenFGA's DSL:

```dsl
model  schema 1.1type usertype document  relations    define owner: [user]    define editor: [user] or owner    define viewer: [user] or editor    define can_view: viewer    define can_edit: editor    define can_delete: owner
```

Reading it: an `owner` is also an `editor` (`or owner`), an `editor` is also a `viewer`, and the permission relations (`can_view`, `can_edit`, `can_delete`) resolve through them. A single `owner` tuple therefore grants view, edit, and delete.

The DSL also supports hierarchies (`viewer from parent`), group usersets (`[user, team#member]`), public access (`[user:*]`), and exclusions (`viewer but not blocked`). OpenFGA's parameterized conditions (ABAC/CEL, `[user with someCondition]`) are not usable through Authorizer's API: tuples cannot carry a condition and checks cannot supply condition context — use contextual tuples or scheduled tuple deletion instead.

### From the dashboard​

Open `/dashboard` → Authorization → Step 1 · Define the model. Two ways in:

- Roles & permissions (the default) — a simple matrix: list your roles (`admin`, `editor`, `viewer`) and the actions they can take (`view`, `edit`, `delete`), then tick which role can do what. The dashboard turns the matrix into a valid OpenFGA RBAC model for you — no DSL to learn. It starts from a standard `admin / editor / viewer` set; your configured instance roles (`--roles`) are offered as one-click additions.
- Advanced (DSL) — the raw editor with a catalog of ready-made examples (document sharing, folder hierarchy, organizations & teams, RBAC, groups, block lists, multi-tenant SaaS, GitHub-style repos, time-bound conditions) and a plain-English summary of whatever you type.

...

A relationship tuple is a single fact: `user` is `relation` of `object`. Tuples are the data that actually grants access; add and remove them any time without touching the model.

| Tuple | Meaning |
| --- | --- |
| `user:1b9d…` `viewer` `document:1` | This user can view document 1 |
| `user:2c8e…` `owner` `document:1` | This user owns document 1 (⇒ editor, viewer) |
| `team:9#member` `viewer` `document:1` | Every member of team:9 can view it |
| `user:*` `viewer` `document:5` | Document 5 is public |

## [Authorizer 2.4: Open-source auth for AI agents and modern enterprise apps](https://blog.authorizer.dev/authorizer-2-4-open-source-auth-for-ai-agents-and-modern-enterprise-apps)

# Authorizer 2.4: Open-source auth for AI agents and modern enterprise apps

Product Hunt · GitHub . Website . Docs

Authorizer 2.4.0 is out.

I've been working on Authorizer for a few years, and 2.4 is one of the biggest releases I've worked on so far.

...

## What's new in Authorizer 2.4?

The release adds quite a lot, but these are the areas I'm most excited about:

- OAuth 2.1 and MCP support
- Service accounts and machine-to-machine authentication
- Agent-to-agent delegation
- Workload identity
- WebAuthn and passkeys
- SAML and OIDC SSO
- SCIM 2.0 user and group provisioning
- Organizations and multi-tenant identity
- Fine-grained authorization with OpenFGA
- GraphQL, REST, gRPC, and MCP APIs
- A redesigned MFA flow
- OAuth and security hardening

...

## Service accounts and workload identity

Not every request comes from a person.

Background workers, services, CI jobs, Kubernetes workloads, and other systems need to authenticate too.

Authorizer 2.4 adds first-class service accounts using the OAuth `client_credentials` flow.

Service accounts can also use workload identity mechanisms including:

- RFC 7523 JWT bearer assertions
- SPIFFE JWT-SVID
- Kubernetes TokenReview

This means workloads don't necessarily have to depend on long-lived static credentials.

Service accounts also become first-class subjects in the authorization layer, which makes it possible to apply authorization rules to machines in much the same way as users.

...

## Fine-grained authorization with OpenFGA

Authentication tells you who someone is.

Authorization answers a different question:

What are they allowed to do?

Authorizer 2.4 uses an embedded OpenFGA-based relationship authorization engine.

This lets you model relationships such as:

```text
user:lakhan
    ↓ member
organization:acme
    ↓ owns
project:authorizer

```

And then make permission checks based on those relationships.

The FGA engine can run embedded or use an external OpenFGA store, depending on the deployment.

...

## Built for self-hosting

Authorizer remains open source and self-hostable.

You can run it yourself and keep control over your authentication and authorization infrastructure.

If you want to try it, the easiest place to start is the Getting Started guide.

## [CHANGELOG.md](https://github.com/authorizerdev/authorizer/blob/main/CHANGELOG.md)

### Added

- **gRPC transport (port 9091)**: all 20 public auth operations and 32 admin operations are now served over native gRPC alongside GraphQL and REST. The listener binds to `--grpc-port` (default `9091`), separate from the HTTP port. All three transports share the same service layer and return identical flat response shapes (#634, #635).
- **`AuthorizerAdminService` gRPC + REST**: 32 admin operations — user management (`Users`, `User`, `UpdateUser`, `DeleteUser`, `InviteMembers`), verification requests, tokens, webhooks, email templates, audit logs, FGA model/tuples (`_fga_model`, `_fga_tuples`, `_add_fga_tuple`, `_delete_fga_tuple`), and admin session/meta — are now reachable over all three transports. Previously admin ops were GraphQL-only (#631).
- **gRPC auth interceptor**: bearer-token / session-cookie authentication is applied uniformly by a gRPC server interceptor. The verified identity is attached as `authctx.Principal` so all handlers share a single auth path with no per-handler duplication (#636).
- **Client metadata helpers** (`transport.MetaFromGRPC`): extract client ID, session token, and access token from gRPC incoming metadata using the same keys as the HTTP handlers, enabling consistent context propagation across transports (#636).

...

- **License: relicensed from MIT to Apache License 2.0.** Per the CNCF IP Policy (Charter §11(b)(iii)), Authorizer's outbound code is now distributed under the Apache License 2.0. Existing copies distributed under the MIT License remain valid under their original grant; this change applies to the project's outbound license going forward. See NOTICE for attribution.
- **Fine-grained authorization is always enforcing.** The previously-proposed `--authorization-enforcement` flag and its dual `permissive`/`enforcing` modes were removed before shipping. `required_permissions` checks against an unmatched or denied `(resource, scope)` pair return `unauthorized`. There is no permissive "log but allow" mode.
- **Authz Prometheus shape**: `authorizer_authz_checks_total` has only a `result` label (`allowed|denied|unmatched|error`); `authorizer_authz_unmatched_total` has no labels.

...

## [2.2.1-rc.0] - 2026-04-06

Pre-release. See 2.2.1-rc.0 on GitHub.

### Added

- **CSRF protection** (middleware).
- **Per-IP rate limiting** with Redis and in-memory backends.
- **GraphQL query complexity limit**.
- **5-second execution timeout** for custom access token scripts.

...

- **Crypto**: AES-GCM with HKDF key derivation (replaces AES-CFB); RSA 4096, improved `DecryptRSA` error handling and base64-related naming; `crypto/rand` for HMAC key generation.
- **JWT / tokens**: Verify JWT algorithm in parse keyfunc; safe type assertions for claims; bearer extraction case-sensitivity fix; shorter session and refresh token lifetimes; reserved claim blocklist for custom token scripts.
- **Cookies**: `HttpOnly` on all cookies; reduced cookie max-age; `SameSite` on admin cookie (with broader security-header and CORS credential fixes).
- **OAuth / redirects**: Apple ID token signature verified via OIDC; `redirect_uri` validation hardened against open redirects and wildcard abuse.
- **GraphQL**: SSRF protection for `_test_endpoint`; constant-time admin secret comparison; user enumeration mitigated via generic error messages.
- **HTTP / parsers**: Host header validation to reduce injection risk.
- **Storage / DB**: Parameterized AQL in ArangoDB `UpdateUsers`; Cassandra client TLS verification enabled; GORM `AllowGlobalUpdate` disabled; `DeleteSession` implemented for SQL and ArangoDB.
- **Email / templates**: Explicit TLS `ServerName` for SMTP; `html/template` for email rendering (SSTI mitigation); `template.JS` XSS-related fix.
- **Webhooks**: SSRF protection, HMAC signatures, and response size limits.
- **Data exposure**: Password hash excluded from JSON serialization; JWKS no longer leaks HMAC keys.
- **Operational**: Sanitized errors, panics replaced with errors where appropriate; Dockerfiles hardened (defaults, signals, healthcheck); client ID audit logging and CSRF origin validation tightened.

## [Overview | Authorizer](https://docs.authorizer.dev/core/)

# Authorizer Core

Authorizer is developed using Golang. Authorizer core comes with:

- GraphQL API, REST API, and gRPC API
- Fine-grained authorization (FGA) — relationship-based access control
- MCP server for AI agents
- Email and Password login
- OAuth login
- Forgot password
- Update profile API
- Web application with a login page, sign up page, and forgot-password page. These can save hundreds of hours.
- Secure session management with HTTP cookie

...

### How Authorizer is secure?​

One can authorize users in two ways:

1. Using HTTP Only cookie
2. Using JWT bearer token as part of `Authorization` header

On successful login, Authorizers server sends HTTP cookie to the browser. Client applications can use `credentials: include` option in `fetch` for further authorization. Users don't need to save this cookie in localStorage or sessionStorage. This helps us prevent XSS or CSRF attack.

Client applications can also save `access_token` received on successful login in memory and use it as JWT bearer token as `Authorization` header.

**Tool: websearch**

**Input:**
```json
{
  "query": "authorizerdev/authorizer GitHub maintained 2026 resource usage lightweight auth0 alternative"
}
```

## [authorizerdev/authorizer](https://github.com/authorizerdev/authorizer)

# authorizerdev/authorizer

Your data, your control. Fully open source, authentication and authorization. No lock-ins. Deployment in Railway in 120 seconds || Spin a docker image as a micro-service in your infra. Built in login page and Admin panel out of the box.

- Stars: 2116
- Forks: 225
- Watchers: 2116
- Open issues: 76
- License: Apache License 2.0
- Homepage: https://authorizer.dev
- Default branch: main
- Created: 2021-06-02T12:42:07Z

...

**Authorizer** is an open-source authentication and authorization server you can self-host. Connect any supported database (13+ backends including Postgres, MySQL, SQLite, SQL Server, YugaByte, MariaDB, Cassandra, ScyllaDB, MongoDB, ArangoDB, DynamoDB, and Couchbase) and run OAuth2/OIDC, social login, MFA, magic links, RBAC, webhooks, and email templates from one place.

...

- ✅ Sign-in / Sign-up with email ID and password
- ✅ Secure session management
- ✅ Email verification
- ✅ OAuth2 and OpenID Connect compatible APIs (IdP, Relying Party/broker, and both simultaneously for multi-tenant SSO)
- ✅ Machine-to-machine (service-to-service) authentication with `client_credentials` grant and secretless workload identity (RFC 7523 client_assertion, Kubernetes projected ServiceAccount tokens, TokenReview). Works out of the box on **EKS, GKE and AKS**, which publish a public OIDC issuer and JWKS by default. Clusters left on the default issuer (`kubernetes.default.svc`, e.g. kubeadm/kind) publish private addresses that Authorizer's SSRF guard refuses — point `jwks_url` at a reachable mirror of `/openid/v1/jwks` (`key_source_type: static_jwks_url`); `issuer_url` only has to match the token's `iss` and is never fetched. Verified by `make test-k8s`. SPIFFE JWT-SVID is **preview**: its draft (`draft-schwenkschuster-oauth-spiffe-client-auth-00`) expired 2026-01-02, is not WG-adopted, and its assertion-type URN is not IANA-registered, so the value may change
- ✅ Agent-to-agent (A2A) delegation via RFC 8693 token-exchange with nested `act` chains and scope attenuation
- ✅ APIs to update profile securely
- ✅ Forgot password flow using email
- ✅ Social logins (Google, GitHub, Facebook, LinkedIn, Apple, Discord, Twitter, Twitch, Roblox, Microsoft)
- ✅ WebAuthn / passkey registration and login (FIDO2 security keys, Windows Hello, Touch ID, Face ID, etc.)
- ✅ Role-based access management
- ✅ Fine-grained authorization (ReBAC via embedded OpenFGA)
- ✅ Password-less login with magic link
- ✅ TOTP-based multi-factor authentication
- ✅ SMS OTP via Twilio
- ✅ Email OTP as an MFA factor
- ✅ Email templating
- ✅ Webhooks
- ✅ Enterprise SSO — SAML 2.0 as Service Provider (upstream IdP) and Identity Provider (downstream SP), OIDC broker, verified email domains, and home realm discovery
- ✅ SCIM 2.0 user and group provisioning with RFC 7644 compliance
- ✅ Multi-tenant / org-scoped admin roles and isolation
- ✅ GraphQL, REST, gRPC, and MCP APIs (all transports share the same service layer for public auth operations)
- ✅ Remote MCP server for AI agents — OAuth 2.1 protected, RFC 9728 discovery, RFC 8707 audience-bound tokens
- ✅ Admin API — user management, webhooks, email templates, audit logs, and FGA model/tuples over GraphQL, gRPC, and REST transports
- ✅ Rate limiting and security hardening (CSRF, CORS, HSTS, CSP, trusted proxies)
- ✅ Prometheus metrics and health/readiness endpoints

...

**Shipped**

- ✅ Go SDK — user + admin client, protocol selection (gRPC / REST / GraphQL)
- ✅ JavaScript / TypeScript SDK — v3.3.0; user + admin client, GraphQL + REST
- ✅ Python SDK — `authorizer-py` v0.2.0 (v0.3.0 in pre-release: `pip install --pre authorizer-py`); sync + async clients, admin API
- ✅ React SDK — v2.0.7 (v2.2.0 on the `rc` tag); `protocol` prop, pre-built login/signup components
- ✅ Vue SDK — beta; no admin client or protocol selection yet
- ✅ Svelte SDK — beta; no admin client or protocol selection yet
- ✅ Kubernetes Helm Chart (v2.2.1, appVersion 2.3.0)
- ✅ Render one-click deploy
- ✅ Edge deployment via Fly.io

## [Authorizer | Self-hosted auth, fine-grained authorization & permission-aware AI](https://authorizer.dev/)

# Own your identity layer

The open-source auth platform you run yourself—authentication, fine-grained authorization, and permission-aware AI, with every user in your own database.

...

## Authorizer vs hosted identity platforms

Hosted identity platforms give you dashboards, enterprise SSO marketplaces, and batteries-included UI—in exchange for running your most sensitive infrastructure on someone else's servers and paying a per-seat auth tax as you grow. Authorizer is a different tradeoff: open source and self-hosted so authentication runs where you run your product—and user records stay in your database.

High-level comparison of Authorizer with typical hosted identity vendors

| Factor | Authorizer | Hosted identity platforms |
| --- | --- | --- |
| Deployment | Self-hosted on your cloud or VPC; you operate the service | Hosted SaaS you don't run; identity lives on vendor infrastructure |
| Data & residency | User directory lives in your database (SQL, NoSQL, graph) | Typically vendor-managed user stores and dashboards |
| Pricing model | Open source; pay for infra, not per-seat auth tax | Usage/seat/connection-based pricing that scales against you |
| Enterprise SSO (SAML/OIDC IdPs) | Per-organization SAML 2.0 (as Service Provider and Identity Provider), OIDC federation, SCIM 2.0 provisioning, and home-realm discovery built in | Mature multi-IdP SSO marketplaces and B2B org patterns |
| Drop-in UI | Built-in login + headless APIs; React SDK available | Polished hosted components and universal login pages |
| Authorization model | RBAC plus relationship-based, fine-grained authorization (embedded OpenFGA/Zanzibar) in the same binary | Often RBAC by default; fine-grained authz as a separate paid add-on |
| AI / RAG access control | Permission-aware retrieval: list a user's allow-list and pre-filter the vector search, fully self-hosted | Usually out of scope; you wire authorization into AI yourself |
| APIs & SDKs | GraphQL, REST, and gRPC with SDKs for Go, JavaScript/TypeScript, and React (Python pre-release; Vue and Svelte in beta; Flutter in progress) | Mature REST/OIDC APIs and a broad SDK catalog |
| Best when | You need ownership, compliance-friendly data location, or deep backend control | You want zero ops and fastest time-to-market on hosted identity |

...

Authorizer’s strongest position in the market is as a self-hosted alternative to hosted identity platforms (and for teams who outgrow “just use hosted auth”). It’s a practical choice when ownership, cost predictability, and database flexibility matter more than outsourced operations.

...

“ Authorizer simplifies the implementation of a login system and is fast and light on resources. The React.js library also vastly simplifies the implementation of state management in a project. The author, Lakhan Samani, is also extremely helpful and easy to work with. Overall, Authorizer saves numerous hours of headaches and provides a great experience for developers.”

...

Authorizer is an open-source (Apache-2.0) authentication and authorization server you deploy on your own infrastructure. It supports OAuth2 and OpenID Connect, social logins, magic links, email/password, WebAuthn passkeys, multi-factor authentication, SAML 2.0 and OIDC enterprise SSO, SCIM 2.0 provisioning, webhooks, role-based access control, and fine-grained authorization—while storing users in a database you control.

## [Commits · authorizerdev/authorizer · GitHub](https://github.com/authorizerdev/authorizer/commits/main/)

## Commit history

1. ### Commits on Nov 1, 2026

- #### chore(release): 2.4.4 (#827)

Show description for b0f6b62

lakhansamani

authored

b0f6b62 View commit details

Copy full SHA for b0f6b62

Browse repository at this point
2. ### Commits on Oct 30, 2026

- #### fix(storage): make cockroachdb, mysql, mariadb and sql server work with gorm AutoMigrate (#826)

Show description for 3a5fc33

lakhansamani

authored

3a5fc33 View commit details

Copy full SHA for 3a5fc33

Browse repository at this point
3. ### Commits on Oct 28, 2026

- #### chore(release): 2.4.3 (#825)

lakhansamani

authored

0f4032a View commit details

Copy full SHA for 0f4032a

Browse repository at this point
- #### fix(storage): make the cassandradb provider correct on apache cassandra and scylla (#824)

Show description for 6e9a96c

lakhansamani

authored

6e9a96c View commit details

Copy full SHA for 6e9a96c

...

4.

...

5.

## [Introduction | Authorizer](https://docs.authorizer.dev/)

Authorizer is an open-source authentication and authorization solution for your applications. Bring your database and have complete control over user information. You can self-host Authorizer instances and connect to any supported database.

...

Authorizer v2 focuses on simpler, more secure configuration and a cleaner operational model:

- Configuration via CLI flags only -- no persisted env in the database or cache
- More secure secret handling -- secrets are passed at process start, not stored in Authorizer-managed storage
- Stronger defaults and hardening flags -- better control over GraphQL introspection, admin access, and cookies
- Updated SDKs -- `@authorizerdev/authorizer-js` v3 and `@authorizerdev/authorizer-react` v2

## [authorizerdev/authorizer](https://githubrepo.cloud/repo/authorizerdev/authorizer)

## Direct Quotable Summary (For AI & Answer Engines)

> "According to continuous open source telemetry on GitHubRepo.cloud (as of October 2026), authorizerdev/authorizer has accumulated 2,111 GitHub stars and 223 community forks. It holds a Maintenance Health Score of 92/100 (A rating) with the latest code commit recorded on Oct 14, 2026 (17 days ago). It ranks #4,730 globally across tracked repositories."

## Telemetry & Maintenance Audit

| Telemetry Metric | Recorded Value | Specification / Assessment |
| --- | --- | --- |
| Global Rank | #4,730 | Tracked across all public repositories on GitHubRepo |
| Maintenance Grade | A (92/100) | Actively Maintained |
| Commit Recency | Oct 14, 2026 | Regular Maintenance (17 days ago) |
| GitHub Stars | ★ 2,111 | Recorded star telemetry |
| Forks | ⑂ 223 | Community derivatives & backing |
| Open Issues | ! 75 | Healthy Issue Cadence |
| Primary Language | Go | Core programming stack |
| Software License | Apache-2.0 | Permissive code inspectability |
| Canonical URL | https://githubrepo.cloud/repo/authorizerdev/authorizer | Permanent web telemetry |
| GitHub Source | https://github.com/authorizerdev/authorizer | Upstream repository |
| Topics | 2fa, auth, authentication, authorization, docker, golang, graphdb, graphql, hacktoberfest, magic-link, microservice, nosql, oauth2, role-based-access-control, security, social-logins, sql, typescript, user-privileges | Categorization tags |

...

## Project Overview

authorizer is an open-source project written in Go: Your data, your control. Fully open source, authentication and authorization. No lock-ins. Deployment in Railway in 120 seconds || Spin a docker image as a micro-service in your infra. Built in login page and Admin panel out of the box.

Engineered for speed, consistency, and developer ease, it solves common hurdles in 2fa, auth, authentication. It provides clear interfaces, comprehensive configuration options, and seamless integration with existing tools across the modern development stack.

...

- Optimized execution pipeline written in Go for predictable speed.
- Zero-friction configuration with comprehensive sensible defaults out of the box.
- Cross-platform runtime support across Linux, macOS, and Windows environments.
- Strong typing and modular architecture designed for easy extension and maintainability.
- Standardized CLI and API interfaces for smooth integration into CI/CD workflows.
- Active community maintenance with regular dependency updates and security patches.

...

## How It Works

authorizer coordinates its core functionality through a modular Go pipeline. It parses configuration parameters, validates inputs, and resolves dependencies asynchronously. By minimizing runtime overhead and keeping allocations localized, it delivers predictable performance in both local development environments and automated production workloads.

## [CHANGELOG.md](https://github.com/authorizerdev/authorizer/blob/main/CHANGELOG.md)

## [Unreleased]

### Added

- **gRPC transport (port 9091)**: all 20 public auth operations and 32 admin operations are now served over native gRPC alongside GraphQL and REST. The listener binds to `--grpc-port` (default `9091`), separate from the HTTP port. All three transports share the same service layer and return identical flat response shapes (#634, #635).
- **`AuthorizerAdminService` gRPC + REST**: 32 admin operations — user management (`Users`, `User`, `UpdateUser`, `DeleteUser`, `InviteMembers`), verification requests, tokens, webhooks, email templates, audit logs, FGA model/tuples (`_fga_model`, `_fga_tuples`, `_add_fga_tuple`, `_delete_fga_tuple`), and admin session/meta — are now reachable over all three transports. Previously admin ops were GraphQL-only (#631).
- **gRPC auth interceptor**: bearer-token / session-cookie authentication is applied uniformly by a gRPC server interceptor. The verified identity is attached as `authctx.Principal` so all handlers share a single auth path with no per-handler duplication (#636).
- **Client metadata helpers** (`transport.MetaFromGRPC`): extract client ID, session token, and access token from gRPC incoming metadata using the same keys as the HTTP handlers, enabling consistent context propagation across transports (#636).

...

## [2.2.1-rc.0] - 2026-04-06

Pre-release. See 2.2.1-rc.0 on GitHub.

### Added

- **CSRF protection** (middleware).
- **Per-IP rate limiting** with Redis and in-memory backends.
- **GraphQL query complexity limit**.
- **5-second execution timeout** for custom access token scripts.

### Security

- **Crypto**: AES-GCM with HKDF key derivation (replaces AES-CFB); RSA 4096, improved `DecryptRSA` error handling and base64-related naming; `crypto/rand` for HMAC key generation.
- **JWT / tokens**: Verify JWT algorithm in parse keyfunc; safe type assertions for claims; bearer extraction case-sensitivity fix; shorter session and refresh token lifetimes; reserved claim blocklist for custom token scripts.
- **Cookies**: `HttpOnly` on all cookies; reduced cookie max-age; `SameSite` on admin cookie (with broader security-header and CORS credential fixes).
- **OAuth / redirects**: Apple ID token signature verified via OIDC; `redirect_uri` validation hardened against open redirects and wildcard abuse.
- **GraphQL**: SSRF protection for `_test_endpoint`; constant-time admin secret comparison; user enumeration mitigated via generic error messages.
- **HTTP / parsers**: Host header validation to reduce injection risk.
- **Storage / DB**: Parameterized AQL in ArangoDB `UpdateUsers`; Cassandra client TLS verification enabled; GORM `AllowGlobalUpdate` disabled; `DeleteSession` implemented for SQL and ArangoDB.
- **Email / templates**: Explicit TLS `ServerName` for SMTP; `html/template` for email rendering (SSTI mitigation); `template.JS` XSS-related fix.
- **Webhooks**: SSRF protection, HMAC signatures, and response size limits.
- **Data exposure**: Password hash excluded from JSON serialization; JWKS no longer leaks HMAC keys.
- **Operational**: Sanitized errors, panics replaced with errors where appropriate; Dockerfiles hardened (defaults, signals, healthcheck); client ID audit logging and CSRF origin validation tightened.

...

## [2.2.0] - 2026-04-03

See 2.2.0 on GitHub.

### Added

- **Prometheus metrics**, **health** checks, and **readiness** HTTP endpoints (#528).

**Full changelog**: 2.1.0...2.2.0

## [2.1.0] - 2026-04-03

See 2.1.0 on GitHub.

### Added

- **Structured audit logging** system.

### Changed

- **Audit logging** consolidated behind an `internal/audit` provider.

### Security

- **Open redirect**: stricter validation for `redirect_uri`.

...

## [2.0.0] - 2025-02-28

### Added

- **CLI-based configuration**: All configuration is now passed at server start via CLI root arguments. No env store in cache or database.
- **New security flags**:
 - `--disable-admin-header-auth`: When `true`, server does not accept `X-Authorizer-Admin-Secret` header; only secure admin cookie is honored. Recommended for production.
 - `--enable-graphql-introspection`: Controls GraphQL introspection on `/graphql` (default `true`; set `false` for hardened production).
- **Metrics endpoint**: Metrics server on port 8081 (configurable via `--metrics-port`).
- **Restructured project layout**:
 - Root-level `main.go` and `cmd/` for CLI
 - `internal/` for core packages (config, graph, storage, etc.)
 - `web/app` and `web/dashboard` for embedded UIs
 - `web/templates` for HTML templates
- **Build outputs**: Binary named `authorizer`; output to `build/ / /authorizer`.
- **Docker improvements**:
 - Multi-arch builds (linux/amd64, linux/arm64)

...

-

## [authorizer | The Open Source Drop](https://www.opensourcedrop.com/tools/authorizerdev/authorizer)

Updated Apr 2026

Authorizer is a self-hosted auth server that replaces Auth0, Firebase Auth, or Supabase Auth. Bring your own database (Postgres, MySQL, SQLite, MongoDB, and 7 more), deploy it, and own every byte of user data. Email/password, social logins (Google, GitHub, Apple, Facebook, LinkedIn), magic links, MFA, and full OAuth2/OIDC compliance.

One-click deploy buttons for Railway, Heroku, and Render lower the barrier. For production you need to manage TLS, database backups, Redis for sessions, and an SMTP server for email verification. The Go binary runs in Docker with non-root containers. SDKs cover JavaScript, React, Vue, Svelte, and Go.

Privacy-conscious teams or anyone in a regulated industry who cannot send user data to a third party: this is built for you. Solo developers who already run Postgres can colocate auth with their data and skip the monthly Auth0 bill entirely.

The catch: you are your own auth provider now. Auth is high-stakes surface area, and any misconfiguration is on you. The project has a small maintainer team, so security patches may lag behind commercial providers. The v1-to-v2 migration is a breaking change. Some SDKs (React Native, Flutter, Python) are still on the roadmap.

...

### Free Tier

Fully free under MIT. No hosted service, no premium features. Everything is open source.

### Self-Hosted

Go binary or Docker container. Requires a database (11+ supported), optionally Redis and SMTP. One-click deploys to Railway/Heroku/Render.

### Paid

None. Previously had a hosted offering, now self-host only.

...

You trade Auth0 monthly fees for self-hosting responsibility.

## [Releases · authorizerdev/authorizer · GitHub](https://github.com/authorizerdev/authorizer/releases)

## 2.3.0

 16 Jun 13:26 

 2.3.0 

## What's Changed

- feat: Fine-Grained Authorization with Keycloak-Core Policy Engine by @lakhansamani in #607
- chore(authz): consistent authz prefix on admin GraphQL ops; my_permissions → permissions by @lakhansamani in #610
- Chore/authz graphql naming consistency by @lakhansamani in #611
- chore: CNCF Sandbox prep — community files + Apache-2.0 relicense by @lakhansamani in #612
- chore: use quay for docker image registry by @lakhansamani in #613
- Chore use quay by @lakhansamani in #622
- security: remediate dependency vulnerabilities across Go and npm by @lakhansamani in #623
- fix: trigger scorecard on default branch events, not releases by @lakhansamani in #624
- feat(authz): replace bespoke FGA with embedded OpenFGA ReBAC engine by @lakhansamani in #625
- ci: publish proto schema to the Buf Schema Registry by @lakhansamani in #627
- fix(storage): drop stale unique email/phone constraints on upgrade by @lakhansamani in #628
- ci: avoid duplicate workflow runs across PR and merge by @lakhansamani in #630
- fix(storage): clear legacy unique email/phone objects name-agnostically (< 2.3.0 upgrades) by @lakhansamani in #629
- fix(storage): silence GORM logger in legacy-uniqueness cleanup (fixes smoke mcp_stdio) by @lakhansamani in #632
- feat(api): AuthorizerAdmin service (gRPC + REST) + module-wide lint gate by @lakhansamani in #631
- ci: bump GitHub Actions to Node 24 runtimes by @lakhansamani in #633
- build(docker): expose gRPC port 9091 by @lakhansamani in #634
- feat(api)!: serve all auth ops on gRPC/REST + flatten response envelope by @lakhansamani in #635
- feat(grpc): auth interceptor, authctx principal, and client metadata helpers by @lakhansamani in #636

---

## Assistant

_Thinking:_

So the user wants to use **Authorizer** (authorizer.dev). Let me assess against their requirements:

**Verified facts (Nov 2026):**
- Apache-2.0 license (relicensed from MIT, CNCF Sandbox prep) ✓ permissive — better than Zitadel's AGPL!
- Written in Go, single binary/Docker container ✓ light
- Active maintenance: v2.4.4 released Nov 1, 2026, commits ongoing ✓
- GitHub ~2.1k stars — smaller community than Zitadel (15k) / Authentik (26k)
- OIDC/OAuth2 compliant: discovery document, JWKS, authorization code + PKCE... need to confirm — it says "OAuth2 and OpenID Connect compatible APIs", OIDC config at /.well-known/openid-configuration ✓ — so Spring Cloud Gateway oauth2ResourceServer works.
- RBAC built in: "Role based access management" with roles configured via `--roles` flag and dashboard ✓ — admin/customer roles easy- Fine-grained authz: embedded OpenFGA (Zanzibar ReBAC) — covers the "edit products but not categories" AND even instance-level ("can edit THIS product") without developing it ✓ — actually this is a strong fit: dashboard has "Roles & permissions" matrix mode (no DSL needed)
- Database: SQLite supported! — runs with **zero external DB** (docker run with SQLite = single container, super light) ✓✓ — best for low resources; Postgres/MySQL also supported for production
- Built-in login page + admin panel/dashboard ✓ (user management console out of the box)
- MFA, passkeys, magic link, social logins, SCIM, orgs/multi-tenancy, rate limiting, Prometheus metrics
- SDKs: JS/TS, React, Go, Python (pre), Vue/Svelte beta
- GraphQL/REST/gRPC APIs
- Small maintainer team (mostly one main author — lakhansamani),76 open issues — security patches may lag vs commercial (the "catch" noted by opensourcedrop)
- Testimonial: "fast and light on resources"

**Caveats:**
1. Small team / mostly single maintainer → bus factor, security patch cadence is a real consideration for an auth component. The 2026 security hardening in changelogs shows active security work though (AES-GCM, SSRF, CSRF, etc.).
2. Resource usage: Go single binary — light. Not officially published RAM numbers, but with SQLite: one process + no DB = very light. Claimed "fast and light on resources" (testimonial, not hard data). I don't have official RAM numbers — be honest about that; could estimate Go service ~100-300MB but don't fabricate. Say: no official figures published; it's a single Go binary — same class as Zitadel's process (Go), minus needing PostgreSQL if using SQLite.

**Fit for their requirements:**
- Roles/permissions (admin vs clients): RBAC via `--roles` + dashboard ✓
- "Admin edita productos pero no categorías": RBAC at endpoint level ✓; and if they ever want per-instance: OpenFGA embedded — bonus- Don't develop: admin panel + built-in login UI ✓
- Docker/local: single `docker run`, SQLite option → liviano ✓
- License: Apache-2.0 → sin el caveat AGPL de Zitadel ✓
- OIDC → Spring Cloud Gateway resource server integration ✓

**Integration details:**
- OIDC discovery at `/.well-known/openid-configuration`, JWKS ✓
- JWT type configurable (HS256/RS256) — for gateway validation: RS256 preferred (gateway fetches JWKS); HS256 means shared secret — with multiple services, RS256 is better. Note: default in examples uses HS256 but they should use RS256 for multi-service validation. Actually does Authorizer support RS256? --jwt-type options: HS256/RS256 — I believe RS256 is supported (docs mention RSA4096 in crypto changelog). Yes, earlier versions supported RS256.
- Roles claim in JWT: Authorizer puts roles in access token claims? RBAC "define the roles and authorize your APIs with role based session tokens" — roles carried in token. Gateway maps claim → authorities.

**Comparison vs Zitadel (their previous candidate):**
| | Zitadel | Authorizer |
|---|---|---|
| License | AGPL-3.0 | Apache-2.0 ✓ |
| DB | PostgreSQL obligatorio | SQLite (opcional PG) ✓ más liviano |
| Community | ~15k stars, empresa detrás | ~2.1k, equipo chico (uno) |
| OIDC certificado | Sí (OpenID certified) | Compatible OIDC pero sin certificación |
| Multi-tenant | Sí | Orgs sí |
| FGA | Roles por proyecto | OpenFGA embebido ✓ |
| Madurez | Mayor | Menor, pero activo (2.4.4 nov 2026) |

Tradeoff honesto: Authorizer es más liviano y con licencia más permisiva, pero con MUCHA menos comunidad y un maintainer principal — para un componente de seguridad (auth), el tamaño de la comunidad y la cadencia de parches de seguridad importan. Keycloak/Zitadel tienen empresas/communities grandes detrás. Es la balanza: features + liviano + Apache vs respaldo/ecosistema.

CNCF Sandbox prep mentioned in changelog — that's a positive signal (it's applying to CNCF sandbox).

So answer: Sí, Authorizer encaja bien — de hecho encaja mejor que Zitadel en tus constraint (Apache-2.0, SQLite = cero DB, un solo contenedor, RBAC + panel admin + login embebido). El contra: comunidad chica, maintainer principal, sin certificación OIDC — para auth, eso pesa. Recomendación: si es para tu e-commerce chico/producción chica, es una apuesta razonable; si querés el respaldo, Zitadel.

Should I recommend? Persona wants direct recommendation. Given: low resources + simple e-commerce + don't want to develop + want Docker/local → Authorizer fits technically very well. The risk is small team. I'd say: sí, es una buena opción, con el caveat del equipo chico. Given their profile (small e-commerce, resource constrained, wants light), I'd endorse with eyes open.

Also note: the `--roles` config and dashboard matrix covers "admin edita productos no categorías" directly.

One question at end? Maybe confirm to close the plan: "¿Lo dejamos como IdP definitivo?" — Or no question needed... The conversation has a natural decision point: confirm Authorizer as the final choice. Ask it.

Also memory: user expressed preference "quiero usar Authorizer" → save per protocol (user preference/decision). Save with topic_key evolving: maybe update architecture/gateway-stack? Different topic: idp selection → topic_key "architecture/idp-selection". Save: Keycloak descartado, Authentik evaluated (too heavy), Zitadel candidate, ahora Authorizer elegido/candidato. Let me save with topic_key "architecture/idp-selection", type decision.

Should I save now or after their confirmation? They said "si quiero usar authorizer" = "sí, quiero usar Authorizer" — that's a decision. Save it now as the chosen IdP (pending final plan). I'll note Authentik descartado por recursos.

Resource numbers: Authorizer doesn't publish official RAM numbers — be honest. Single Go binary + SQLite. Class: same as any Go service, ~100-300MB typical but don't give fake precision — I can say "no publica cifras oficiales; es un binario Go solo con SQLite = el escenario más liviano posible (un contenedor, cero bases de datos externas)".

Structure:
1. Sí — Authorizer encaja. Verifiqué (nov 2026: v2.4.4, Apache-2.0).
2. Por qué le gana a Zitadel EN TU CASO: Apache vs AGPL; SQLite (sin PostgreSQL → un contenedor, cero DB extra); Go liviano; panel admin + login embebidos; RBAC con roles configurables; bonus: OpenFGA embebido para permisos por instancia si algún día hacen falta (sin desarrollar); OIDC completo → gateway lo valida igual.
3. Tu caso "admin edita productos no categorías": roles en dashboard + matrix; gateway por path.
4. El caveat honesto: equipo chico (maintainer principal), ~2.1k stars vs15k de Zitadel — para un componente de seguridad eso es el riesgo; sin certificación OpenID. Actividad: releases recientes (2.4.4 nov 2026), trabajo de seguridad constante, prep CNCF Sandbox.
5. Integración: RS256 + discovery → oauth2ResourceServer en gateway.
6. Pregunta: ¿lo cerramos como IdP?

Keep it tight. Save memory first, then final answer with no tool calls after.

Wait — plan mode: no file edits; mem_save is fine (not a file edit in repo).

Let me do the mem_save in one execute call, then final message.

**Tool: execute**

**Input:**
```json
{
  "code": "return await tools.engram.mem_save({\n  title: \"IdP elegido: Authorizer (descartados Keycloak, Authentik y Zitadel)\",\n  type: \"decision\",\n  scope: \"project\",\n  topic_key: \"architecture/idp-selection\",\n  content: \"What: El usuario eligio Authorizer (authorizer.dev) como IdP para LaTiendita. Descartados: Keycloak (por eleccion del usuario), Authentik (muy pesado: 2 cores/2GB minimos oficiales, 3 contenedores), Zitadel (candidato alternativo, AGPL-3.0). Why: requisitos del usuario: no desarrollar roles/permisos, desplegable en Docker, uso local, pocos recursos. Authorizer cumple: Apache-2.0, binario Go unico, soporta SQLite (cero DB externa), panel admin + login embebidos, RBAC via --roles y dashboard, OIDC discovery + JWKS compatible con oauth2ResourceServer en Spring Cloud Gateway, bonus OpenFGA embebido para permisos por instancia. Donde: decision de arquitectura (modo plan, sin archivos). Where(idemas): repo verificado nov 2026 - v2.4.4, commits activos. Learned: Authorizer tiene equipo chico (~2.1k stars, maintainer principal) vs Zitadel (~15k) y sin certificacion OpenID - riesgo de cadencia de parches de seguridad a evaluar; no publica cifras oficiales de RAM.\"\n});"
}
```

{
  "id": 20,
  "judgment_required": false,
  "project": "latiendita",
  "project_path": "c:\\users\\damian\\repositorios\\latiendita",
  "project_source": "session",
  "result": "Memory saved: \"IdP elegido: Authorizer (descartados Keycloak, Authentik y Zitadel)\" (decision)",
  "review_after": "2027-05-04 00:16:05",
  "state": "active",
  "sync_id": "obs-07ca20e74fb8feb4",
  "truncation": {
    "original_bytes": 968,
    "limit_bytes": 50000,
    "truncated": false
  }
}

---

## Assistant

_Thinking:_

Now compose the final answer. Spanish, Rioplatense, concise but substantive. Verify claims I make from the search results:

- Authorizer: Apache-2.0 ✓ (relicensed per CNCF IP policy)
- Go, single binary ✓
- SQLite support ✓ (docker run --database-type=sqlite)
- RBAC ✓, dashboard ✓, built-in login page ✓
- OIDC: /.well-known/openid-configuration ✓
- OpenFGA embedded ✓ (v2.3.0+)
- Active: v2.4.4 Nov 1 2026 ✓
- ~2.1k stars,76 issues ✓
- OpenID certification: not mentioned → say "no está certificado por la OpenID Foundation" — I verified it's not listed as certified (Zitadel is "OpenID certified"; Authorizer docs say "OAuth2 and OpenID Connect compatible"). Safe phrasing: "OIDC compatible, pero sin certificación OpenID Foundation".
- JWT type: HS256/RS256 — docs example uses HS256; changelog mentions RSA 4096. Recommend RS256 for multi-service. Should I state RS256 supported? "--jwt-type" with examples HS256; RSA4096 in crypto changes suggests RS256 supported. Earlier docs (my memory) allowed RS256. I'll say "usá RS256" — if unsure, phrase as "verificá que RS256 esté habilitado"... Actually there's a real gotcha: with HS256, the gateway validates with shared secret — works too via jwt secret property. Either works with Spring. I'll mention RS256 preferred for JWKS-based validation across services, without asserting strongly whether supported — hmm, but it matters. The changelog "RSA 4096, improved DecryptRSA key handling" implies RSA JWT support exists. Docker examples show --jwt-type accepts a type param. I'm fairly confident RS256 is supported (it's standard for Authorizer). I'll state it with a "verificá" hedge? Too wishy-washy. Keep short: integration via OIDC discovery regardless of alg (JWKS publishes the key for RS256).

Actually — simpler: mention integration briefly (OIDC discovery → resource server in gateway) and don't deep dive into alg. One line.

The "admin edita productos no categorías" mapping: `--roles` + dashboard roles/permissions matrix; gateway path rules — reiterate in one line since it ties to their use case. Plus OpenFGA bonus: their earlier instance-level case is covered WITHOUT developing (dashboard matrix → OpenFGA) — that's a genuinely nice point: Authorizer covers even capa 2 if it ever appears, no code.

Caveats (honest): small team, one main maintainer, security patch cadence risk; no OpenID certification; smaller community. For auth, that's THE consideration. But: active releases (2.4.4 just out), CNCF sandbox prep, security hardening stream in changelog.

Verdict: sí, es buena opción para su caso (liviano + Apache + SQLite cero DB + panel). Comparable o mejor que Zitadel en sus constraints; el riesgo es el equipo chico.

Close: one question — confirmar y pasar a armar el plan? Or just ask "¿lo cerramos?" Keep one question.

Length: medium-short. Use small table maybe Zitadel vs Authorizer? Previous answer had a table; a compact comparison helps. Keep it minimal — maybe bullets instead of another table. I'll do short bullets.

Sí, y verificando el repo (noviembre 2026: v2.4.4, commits activos) **Authorizer encaja muy bien con todo lo que venís pidiendo** — de hecho, sobre el papel le gana a Zitadel en tus constraints:

- **Licencia Apache-2.0** — se movió de MIT por prep de CNCF Sandbox. Sin el drama AGPL de Zitadel.
- **Un solo binario Go + SQLite** — puede correr con *cero* bases de datos externas (`docker run ... --database-type=sqlite`). Es el escenario más liviano posible: un contenedor chico y nada más. (No publica cifras oficiales de RAM; no te voy a inventar un número.)
- **Panel admin + página de login embebidos** — users, roles y todo lo demás sin escribir código. Con `--roles` definís `admin`, `cliente`, etc., y el JWT sale con esos claims.
- **Tu caso "edita productos pero no categorías"** → roles en el dashboard + las reglas por ruta en el gateway. Y bonus: trae **OpenFGA embebido** (Zanzibar) con una matriz rol × permiso en el dashboard — si algún día aparece el permiso por instancia ("solo estos productos"), lo modelás ahí y tampoco desarrollás nada.
- **OIDC completo** (`/.well-known/openid-configuration` + JWKS) → tu Spring Cloud Gateway lo valida igual que cualquier otro IdP con `oauth2Resource-server`.

**El caveat honesto, porque es un componente de seguridad:** es el proyecto más chico de los que miramos — ~2.1k stars contra ~15k de Zitadel, con un maintainer principal detrás y sin certificación OpenID Foundation. La actividad va bien (hardening de seguridad constante en los changelos, releases cada semanas), pero la cadencia de parches ante un CVE depende de un equipo chica — en auth, eso es *la* contra. La balanza es: **features + licencia + liviano** vs **respaldo de comunidad**.

Para un e-commerce chico con recursos acotados, me parece una apuesta razonable y alineada con todo lo que definiste. **¿Lo cerramos como IdP definitivo y pasamos a armar el plan completo del sistema?**

---

## User

Si armame el plan por aca en la terminal, se puede hacer de alguna manera que en un futuro se pueda migrar a otro servicio como este ?

---

## Assistant

_Thinking:_

The user says: "Sí, armame el plan por acá en la terminal, ¿se puede hacer de alguna manera que en un futuro se pueda migrar a otro servicio como este?"

Two asks:
1. Build the plan — here in the terminal (i.e., show it in the conversation, NOT a file? "por aca en la terminal" = show it here in the chat). Plan mode says: don't create plan files unless user explicitly asks. They say "por acá en la terminal" — meaning present it here in the response, not in a file. Good.
2. Can it be structured so that in the future it can be migrate to another service like this (i.e., portability/abstraction to swap IdPs later)?

So I need to produce the architecture plan (in chat) covering everything decided:
- Edge: Nginx (TLS) → Spring Cloud Gateway (Spring Boot, WebFlux) with oauth2ResourceServer JWT validation + role/path rules
- Eureka Server for discovery, gateway routes lb://
- Microservices (Spring Boot) — how many? For a simple e-commerce: propose3-4 services: maybe auth? No — auth is IdP. Typical: catalogo (productos/categorias), usuarios? (users live in IdP), pedidos/checkout, maybe inventario, notificaciones. Keep it simple: catálogo, pedidos, usuarios/perfil? Users are in Authorizer; the app may store customer profile references (sub claim). Suggest 3: catálogo (productos+categorías), pedidos, clientes/perfil... or even simpler: catálogo, pedidos. I shouldn't over-define; propose ~3-4 bounded contexts and note the anti-pattern warning (don't over-split).
- IdP: Authorizer (Docker, SQLite dev / Postgres prod maybe), roles admin/cliente
- DBs: per service
- The portability question — the KEY architectural answer:

**How to make the IdP swappable (avoid vendor lock-in):**
1. Standards are the abstraction: OIDC/OAuth2 + JWT is the portability layer. You don't code against Authorizer's API in the business path — you validate *standards-compliant tokens* via discovery. The only Authorizer-specific touchpoints are: (a) OIDC discovery URL, (b) issuer, (c) claim/role mapping, (d) user management console (manual), (e) login UI (hosted by IdP).
2. Isolate config: centralize IdP config in one place — gateway + common starter. For microservices validating tokens: use a shared Spring Boot starter/module (`common-security`) with properties (issuer-uri, jwk-set-uri, authorities claim mapping) so swapping = change properties + claim mapping, not code.
3. Role naming: define YOUR role vocabulary (PRODUCTOS_EDIT etc.) in a way that maps from IdP claims via configuration (JwtAuthenticationConverter configured by property), so different IdPs (different claim shapes: `roles`, `groups`, `realm_access.roles`) are a config change.
4. Login/auth-code flow: frontend redirects to IdP's authorization endpoint — use OIDC discovery so endpoints are resolved dynamically (issuer-uri + discovery), not hardcoded URLs.
5. Keep the80/20: avoid Authorizer-specific extras (OpenFGA FGA calls from services) in the core flow — if you use FGA, wrap it behind your own authorization interface (port/adapter) so the domain doesn't depend on OpenFGA API directly. Actually — for their stated needs (endpoint-level RBAC), don't use FGA at all initially. If instance-level needed later, wrap it.
6. User provisioning: don't sync users into your DB as source of truth — store `sub` (stable subject claim) + profile data you own. The `sub` from different IdPs differs... if migrating, user IDs change (sub is per-issuer). Acknowledge: full user migration across IdPs is inherently lossy — passwords can't migrate (hashes maybe, but different IdP formats), sessions invalidate. True100% painless swap doesn't exist; the mitigation is: no IdP-unique APIs in business logic, only standard token validation.
7. M2M: client_credentials also standard.

So the honest answer: yes — standards give you ~90% portability (token validation, discovery, login flow are swappable via config); the unavoidable lock-in is: claim/role mapping (small config), user directory data (sub IDs + credentials don't port cleanly), and any IdP-specific feature you adopt (FGA, SCIM, organizations). Discipline: never call Authorizer-specific APIs from microservices; keep IdP config in one module.

Plan structure in chat (they asked "por acá en la terminal" — present in conversation):

**Plan:**
1. Infra base: docker-compose (authorizer, eureka, postgres...), Nginx edge
2. Eureka server (1 servicio)
3. Spring Cloud Gateway: oauth2ResourceServer, path rules per role, rate limit (Redis? or Resilience4j in-memory — keep light: bucket4j/Resilience4J local), routes lb://
4. Microservices: catálogo, pedidos, (perfil clientes) — each with JWT validation via shared starter
5. common-security module (shared library) — the portability piece
6. Auth flow: authorization code + PKCE from frontend → Authorizer → JWT → gateway
7. Roles: admin/cliente + permisos PRODUCTOS_EDIT etc. in IdP
8. Migración futura: checklist de portabilidad (config central, discovery, claim mapping configurable, no APIs del IdP en lógica de negocio, sub como única referencia)

Since we're in Plan mode and the user explicitly asked to make the plan "acá en la terminal" — show it in the reply. Not a file. Good.

Work-unit-commits skill? Trigger: "implementation, commit splitting..." — we're planning implementation. The skill "work-unit-commits: Plan commits as reviewable work units. Trigger: implementation, commit splitting, chained PRs, or keeping tests and docs with code." The user asks to build a plan — arguably implementation planning. Hmm. The persona mandatory self-check: does this match? The plan is an architecture/deployment plan, not really commit splitting. But "implementation" trigger... The plan includes future implementation steps. I think the skill is about planning commits when implementing — partially relevant. Loading it could shape the plan's work units into commits. Given the blocking requirement and that the plan will likely lead to implementation, loading work-unit-commits seems prudent... but the plan here is architectural (what to build), and user wants the plan in terminal. The skill would add commit-level guidance which isn't requested yet.

Also cognitive-doc-design? "Design docs that reduce cognitive load. Trigger: writing guides, READMEs, RFCs..." — the plan is a doc-ish artifact in chat, not a file. Borderline. The user asked for a plan in the terminal — it's a plan presentation, not a document to write to file.

I'll consider work-unit-commits relevant enough (planning implementation) — but honestly, the primary need now is the architecture plan itself. The rules say multiple skills can apply; match by task context. "Plan commits as reviewable work units. Trigger: implementation..." — I'm planning *what* to implement, in plan mode, not committing. I'll skip it for now; when we switch to implementation, it applies. Hmm — "The `<available_skills>` block is authoritative... Self-check BEFORE every response: does this request match any skill?" — "armame el plan" for implementation. I'd say it's a genuine match for work-unit-commits (planning how work will be structured). Let me load it — cheap, and might improve the plan with work-unit structure. Actually wait — will it? It's about commits/PR splitting. The user wants an architecture plan. When implementation starts, we'd structure commits. Including "work units" in the plan now = good planning discipline.

Decision: load work-unit-commits to comply with the blocking requirement (plan for implementation matches its trigger).

Also memory: save the plan decision? The plan itself is presented in chat. Save a summary? The protocol: save decisions. The portability requirement is a new constraint — save it. Also plan mode — no file changes. mem_save: add "plan presentado + requisito de portabilidad IdP". Maybe combine into one save at the end... Actually the final message must be last with no tool calls after. So: skill load + maybe save, then compose.

Sequence:
1. load work-unit-commits (check trigger)
2. compose plan in chat (final message, no tool calls after)

Should I save memory before final answer? The portability requirement learned → preference/constraint save. And plan presented. I could save one observation capturing "plan arquitectónico presentado en chat + requisito de portabilidad del IdP". Let's do: load skill first, then mem_save, then final message.

Wait — plan mode: "Discuss the plan with the user directly in the conversation. Do not create or update plan files unless the user explicitly asks you to." They said "por acá en la terminal" — explicitly in terminal, so no file. 

Let me load the skill.

**Tool: skill**

**Input:**
```json
{
  "id": "work-unit-commits"
}
```

<skill_content name="work-unit-commits">
# Skill: work-unit-commits

## When to Use

Load this skill when deciding what belongs in each commit or PR.

Use it for:

- Splitting a feature into reviewable work.
- Preparing commits before opening a PR.
- Turning a large change into chained or stacked PRs.
- Keeping reviewer cognitive load healthy.
- Committing ODD work units in reviewable slices.

## Critical Rules

| Rule | Requirement |
|------|-------------|
| Commit by work unit | A commit represents a deliverable behavior, fix, migration, or docs unit. |
| Do not commit by file type | Avoid `models`, then `services`, then `tests` if none works alone. |
| Keep tests with code | Tests belong in the same commit as the behavior they verify. |
| Keep docs with the user-visible change | Docs belong with the feature or workflow they explain. |
| Tell a story | A reviewer should understand why each commit exists from its diff and message. |
| Future PR-ready | Each commit should be a candidate chained PR when the change grows. |
| Budget is not code-golf | Never shrink a diff by deleting comments, blank lines, docs, or tests, or by compressing code, to fit the review budget (400 by default, or the session `review_budget_lines`). Slice by work unit or report the overage. |

## Work Unit Checklist

Before committing, confirm:

- [ ] The commit has one clear purpose.
- [ ] The repo still makes sense after applying only this commit.
- [ ] Tests or docs for this unit are included when relevant.
- [ ] Rollback is reasonable without reverting unrelated work.
- [ ] Focused test command and exact result are recorded.
- [ ] Runtime harness command/scenario and exact result are recorded, or explicit `N/A` explains why no runtime boundary exists.
- [ ] Rollback boundary names the exact files/behavior removable without unrelated work.
- [ ] The commit message explains the outcome, not the file list.

## Split Examples

| Weak split | Better work-unit split |
|------------|------------------------|
| `add models` | `feat(auth): add token validation domain model and tests` |
| `add services` | `feat(auth): wire token validation into login flow` |
| `add tests` | Tests included with each behavior commit |
| `update docs` | Docs included with the user-facing change they explain |

## PR Relationship

Use work-unit commits as the foundation for chained PRs:

1. Build the smallest independent work unit.
2. Include verification for that unit.
3. Commit it with a Conventional Commit message.
4. If the PR approaches 400 changed lines, promote commits or groups of commits into chained PRs.

## ODD Relationship

Organic Driven Development (ODD) commits substantial work in reviewable slices on the feature branch, not one commit per task:

- Commit work units in reviewable slices of about 400 authored changed lines, branch first when on the default branch, with tests and docs alongside the behavior and a Conventional Commit message. A unit whose Risk line names a high-risk item gets its own commit so native review can take it alone, and the last slice closes the feature.
- The native review candidate is that commit, or the PR slice it belongs to when review is deferred, against the previous reviewed boundary. It is never a TODO checkbox and never the accumulated feature branch.
- The running authored line count from work-unit commits feeds the delivery-strategy vocabulary: `ask-on-risk`, `auto-chain`, `single-pr`, `exception-ok`.
- The ODD feature document records the commit that holds each task as evidence and, once a delivery strategy applies, the chosen chain strategy and slice boundaries (which commits each PR holds).

## Commands

```bash
# Review the story before committing
git diff --stat
git diff --cached --stat

# Check recent commit style
git log --oneline -5
```

Base directory for this skill: C:\Users\damian\.config\opencode\skills\work-unit-commits
Relative paths in this skill (e.g., scripts/, reference/) are relative to this base directory.
Note: file list is sampled.

<skill_files>
</skill_files>
</skill_content>

---

## Assistant

_Thinking:_

The skill is about commits — I'll factor work-unit thinking into the plan's implementation phases (each phase = reviewable work unit with tests), but the plan itself is architectural.

Now save memory about the portability requirement, then compose the final plan in chat.

Actually — is a mem_save needed? New constraint learned: "que en el futuro se pueda migrar a otro IdP/servicio" — user preference/constraint. Yes, save it (proactive rule). Also the plan was produced. One save with topic_key requirements/idp-portability or update... Let me do one save: type preference, topic_key "requirements/idp-portability".

Then compose the plan.

Plan content — let me design it properly.

**Objetivo:** e-commerce chico, microservicios Spring Boot, roles/permisos manejados por IdP externo, pocos recursos, portabilidad futura del IdP.

**Stack final:**
- Borde: Nginx (TLS, estáticos) — opcional en dev
- Spring Cloud Gateway (Spring Boot WebFlux): validación JWT (oauth2Resource-server), reglas rol×ruta, rate limiting (Resilience4j — in-memory, sin Redis para ahorrar recursos)
- Eureka Server: discovery; gateway resuelve lb://servicio
- IdP: Authorizer (1 contenedor Docker; SQLite en dev, PG en prod si hace falta)
- Microservicios: no over-split — para un e-commerce chico: 
  - `svc-catalogo`: productos, categorías (CRUD admin)
  - `svc-pedidos`: carrito? checkout, pedidos
  - `svc-clientes`: perfil de clientes (referencia `sub` del IdP), direcciones
  - Hmm — do they need carrito service? Keep as suggestion: 3 servicios + gateway + eureka. Also maybe `svc-notificaciones` later. Keep: catálogo, pedidos, clientes.
- Bases de datos: una por servicio (PostgreSQL o MySQL)

**Flujos:**
1. Login: frontend → authorization code + PKCE → Authorizer → tokens → frontend guarda access token
2. API call: frontend → Nginx → Gateway: valida JWT (issuer/jwks por discovery), mapea roles→authorities, aplica path rules, rutea vía Eureka al servicio
3. Servicios: validan el JWT también (o confían en header interno — better: validan JWT ellos mismos, es config)
 Actually: recommendation — gateway valida y servicios validan también (defense in depth, es solo config con issuer-uri). For light resources, validating JWT locally (JWKS cached) is cheap — no network per request after cache.
4. Admin UI: Authorizer dashboard para gestionar usuarios/roles

**Matriz de permisos (ejemplo):**
- `cliente`: GET productos, crear pedidos propio...
- `admin`: todo- `admin-productos` (o admin con PRODUCTOS_EDIT): PUT/DELETE productos, no categorías
- Rules in gateway: pathMatchers per authority; deny by default

**Unidades de trabajo (fases, cada una un work unit testeable):**
1. `chore(infra)`: docker-compose base (authorizer + eureka) — verificación: servicios levantan, OIDC discovery responde
2. `feat(eureka)`: eureka-server + registro de un servicio demo
3. `feat(gateway)`: spring cloud gateway con oauth2ResourceServer + rutas lb:// + reglas por rol (tests: WebTestClient con tokens de prueba... tests with mocked JwtDecoder)
4. `feat(security-common)`: módulo/starter común `common-security` con JwtAuthenticationConverter por properties (la pieza de portabilidad)
5. `feat(catalogo)`, `feat(pedidos)`, `feat(clientes)`: CRUDs + validación JWT + @PreAuthorize donde haga falta
6. `chore(edge)`: Nginx TLS (prod) — opcional
Each with tests, conventional commits, ~400 lines per slice.

**Portabilidad del IdP (la pregunta clave):**
El estándar ES la capa de abstracción:
1. **OIDC discovery**: configurá `issuer-uri`/`jwk-set-uri` — los endpoints no se hardcodean. Cambiar de IdP = cambiar una property.
2. **Config centralizada**: toda la config del IdP (issuer, claim de roles, scopes) vive en `common-security` (starter compartido) + properties. Los microservicios no saben qué IdP hay atrás.
3. **Vocabulario de roles propio**: definís `PRODUCTOS_EDIT` etc. en el IdP (mapping) y en el gateway mapeás claim→authority por configuración (claim name configurable: `roles`, `groups`, `realm_access.roles`...). Cambiás de IdP → cambiás el nombre del claim, no el código.
4. **Nunca llames a APIs específicas del IdP desde la lógica de negocio**: ni SDK de Authorizer en los servicios, ni GraphQL de Authorizer en pedidos. Solo validación de JWT estándar. La gestión de usuarios va por la consola del IdP (manual) o, si la automatizás, por un adapter propio aislado tras tu interfaz.
5. **Referencia de usuario = `sub`**: guardá el `sub` (subject) como FK, más los datos de perfil que son tuyos (nombre, dirección). Ojo: al migrar de IdP, el `sub` cambia — por eso se llama "migración de usuarios" y no es automática; mitigación: guardar email normalizado para poder re-vincular.
6. **FGA/OpenFGA**: si lo usás algún día, tras una interfaz propia (port/adapter) — no acoplarse a la API de OpenFGA en los servicios. Para empezar: no usarlo (RBAC por endpoint alcanza).
7. Lo que NO se puede migrar nunca: hashes de contraseñas, sesiones activas, tokens emitidos. Eso se asume: en la migración, los usuarios re-login (o importación de hashes si el IdP destino lo acepta — Authorizer acepta import con hashes, por eso elegir IdPs que soporten import de hashes ayuda).

Límite honesto: portabilidad ~90% (validación, login flow, config) —10% inevitable (directorios de usuarios, features proprietary).

Also resource notes: dev compose: authorizer(sqlite)+eureka+gateway+2-3 servicios+db≈ cómo ajustar -Xmx (256-384MB por servicio), etc.

Format: plan in chat, structured, not too long. The user wants the plan. This is the deliverable — can be a bit longer but still tight. Use sections. Avoid excessive code. Maybe one config snippet for portability (properties) — helpful. Keep code minimal.

Also they asked "se puede hacer de alguna manera que en un futuro se pueda migrar a otro servicio como este?" — answer explicitly YES + how (the portability section is the answer).

Order: answer the migration question first (short: sí, estándar OIDC es la abstracción), then the plan, then portability details integrated. Or plan first then portability? The question's second part deserves a direct answer. Structure:

1. Plan (fases + stack) — compact
2. Portabilidad — the how
3. Closing question? Maybe ask about starting point (which phase first / DB choice?). One question max. Could ask: "¿Arrancamos por la fase 1 (compose + Eureka)?" But plan mode — they need to switch agents to implement. I should remind: for implementation, switch agent. Actually the reminder: "If the user asks to implement changes, do not do so. Tell them they need to switch agents." They only asked for the plan — fine. End with a light question or note.

Memory save first.

Let me write the mem_save + then final. Combine: save portability requirement + plan presented.

**Tool: execute**

**Input:**
```json
{
  "code": "return await tools.engram.mem_save({\n  title: \"Requisito: portabilidad del IdP (migrable a otro servicio en el futuro)\",\n  type: \"preference\",\n  scope: \"project\",\n  topic_key: \"requirements/idp-portability\",\n  content: \"What: El usuario quiere que la arquitectura permita migrar el IdP (Authorizer) a otro servicio equivalente en el futuro sin rehacer el sistema. Why: evitar vendor lock-in en la capa de autenticacion. Where: plan presentado en chat (modo plan, sin archivos). Learned: estrategia acordada - (1) usar solo OIDC discovery + JWKS (issuer-uri configurable), (2) config del IdP centralizada en un starter compartido common-security con authorities mapping por properties (nombre de claim configurable), (3) vocabulario de roles propio (PRODUCTOS_EDIT etc.) definido en el IdP, (4) cero APIs especificas del IdP en la logica de negocio - solo validacion de JWT estandar, (5) FK = sub + email normalizado para re-vincular en migracion, (6) FGA/OpenFGA detras de adapter propio si se usa. Portabilidad estimada ~90%; inmigrables: hashes de passwords, sesiones, tokens.\"\n});"
}
```

{
  "candidates": [
    {
      "id": 19,
      "judgment_id": "rel-dc2c0298f3057bb0",
      "score": -4.220514366675497,
      "sync_id": "obs-64f66a6267ae0c09",
      "title": "Requisito authz: RBAC por recurso a nivel endpoint en e-commerce",
      "topic_key": "requirements/authz-granularity",
      "type": "preference"
    }
  ],
  "id": 21,
  "judgment_id": "rel-dc2c0298f3057bb0",
  "judgment_required": true,
  "judgment_status": "pending",
  "project": "latiendita",
  "project_path": "c:\\users\\damian\\repositorios\\latiendita",
  "project_source": "session",
  "result": "Memory saved: \"Requisito: portabilidad del IdP (migrable a otro servicio en el futuro)\" (preference)\nCONFLICT REVIEW PENDING — 1 candidate(s); use mem_judge to record verdicts.",
  "review_after": "2027-02-04 00:22:26",
  "state": "active",
  "sync_id": "obs-5edc9965febde74d",
  "truncation": {
    "original_bytes": 858,
    "limit_bytes": 50000,
    "truncated": false
  }
}

---

## Assistant

_Thinking:_

There's a judgment required: candidate id 19 (authz-granularity) vs new obs 21 (idp-portability), judgment_id rel-dc2c0298f3057bb0, score -4.22. The protocol says: if judgment_required, prefer mem_judge_many (max 32) with candidates[].judgment_id; never reuse top-level judgment_id. Here there's one candidate with judgment_id "rel-dc2c0298f3057bb0". I should call mem_judge (single) or mem_judge_many with the candidate's judgment_id.

The protocol: "After mem_save: if judgment_required, prefer mem_judge_many (max 32) with candidates[].judgment_id; never reuse the top-level judgment_id."

So call mem_judge_many with candidates: [{id: 19, judgment_id: "rel-dc2c0298f3057bb0"}] and a verdict: these are two distinct topics (authz granularity vs idp portability) — both are "requirements" but different concerns: one about permission level (endpoint vs instance), the other about IdP swap. Verdict: they're related but distinct — "not_conflict" or "compatible". They don't conflict — portability doesn't contradict granularity. Relation: compatible (both are authz requirements, different aspects).

mem_judge_many signature — I don't know exact schema. Let me search the catalog for mem_judge_many... Or mem_judge. The engram description says mem_judge — "record a verdict on a pending memory conflict (judgment_required flow)". Schema unknown. I need to find it: use search() in execute.

Let me search for the tool signature first.

**Tool: execute**

**Input:**
```json
{
  "code": "const hits = search({ query: \"mem_judge_many mem_judge judgment verdict\", namespace: \"engram\", limit: 5 });\nreturn hits;"
}
```

{
  "items": [
    {
      "path": "tools.engram.mem_judge_many",
      "description": "Record 1..32 caller-authored judgments in submitted order. Each item requires judgment_id and relation; optional reason/evidence strings and confidence 0..1. Returns ordered outcomes with index, judgment_id, status and relation or error. Item errors do not stop later items. Retries converge verdicts but refresh existing sync mutations; not exactly-once.",
      "signature": "tools.engram.mem_judge_many({\n  judgments: Array<{\n      confidence?: number,\n      evidence?: string,\n      judgment_id: string,\n      reason?: string,\n      relation: string,\n    }>,\n  /** Optional shared session provenance, as in mem_judge */\n  session_id?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_judge",
      "description": "Record a verdict on a pending memory conflict surfaced by mem_save.\n\nWHEN TO CALL: After mem_save returns judgment_required=true, iterate candidates[] and call mem_judge once per entry using that entry's judgment_id.\n\nPARAMS:\n  judgment_id (required) — from candidates[].judgment_id in the mem_save response\n  relation    (required) — one of: related, compatible, scoped, conflicts_with, supersedes, not_conflict\n  reason      (optional) — free-text explanation of the verdict\n  evidence    (optional) — JSON or text supporting the verdict\n  confidence  (optional) — float 0..1; default 1.0\n\nWHEN TO ASK USER:\n  - confidence < 0.7: surface to user before judging\n  - relation in {supersedes, conflicts_with} AND type in {architecture, policy, decision}: ask user\n  - otherwise: resolve silently\n\nSUCCESS: Returns the updated relation row with judgment_status=\"judged\".\nERROR: Returns IsError=true if judgment_id is unknown or relation verb is invalid. Row is NOT mutated on error.\n\nRe-judging an already-judged ID overwrites the verdict (deliberate revision).",
      "signature": "tools.engram.mem_judge({\n  /** Confidence score 0.0..1.0 (default: 1.0) */\n  confidence?: number,\n  /** Supporting evidence (JSON or free text) */\n  evidence?: string,\n  /** The judgment_id from candidates[] in the mem_save response (format: rel-<hex>) */\n  judgment_id: string,\n  /** Free-text explanation of the verdict */\n  reason?: string,\n  /** Verdict: related | compatible | scoped | conflicts_with | supersedes | not_conflict */\n  relation: string,\n  /** Session ID for provenance (default: auto) */\n  session_id?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_compare",
      "description": "Persist a semantic verdict you have already judged externally (with your LLM) into Engram.\n\nWHEN TO CALL: After you have evaluated two memories and reached a verdict, call mem_compare to PERSIST that verdict into the relation store. You do the judgment; mem_compare records it.\n\nPARAMS:\n  memory_id_a  (required) — integer id of the first observation (from mem_search or mem_get_observation)\n  memory_id_b  (required) — integer id of the second observation\n  relation     (required) — one of: related, compatible, scoped, conflicts_with, supersedes, not_conflict\n  confidence   (required) — float 0..1; your self-reported confidence in the verdict\n  reasoning    (required) — explanation of the verdict, max 200 chars\n  model        (optional) — your model identifier, stored for provenance (e.g. \"claude-haiku-4-5\")\n\nBEHAVIOR:\n  - Persists the verdict via JudgeBySemantic with system provenance (marked_by_actor=\"engram\").\n  - not_conflict: persists a judged relation and returns its sync_id, so future scans do not re-evaluate the pair.\n  - Idempotent: calling again for the same pair updates the existing row.\n  - Cross-project pairs are rejected.\n\nSUCCESS: Returns { \"sync_id\": \"rel-...\" } on every persisted verdict.\nERROR: Returns IsError=true if IDs are unknown, relation is invalid, or cross-project pair.",
      "signature": "tools.engram.mem_compare({\n  /** Confidence score 0.0..1.0 */\n  confidence: number,\n  /** Integer id of the first observation (from mem_search #id) */\n  memory_id_a: number,\n  /** Integer id of the second observation (from mem_search #id) */\n  memory_id_b: number,\n  /** Your model identifier for provenance (e.g. \"claude-haiku-4-5\") */\n  model?: string,\n  /** Brief explanation of the verdict (max 200 chars) */\n  reasoning: string,\n  /** Verdict: related | compatible | scoped | conflicts_with | supersedes | not_conflict */\n  relation: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_save",
      "description": "Save an important observation to persistent memory. Call this PROACTIVELY after completing significant work — don't wait to be asked.\n\nWHEN to save (call this after each of these):\n- Architectural decisions or tradeoffs\n- Bug fixes (what was wrong, why, how you fixed it)\n- New patterns or conventions established\n- Configuration changes or environment setup\n- Important discoveries or gotchas\n- File structure changes\n\nFORMAT for content — use this structured format:\n  **What**: [concise description of what was done]\n  **Why**: [the reasoning, user request, or problem that drove it]\n  **Where**: [files/paths affected, e.g. src/auth/middleware.ts, internal/store/store.go]\n  **Learned**: [any gotchas, edge cases, or decisions made — omit if none]\n\nIf candidate_detection_status is incomplete, the observation was saved but conflict detection failed or only partially persisted. Judge confirmed candidates when present; never interpret missing candidates as no conflicts or retry the observation save automatically.\n\nTITLE should be short and searchable, like: \"JWT auth middleware\", \"FTS5 query sanitization\", \"Fixed N+1 in user list\"\n\nExamples:\n  title: \"Switched from sessions to JWT\"\n  type: \"decision\"\n  content: \"**What**: Replaced express-session with jsonwebtoken for auth\\n**Why**: Session storage doesn't scale across multiple instances\\n**Where**: src/middleware/auth.ts, src/routes/login.ts\\n**Learned**: Must set httpOnly and secure flags on the cookie, refresh tokens need separate rotation logic\"\n\n  title: \"Fixed FTS5 syntax error on special chars\"\n  type: \"bugfix\"\n  content: \"**What**: Wrapped each search term in quotes before passing to FTS5 MATCH\\n**Why**: Users typing queries like 'fix auth bug' would crash because FTS5 interprets special chars as operators\\n**Where**: internal/store/store.go — sanitizeFTS() function\\n**Learned**: FTS5 MATCH syntax is NOT the same as LIKE — always sanitize user input\"",
      "signature": "tools.engram.mem_save({\n  /**\n   * Automatically capture the current user prompt when available. Omission defaults to true; false skips capture for automated saves. If present, must be a boolean; other values return capture_prompt must be a boolean before project resolution or writes.\n   */\n  capture_prompt?: boolean,\n  /**\n   * Structured content using **What**, **Why**, **Where**, **Learned** format. Required unless observation alias is provided.\n   */\n  content?: string,\n  /** Backward-compatible alias for content. Prefer content for new clients. */\n  observation?: string,\n  /**\n   * Optional explicit project for this memory. Accepted only when backed by known context (existing project, matching session, repo config, or ambiguous-project recovery); invalid or unbacked names fail loudly.\n   */\n  project?: string,\n  /**\n   * Must be user_selected_after_ambiguous_project, and only after the user explicitly chose one of available_projects from an ambiguous_project error.\n   */\n  project_choice_reason?: string,\n  /**\n   * Short-lived token returned by an ambiguous_project error. Required with project_choice_reason=user_selected_after_ambiguous_project.\n   */\n  recovery_token?: string,\n  /** Scope for this observation: project (default), personal, or global */\n  scope?: string,\n  /**\n   * Optional authoritative session ID already registered by the runtime or mem_session_start; never invent one. Omit by default; ambiguity fails closed.\n   */\n  session_id?: string,\n  /** Short, searchable title (e.g. 'JWT auth middleware', 'Fixed N+1 query') */\n  title: string,\n  /**\n   * Optional topic identifier for upserts (e.g. architecture/auth-model). Reuses and updates the latest observation in same project+scope.\n   */\n  topic_key?: string,\n  /** Category: decision, architecture, bugfix, pattern, config, discovery, learning (default: manual) */\n  type?: string,\n}): Promise<unknown>"
    },
    {
      "path": "tools.engram.mem_context",
      "description": "Get recent memory context from previous sessions. Shows recent sessions and observations to understand what was done before.",
      "signature": "tools.engram.mem_context({\n  /** Drop the inline content preview from observation bullets to save tokens. */\n  compact?: boolean,\n  /** Total context budget in bytes. Default 16384; values above 65536 are clamped to 65536. */\n  max_bytes?: number,\n  /** Include a bounded Older History overview with counts and sampled titles outside the recent window. */\n  older_history?: boolean,\n  /** Filter by project (omit for all projects) */\n  project?: string,\n  /** Filter observations by scope: project, personal, or global. Omit to apply no scope filter. */\n  scope?: string,\n}): Promise<unknown>"
    }
  ],
  "remaining": 18,
  "next": {
    "offset": 5
  }
}

---

## Assistant

_Thinking:_

Now judge the pending conflict: judgment_id "rel-dc2c0298f3057bb0", candidate obs 19 (authz-granularity: RBAC endpoint vs instance) vs obs 21 (idp-portability). Relation: they're two different requirements — related, not conflicting. I'd say "compatible": both are authz requirements, orthogonal (granularity of permissions vs portability of IdP). Confidence high (1.0? I'd say 0.9). Use mem_judge with judgment_id from candidates (rel-dc2c0298f3057bb0), relation "compatible", reason.

Then compose the final plan message (no tool calls after).

**Tool: execute**

**Input:**
```json
{
  "code": "return await tools.engram.mem_judge({\n  judgment_id: \"rel-dc2c0298f3057bb0\",\n  relation: \"compatible\",\n  confidence: 0.95,\n  reason: \"Requisitos ortogonales: uno define la granularidad de permisos (endpoint vs instancia), el otro la portabilidad del IdP. No se contradicen; coexisten en el mismo plan de authz.\"\n});"
}
```

{
  "relation": {
    "id": 9,
    "sync_id": "rel-dc2c0298f3057bb0",
    "source_id": "obs-5edc9965febde74d",
    "target_id": "obs-64f66a6267ae0c09",
    "relation": "compatible",
    "reason": "Requisitos ortogonales: uno define la granularidad de permisos (endpoint vs instancia), el otro la portabilidad del IdP. No se contradicen; coexisten en el mismo plan de authz.",
    "confidence": 0.95,
    "judgment_status": "judged",
    "marked_by_actor": "agent",
    "marked_by_kind": "agent",
    "created_at": "2026-11-04 00:22:26",
    "updated_at": "2026-11-04 00:22:47"
  }
}

---

## Assistant

_Thinking:_

Now compose the final plan in chat. Spanish, structured, actionable, covering:

1. Answer to portability question (sí, cómo)
2. The plan: stack + phases/work units
3. Portability mechanics

Keep it organized but not bloated. This is the main deliverable so some length is justified, but respect the length contract — tight structure, no fluff.

Plan outline:

**Respuesta corta a la migración:** Sí — el estándar OIDC es tu capa de abstracción. Si validás tokens por discovery y nunca tocás APIs privadas del IdP, migrar = cambiar properties + re-mapear claims. ~90% portátil; lo inmigrable es directorio de usuarios (hashes, sesiones) — eso tiene mitigación pero no es cero.

**Stack:**
```
[Nginx TLS] → [Spring Cloud Gateway] → servicios (vía Eureka)
                    ↑ valida JWT
 [Authorizer en Docker] (IdP, SQLite/PG)
```

**Fases (cada una = work unit con tests, commits convencionales, ≤400 líneas por slice):**
1. `chore(infra)`: compose base: Authorizer (SQLite) + Eureka Server. Verificación: discovery OIDC responde, Eureka UI registra.
2. `feat(gateway)`: Spring Cloud Gateway + oauth2ResourceServer + rutas lb:// + reglas rol×ruta (deny by default). Tests con JwtDecoder mockeado.
3. `feat(common-security)`: starter compartido con mapping de claims configurable (la pieza de portabilidad).
4. `feat(catalogo)`: productos/categorías + validación JWT + @PreAuthorize.
5. `feat(pedidos)`: checkout/pedidos con `sub` del cliente.
6. `feat(clientes)`: perfil/direcciones (datos propios referenciando sub).
7. `chore(edge)`: Nginx TLS + rate limiting.

Servicios: 3 (catálogo, pedidos, clientes) — no más para un e-commerce chico.

**Matriz de roles inicial** (en Authorizer): cliente, admin, admin-productos (o permisos fine-grained). Example table small.

**Portabilidad — las 6 reglas:**
1. issuer-uri + discovery (no hardcodear endpoints)
2. config del IdP solo en common-security (starter) — services only know "validate JWT"
3. claim de roles configurable (roles/groups/realm_access.roles)
4. vocabulario de roles propio en el IdP
5. cero SDKs del IdP en negocio; FGA si acaba tras adapter
6. usuarios: FK = sub + email normalizado; en migración, re-login o import de hashes (Authorizer acepta import) — lo inmigrable: sesiones y tokens.

**Recursos (lo que pediste):** compose de dev con todo: Authorizer(sqlite) + eureka + gateway + 3 servicios con `-Xmx=256m` + DBs... real: ~2.5-3GB. Ojo: no levantar todos a la vez si es muy justo; PG compartido en dev es aceptable? Por service una DB — en dev puede usar un solo PostgreSQL con schemas/db separadas.

Hmm — keep resource note brief.

End: for implementation, switch agent (plan mode). And one question? Maybe not needed — plan delivered. Could ask "¿Qué fase arrancamos?" but implementation requires agent switch. Per plan-mode reminder: if user asks to implement, tell them to switch agents. I'll note: cuando quieras arrancar la fase 1, cambiá de agente.

No tool calls after this message.

Perfecto — acá está el plan completo, y sí: **la migración futura a otro IdP es posible, y la clave ya está en cómo lo diseñamos desde el día uno**. Te explico las dos cosas.

---

## Arquitectura objetivo — LaTiendita

```
Browser/SPA
   │  (authorization code + PKCE)
   ▼
Nginx (TLS, estáticos) ── borde, sin lógica
   ▼
Spring Cloud Gateway (Spring Boot/WebFlux)
   │  · valida JWT (issuer + JWKS por OIDC discovery)
   │  · claim de rol → authorities
   │  · reglas ruta × rol (deny by default)
   │  · rate limiting (Resilience4j, in-memory — sin Redis)
   ▼  rutas lb://servicio via Eureka
┌──────────┬───────────┬───────────┐
│ catalogo │  pedidos  │ clientes  │   3 microservicios Spring Boot
└──────────┴───────────┴───────────┘   cada uno valida el JWT también
   ▲
   │ OIDC (issuer, tokens, consola de usuarios/roles)
Authorizer — 1 contenedor Docker (SQLite en dev)
```

**Roles iniciales (se crean en Authorizer, no en código):** `cliente`, `admin`, `admin-productos` con permisos tipo `PRODUCTOS_EDIT`, `CAT_EDIT`.

---

## Fases de implementación (cada una = un work unit con tests y commit convencional)

| # | Unidad | Qué incluye | Verificación |
|---|--------|-------------|--------------|
| 1 | `chore(infra)` | docker-compose: Authorizer (SQLite) + Eureka Server | OIDC discovery responde; Eureka UI arriba |
| 2 | `feat(gateway)` | Gateway + resource server + rutas `lb://` + reglas por rol | Tests con `JwtDecoder` mockeado: token con rol A pasa, sin rol B → 403 |
| 3 | `feat(common-security)` | Starter compartido: mapping claim→authorities configurable | Test de conversión con claims con distintas formas |
| 4 | `feat(catalogo)` | CRUD productos/categorías + JWT + `@PreAuthorize` | Tests de autorización endpoint a endpoint |
| 5 | `feat(pedidos)` | Checkout y pedidos, referencia `sub` del cliente | Tests de flujo con token de prueba |
| 6 | `feat(clientes)` | Perfil y direcciones (datos propios) | Tests CRUD |
| 7 | `chore(edge)` | Nginx con TLS | Smoke test manual |

Sin sobredimensionar: **3 servicios y hasta acá**. Un e-commerce chico no necesita 8 microservicios — cada uno es otra JVM en RAM (apuntá a `-Xmx=256m` en dev; con todo el compose rondarás los 2.5-3 GB).

---

## La respuesta a tu pregunta de migración: **sí, y estas son las 6 reglas que lo hacen posible**

1. **OIDC discovery, nunca URLs hardcodeadas** — el gateway y los servicios usan `issuer-uri`; los endpoints del IdP se resuelven solos. Cambiar de IdP = cambiar una property.
2. **Toda la config del IdP vive en un solo lugar** — el starter `common-security`. Los microservicios solo saben "validá este JWT"; no saben qué IdP hay atrás.
3. **El nombre del claim de roles es configurable** — `roles`, `groups`, `realm_access.roles`… cada IdP lo llama distinto. Se cambia por property, no por código.
4. **Vocabulario de roles propio** — `PRODUCTOS_EDIT` lo definís *vos* (en Authorizer o en el IdP destino). El dominio no habla el idioma de ningún vendor.
5. **Cero APIs específicas del IdP en la lógica de negocio** — ni SDK de Authorizer en pedidos, ni GraphQL de Authorizer en catálogo. Solo validación de JWT estándar. Si algún día usás el FGA (OpenFGA), va tras una interfaz tuya (adapter), nunca crudo en los servicios.
6. **Referencias de usuario: `sub` + email normalizado** — guardás el `sub` como FK más el email, para poder re-vincular usuarios si cambiás de IdP.

**El límite honesto (~90% portátil):** lo que *nunca* se migra solo son hashes de contraseñas, sesiones activas y tokens vigentes — en un cambio de IdP la gente re-login (o importás hashes si el destino lo acepta; Authorizer los acepta, así que empezar por ahí te deja esa puerta abierta).

---

El plan está listo en la conversación (nada escrito a disco, como pediste). Cuando quieras arrancar la Fase 1, salí de Plan mode y cambiá de agente para implementar.
