package postgres

import (
	"context"
	"log"

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
	var stockEntity stockEntity
	if err := r.DB.WithContext(ctx).Where("public_id = ?", item.Stock.StockID.ToPublic()).First(&stockEntity).Error; err != nil {
		return err
	}

	itemEntity := toItemEntity(&item)
	itemEntity.StockID = stockEntity.ID
	itemEntity.Stock = nil

	log.Println("itemEntity")
	log.Println(itemEntity)
	result := r.DB.WithContext(ctx).Create(&itemEntity)

	return result.Error
}

func (r *PostgresStockRepository) InsertStock(ctx context.Context, s *stock.Stock) error {
	entity := toStockEntity(s)
	result := r.DB.WithContext(ctx).Create(&entity)

	return result.Error
}

func (r *PostgresStockRepository) FindStocks(ctx context.Context) []*stock.Stock {
	var entities []stockEntity
	r.DB.WithContext(ctx).Find(&entities)

	stocks := make([]*stock.Stock, len(entities))
	for i, e := range entities {
		stocks[i] = toStockDomain(&e)
	}

	return stocks
}
