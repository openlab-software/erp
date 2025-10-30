package product

import (
	"context"

	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
)

type ProductService interface {
	Create(ctx context.Context, p *CreateProductPayload) (*Product, error)
	GetProducts(ctx context.Context, filter *GetProductsFilter) []*Product
	Delete(ctx context.Context, id ProductID) error
	GetById(ctx context.Context, id ProductID) *Product
}

type GetProductsFilter struct {
	Q string
}

type CreateProductPayload struct {
	Description      string
	ShortDescription string
	UnitOfMeasure    string
	CategoryID       category.CategoryID
}
