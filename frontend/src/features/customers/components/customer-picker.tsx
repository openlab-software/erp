import { TypeaheadPicker } from "@/components/document/typeahead-picker";
import { formatDocument } from "@/lib/format";
import { useCustomerSearch } from "../queries";
import type { Customer } from "../types";

interface CustomerPickerProps {
  id?: string;
  /** Selected customer id, or "". */
  value: string;
  /** Name to show for the selected id. */
  label?: string;
  onSelect: (customer: Customer) => void;
  onClear: () => void;
  disabled?: boolean;
  invalid?: boolean;
}

/** Type-ahead customer selector (name, document or e-mail), backed by customer-service. */
export function CustomerPicker({
  id,
  value,
  label,
  onSelect,
  onClear,
  disabled,
  invalid,
}: CustomerPickerProps) {
  return (
    <TypeaheadPicker<Customer>
      id={id}
      value={value}
      selectedLabel={label}
      placeholder="Buscar cliente por nome ou documento…"
      useOptions={useCustomerSearch}
      getId={(c) => c.customer_id}
      renderOption={(c) => (
        <>
          <span>{c.name}</span>
          <span className="mono" style={{ color: "var(--ink-3)" }}>
            {formatDocument(c.document)}
          </span>
        </>
      )}
      onSelect={onSelect}
      onClear={onClear}
      disabled={disabled}
      invalid={invalid}
    />
  );
}
