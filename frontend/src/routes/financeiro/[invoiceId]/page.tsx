import { AccountingInvoicePage } from "@/features/invoices";
import { useParams } from "@modern-js/runtime/router";

export default function Fatura() {
  const { invoiceId = "" } = useParams<{ invoiceId: string }>();
  return <AccountingInvoicePage invoiceId={invoiceId} />;
}
