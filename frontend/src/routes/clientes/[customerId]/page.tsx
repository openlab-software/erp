import { AddressesPanel } from "@/features/addresses";
import { CustomerDetailPage } from "@/features/customers";
import { useParams } from "@modern-js/runtime/router";

export default function ClienteDetalhe() {
  const { customerId = "" } = useParams<{ customerId: string }>();
  return (
    <CustomerDetailPage
      customerId={customerId}
      addresses={<AddressesPanel customerId={customerId} />}
    />
  );
}
