package services

import (
	"errors"
	"fmt"

	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
)

type CategoryServiceImpl struct {
	category.CategoryService
	repo category.CategoryRepository
	pub  event.Publisher
}

func NewCategoryService(repo category.CategoryRepository, pub event.Publisher) category.CategoryService {
	return &CategoryServiceImpl{
		repo: repo,
		pub:  pub,
	}
}

func (svc *CategoryServiceImpl) Create(payload *category.CreateCategoryPayload) (*category.Category, error) {
	sameDescription := svc.repo.FindByDescription(payload.Description)

	if sameDescription != nil {
		fmt.Sprintln(sameDescription)
		return nil, errors.New("a category with same description already exists")
	}

	newCategory := category.NewCategory(payload.Description)
	if err := svc.repo.Insert(newCategory); err != nil {
		return nil, err
	}

	eventPayload := category.CategoryCreatedPayload{ID: string(newCategory.CategoryID), Description: newCategory.Description}
	svc.pub.Publish(event.NewEvent(category.CategoryCreatedEvent, eventPayload))

	return newCategory, nil
}

func (svc *CategoryServiceImpl) GetById(id category.CategoryID) *category.Category {
	return svc.repo.FindById(id)
}

func (svc *CategoryServiceImpl) GetCategories(filter *category.GetCategoriesFilter) []category.Category {
	return svc.repo.Find(filter.Q)
}

func (svc *CategoryServiceImpl) Delete(id category.CategoryID) error {
	return svc.repo.DeleteById(id)
}
