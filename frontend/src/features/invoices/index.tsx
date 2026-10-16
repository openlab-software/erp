import { ModuleGate } from "@/features/modules";
import { InvoicePage } from "./pages/invoice-page";
import { InvoicesPage } from "./pages/invoices-page";

// Public API of the invoices feature (accounting module): routes import only from here.
export const AccountingInvoicesPage = () => (
  <ModuleGate module="accounting">
    <InvoicesPage />
  </ModuleGate>
);

export const AccountingInvoicePage = ({ invoiceId }: { invoiceId?: string }) => (
  <ModuleGate module="accounting">
    <InvoicePage invoiceId={invoiceId} />
  </ModuleGate>
);
