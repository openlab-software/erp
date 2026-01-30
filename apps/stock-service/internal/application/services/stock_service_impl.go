package services

import (
	"context"
	"encoding/json"

	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/stock"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/event"
)

type StockServiceImpl struct {
	stock.StockService
	repo stock.StockRepository
	pub  event.Publisher
}

type productCreatedPayload struct {
	ID          string `json:"product_id"`
	Description string `json:"description"`
}

type productCreatedEvent struct {
	event.Event
	Payload productCreatedPayload `json:"payload"`
}

func NewStockService(repo stock.StockRepository, pub event.Publisher, sub event.Subscriber) stock.StockService {
	svc := &StockServiceImpl{
		repo: repo,
		pub:  pub,
	}

	sub.Subscribe([]string{"product.created"}, func(body []byte) error {
		var event productCreatedEvent
		if err := json.Unmarshal(body, &event); err != nil {
			return err
		}

		return svc.InitItem(context.Background(), event.Payload.ID)
	})

	return svc
}

func (svc *StockServiceImpl) InitItem(ctx context.Context, productID string) error {
	allStocks := svc.repo.FindStocks(ctx)

	for _, s := range allStocks {
		newStockItem := stock.NewEmptyItem(productID, *s)

		svc.repo.InsertItem(ctx, *newStockItem)
	}
	return nil
}

func (svc *StockServiceImpl) CreateStock(ctx context.Context, payload stock.CreateStockPayload) (*stock.Stock, error) {
	newStock := stock.NewStock(payload.Description)
	if err := svc.repo.InsertStock(ctx, newStock); err != nil {
		return nil, err
	}

	return newStock, nil
}
