# Design System — LaTiendita (Wireframe)

Fuente de verdad del sistema de diseño en modo wireframe. Todo screen nuevo debe
nacer de estos tokens, no de valores sueltos.

- **Archivo de diseño:** [`docs/OpenPencil/LaTiendita.fig`](../OpenPencil/LaTiendita.fig)
- **Tokens machine-readable:** [`tokens/`](./tokens) (W3C DTCG, importables en OpenPencil)
- **Análisis base:** 3/11/2026 — 208 nodos, 11 colores, 103 nodos de texto sobre el frame `Home Wireframe` (1440 × 2354)

---

## 1. Principios del wireframe

1. **Solo escala de grises.** Ningún color de marca hasta el handoff de UI final.
2. **Placeholders, no fotos.** Toda imagen es un fill `background/subtle` con la
   etiqueta `[ Imagen ]` centrada en `text/subtle`.
3. **Borde dashed = pendiente.** Cajas y controles con trazo discontinuo están
   por definir (no son definitivos de la UI).
4. **Sin sombras ni degradés.** El wireframe comunica estructura, no acabado.
5. **Grid 4/8.** Los bloques de layout van en múltiplos de 8; los detalles
   (gaps chicos, paddings de control) en múltiplos de 4.

---

## 2. Tokens

### 2.1 Color — [`tokens/Colors/Default.tokens.json`](./tokens/Colors/Default.tokens.json)

11 valores físicos, 14 tokens semánticos. La escala es un gris neutro cálido
 estilo Zinc (el valor `#8A8A93` queda marcado como candidato a unificar).

| Token | Hex | Uso | Usos actuales |
|---|---|---|---|
| `background/canvas` | `#F5F5F6` | Fondo de página | 1 |
| `background/surface` | `#FFFFFF` | Secciones, cards, inputs | 38 |
| `background/subtle` | `#E4E4E7` | Placeholders de imagen, bandas sutiles | — |
| `background/inverse` | `#27272A` | Botón primario, badge de carrito | 12 |
| `background/muted` | `#D4D4D8` | Fill de logo/íconos placeholder | 2 |
| `border/default` | `#E4E4E7` | Hairlines de secciones y cards | 36 (compartido) |
| `border/strong` | `#A1A1AA` | Inputs, outline, cajas dashed | 18 |
| `text/primary` | `#18181B` | Títulos, precios, títulos de card | 35 |
| `text/secondary` | `#3F3F46` | Nav, labels de outline | 6 |
| `text/tertiary` | `#52525B` | Anuncio y badges | 2 |
| `text/muted` | `#71717A` | Copy secundario, links de footer | 24 |
| `text/placeholder` | `#A1A1AA` | Placeholder de input, flechas → | 18 |
| `text/subtle` | `#8A8A93` | Etiquetas `[ Imagen ]` | 15 |
| `text/on-inverse` | `#FFFFFF` | Texto sobre fills inverse | (compartido) |

> `text/subtle` (`#8A8A93`) es el único valor fuera de familia. Candidato a
> unificarse con `text/muted` (`#71717A`) cuando se re-coloquen los textos.

### 2.2 Tipografía — [`tokens/Typography/Default.tokens.json`](./tokens/Typography/Default.tokens.json)

- **Familia:** Inter (única, 103/103 textos)
- **Pesos:** 400 regular · 500 medium · 700 bold
- **Line-height:** `1.2` unitless (medido sobre todos los nodos de texto)
- **Escala:** 12 · 14 · 16 · 18 · 24 · 48 (6 tamaños, normalizada desde 11)

| Rol | Size / Weight | Uso |
|---|---|---|
| `display` | 48 / 700 | H1 del hero |
| `heading/section` | 24 / 700 | Títulos de sección |
| `title` | 18 / 700 | Wordmark del logo |
| `price` | 18 / 700 | Precios de producto |
| `body` | 16 / 400 | Párrafos, sub del hero |
| `body-strong` | 16 / 500 | Botones, títulos de card |
| `body-sm` | 14 / 400 | Nav, links, footer |
| `caption` | 12 / 400 | Metadata, categorías, contadores |

### 2.3 Espaciado — [`tokens/Spacing/Default.tokens.json`](./tokens/Spacing/Default.tokens.json)

`4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 48 · 64`

Granularidad de 4px para tokens; los bloques de layout respetan la grilla de 8px.

| Rol | Token | Uso actual |
|---|---|---|
| Card padding | `16` | 36 usos (dominante) |
| Page gutter / sección | `48` | 22 usos |
| Section padding Y | `40` | 6 usos |
| Grid gutter | `24` | 5 usos |
| Block gap | `32` / `64` | separación entre bloques |

### 2.4 Radios — [`tokens/Radius/Default.tokens.json`](./tokens/Radius/Default.tokens.json)

| Token | Valor | Uso |
|---|---|---|
| `8` | 8px | Controles: botones, inputs, logo |
| `12` | 12px | Superficies: cards, cajas grandes |
| `full` | 999px | Píldoras y círculos: badges, contadores |

### 2.5 Layout

| Valor | Definición |
|---|---|
| 1440 | Ancho de canvas / viewport |
| 48 | Margen de página |
| 1344 | Ancho de contenido (1440 − 96) |
| 72 | Altura de header |
| 240 | Ancho de sidebar del panel admin |
| 1136 | Ancho de contenido admin (1200 − 64) |

---

## 3. Componentes

Inventario medido en el documento (clusters con ≥ 2 repeticiones):

| Componente | Medidas | Tokens clave |
|---|---|---|
| `Card/Product` | 318 × 350 (imagen 318 × 200, cuerpo padding 16) | `border/default`, `radius/12`, `body-strong` |
| `Card/Category` | 204 × 141 (imagen 204 × 100) | `border/default`, `radius/12` |
| `Button` | alto 40 (compact) / 48 (hero), radio 8 | `background/inverse` + `text/on-inverse` · outline: `border/strong` + `text/secondary` |
| `Input` | Search 260 × 40 · Newsletter 320 × 48, radio 8 | `background/surface`, `border/strong`, `text/placeholder` |
| `Benefit item` | 318 × 44 (ícono 44 × 44 + bloque de texto) | `background/subtle`, `body-sm` |
| `Hero image` | 560 × 340, radio 12, borde dashed | `background/subtle`, `text/subtle` |
| `Newsletter box` | 1344 × 132, radio 12, borde dashed | `background/surface`, `border/strong` |
| `Logo` | ícono 28 × 28 radio 8 + wordmark 18/700 | `background/muted`, `title` |
| `Card/Category (grid)` | 318 × 232 (imagen 318 × 160, cuerpo padding 16) | `border/default`, `radius/12`, `background/subtle` |
| `Filter pill` | alto 40, radio full, padding X 16 | activa: `background/inverse` + `text/on-inverse` · inactiva: `border/strong` + `text/secondary` |
| `Step indicator` | círculo 24 × 24 + label 14, gap 8 | `background/inverse` / `background/subtle`, `radius/full` |
| `Method card` | ancho 944, padding 20, radio 12 (seleccionada: borde 2) | `background/surface`, `border/default` / `border/strong` |
| `Store option` | 904 × 89, padding 16, radio 12 | `background/surface`, `border/default`, `radius/12` |
| `Order summary` | ancho 376, padding 20, radio 12 | `background/surface`, `border/default`, `radius/12` |
| `Radio / Checkbox` | 24 × 24 | `background/inverse` / `border/strong`, `radius/full` / `8` |
| `Admin shell` | sidebar 240 + main 1200, topbar 72 | `background/surface`, `border/default`, gutter 32 |
| `Nav item` | 208 × 40, padding X 12, radio 8 | activo: `background/inverse` + `text/on-inverse` · inactivo: `text/secondary` |
| `KPI card` | 266 × 117, padding 20, radio 12 | `background/surface`, `border/default`, valor `24/700` |
| `Stat strip` | 1136 × 81, padding 20/24, 4 stats de 235, radio 12 | `background/surface`, `border/default`, divisores verticales de 1 px |
| `Data table` | fila 56, padding X 20, header 40 | `border/default` hairline, header `background/canvas` |
| `Status badge` | alto 24, padding X 8, radio full | pagado/aprobado: `background/inverse` · enviado: `background/subtle` · pendiente: borde sólido · cancelado/rechazado: borde dashed |
| `List row` | alto 56–60, padding X 20 | `border/default` hairline, `body-sm` + `caption` |
| `Bulk action bar` | alto 56, padding X 20, radio 12 | `background/canvas`, `border/strong`, acciones de 32 de alto |
| `Category list row` | 700 × 72 (ícono 40), padding X 20 | seleccionada: `background/canvas`; `border/default` hairline |
| `Detail panel` | 412 de ancho, padding 20, radio 12 | `background/surface`, `border/default`, acción primaria + outline |
| `Status tabs` | alto 40, radio full, padding X 16, contador 12/700 | activa: `background/inverse` + `text/on-inverse` · inactiva: `border/strong` + contador `text/muted` |
| `Chart placeholder` | barras 48 de ancho, alto variable, radio 8 | `background/subtle`, barra del día actual en `background/inverse` |

---

## 4. Migración de valores actuales → tokens

El documento todavía tiene valores hardcodeados fuera de escala. Al vincular
variables, aplicar esta tabla:

### Tipografía

| Actual | Token | Nota |
|---|---|---|
| 10 | `size/12` | Contador del badge |
| 13 | `size/14` | Links de footer |
| 15 | `size/16` | Títulos de card, botones |
| 17 | `size/18` | Precios |
| 22 | `size/24` | Título de newsletter |
| 12, 14, 16, 18, 24, 48 | igual | ya en escala |

### Espaciado

| Actual | Token | Nota |
|---|---|---|
| 2 | `4` | Gap del bloque de benefits |
| 10 | `12` | Gaps de logo y redes |
| 14 | `16` | Paddings de card (16 usos) |
| 18 | `16` | Padding de botón login — unifica control padding en 16 |
| 28 | `32` | Paddings de CTA y gaps de nav/footer |
| 4, 8, 12, 16, 20, 24, 32, 40, 48, 64 | igual | ya en escala |

### Radios

| Actual | Token |
|---|---|
| 6 (logo) | `8` |
| 10 (cards) | `12` |
| 8, 999 | igual |

---

## 5. Cómo usarlos

### En OpenPencil

Importar las colecciones al `.fig` (agrega colecciones/modos; nada se borra):

```bash
openpencil tool call import_design_tokens docs/OpenPencil/LaTiendita.fig \
  --args-file tokens.json --write
```

o vía MCP `import_design_tokens` pasando cada archivo de `tokens/` como
`{ path, text }`. La carpeta define la colección (`Colors/Default.tokens.json`
→ colección `Colors`, modo `Default`).

Después de importar, vincular los nodos existentes y verificar con
`analyze colors` (debe mostrar `variableName` en cada color) y `lint`.

### En código (próximo paso del frontend)

```bash
openpencil tokens docs/OpenPencil/LaTiendita.fig --format css > tokens.css
openpencil tokens docs/OpenPencil/LaTiendita.fig --format tailwind > theme.css
```

`--format tailwind` genera un `@theme` de Tailwind v4 listo para importar.

---

## 6. Mantenimiento

- Pantalla nueva = tokens de este doc; si necesitás un valor que no existe,
  agregarlo primero acá y al archivo de tokens, no usar un literal suelto.
- Antes de cerrar un PR de diseño: `openpencil analyze spacing design.fig --grid 8`,
  `openpencil analyze colors design.fig` y `openpencil lint design.fig`.
- Cambios de tokens: editar los JSON de `tokens/` y re-importar (los tokens con
  el mismo nombre se actualizan, nada se duplica ni se borra).
