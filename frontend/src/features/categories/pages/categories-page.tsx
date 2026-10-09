import { SelectField, TextField } from "@/components/resource/form-fields";
import type { FormFieldsProps } from "@/components/resource/resource-form-dialog";
import { ResourcePage } from "@/components/resource/resource-page";
import { formatDateTime } from "@/lib/format";
import { categories, useCategoryOptions } from "../queries";
import { type CategoryFormValues, categorySchema } from "../schemas";
import type { Category } from "../types";

function CategoryFields({ register, errors }: FormFieldsProps<CategoryFormValues>) {
  const options = useCategoryOptions();
  return (
    <>
      <TextField
        id="category-description"
        label="Descrição"
        autoFocus
        error={errors.description?.message}
        {...register("description")}
      />
      <SelectField
        id="category-parent"
        label="Categoria pai"
        placeholder="— Nenhuma (categoria raiz) —"
        options={(options.data ?? []).map((c) => ({
          value: c.category_id,
          label: c.description,
        }))}
        error={errors.parent_category_id?.message}
        {...register("parent_category_id")}
      />
    </>
  );
}

export function CategoriesPage() {
  const options = useCategoryOptions();
  const parentName = (id: string | null) =>
    id ? (options.data?.find((c) => c.category_id === id)?.description ?? id) : "—";

  return (
    <ResourcePage<Category, CategoryFormValues>
      eyebrow="CADASTRO · CATEGORIAS"
      title="Categorias"
      noun="categoria"
      feminine
      searchPlaceholder="Buscar por descrição…"
      columns={[
        { header: "Código", className: "id", cell: (c) => c.category_id },
        { header: "Descrição", cell: (c) => <b style={{ fontWeight: 500 }}>{c.description}</b> },
        { header: "Categoria pai", muted: true, cell: (c) => parentName(c.parent_category_id) },
        { header: "Criada em", muted: true, cell: (c) => formatDateTime(c.created_at) },
      ]}
      getId={(c) => c.category_id}
      getLabel={(c) => c.description}
      useList={categories.useList}
      useCreate={categories.useCreate}
      useUpdate={categories.useUpdate}
      useRemove={categories.useRemove}
      schema={categorySchema}
      defaults={(c) => ({
        description: c?.description ?? "",
        parent_category_id: c?.parent_category_id ?? "",
      })}
      renderFields={(props) => <CategoryFields {...props} />}
      deleteHint="Categorias com produtos ou subcategorias não podem ser excluídas."
    />
  );
}
