package stock

import (
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/publicid"
)

type StockID = publicid.PublicID

func ParseStockID(s string) (StockID, error) {
	publicID, err := publicid.ParsePublic("stock", s)
	return StockID(publicID), err
}

type Stock struct {
	audit.Audit
	StockID     StockID
	Description string
	Items       []*StockItem
}

type StockItem struct {
	audit.Audit
	Stock        Stock
	ProductID    string
	MinValue     *int
	CurrentValue int
	MaxValue     *int
}

func NewEmptyItem(productID string, stock Stock) *StockItem {
	return &StockItem{
		Audit:        audit.CreatedNow(),
		ProductID:    productID,
		Stock:        stock,
		MinValue:     nil,
		CurrentValue: 0,
		MaxValue:     nil,
	}
}

func NewStock(description string) *Stock {
	return &Stock{
		StockID:     publicid.New("stock"),
		Audit:       audit.CreatedNow(),
		Description: description,
	}
}
