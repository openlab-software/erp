package postgres

import (
	"context"
	"fmt"

	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/reassignment"
	"gorm.io/gorm"
)

type PostgresReassignmentRepository struct {
	DB *gorm.DB
}

func NewPostgresReassignmentRepository(db *gorm.DB) reassignment.ReassignmentRepository {
	return &PostgresReassignmentRepository{
		DB: db,
	}
}

func (r *PostgresReassignmentRepository) Save(ctx context.Context, re *reassignment.Reassignment) error {
	db := r.DB.WithContext(ctx)

	var fromStock stockEntity
	if err := db.Where("public_id = ?", re.FromStockID.ToPublic()).First(&fromStock).Error; err != nil {
		return fmt.Errorf("from_stock not found: %w", err)
	}

	var toStock stockEntity
	if err := db.Where("public_id = ?", re.ToStockID.ToPublic()).First(&toStock).Error; err != nil {
		return fmt.Errorf("to_stock not found: %w", err)
	}

	items := make([]*reassignmentItemEntity, len(re.Items))
	for i, item := range re.Items {
		catalogProductID, err := findCatalogProductInternalID(db, item.ProductID)
		if err != nil {
			return err
		}
		items[i] = toReassignmentItemEntity(item, catalogProductID)
	}

	entity := toReassignmentEntity(re, fromStock.ID, toStock.ID, items)
	return db.Create(entity).Error
}
