import { Icon } from "@/components/icon";
import { Card, CardHead } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, FieldError, Input } from "@openlab-ui/react";
import { useFieldArray, useForm } from "react-hook-form";
import { useReplaceAttributes } from "../queries";
import { type AttributeFormValues, attributeSchema } from "../schemas";
import type { Product } from "../types";

/** Attributes are replaced as a whole (PUT), so the panel edits the full list and saves it at once. */
export function AttributesPanel({ product }: { product: Product }) {
  const { showToast } = useToast();
  const replace = useReplaceAttributes();
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isDirty },
  } = useForm<AttributeFormValues>({
    resolver: zodResolver(attributeSchema),
    defaultValues: { attributes: product.attributes },
  });
  const { fields, append, remove } = useFieldArray({ control, name: "attributes" });

  const submit = handleSubmit((values) =>
    replace.mutate(
      { id: product.product_id, values },
      {
        onSuccess: () => showToast("Atributos salvos"),
        onError: (e) =>
          setError("root.server", { message: errorMessage(e, "Erro ao salvar atributos") }),
      },
    ),
  );

  return (
    <Card>
      <CardHead>
        <h3>Atributos</h3>
        <div style={{ display: "flex", gap: 8 }}>
          <Button
            type="button"
            variant="ghost"
            size="xs"
            onClick={() => append({ name: "", value: "" })}
          >
            <Icon name="plus" size={12} /> Adicionar atributo
          </Button>
          <Button size="xs" onClick={submit} disabled={replace.isPending || !isDirty}>
            {replace.isPending ? "Salvando…" : "Salvar atributos"}
          </Button>
        </div>
      </CardHead>
      <form onSubmit={submit} style={{ padding: 18, display: "grid", gap: 10 }}>
        {fields.length === 0 && (
          <div style={{ color: "var(--ink-3)", fontSize: 13 }}>
            Nenhum atributo (ex.: cor, voltagem, material).
          </div>
        )}
        {fields.map((field, i) => (
          <div
            key={field.id}
            style={{ display: "grid", gridTemplateColumns: "1fr 1fr auto", gap: 10 }}
          >
            <div>
              <Input
                aria-label="Nome do atributo"
                placeholder="Nome"
                aria-invalid={errors.attributes?.[i]?.name ? true : undefined}
                {...register(`attributes.${i}.name`)}
              />
              <FieldError errors={[errors.attributes?.[i]?.name]} />
            </div>
            <div>
              <Input
                aria-label="Valor do atributo"
                placeholder="Valor"
                aria-invalid={errors.attributes?.[i]?.value ? true : undefined}
                {...register(`attributes.${i}.value`)}
              />
              <FieldError errors={[errors.attributes?.[i]?.value]} />
            </div>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              aria-label="Remover atributo"
              onClick={() => remove(i)}
            >
              <Icon name="trash" size={13} />
            </Button>
          </div>
        ))}
        {errors.root?.server && (
          <FieldError errors={[{ message: errors.root.server.message }]} />
        )}
      </form>
    </Card>
  );
}
