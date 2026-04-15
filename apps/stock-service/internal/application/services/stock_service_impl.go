package services

import (
	"context"
	"encoding/json"

	"github.com/openlab-software/erp/apps/stock-service/internal/domain/stock"
	commondb "github.com/openlab-software/erp/libs/go-common/db"
	"github.com/openlab-software/erp/libs/go-common/event"
)

type StockServiceImpl struct {
	stock.StockService
	repo stock.StockRepository
	pub  event.Publisher
	txm  commondb.TxManager
}

type productCreatedPayload struct {
	ID          string `json:"product_id"`
	Description string `json:"description"`
}

type productCreatedEvent struct {
	event.Event
	Payload productCreatedPayload `json:"payload"`
}

func NewStockService(repo stock.StockRepository, pub event.Publisher, sub event.Subscriber, txm commondb.TxManager) stock.StockService {
	svc := &StockServiceImpl{
		repo: repo,
		pub:  pub,
		txm:  txm,
	}

	sub.Subscribe([]string{"product.created"}, func(body []byte) error {
		var e productCreatedEvent
		if err := json.Unmarshal(body, &e); err != nil {
			return err
		}
		return svc.InitItems(context.Background(), e.Payload.ID)
	})

	return svc
}

// InitItems creates a StockItem for every existing stock when a new product is registered.
// All inserts run inside a single transaction: either every stock gets the item, or none do.
func (svc *StockServiceImpl) InitItems(ctx context.Context, productID string) error {
	allStocks := svc.repo.FindStocks(ctx)

	return svc.txm.RunInTx(ctx, func(txCtx context.Context) error {
		for _, s := range allStocks {
			newItem := stock.NewEmptyItem(productID, *s)
			if err := svc.repo.InsertItem(txCtx, *newItem); err != nil {
				return err
			}
		}
		return nil
	})
}

func (svc *StockServiceImpl) CreateStock(ctx context.Context, payload stock.CreateStockPayload) (*stock.Stock, error) {
	newStock := stock.NewStock(payload.Description)
	if err := svc.repo.InsertStock(ctx, newStock); err != nil {
		return nil, err
	}
	return newStock, nil
}
