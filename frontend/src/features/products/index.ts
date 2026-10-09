// Public API of the products feature: routes and other features import only from here.
export { ProductsPage } from "./pages/products-page";
export { ProductDetailPage } from "./pages/product-detail-page";
export type { ExtraTab } from "./pages/product-detail-page";
export { ProductPicker } from "./components/product-picker";
export { useProduct, useProductsById } from "./queries";
export type { Product } from "./types";
export { listProducts } from "./api";
export { productKeys } from "./queries";
export { STATUS_LABEL } from "./labels";
