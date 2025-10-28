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

func NewProductService(repo product.ProductRepository, pub event.Publisher, sub event.Subscriber) product.ProductService {
	sub.Subscribe([]string{
		"user.created",
		"user.login.failed",
	}, handleCategoryCreated)

	return &ProductServiceImpl{
		repo: repo,
		pub:  pub,
	}
}

func handleCategoryCreated(body []byte) error {
	return nil
}
