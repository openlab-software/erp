import styled from "@emotion/styled";
import React from "react";

import { FormProvider, useForm } from "react-hook-form";

interface FormProps extends React.FormHTMLAttributes<HTMLFormElement> {
  form?: ReturnType<typeof useForm<any>>;
}

export default function Form(props: FormProps) {
  const { form, ...formProps } = props;

  const handleBlur: React.FocusEventHandler<HTMLFormElement> = (event) => {
    formProps.onBlur?.(event);
  };

  const handleReset: React.FormEventHandler<HTMLFormElement> = (event) => {
    formProps.onReset?.(event);
  };

  const handleSubmit: React.FormEventHandler<HTMLFormElement> = (event) => {
    formProps.onSubmit?.(event);
  };

  console.log({ form });

  return (
    <StyledForm
      {...formProps}
      onBlur={handleBlur}
      onReset={handleReset}
      onSubmit={handleSubmit}
    >
      {!!form ? (
        <FormProvider {...form}>{props.children}</FormProvider>
      ) : (
        props.children
      )}
    </StyledForm>
  );
}

const StyledForm = styled.form`
  display: grid;
  gap: 0.5rem;
`;
