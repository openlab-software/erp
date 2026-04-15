package postgres

import (
	"github.com/openlab-software/erp/apps/catalog-service/internal/domain/category"
	"github.com/openlab-software/erp/libs/go-common/audit"
	"gorm.io/gorm"
)

type categoryEntity struct {
	gorm.Model
	ID          uint
	PublicID    string
	Description string
}

func (categoryEntity) TableName() string {
	return "catalog.categories"
}

func toCategoryEntity(c *category.Category) *categoryEntity {
	return &categoryEntity{
		PublicID:    c.CategoryID.ToPublic(),
		Description: c.Description,
		Model: gorm.Model{
			CreatedAt: c.CreatedAt,
		},
	}
}

func toCategoryDomain(e *categoryEntity) *category.Category {
	categoryID, _ := category.ParseCategoryID(e.PublicID)

	return &category.Category{
		CategoryID:  categoryID,
		Description: e.Description,
		Audit: audit.Audit{
			CreatedAt: e.CreatedAt,
		},
	}
}
