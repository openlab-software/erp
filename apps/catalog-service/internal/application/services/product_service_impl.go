package services

import (
	"context"

	"github.com/openlab-software/erp/apps/catalog-service/internal/domain/product"
	commondb "github.com/openlab-software/erp/libs/go-common/db"
	"github.com/openlab-software/erp/libs/go-common/event"
)

type ProductServiceImpl struct {
	product.ProductService
	repo product.ProductRepository
	pub  event.Publisher
	txm  commondb.TxManager
}

func NewProductService(repo product.ProductRepository, pub event.Publisher, txm commondb.TxManager) product.ProductService {
	return &ProductServiceImpl{repo: repo, pub: pub, txm: txm}
}

func (svc *ProductServiceImpl) Create(ctx context.Context, payload *product.CreateProductPayload) (*product.Product, error) {
	var newProduct *product.Product

	err := svc.txm.RunInTx(ctx, func(txCtx context.Context) error {
		newProduct = product.NewProduct(payload.Description, payload.ShortDescription, payload.UnitOfMeasure, payload.CategoryID)
		if err := svc.repo.Insert(txCtx, newProduct); err != nil {
			return err
		}

		payload := product.ProductCreatedPayload{
			ID:          newProduct.ProductID,
			Description: newProduct.Description,
		}

		return svc.pub.Publish(txCtx, event.NewEvent(product.ProductCreatedEvent, payload))
	})
	return newProduct, err
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
