package postgres

import (
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/stock"
	"gorm.io/gorm"
)

type stockEntity struct {
	gorm.Model
	ID          uint
	PublicID    string
	Description string
}

func (stockEntity) TableName() string {
	return "stock.stocks"
}

func toStockEntity(s *stock.Stock) *stockEntity {
	return &stockEntity{
		PublicID:    s.StockID.ToPublic(),
		Description: s.Description,
	}
}

func toItemEntity(i *stock.StockItem, catalogProductID uint) *stockItemEntity {
	return &stockItemEntity{
		CatalogProductID: catalogProductID,
		MinValue:         i.MinValue,
		CurrentValue:     i.CurrentValue,
		MaxValue:         i.MaxValue,
		Stock:            toStockEntity(&i.Stock),
	}
}

func toStockDomain(e *stockEntity) *stock.Stock {
	return &stock.Stock{
		StockID:     stock.StockID(e.PublicID),
		Description: e.Description,
	}
}

type stockItemEntity struct {
	gorm.Model
	ID               uint
	PublicID         string
	CatalogProductID uint `gorm:"not null"`
	MinValue         *int
	CurrentValue     int
	MaxValue         *int
	StockID          uint
	Stock            *stockEntity
}

func (stockItemEntity) TableName() string {
	return "stock.items"
}
