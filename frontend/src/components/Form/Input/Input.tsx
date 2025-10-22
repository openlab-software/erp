import {
  FormGroup,
  FormGroupProps,
  InputGroup,
  InputGroupProps,
} from "@blueprintjs/core";

import styled from "@emotion/styled";
import { forwardRef } from "react";
import useInput from "./useInput";

export interface InputProps extends InputGroupProps {
  name: string;
  label?: string;
  formGroup?: FormGroupProps;
}

const Input: React.ForwardRefRenderFunction<HTMLInputElement, InputProps> = (
  props,
  ref
) => {
  const { name, label, isTouched, ...inputProps } = useInput(props);

  console.log({ isTouched });
  const meta: any = {};

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
      <StyledInputGroup
        inputRef={ref}
        {...inputProps}
        isTouched={isTouched}
        // inputClassName={twMerge(
        //   "",
        //   meta.touched ? (meta.error ? "bg-red-50" : "bg-green-50") : undefined
        // )}
      />
    </FormGroup>
  );
};

const StyledInputGroup = styled(InputGroup)<{ isTouched: boolean }>`
  background-color: ${(props) => (props.isTouched ? "red" : "green")};
`;

export default forwardRef(Input);
