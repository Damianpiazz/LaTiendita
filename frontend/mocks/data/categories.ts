import type { Category } from "@/features/product/types";

/**
 * Catalog categories in the order the `Categories Wireframe` frame draws them.
 *
 * The product counts are the ones printed on the frame: they model what the
 * listing API reports, not the size of the sample in `products.ts`.
 */
export const categories: Category[] = [
  {
    id: "tecnologia",
    slug: "tecnologia",
    name: "Tecnología",
    productCount: 24,
    description: "Auriculares, computación, celulares y accesorios.",
    featured: true,
  },
  {
    id: "hogar",
    slug: "hogar",
    name: "Hogar",
    productCount: 18,
    featured: true,
  },
  {
    id: "ropa",
    slug: "ropa",
    name: "Ropa",
    productCount: 32,
    featured: true,
  },
  {
    id: "deportes",
    slug: "deportes",
    name: "Deportes",
    productCount: 14,
    featured: true,
  },
  {
    id: "belleza",
    slug: "belleza",
    name: "Belleza",
    productCount: 21,
    featured: false,
  },
  {
    id: "juguetes",
    slug: "juguetes",
    name: "Juguetes",
    productCount: 12,
    featured: true,
  },
  {
    id: "libros",
    slug: "libros",
    name: "Libros",
    productCount: 16,
    featured: false,
  },
  {
    id: "mascotas",
    slug: "mascotas",
    name: "Mascotas",
    productCount: 9,
    featured: false,
  },
  {
    id: "jardin",
    slug: "jardin",
    name: "Jardín",
    productCount: 11,
    featured: false,
  },
  {
    id: "cocina",
    slug: "cocina",
    name: "Cocina",
    productCount: 27,
    featured: false,
  },
  {
    id: "audio",
    slug: "audio",
    name: "Audio",
    productCount: 19,
    featured: false,
  },
  {
    id: "accesorios",
    slug: "accesorios",
    name: "Accesorios",
    productCount: 23,
    featured: false,
  },
];
