import styled from "@emotion/styled";
import { forwardRef } from "react";

interface FiledsetProps
  extends React.FieldsetHTMLAttributes<HTMLFieldSetElement> {
  legenda?: string;
}

const Filedset: React.ForwardRefRenderFunction<
  HTMLFieldSetElement,
  FiledsetProps
> = (props, ref) => {
  const { legenda, ...fieldsetProps } = props;

  return (
    <Fieldset ref={ref} {...fieldsetProps}>
      {!!legenda && <legend className="font-bold">{legenda}</legend>}
      {props.children}
    </Fieldset>
  );
};

const Fieldset = styled.fieldset`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding-top: 1rem;
  padding-bottom: 1rem;
`;

export default forwardRef(Filedset);
