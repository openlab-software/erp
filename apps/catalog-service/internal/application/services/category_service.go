package services

import (
	"errors"
	"fmt"

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

func (svc *CategoryServiceImpl) Create(payload *category.CreateCategoryPayload) (*category.Category, error) {
	sameDescription := svc.repository.FindByDescription(payload.Description)

	if sameDescription != nil {
		fmt.Sprintln(sameDescription)
		return nil, errors.New("a category with same description already exists")
	}

	newCategory := category.NewCategory(payload.Description)
	if err := svc.repository.Insert(newCategory); err != nil {
		return nil, err
	}

	eventPayload := category.CategoryCreatedPayload{ID: string(newCategory.CategoryID), Description: newCategory.Description}
	svc.eventPublisher.Publish(event.NewEvent(category.CategoryCreatedEvent, eventPayload))

	return newCategory, nil
}

func (svc *CategoryServiceImpl) GetCategories() []category.Category {
	return svc.repository.Find()
}

func (svc *CategoryServiceImpl) Delete(id category.CategoryID) error {
	return svc.repository.DeleteById(id)
}
