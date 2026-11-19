export { AddToCartButton } from "@/features/cart/components/add-to-cart-button";
export { Breadcrumbs } from "./components/breadcrumbs";
export { ProductCard } from "./components/product-card";
export { ProductDetails } from "./components/product-details";
export { ProductGallery } from "./components/product-gallery";
export { ProductInfo } from "./components/product-info";
export { RatingStars } from "./components/rating-stars";
export { RelatedProducts } from "./components/related-products";
export { SectionHeader } from "./components/section-header";
export { formatPrice } from "./helpers/format-price";
export { ProductDetailPage } from "./pages/product-detail-page";
export {
  findProductBySlug,
  getCategories,
  getFeaturedProducts,
  getProductBySlug,
  getProducts,
  getRelatedProducts,
} from "./services/catalog.service";
export type { Category, Product, ProductDetail, ProductSpec } from "./types";
