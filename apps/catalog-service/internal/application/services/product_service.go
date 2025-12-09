package services

import (
	"context"

	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/product"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
)

type ProductServiceImpl struct {
	product.ProductService
	repo product.ProductRepository
	pub  event.Publisher
}

func NewProductService(repo product.ProductRepository, pub event.Publisher) product.ProductService {
	return &ProductServiceImpl{
		repo: repo,
		pub:  pub,
	}
}

func (svc *ProductServiceImpl) Create(ctx context.Context, payload *product.CreateProductPayload) (*product.Product, error) {
	newProduct := product.NewProduct(payload.Description, payload.ShortDescription, payload.UnitOfMeasure, payload.CategoryID)
	if err := svc.repo.Insert(ctx, newProduct); err != nil {
		return nil, err
	}

	eventPayload := product.ProductCreatedPayload{
		ID:          newProduct.ProductID,
		Description: newProduct.Description,
	}
	svc.pub.Publish(event.NewEvent(product.ProductCreatedEvent, eventPayload))

	return newProduct, nil
}

func (svc *ProductServiceImpl) GetById(ctx context.Context, id product.ProductID) *product.Product {
	return svc.repo.FindById(ctx, id)
}

func (svc *ProductServiceImpl) GetProducts(ctx context.Context, filter *product.GetProductsFilter) []*product.Product {
	return svc.repo.Find(ctx, filter.Q)
}

func (svc *ProductServiceImpl) Delete(ctx context.Context, id product.ProductID) error {
	return svc.repo.DeleteById(ctx, id)
}
