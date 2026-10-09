import { SelectField, TextField } from "@/components/resource/form-fields";
import {
  type FormFieldsProps,
  ResourceFormDialog,
} from "@/components/resource/resource-form-dialog";
import { useBrandOptions } from "@/features/brands";
import { useCategoryOptions } from "@/features/categories";
import { useSupplierOptions } from "@/features/suppliers";
import { useUnitOptions } from "@/features/units";
import { TYPE_LABEL } from "../labels";
import { useCreateProduct, useUpdateProduct } from "../queries";
import { type ProductFormValues, productSchema } from "../schemas";
import type { Product } from "../types";

function ProductFields({
  register,
  errors,
  editing,
}: FormFieldsProps<ProductFormValues> & { editing: boolean }) {
  const categories = useCategoryOptions();
  const brands = useBrandOptions();
  const suppliers = useSupplierOptions();
  const units = useUnitOptions();
  return (
    <>
      <TextField
        id="product-description"
        label="Descrição"
        autoFocus
        error={errors.description?.message}
        {...register("description")}
      />
      <TextField
        id="product-short-description"
        label="Descrição curta"
        error={errors.short_description?.message}
        {...register("short_description")}
      />
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
        {/* Type is immutable after creation; editing keeps the default value. */}
        {!editing && (
          <SelectField
            id="product-type"
            label="Tipo"
            options={Object.entries(TYPE_LABEL).map(([value, label]) => ({
              value,
              label,
            }))}
            error={errors.type?.message}
            {...register("type")}
          />
        )}
        <SelectField
          id="product-unit"
          label="Unidade de medida"
          placeholder="Selecione…"
          options={(units.data ?? []).map((u) => ({
            value: u.unit_of_measure_id,
            label: `${u.code} — ${u.description}`,
          }))}
          error={errors.unit_of_measure_id?.message}
          {...register("unit_of_measure_id")}
        />
      </div>
      <SelectField
        id="product-category"
        label="Categoria"
        placeholder="Selecione…"
        options={(categories.data ?? []).map((c) => ({
          value: c.category_id,
          label: c.description,
        }))}
        error={errors.category_id?.message}
        {...register("category_id")}
      />
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
        <SelectField
          id="product-brand"
          label="Marca"
          placeholder="— Sem marca —"
          options={(brands.data ?? []).map((b) => ({
            value: b.brand_id,
            label: b.description,
          }))}
          error={errors.brand_id?.message}
          {...register("brand_id")}
        />
        <SelectField
          id="product-supplier"
          label="Fornecedor padrão"
          placeholder="— Sem fornecedor —"
          options={(suppliers.data ?? []).map((s) => ({
            value: s.supplier_id,
            label: s.name,
          }))}
          error={errors.default_supplier_id?.message}
          {...register("default_supplier_id")}
        />
      </div>
    </>
  );
}

interface ProductFormDialogProps {
  open: boolean;
  /** Product being edited; omit to create. Remount with a new `key` to reset. */
  product?: Product;
  onClose: () => void;
  onSaved: (message: string) => void;
}

export function ProductFormDialog({
  open,
  product,
  onClose,
  onSaved,
}: ProductFormDialogProps) {
  return (
    <ResourceFormDialog<Product, ProductFormValues>
      open={open}
      item={product}
      noun="produto"
      schema={productSchema}
      defaults={(p) => ({
        description: p?.description ?? "",
        short_description: p?.short_description ?? "",
        type: p?.type ?? "GOOD",
        unit_of_measure_id: p?.unit_of_measure.unit_of_measure_id ?? "",
        category_id: p?.category.category_id ?? "",
        brand_id: p?.brand?.brand_id ?? "",
        default_supplier_id: p?.default_supplier_id ?? "",
      })}
      getId={(p) => p.product_id}
      useCreate={useCreateProduct}
      useUpdate={useUpdateProduct}
      renderFields={(props) => <ProductFields {...props} editing={!!product} />}
      onClose={onClose}
      onSaved={onSaved}
    />
  );
}
