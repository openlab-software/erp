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

func toStockDomain(s *stock.Stock) *stockEntity {
	return &stockEntity{
		PublicID:    s.StockID.ToPublic(),
		Description: s.Description,
	}
}

type stockItemEntity struct {
	gorm.Model
	ID           uint
	PublicID     string
	ProductID    string
	MinValue     *int
	CurrentValue int
	MaxValue     *int
	StockID      uint
	Stock        *stockEntity
}

func (stockItemEntity) TableName() string {
	return "stock.items"
}
