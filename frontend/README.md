This is the LaTiendita storefront, built with [Next.js](https://nextjs.org).

## Getting Started

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## Mock mode

The app runs without a backend by default. `NEXT_PUBLIC_USE_MOCKS` (default `true`)
routes every axios request through the in-memory adapter in `mocks/`, which answers the
same routes the API will expose:

| Method | Path                              | Seed data            |
| ------ | --------------------------------- | -------------------- |
| `GET`  | `/api/categories?featured=true`   | `mocks/data/categories.ts` |
| `GET`  | `/api/products?featured=true`     | `mocks/data/products.ts`   |
| `PUT`  | `/api/categories`                 | updates the featured flag in both seeds |

The service layer (`features/*/services`) is written against the real API contract; mock
mode only swaps the axios adapter in `lib/http/client.ts`. Catalog reads unwrap Spring
`Page.content` into arrays and map `categoryId`/`title`/string prices into the screen
types, so the wireframes render identically from either transport. The admin category
detail card marks a category as featured through `PUT /api/categories` and revalidates
`/admin/categorias` and `/`.

To hit a real backend instead:

```bash
# .env.local
NEXT_PUBLIC_USE_MOCKS=false
NEXT_PUBLIC_API_URL=http://localhost:8081
```

## Project structure

| Path | Contents |
| ---- | -------- |
| `app/(public)` | Public route group (header, content, footer shell) |
| `app/layout.tsx` | Root layout, fonts and metadata |
| `components/ui` | Shared primitives (`button`, `image-placeholder`) |
| `components/layout` | Shell and page sections (header, footer, benefits, newsletter) |
| `config` | Runtime `env` and static `site` copy/navigation |
| `features/<domain>` | Domain code: `components`, `helpers`, `pages`, `services`, `store`, `types` |
| `lib/http` | Shared axios instance |
| `mocks` | Mock axios adapter, routes and seed data |

## Scripts

```bash
npm run dev    # development server
npm run lint   # eslint
npm run test   # vitest
npm run build  # production build
```
