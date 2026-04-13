package postgres

import (
	"github.com/patrickdevbr-portfolio/erp/apps/stock-service/internal/domain/reassignment"
	"gorm.io/gorm"
)

type reassignmentEntity struct {
	gorm.Model
	ID          uint
	PublicID    string
	FromStockID uint
	ToStockID   uint
	Items       []*reassignmentItemEntity `gorm:"foreignKey:ReassignmentID"`
}

func (reassignmentEntity) TableName() string {
	return "stock.reassignments"
}

type reassignmentItemEntity struct {
	gorm.Model
	ID               uint
	CatalogProductID uint `gorm:"not null"`
	Quantity         int
	ReassignmentID   uint
}

func (reassignmentItemEntity) TableName() string {
	return "stock.reassignment_items"
}

func toReassignmentEntity(r *reassignment.Reassignment, fromID, toID uint, items []*reassignmentItemEntity) *reassignmentEntity {
	return &reassignmentEntity{
		PublicID:    r.ReassignmentID.ToPublic(),
		FromStockID: fromID,
		ToStockID:   toID,
		Items:       items,
	}
}

func toReassignmentItemEntity(i *reassignment.ReassignmentItem, catalogProductID uint) *reassignmentItemEntity {
	return &reassignmentItemEntity{
		CatalogProductID: catalogProductID,
		Quantity:         i.Quantity,
	}
}
