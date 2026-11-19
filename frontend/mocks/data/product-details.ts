import type { Product, ProductDetail } from "@/features/product/types";

import { products } from "./products";

/** Detail copy measured from the `Product` / `Details` frames for the target product. */
const featuredDetail: Omit<ProductDetail, keyof Product> = {
  sku: "0042",
  originalPrice: 32500,
  installmentCount: 6,
  stock: 24,
  fulfillment: "24/48 hs",
  description: [
    "Los Auriculares inalámbricos NC-450 combinan cancelación activa de ruido con drivers de 40 mm para un sonido claro en cualquier entorno. El modo transparencia te deja escuchar lo que pasa a tu alrededor sin sacarte los auriculares.",
    "El estuche de carga brinda hasta 40 horas de reproducción y una carga rápida de 10 minutos rinde 4 horas de música. Son plegables, pesan 250 g y vienen con funda rígida y cable USB-C.",
  ],
  highlights: [
    "Cancelación activa de ruido con 3 modos",
    "Bluetooth 5.3 con conexión simultánea a 2 dispositivos",
    "Micrófono integrado con reducción de eco",
  ],
  specs: [
    { label: "Marca", value: "SoundCore" },
    { label: "Modelo", value: "NC-450" },
    { label: "Conectividad", value: "Bluetooth 5.3 · USB-C" },
    { label: "Autonomía", value: "40 horas con cancelación activa" },
    { label: "Peso", value: "250 g" },
    { label: "Garantía", value: "12 meses oficial" },
  ],
  rating: 4.8,
  reviewCount: 124,
  soldCount: 200,
  galleryCount: 4,
};

/**
 * Detail for every seeded product.
 *
 * The wireframe only specifies the target product, so the rest reuse the same
 * structure with neutral copy instead of inventing screen-specific content.
 */
export const productDetails: ProductDetail[] = products.map((product, index) => {
  const sku = String(index + 42).padStart(4, "0");

  if (product.slug === "auriculares-inalambricos") {
    return { ...product, ...featuredDetail };
  }

  return {
    ...product,
    sku,
    originalPrice: null,
    installmentCount: 6,
    stock: 12,
    fulfillment: "24/48 hs",
    description: [
      `${product.name} forma parte de la selección de ${product.categoryName} de La Tiendita. Revisá las especificaciones para ver el detalle completo antes de comprarlo.`,
      "Todos los productos se envían a todo el país en 24-48 hs y tienen 30 días de devolución sin cargo.",
    ],
    highlights: [
      "Garantía oficial de 12 meses",
      "Envío gratis en compras mayores a $ 20.000",
      "Devoluciones sin cargo dentro de los 30 días",
    ],
    specs: [
      { label: "Marca", value: "La Tiendita" },
      { label: "Modelo", value: `REF-${sku}` },
      { label: "Categoría", value: product.categoryName },
      { label: "Presentación", value: "Unidad" },
      { label: "Origen", value: "Argentina" },
      { label: "Garantía", value: "12 meses oficial" },
    ],
    rating: 0,
    reviewCount: 0,
    soldCount: 0,
    galleryCount: 4,
  };
});
