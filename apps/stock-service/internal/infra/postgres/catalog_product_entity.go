package postgres

import (
	"fmt"

	"gorm.io/gorm"
)

// catalogProductEntity é uma projeção read-only de catalog.products usada
// apenas para resolver o ID interno a partir do public_id.
type catalogProductEntity struct {
	ID       uint
	PublicID string
}

func (catalogProductEntity) TableName() string {
	return "catalog.products"
}

func findCatalogProductInternalID(db *gorm.DB, publicID string) (uint, error) {
	var p catalogProductEntity
	if err := db.Select("id").Where("public_id = ?", publicID).First(&p).Error; err != nil {
		return 0, fmt.Errorf("product %q not found in catalog: %w", publicID, err)
	}
	return p.ID, nil
}
