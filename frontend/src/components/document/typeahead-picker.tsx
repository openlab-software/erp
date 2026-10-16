import { Icon } from "@/components/icon";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import {
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { type ReactNode, useState } from "react";

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

interface TypeaheadPickerProps<T> {
  id?: string;
  /** Selected id, or "" when nothing is selected. */
  value: string;
  /** What to show for the selected id (the id itself when unknown). */
  selectedLabel?: string;
  placeholder: string;
  /** Search hook, called with the debounced term (empty until the user types). */
  useOptions: (term: string) => { data: T[] | undefined; isFetching: boolean };
  getId: (option: T) => string;
  renderOption: (option: T) => ReactNode;
  onSelect: (option: T) => void;
  onClear: () => void;
  disabled?: boolean;
  invalid?: boolean;
}

/** Type-ahead selector: a text box that lists the matches of a server-side search and holds the chosen one. */
export function TypeaheadPicker<T>({
  id,
  value,
  selectedLabel,
  placeholder,
  useOptions,
  getId,
  renderOption,
  onSelect,
  onClear,
  disabled,
  invalid,
}: TypeaheadPickerProps<T>) {
  const [text, setText] = useState("");
  const [open, setOpen] = useState(false);
  const term = useDebouncedValue(text.trim());
  const options = useOptions(term);

  if (value && !open) {
    return (
      <InputGroup>
        <InputGroupInput
          id={id}
          readOnly
          value={selectedLabel ?? value}
          aria-invalid={invalid ? true : undefined}
        />
        {!disabled && (
          <InputGroupAddon align="inline-end">
            <button
              type="button"
              aria-label="Trocar seleção"
              onClick={() => {
                onClear();
                setText("");
              }}
              style={{ background: "none", border: 0, cursor: "pointer" }}
            >
              <Icon name="x" size={12} />
            </button>
          </InputGroupAddon>
        )}
      </InputGroup>
    );
  }

  const list = options.data ?? [];
  return (
    <Wrap>
      <Input
        id={id}
        autoComplete="off"
        placeholder={placeholder}
        value={text}
        disabled={disabled}
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
          {list.length === 0 && (
            <Option as="li" style={{ color: "var(--ink-3)", cursor: "default" }}>
              {options.isFetching ? "Buscando…" : "Nada encontrado"}
            </Option>
          )}
          {list.map((option) => (
            <Option
              key={getId(option)}
              // mousedown (not click) so it fires before the input's blur closes the list.
              onMouseDown={(e) => {
                e.preventDefault();
                onSelect(option);
                setOpen(false);
              }}
            >
              {renderOption(option)}
            </Option>
          ))}
        </List>
      )}
    </Wrap>
  );
}
