import { ModuleGate } from "@/features/modules";
import { PickingPage } from "./pages/picking-page";
import { PickingsPage } from "./pages/pickings-page";

// Public API of the pickings feature (stock transfers of the inventory module): routes import only from here.
export const StockTransfersPage = () => (
  <ModuleGate module="inventory">
    <PickingsPage />
  </ModuleGate>
);

export const StockTransferPage = ({ pickingId }: { pickingId?: string }) => (
  <ModuleGate module="inventory">
    <PickingPage pickingId={pickingId} />
  </ModuleGate>
);
