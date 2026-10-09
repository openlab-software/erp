import { Icon } from "@/components/icon";
import { TextField } from "@/components/resource/form-fields";
import { Card, CardHead, Empty, Status } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, FieldError } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { useForm } from "react-hook-form";
import { useAddImage, useDeleteImage, useSetPrimaryImage } from "../queries";
import { type ImageFormValues, imageSchema } from "../schemas";
import type { Product } from "../types";

const Grid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
  padding: 18px;
`;

const Tile = styled.div`
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--surface);
  img {
    display: block;
    width: 100%;
    height: 140px;
    object-fit: cover;
    background: var(--surface-sunken);
  }
`;

const TileFoot = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 6px 8px;
`;

export function ImagesPanel({ product }: { product: Product }) {
  const { showToast } = useToast();
  const add = useAddImage();
  const setPrimary = useSetPrimaryImage();
  const remove = useDeleteImage();
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<ImageFormValues>({
    resolver: zodResolver(imageSchema),
    defaultValues: { url: "" },
  });

  const submit = handleSubmit(({ url }) =>
    add.mutate(
      { id: product.product_id, url },
      {
        onSuccess: () => {
          reset();
          showToast("Imagem adicionada");
        },
        onError: (e) =>
          setError("root.server", { message: errorMessage(e, "Erro ao adicionar imagem") }),
      },
    ),
  );
  const fail = (fallback: string) => (e: Error) => showToast(errorMessage(e, fallback));

  return (
    <Card>
      <CardHead>
        <h3>Imagens</h3>
      </CardHead>
      <form
        onSubmit={submit}
        style={{
          display: "grid",
          gridTemplateColumns: "1fr auto",
          gap: 12,
          alignItems: "start",
          padding: 18,
          borderBottom: "1px solid var(--line)",
        }}
      >
        <TextField
          id="image-url"
          label="URL da imagem"
          placeholder="https://…"
          error={errors.url?.message}
          {...register("url")}
        />
        <Button type="submit" disabled={add.isPending} style={{ marginTop: 22 }}>
          <Icon name="plus" size={12} /> {add.isPending ? "Adicionando…" : "Adicionar"}
        </Button>
        {errors.root?.server && (
          <div style={{ gridColumn: "1 / -1" }}>
            <FieldError errors={[{ message: errors.root.server.message }]} />
          </div>
        )}
      </form>

      {product.images.length === 0 ? (
        <Empty>Nenhuma imagem cadastrada.</Empty>
      ) : (
        <Grid>
          {product.images.map((image) => (
            <Tile key={image.image_id}>
              <img src={image.url} alt={product.description} loading="lazy" />
              <TileFoot>
                {image.is_primary ? (
                  <Status variant="pos">Principal</Status>
                ) : (
                  <Button
                    variant="ghost"
                    size="xs"
                    disabled={setPrimary.isPending}
                    onClick={() =>
                      setPrimary.mutate(
                        { id: product.product_id, imageId: image.image_id },
                        {
                          onSuccess: () => showToast("Imagem principal atualizada"),
                          onError: fail("Erro ao definir imagem principal"),
                        },
                      )
                    }
                  >
                    Tornar principal
                  </Button>
                )}
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label="Excluir imagem"
                  disabled={remove.isPending}
                  onClick={() =>
                    remove.mutate(
                      { id: product.product_id, imageId: image.image_id },
                      {
                        onSuccess: () => showToast("Imagem removida"),
                        onError: fail("Erro ao remover imagem"),
                      },
                    )
                  }
                >
                  <Icon name="trash" size={13} />
                </Button>
              </TileFoot>
            </Tile>
          ))}
        </Grid>
      )}
    </Card>
  );
}
