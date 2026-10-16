import { SalesOrderPage } from "@/features/orders";
import { useParams } from "@modern-js/runtime/router";

export default function PedidoDeVenda() {
  const { orderId = "" } = useParams<{ orderId: string }>();
  return <SalesOrderPage orderId={orderId} />;
}
