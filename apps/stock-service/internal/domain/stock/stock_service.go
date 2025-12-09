package stock

import "context"

type CreateStockPayload struct {
	Description string
}

type StockService interface {
	InitItem(ctx context.Context, productID string) error
	CreateStock(ctx context.Context, payload CreateStockPayload) (*Stock, error)
}
