# Agregar pestañas faltantes de Mi cuenta al wireframe

Objective: dibujar en el `.fig` los paneles de cuenta implementados en el frontend que hoy no tienen frame. Branch: main (preferencia del usuario). Entrega: sin commits. Verificación: OpenPencil MCP (`node_bounds`, `get_jsx`, `export_image`) + lectura estructural del README.

## Specs

**S1.** "Podrias agregar todas las pestañas que faltan y que se deberia ver al apretar los botones, respeta el docs\design-system y las pestañas que ya estan implementadas en el frontend. Usa el mcp de openpencil"
Alcance: agregar un frame por panel de `Account nav` que esté implementado en el frontend y no esté dibujado: **Mis pedidos**, **Direcciones**, **Datos personales**, **Newsletter**. Cada frame: 1440 de ancho, header/breadcrumb/account nav con el ítem activo/panel/footer, usando los tokens de `docs/design-system` y los mismos textos que el frontend. No inventar KPIs, tablas ni copy que el frontend no renderiza.

**S2.** "respeta el docs\design-system y las pestañas que ya estan implementadas en el frontend"
No dibujar `Reportes` ni `Configuración` (en el frontend son pantallas "en preparación", sin contenido). No duplicar los tabs de detalle de producto ni los filtros de estado de Admin Pedidos/Admin Pagos: sus paneles ya están dibujados en `0:213`, `0:2235` y `0:2531`. Escala gris, Inter, grilla 4/8, radios 8/12/full, componentes `Data table`, `Status badge`, `Action button` del design system.

**S3.** "Usa el mcp de openpencil"
Toda mutación del diseño se hace con el MCP de OpenPencil sobre el documento `tab-2` (copia byte-idéntica del `.fig` del repo, estacionada en `C:\Users\damian\AppData\Local\Temp\opencode\latiendita-fig` porque el root del MCP es `C:\Users\damian`). Al final se copia el `.fig` resultante de vuelta a `docs/OpenPencil/LaTiendita.fig` y se verifica el hash.

**S4.** Suposición (no pedida literalmente): documentar los frames nuevos en `docs/design-system/README.md`, siguiendo el formato de las secciones 3.1 y 3.2, porque el README es la fuente de verdad y ya registra los componentes nuevos del `.fig`.

## Tasks

- **T1** — S1, S2, S3 — Agregar 4 frames de Mi cuenta con el MCP — inline — sin commit
- **T2** — S4 — Documentar los frames nuevos en el README del design system — inline — sin commit
- **T3** — S1, S3 — Verificar geometry/tokens y copiar el `.fig` al repo — inline — sin commit

## Log

**L1 (usuario, 2026-11-24):** Podrias agregar todas las pestañas que faltan y que se deberia ver al apretar los botones, respeta el docs\design-system y las pestañas que ya estan implementadas en el frontend. Usa el mcp de openpencil

**L2 (análisis):** El `.fig` del repo tiene 19 frames en `WireFrame`: 16 wireframes + 3 modales de Admin Productos. `Perfil Cliente Wireframe` (`0:3145`) dibuja sólo el panel Resumen; su `Account nav` (`0:3171`) muestra Resumen, Mis pedidos, Direcciones, Datos personales, Newsletter y Cerrar sesión. El mapeo del frontend (agente explorador, observación Engram 127) confirma que los cuatro paneles faltantes tienen ruta y componentes reales; Reportes y Configuración son placeholders; los tabs de producto y los filtros de estado ya están dibujados.

**L3 (discrepancia reportada, no resuelta):** El README documenta tres modales de Admin Categorías (`566369648:276`, `:318`, `:358`) que no existen en el `.fig` actual ni en `Internal Only Canvas`. Una copia vieja de verificación tiene frames de categoría con otros IDs. No se restauran: no son pestañas y reconstruirlos desde una copia stale sería adivinar. Queda como hallazgo para el usuario.
