import { FSelect } from "@/components/ui";
import { Field, FieldError, FieldLabel, Input } from "@openlab-ui/react";
import type { ComponentProps, ReactNode } from "react";

interface FieldShellProps {
  id: string;
  label: string;
  error?: string;
  hint?: ReactNode;
  children: ReactNode;
}

export function FieldShell({ id, label, error, hint, children }: FieldShellProps) {
  return (
    <Field>
      <FieldLabel htmlFor={id}>{label}</FieldLabel>
      {children}
      {hint && (
        <span style={{ fontSize: 11, color: "var(--ink-4)" }}>{hint}</span>
      )}
      <FieldError errors={error ? [{ message: error }] : []} />
    </Field>
  );
}

type InputProps = ComponentProps<typeof Input>;

interface TextFieldProps extends Omit<InputProps, "id"> {
  id: string;
  label: string;
  error?: string;
  hint?: ReactNode;
}

/** Label + input + error line; spread `register("name")` into the props. */
export function TextField({ id, label, error, hint, ...input }: TextFieldProps) {
  return (
    <FieldShell id={id} label={label} error={error} hint={hint}>
      <Input id={id} aria-invalid={error ? true : undefined} {...input} />
    </FieldShell>
  );
}

interface SelectFieldProps extends ComponentProps<typeof FSelect> {
  id: string;
  label: string;
  error?: string;
  hint?: ReactNode;
  options: { value: string; label: string }[];
  /** Label of the empty option; omit to make the field required-looking. */
  placeholder?: string;
}

/** Native select styled like the rest of the app; spread `register("name")` into the props. */
export function SelectField({
  id,
  label,
  error,
  hint,
  options,
  placeholder,
  ...select
}: SelectFieldProps) {
  return (
    <FieldShell id={id} label={label} error={error} hint={hint}>
      <FSelect id={id} {...select}>
        {placeholder !== undefined && <option value="">{placeholder}</option>}
        {options.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </FSelect>
    </FieldShell>
  );
}
