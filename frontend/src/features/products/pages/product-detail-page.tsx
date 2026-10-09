import { Icon } from "@/components/icon";
import {
  Card,
  CardBody,
  CardHead,
  Empty,
  Kv,
  Page,
  PageActions,
  PageHead,
  ProdThumbLg,
  SectionLabel,
  Status,
  Subtitle,
  Tab,
  Tabs,
} from "@/components/ui";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
import { useToast } from "@/contexts/toast-context";
import { fmtBRL } from "@/data";
import { formatDateTime } from "@/lib/format";
import { errorMessage } from "@/lib/query";
import { useNavigate } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { type ReactNode, useState } from "react";
import { AttributesPanel } from "../components/attributes-panel";
import { BarcodesPanel } from "../components/barcodes-panel";
import { ImagesPanel } from "../components/images-panel";
import { PricePanel, margin } from "../components/price-panel";
import { ProductFormDialog } from "../components/product-form-dialog";
import { NEXT_STATUS, STATUS_LABEL, TYPE_LABEL } from "../labels";
import {
  useChangeProductStatus,
  useDeleteProduct,
  useProduct,
} from "../queries";
import type { Product } from "../types";

const DetailGrid = styled.div`
  display: grid;
  grid-template-columns: 260px 1fr 1fr;
  gap: 20px;
`;

const ThumbImage = styled.img`
  display: block;
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border: 1px solid var(--line);
  border-radius: var(--radius);
`;

const ThumbCaption = styled.div`
  margin-top: 10px;
  font-size: 11px;
  color: var(--ink-3);
  text-align: center;
`;

export interface ExtraTab {
  id: string;
  label: string;
  /** Receives the product id; rendered when the tab is active. */
  render: (productId: string) => ReactNode;
}

interface ProductDetailPageProps {
  productId: string;
  /** Tabs owned by other features (stock position, movements…), shown after "Geral". */
  extraTabs?: ExtraTab[];
}

export function ProductDetailPage({ productId, extraTabs = [] }: ProductDetailPageProps) {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const query = useProduct(productId);
  const changeStatus = useChangeProductStatus();
  const [tab, setTab] = useState("geral");
  const [form, setForm] = useState({ open: false, key: 0 });
  const [deleting, setDeleting] = useState<Product | null>(null);

  const product = query.data;

  const back = (
    <div style={{ marginBottom: 14 }}>
      <Button
        variant="ghost"
        onClick={() => navigate("/produtos")}
        style={{ marginLeft: -10, color: "var(--ink-3)" }}
      >
        ← Voltar para Produtos
      </Button>
    </div>
  );

  if (!product) {
    return (
      <Page>
        {back}
        <Empty>
          {query.isError
            ? errorMessage(query.error, "Erro ao carregar o produto")
            : "Carregando…"}
        </Empty>
      </Page>
    );
  }

  const tabs = [
    { id: "geral", label: "Geral" },
    ...extraTabs.map(({ id, label }) => ({ id, label })),
    { id: "precos", label: "Preços" },
    { id: "codigos", label: `Códigos de barras (${product.barcodes.length})` },
    { id: "imagens", label: `Imagens (${product.images.length})` },
    { id: "atributos", label: `Atributos (${product.attributes.length})` },
  ];
  const status = STATUS_LABEL[product.status];
  const primaryImage =
    product.images.find((i) => i.is_primary) ?? product.images[0];
  const extra = extraTabs.find((t) => t.id === tab);

  return (
    <Page>
      {back}

      <PageHead>
        <div>
          <SectionLabel style={{ fontFamily: "var(--font-mono)" }}>
            {product.product_id}
          </SectionLabel>
          <h1>{product.description}</h1>
          <Subtitle>
            {product.category.description}
            {product.brand && ` · ${product.brand.description}`} ·{" "}
            {TYPE_LABEL[product.type]} ·{" "}
            <Status variant={status.variant || undefined}>{status.label}</Status>
          </Subtitle>
        </div>
        <PageActions>
          {NEXT_STATUS[product.status].map((next) => (
            <Button
              key={next.to}
              variant="ghost"
              disabled={changeStatus.isPending}
              onClick={() =>
                changeStatus.mutate(
                  { id: product.product_id, status: next.to },
                  {
                    onSuccess: () => showToast(`Produto ${STATUS_LABEL[next.to].label.toLowerCase()}`),
                    onError: (e) => showToast(errorMessage(e, "Erro ao alterar o status")),
                  },
                )
              }
            >
              {next.label}
            </Button>
          ))}
          <Button variant="ghost" onClick={() => setForm((f) => ({ open: true, key: f.key + 1 }))}>
            <Icon name="edit" size={13} /> Editar
          </Button>
          <Button variant="ghost" onClick={() => setDeleting(product)}>
            <Icon name="trash" size={13} /> Excluir
          </Button>
        </PageActions>
      </PageHead>

      <Tabs>
        {tabs.map(({ id, label }) => (
          <Tab key={id} $active={tab === id} onClick={() => setTab(id)}>
            {label}
          </Tab>
        ))}
      </Tabs>

      {tab === "geral" && (
        <DetailGrid>
          <div>
            {primaryImage ? (
              <ThumbImage src={primaryImage.url} alt={product.description} />
            ) : (
              <>
                <ProdThumbLg />
                <ThumbCaption>Sem imagem cadastrada</ThumbCaption>
              </>
            )}
          </div>

          <Card>
            <CardHead>
              <h3>Identificação</h3>
            </CardHead>
            <CardBody>
              <Kv>
                <dt>Código</dt>
                <dd className="mono">{product.product_id}</dd>
                <dt>Descrição</dt>
                <dd>{product.description}</dd>
                <dt>Descrição curta</dt>
                <dd>{product.short_description}</dd>
                <dt>Tipo</dt>
                <dd>{TYPE_LABEL[product.type]}</dd>
                <dt>Categoria</dt>
                <dd>{product.category.description}</dd>
                <dt>Marca</dt>
                <dd>{product.brand?.description ?? "—"}</dd>
                <dt>Unidade</dt>
                <dd className="mono">{product.unit_of_measure.code}</dd>
                <dt>Fornecedor</dt>
                <dd className="mono">{product.default_supplier_id ?? "—"}</dd>
              </Kv>
            </CardBody>
          </Card>

          <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
            <Card>
              <CardHead>
                <h3>Preços e custos</h3>
              </CardHead>
              <CardBody>
                <Kv>
                  <dt>Custo</dt>
                  <dd className="mono">{fmtBRL(product.cost_price)}</dd>
                  <dt>Preço de venda</dt>
                  <dd className="mono">{fmtBRL(product.sale_price)}</dd>
                  <dt>Margem</dt>
                  <dd className="mono" style={{ color: "var(--pos)" }}>
                    {margin(product)}
                  </dd>
                </Kv>
              </CardBody>
            </Card>
            <Card>
              <CardHead>
                <h3>Registro</h3>
              </CardHead>
              <CardBody>
                <Kv>
                  <dt>Criado em</dt>
                  <dd className="mono">{formatDateTime(product.created_at)}</dd>
                  <dt>Atualizado em</dt>
                  <dd className="mono">{formatDateTime(product.updated_at)}</dd>
                </Kv>
              </CardBody>
            </Card>
          </div>
        </DetailGrid>
      )}

      {extra?.render(product.product_id)}
      {tab === "precos" && <PricePanel product={product} />}
      {tab === "codigos" && <BarcodesPanel product={product} />}
      {tab === "imagens" && <ImagesPanel product={product} />}
      {tab === "atributos" && <AttributesPanel product={product} />}

      <ProductFormDialog
        key={form.key}
        open={form.open}
        product={product}
        onClose={() => setForm((f) => ({ ...f, open: false }))}
        onSaved={(message) => {
          setForm((f) => ({ ...f, open: false }));
          showToast(message);
        }}
      />

      <ResourceDeleteDialog<Product>
        item={deleting}
        noun="produto"
        getId={(p) => p.product_id}
        getLabel={(p) => p.description}
        hint="Produtos com saldo em estoque podem não ser excluídos."
        useRemove={useDeleteProduct}
        onClose={() => setDeleting(null)}
        onDeleted={() => navigate("/produtos")}
      />
    </Page>
  );
}
