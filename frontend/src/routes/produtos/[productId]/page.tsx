import { ProductDetailPage } from "@/features/products";
import { productStockTabs } from "@/features/stock";
import { useParams } from "@modern-js/runtime/router";

export default function ProdutoDetalhe() {
  const { productId = "" } = useParams<{ productId: string }>();
  return (
    <ProductDetailPage productId={productId} extraTabs={productStockTabs} />
  );
}
