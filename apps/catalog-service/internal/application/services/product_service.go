package services

import (
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/product"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
)

type ProductServiceImpl struct {
	product.ProductService
	repo product.ProductRepository
}

func NewProductService(repo product.ProductRepository, pub event.Publisher) product.ProductService {

	return &ProductServiceImpl{
		repo: repo,
	}
}
