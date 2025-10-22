package services

import (
	"github.com/patrickdevbr-portfolio/erp/apps/inventory-service/internal/domain/product"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
)

type ProductServiceImpl struct {
	product.ProductService
	repository     product.ProductRepository
	eventPublisher event.Publisher
}

func NewPageService(repo product.ProductRepository, eventPublisher event.Publisher) product.ProductService {
	return &ProductServiceImpl{
		repository:     repo,
		eventPublisher: eventPublisher,
	}
}
