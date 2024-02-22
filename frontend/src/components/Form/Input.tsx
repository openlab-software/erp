import { Label, TextInput, TextInputProps } from "flowbite-react";
import { useField } from "formik";
import { forwardRef } from "react";

interface InputProps extends TextInputProps {
  name: string;
  label?: string;
}

const Input: React.ForwardRefRenderFunction<HTMLInputElement, InputProps> = (
  props,
  ref
) => {
  const { name, label, ...textInputProps } = props;

  const [field, meta] = useField(name);

  return (
    <div>
      <div className="mb-2 block">
        <Label htmlFor={name} value={label} />
      </div>

      <TextInput
        ref={ref}
        {...field}
        {...textInputProps}
        color={
          meta.touched
            ? !!meta.error
              ? "failure"
              : field.value
              ? "success"
              : undefined
            : undefined
        }
        helperText={meta.touched && meta.error && <span>{meta.error}</span>}
      />
    </div>
  );
};

export default forwardRef(Input);
