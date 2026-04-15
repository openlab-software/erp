package reassignment

import (
	"context"

	"github.com/openlab-software/erp/apps/stock-service/internal/domain/stock"
)

type CreateReassignmentItemPayload struct {
	ProductID string
	Quantity  int
}

type CreateReassignmentPayload struct {
	FromStockID stock.StockID
	ToStockID   stock.StockID
	Items       []CreateReassignmentItemPayload
}

type ReassignmentService interface {
	CreateReassignment(ctx context.Context, payload CreateReassignmentPayload) (*Reassignment, error)
}
