"use client";

import { Button as BlueprintButton, ButtonProps } from "@blueprintjs/core";
import styled from "@emotion/styled";
import { forwardRef } from "react";

const Button: React.ForwardRefRenderFunction<HTMLButtonElement, ButtonProps> = (
  { className, children, ...props },
  ref
) => {
  return (
    <StyledButton ref={ref} {...props}>
      {children}
    </StyledButton>
  );
};

const StyledButton = styled(BlueprintButton)`
  background-color: #f6f7f9;
  color: #1c2127;

  :hover {
    background-color: #edeff2;
  }
`;

export default forwardRef(Button);
