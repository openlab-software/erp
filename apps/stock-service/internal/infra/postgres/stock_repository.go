package postgres

import (
	"context"

	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/stock"
	"gorm.io/gorm"
)

type PostgresStockRepository struct {
	DB *gorm.DB
}

func NewPostgresStockRepository(db *gorm.DB) stock.StockRepository {
	db.AutoMigrate(&stockEntity{})
	db.AutoMigrate(&stockItemEntity{})
	return &PostgresStockRepository{
		DB: db,
	}
}

func (r *PostgresStockRepository) InsertItem(ctx context.Context, item stock.StockItem) error {
	return nil
}

func (r *PostgresStockRepository) InsertStock(ctx context.Context, s *stock.Stock) error {
	entity := toStockEntity(s)
	return nil
}

func (r *PostgresStockRepository) FindStocks(ctx context.Context) []*stock.Stock {
	return nil
}
