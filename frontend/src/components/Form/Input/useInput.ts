import { useFormContext } from "react-hook-form";
import { InputProps } from "./Input";

type UseInputOptions = InputProps;
type UseInputReturn = InputProps & {
  isTouched: boolean;
};

const useInput = (props: UseInputOptions): UseInputReturn => {
  return useInputWithReactHookFormContext(props);
};

const useInputWithReactHookFormContext: typeof useInput = (props) => {
  const form = useFormContext();

  const isTouched = !!form.formState.touchedFields[props.name];

  return { ...props, ...form.register(props.name), isTouched };
};

export default useInput;
