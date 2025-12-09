package stock

import "context"

type StockRepository interface {
	InsertItem(ctx context.Context, item StockItem) error
	InsertStock(ctx context.Context, stock *Stock) error
	FindStocks(ctx context.Context) []*Stock
}
