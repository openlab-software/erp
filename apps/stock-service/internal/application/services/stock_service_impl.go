package services

import (
	"context"

	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/stock"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
)

type StockServiceImpl struct {
	stock.StockService
	repo stock.StockRepository
	pub  event.Publisher
}

func NewStockService(repo stock.StockRepository, pub event.Publisher) stock.StockService {
	return &StockServiceImpl{
		repo: repo,
		pub:  pub,
	}
}

func (svc *StockServiceImpl) InitItem(ctx context.Context, productID string) error {
	allStocks := svc.repo.FindStocks(ctx)

	for _, s := range allStocks {
		newStockItem := stock.NewEmptyItem(productID, *s)
		svc.repo.InsertItem(ctx, *newStockItem)
	}
	return nil
}
