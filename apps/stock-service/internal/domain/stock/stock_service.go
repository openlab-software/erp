package stock

import "context"

type StockService interface {
	InitItem(ctx context.Context, productID string) error
}
