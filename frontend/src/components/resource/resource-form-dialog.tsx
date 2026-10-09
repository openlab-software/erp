import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  FieldError,
} from "@openlab-ui/react";
import type { UseMutationResult } from "@tanstack/react-query";
import type { ReactNode } from "react";
import {
  type Control,
  type DefaultValues,
  type FieldErrors,
  type FieldValues,
  type UseFormRegister,
  type UseFormSetValue,
  useForm,
} from "react-hook-form";
import type { ZodType, ZodTypeDef } from "zod";

export interface FormFieldsProps<V extends FieldValues> {
  register: UseFormRegister<V>;
  errors: FieldErrors<V>;
  control: Control<V>;
  setValue: UseFormSetValue<V>;
}

export type CreateHook<V> = () => UseMutationResult<unknown, Error, V>;
export type UpdateHook<V> = () => UseMutationResult<
  unknown,
  Error,
  { id: string; values: V }
>;

/** "marca" + feminine → { article: "a", novo: "Nova", done: "criada" … } */
export const wording = (noun: string, feminine?: boolean) => ({
  article: feminine ? "a" : "o",
  new: `${feminine ? "Nova" : "Novo"} ${noun}`,
  created: `${cap(noun)} ${feminine ? "criada" : "criado"}`,
  updated: `${cap(noun)} ${feminine ? "atualizada" : "atualizado"}`,
  deleted: feminine ? "excluída" : "excluído",
});

const cap = (text: string) => text.charAt(0).toUpperCase() + text.slice(1);

interface ResourceFormDialogProps<T, V extends FieldValues> {
  open: boolean;
  /** Item being edited; omit to create. */
  item?: T;
  /** "marca", "fornecedor"… (lower case, used in titles and toasts). */
  noun: string;
  /** Grammatical gender of the noun, for "Nova"/"Novo" and "criada"/"criado". */
  feminine?: boolean;
  schema: ZodType<V, ZodTypeDef, V>;
  defaults: (item?: T) => DefaultValues<V>;
  getId: (item: T) => string;
  useCreate: CreateHook<V>;
  useUpdate: UpdateHook<V>;
  renderFields: (props: FormFieldsProps<V>) => ReactNode;
  onClose: () => void;
  onSaved: (message: string) => void;
}

/**
 * Keep this mounted and drive it with `open`; remount with a new `key` to reset the form.
 * Server-side rejections (duplicates, referential rules) show up under the fields.
 */
export function ResourceFormDialog<T, V extends FieldValues>({
  open,
  item,
  noun,
  feminine,
  schema,
  defaults,
  getId,
  useCreate,
  useUpdate,
  renderFields,
  onClose,
  onSaved,
}: ResourceFormDialogProps<T, V>) {
  const {
    register,
    control,
    setValue,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: defaults(item),
  });
  const create = useCreate();
  const update = useUpdate();
  const saving = create.isPending || update.isPending;
  const words = wording(noun, feminine);

  const submit = handleSubmit((values) => {
    const options = {
      onError: (err: Error) =>
        setError("root.server", {
          message: errorMessage(err, `Erro ao salvar ${noun}`),
        }),
    };
    if (item) {
      update.mutate(
        { id: getId(item), values },
        { ...options, onSuccess: () => onSaved(words.updated) },
      );
    } else {
      create.mutate(values, {
        ...options,
        onSuccess: () => onSaved(words.created),
      });
    }
  });

  const serverError = errors.root?.server?.message;

  return (
    <Dialog open={open} onOpenChange={(next) => !next && !saving && onClose()}>
      <DialogContent>
        <form onSubmit={submit}>
          <DialogHeader>
            <DialogTitle>{item ? `Editar ${noun}` : words.new}</DialogTitle>
            <DialogDescription>
              {item
                ? getId(item)
                : `Preencha os dados d${words.article} ${noun}.`}
            </DialogDescription>
          </DialogHeader>
          <div style={{ display: "grid", gap: 14, marginBottom: 16 }}>
            {renderFields({ register, errors, control, setValue })}
            {serverError && <FieldError errors={[{ message: serverError }]} />}
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="ghost"
              onClick={onClose}
              disabled={saving}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando…" : "Salvar"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
