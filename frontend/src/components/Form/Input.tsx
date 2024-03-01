import {
  FormGroup,
  FormGroupProps,
  InputGroup,
  InputGroupProps,
} from "@blueprintjs/core";
import { useField } from "formik";
import { forwardRef } from "react";
import { twMerge } from "tailwind-merge";

interface InputProps extends InputGroupProps {
  name: string;
  label?: string;
  formGroup?: FormGroupProps;
}

const Input: React.ForwardRefRenderFunction<HTMLInputElement, InputProps> = (
  props,
  ref
) => {
  const { name, label, ...inputProps } = props;
  const [field, meta] = useField(name);

  return (
    <FormGroup
      helperText={
        meta.touched && meta.error ? (
          <span className="text-red-500">{meta.error}</span>
        ) : (
          <span />
        )
      }
      label={label}
      labelFor={name}
      labelInfo="(required)"
    >
      <InputGroup
        {...field}
        {...inputProps}
        inputClassName={twMerge(
          "",
          meta.touched ? (meta.error ? "bg-red-50" : "bg-green-50") : undefined
        )}
      />
    </FormGroup>
  );
};

export default forwardRef(Input);
