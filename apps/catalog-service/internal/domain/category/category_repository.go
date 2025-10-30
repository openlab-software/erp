package category

import "context"

type CategoryRepository interface {
	Insert(ctx context.Context, c *Category) error
	Find(ctx context.Context, description string) []Category
	FindById(ctx context.Context, id CategoryID) *Category
	FindByDescription(ctx context.Context, description string) *Category
	DeleteById(ctx context.Context, id CategoryID) error
}
