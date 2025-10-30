package product

import "github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"

type ProductService interface {
	Create(p *CreateProductPayload) (*Product, error)
	GetProducts(filter *GetProductsFilter) []*Product
	Delete(id ProductID) error
	GetById(id ProductID) *Product
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
