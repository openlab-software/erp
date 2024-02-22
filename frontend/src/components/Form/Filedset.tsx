import { forwardRef } from "react";
import { twMerge } from "tailwind-merge";

interface FiledsetProps
  extends React.FieldsetHTMLAttributes<HTMLFieldSetElement> {
  legenda?: string;
}

const Filedset: React.ForwardRefRenderFunction<
  HTMLFieldSetElement,
  FiledsetProps
> = (props, ref) => {
  const { legenda, className, ...fieldsetProps } = props;

  return (
    <fieldset
      ref={ref}
      className={twMerge("flex flex-col gap-2 py-4", className)}
      {...fieldsetProps}
    >
      {!!legenda && <legend className="font-bold">{legenda}</legend>}
      {props.children}
    </fieldset>
  );
};

export default forwardRef(Filedset);
