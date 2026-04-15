package postgres

import (
	"context"

	"github.com/openlab-software/erp/apps/stock-service/internal/domain/stock"
	commondb "github.com/openlab-software/erp/libs/go-common/db"
	"gorm.io/gorm"
)

type PostgresStockRepository struct {
	DB *gorm.DB
}

func NewPostgresStockRepository(db *gorm.DB) stock.StockRepository {
	return &PostgresStockRepository{
		DB: db,
	}
}

func (r *PostgresStockRepository) InsertItem(ctx context.Context, item stock.StockItem) error {
	db := commondb.TxFromContext(ctx, r.DB).WithContext(ctx)

	var stockEnt stockEntity
	if err := db.Where("public_id = ?", item.Stock.StockID.ToPublic()).First(&stockEnt).Error; err != nil {
		return err
	}

	catalogProductID, err := findCatalogProductInternalID(db, item.ProductID)
	if err != nil {
		return err
	}

	itemEntity := toItemEntity(&item, catalogProductID)
	itemEntity.StockID = stockEnt.ID
	itemEntity.Stock = nil

	return db.Create(&itemEntity).Error
}

func (r *PostgresStockRepository) InsertStock(ctx context.Context, s *stock.Stock) error {
	db := commondb.TxFromContext(ctx, r.DB)
	entity := toStockEntity(s)
	return db.WithContext(ctx).Create(&entity).Error
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
