package reassignment

import (
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/stock"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

type ReassignmentID = publicid.PublicID

type Reassignment struct {
	audit.Audit
	ReassignmentID ReassignmentID
	FromStockID    stock.StockID
	ToStockID      stock.StockID
	Items          []*ReassignmentItem
}

type ReassignmentItem struct {
	audit.Audit
	ProductID string
	Quantity  int
}

func NewReassignment(fromStockID, toStockID stock.StockID) *Reassignment {
	return &Reassignment{
		Audit:          audit.CreatedNow(),
		ReassignmentID: publicid.New("stock_reassignment"),
		FromStockID:    fromStockID,
		ToStockID:      toStockID,
		Items:          make([]*ReassignmentItem, 0),
	}
}

func NewReassignmentItem(productID string, quantity int) *ReassignmentItem {
	return &ReassignmentItem{
		Audit:     audit.CreatedNow(),
		ProductID: productID,
		Quantity:  quantity,
	}
}
