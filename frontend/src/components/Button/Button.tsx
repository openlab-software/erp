"use client";

import { Button as BlueprintButton, ButtonProps } from "@blueprintjs/core";
import { forwardRef } from "react";
import { twMerge } from "tailwind-merge";

const Button: React.ForwardRefRenderFunction<HTMLButtonElement, ButtonProps> = (
  { className, children, ...props },
  ref
) => {
  return (
    <BlueprintButton
      ref={ref}
      {...props}
      className={twMerge(
        "bg-[#f6f7f9] text-[#1c2127] hover:bg-[#edeff2]",
        className
      )}
    >
      {children}
    </BlueprintButton>
  );
};

export default forwardRef(Button);
