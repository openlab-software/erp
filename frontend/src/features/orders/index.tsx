import { ModuleGate } from "@/features/modules";
import { purchaseKind, salesKind } from "./kinds";
import { OrderPage } from "./pages/order-page";
import { OrdersPage } from "./pages/orders-page";
import { createOrderHooks } from "./queries";

const salesHooks = createOrderHooks(salesKind);
const purchaseHooks = createOrderHooks(purchaseKind);

// Public API of the orders feature: sales and purchase orders share the same screens.
export const SalesOrdersPage = () => (
  <ModuleGate module={salesKind.module}>
    <OrdersPage kind={salesKind} hooks={salesHooks} />
  </ModuleGate>
);

export const SalesOrderPage = ({ orderId }: { orderId?: string }) => (
  <ModuleGate module={salesKind.module}>
    <OrderPage kind={salesKind} hooks={salesHooks} orderId={orderId} />
  </ModuleGate>
);

export const PurchaseOrdersPage = () => (
  <ModuleGate module={purchaseKind.module}>
    <OrdersPage kind={purchaseKind} hooks={purchaseHooks} />
  </ModuleGate>
);

export const PurchaseOrderPage = ({ orderId }: { orderId?: string }) => (
  <ModuleGate module={purchaseKind.module}>
    <OrderPage kind={purchaseKind} hooks={purchaseHooks} orderId={orderId} />
  </ModuleGate>
);
