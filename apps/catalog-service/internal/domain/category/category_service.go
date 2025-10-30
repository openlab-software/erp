package category

import "context"

type CategoryService interface {
	Create(ctx context.Context, c *CreateCategoryPayload) (*Category, error)
	GetById(ctx context.Context, id CategoryID) *Category
	GetCategories(ctx context.Context, filter *GetCategoriesFilter) []Category
	Delete(ctx context.Context, id CategoryID) error
}

type CreateCategoryPayload struct {
	Description string
}

type GetCategoriesFilter struct {
	Q string
}
