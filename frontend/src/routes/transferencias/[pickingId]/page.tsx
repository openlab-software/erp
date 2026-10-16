import { StockTransferPage } from "@/features/pickings";
import { useParams } from "@modern-js/runtime/router";

export default function Transferencia() {
  const { pickingId = "" } = useParams<{ pickingId: string }>();
  return <StockTransferPage pickingId={pickingId} />;
}
