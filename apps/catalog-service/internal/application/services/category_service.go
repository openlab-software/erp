package services

import (
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
)

type CategoryServiceImpl struct {
	category.CategoryService
	repository     category.CategoryRepository
	eventPublisher event.Publisher
}

func NewCategoryService(repo category.CategoryRepository, eventPublisher event.Publisher) category.CategoryService {
	return &CategoryServiceImpl{
		repository:     repo,
		eventPublisher: eventPublisher,
	}
}

func (svc *CategoryServiceImpl) Create(c *category.Category) error {
	err := svc.repository.Insert(c)
	return err
}

func (svc *CategoryServiceImpl) GetCategories() []category.Category {
	return svc.repository.Find()
}
