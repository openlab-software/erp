package services

import (
	"context"
	"errors"

	"github.com/openlab-software/erp/apps/catalog-service/internal/domain/category"
	commondb "github.com/openlab-software/erp/libs/go-common/db"
	"github.com/openlab-software/erp/libs/go-common/event"
)

type CategoryServiceImpl struct {
	category.CategoryService
	repo category.CategoryRepository
	pub  event.Publisher
	txm  commondb.TxManager
}

func NewCategoryService(repo category.CategoryRepository, pub event.Publisher, txm commondb.TxManager) category.CategoryService {
	return &CategoryServiceImpl{repo: repo, pub: pub, txm: txm}
}

func (svc *CategoryServiceImpl) Create(ctx context.Context, payload *category.CreateCategoryPayload) (*category.Category, error) {
	if svc.repo.FindByDescription(ctx, payload.Description) != nil {
		return nil, errors.New("a category with same description already exists")
	}

	var newCategory *category.Category
	err := svc.txm.RunInTx(ctx, func(txCtx context.Context) error {
		newCategory = category.NewCategory(payload.Description)
		if err := svc.repo.Insert(txCtx, newCategory); err != nil {
			return err
		}

		payload := category.CategoryCreatedPayload{
			ID:          string(newCategory.CategoryID),
			Description: newCategory.Description,
		}
		return svc.pub.Publish(
			txCtx,
			event.NewEvent(category.CategoryCreatedEvent, payload),
		)
	})
	return newCategory, err
}

func (svc *CategoryServiceImpl) GetById(ctx context.Context, id category.CategoryID) *category.Category {
	return svc.repo.FindById(ctx, id)
}

func (svc *CategoryServiceImpl) GetCategories(ctx context.Context, filter *category.GetCategoriesFilter) []category.Category {
	return svc.repo.Find(ctx, filter.Q)
}

func (svc *CategoryServiceImpl) Delete(ctx context.Context, id category.CategoryID) error {
	return svc.repo.DeleteById(ctx, id)
}
