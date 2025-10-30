package services

import (
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

func (svc *ProductServiceImpl) Create(payload *product.CreateProductPayload) (*product.Product, error) {
	newProduct := product.NewProduct(payload.Description, payload.ShortDescription, payload.UnitOfMeasure, payload.CategoryID)
	if err := svc.repo.Insert(newProduct); err != nil {
		return nil, err
	}

	eventPayload := product.ProductCreatedPayload{
		// ID:          string(newProduct.ProductID),
		Description: newProduct.Description,
	}
	svc.pub.Publish(event.NewEvent(product.ProductCreatedEvent, eventPayload))

	return newProduct, nil
}

func (svc *ProductServiceImpl) GetById(id product.ProductID) *product.Product {
	return svc.repo.FindById(id)
}

func (svc *ProductServiceImpl) GetProducts(filter *product.GetProductsFilter) []*product.Product {
	return svc.repo.Find(filter.Q)
}

func (svc *ProductServiceImpl) Delete(id product.ProductID) error {
	return svc.repo.DeleteById(id)
}
