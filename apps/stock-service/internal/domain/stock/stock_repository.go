package stock

import "context"

type StockRepository interface {
	InsertItem(ctx context.Context, item StockItem) error
	FindStocks(ctx context.Context) []*Stock
}
