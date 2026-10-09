import { Icon } from "@/components/icon";
import { fmtBRL } from "@/data";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import {
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { useState } from "react";
import { useProductSuggestions, useProductsById } from "../queries";

const Wrap = styled.div`
  position: relative;
`;

const List = styled.ul`
  position: absolute;
  z-index: 20;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  margin: 0;
  padding: 4px;
  list-style: none;
  max-height: 240px;
  overflow-y: auto;
  background: var(--surface);
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
`;

const Option = styled.li`
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 10px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  &:hover {
    background: var(--surface-sunken);
  }
`;

interface ProductPickerProps {
  id?: string;
  /** Selected product id, or "" when nothing is selected. */
  value: string;
  onChange: (productId: string) => void;
  invalid?: boolean;
  autoFocus?: boolean;
}

/** Type-ahead product selector backed by catalog-service's `/products/autocomplete`. */
export function ProductPicker({
  id,
  value,
  onChange,
  invalid,
  autoFocus,
}: ProductPickerProps) {
  const [text, setText] = useState("");
  const [open, setOpen] = useState(false);
  const term = useDebouncedValue(text.trim());
  const suggestions = useProductSuggestions(term);
  const selected = useProductsById(value ? [value] : []).get(value);

  if (value && !open) {
    return (
      <InputGroup>
        <InputGroupInput
          id={id}
          readOnly
          value={selected?.description ?? value}
          aria-invalid={invalid ? true : undefined}
        />
        <InputGroupAddon align="inline-end">
          <button
            type="button"
            aria-label="Trocar produto"
            onClick={() => {
              onChange("");
              setText("");
            }}
            style={{ background: "none", border: 0, cursor: "pointer" }}
          >
            <Icon name="x" size={12} />
          </button>
        </InputGroupAddon>
      </InputGroup>
    );
  }

  const options = suggestions.data ?? [];
  return (
    <Wrap>
      <Input
        id={id}
        autoFocus={autoFocus}
        autoComplete="off"
        placeholder="Buscar produto por descrição…"
        value={text}
        aria-invalid={invalid ? true : undefined}
        onChange={(e) => {
          setText(e.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        onBlur={() => setOpen(false)}
      />
      {open && term && (
        <List>
          {options.length === 0 && (
            <Option as="li" style={{ color: "var(--ink-3)", cursor: "default" }}>
              {suggestions.isFetching ? "Buscando…" : "Nenhum produto encontrado"}
            </Option>
          )}
          {options.map((p) => (
            <Option
              key={p.id}
              // mousedown (not click) so it fires before the input's blur closes the list.
              onMouseDown={(e) => {
                e.preventDefault();
                onChange(p.id);
                setOpen(false);
              }}
            >
              <span>{p.description}</span>
              <span className="mono" style={{ color: "var(--ink-3)" }}>
                {fmtBRL(p.sale_price)}
              </span>
            </Option>
          ))}
        </List>
      )}
    </Wrap>
  );
}
