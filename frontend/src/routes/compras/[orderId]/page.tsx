import { PurchaseOrderPage } from "@/features/orders";
import { useParams } from "@modern-js/runtime/router";

export default function OrdemDeCompra() {
  const { orderId = "" } = useParams<{ orderId: string }>();
  return <PurchaseOrderPage orderId={orderId} />;
}
