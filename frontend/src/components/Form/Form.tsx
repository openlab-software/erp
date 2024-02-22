import { useFormikContext } from "formik";
import { twMerge } from "tailwind-merge";

interface FormProps extends React.FormHTMLAttributes<HTMLFormElement> {}

export default function Form(props: FormProps) {
  const { className = "", ...formProps } = props;
  const { handleBlur, handleReset, handleSubmit } = useFormikContext();

  return (
    <form
      className={twMerge("grid gap-2", className)}
      {...formProps}
      onBlur={handleBlur}
      onReset={handleReset}
      onSubmit={handleSubmit}
    >
      {props.children}
    </form>
  );
}
