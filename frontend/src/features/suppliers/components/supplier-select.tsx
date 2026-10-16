import { FSelect } from "@/components/ui";
import { useSupplierOptions } from "../queries";

interface SupplierSelectProps {
  id?: string;
  /** Selected supplier id, or "". */
  value: string;
  /** Name to show when the selected id is not among the loaded suppliers. */
  label?: string;
  onChange: (supplierId: string, name: string) => void;
  disabled?: boolean;
  invalid?: boolean;
}

/** Supplier selector (the registry is small, so a plain select over the first page is enough). */
export function SupplierSelect({ id, value, label, onChange, disabled, invalid }: SupplierSelectProps) {
  const suppliers = useSupplierOptions();
  const options = suppliers.data ?? [];
  const known = options.some((s) => s.supplier_id === value);

  return (
    <FSelect
      id={id}
      value={value}
      disabled={disabled}
      aria-invalid={invalid ? true : undefined}
      onChange={(e) => {
        const chosen = options.find((s) => s.supplier_id === e.target.value);
        onChange(e.target.value, chosen?.name ?? "");
      }}
    >
      <option value="">{suppliers.isPending ? "Carregando…" : "Selecione o fornecedor"}</option>
      {value && !known && <option value={value}>{label || value}</option>}
      {options.map((s) => (
        <option key={s.supplier_id} value={s.supplier_id}>
          {s.name}
        </option>
      ))}
    </FSelect>
  );
}
