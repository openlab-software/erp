import { errorMessage } from "@/lib/query";
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@openlab-ui/react";
import { useState } from "react";
import { useImportProducts } from "../queries";
import type { ImportReport } from "../types";

interface ImportProductsDialogProps {
  open: boolean;
  onClose: () => void;
}

const HEADER =
  "description,short_description,type,unit_of_measure_code,category_id,brand_id,sale_price,cost_price";

/** Uploads a CSV (same columns as the export) and shows the per-line report. */
export function ImportProductsDialog({ open, onClose }: ImportProductsDialogProps) {
  const importProducts = useImportProducts();
  const [file, setFile] = useState<File | null>(null);
  const [report, setReport] = useState<ImportReport | null>(null);
  const [error, setError] = useState<string | null>(null);

  const close = () => {
    setFile(null);
    setReport(null);
    setError(null);
    importProducts.reset();
    onClose();
  };

  const submit = async () => {
    if (!file) return;
    setError(null);
    importProducts.mutate(await file.text(), {
      onSuccess: setReport,
      onError: (e) => setError(errorMessage(e, "Erro ao importar o arquivo")),
    });
  };

  return (
    <Dialog open={open} onOpenChange={(next) => !next && close()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Importar produtos</DialogTitle>
          <DialogDescription>
            Envie um CSV com o cabeçalho <code className="mono">{HEADER}</code>.
          </DialogDescription>
        </DialogHeader>

        <div style={{ display: "grid", gap: 12, marginBottom: 16 }}>
          <input
            type="file"
            accept=".csv,text/csv"
            onChange={(e) => {
              setFile(e.target.files?.[0] ?? null);
              setReport(null);
            }}
          />
          {error && <div style={{ color: "var(--neg)", fontSize: 12.5 }}>{error}</div>}
          {report && (
            <div style={{ fontSize: 13 }}>
              <b>{report.created}</b> de <b>{report.total}</b> produto
              {report.total === 1 ? "" : "s"} criado
              {report.created === 1 ? "" : "s"}.
              {report.failed.length > 0 && (
                <ul style={{ margin: "8px 0 0", paddingLeft: 18, color: "var(--neg)" }}>
                  {report.failed.map((f) => (
                    <li key={f.line}>
                      Linha {f.line}: {f.error}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="ghost" onClick={close}>
            {report ? "Fechar" : "Cancelar"}
          </Button>
          <Button onClick={submit} disabled={!file || importProducts.isPending}>
            {importProducts.isPending ? "Importando…" : "Importar"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
