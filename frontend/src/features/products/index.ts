// Public API of the products feature: routes and other features import only from here.
export { ProductsPage } from "./pages/products-page";
export { ProductDetailPage } from "./pages/product-detail-page";
export type { ExtraTab } from "./pages/product-detail-page";
export { ProductPicker } from "./components/product-picker";
export { useProduct, useProductSuggestions, useProductsById } from "./queries";
export type { Product } from "./types";
export { getProduct, listProducts } from "./api";
export { productKeys } from "./queries";
export { STATUS_LABEL } from "./labels";
