package product

import "context"

type ProductRepository interface {
	Insert(ctx context.Context, p *Product) error
	Update(ctx context.Context, p *Product) error
	Find(ctx context.Context, description string) []*Product
	FindById(ctx context.Context, id ProductID) *Product
	DeleteById(ctx context.Context, id ProductID) error
}
