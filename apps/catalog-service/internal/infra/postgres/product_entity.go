package postgres

import (
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/product"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"gorm.io/gorm"
)

type productEntity struct {
	gorm.Model
	ID               uint
	PublicID         string `gorm:"uniqueIndex"`
	Description      string
	ShortDescription string
	UnitOfMeasure    string
	Status           string
	CategoryID       uint
	Category         *categoryEntity
}

func (productEntity) TableName() string {
	return "catalog.products"
}

func toProductDomain(e *productEntity) *product.Product {
	productID, _ := product.ParseProductID(e.PublicID)

	var categoryID category.CategoryID
	if e.Category != nil {
		categoryID, _ = category.ParseCategoryID(e.Category.PublicID)
	}

	return &product.Product{
		ProductID:        productID,
		Description:      e.Description,
		ShortDescription: e.ShortDescription,
		UnitOfMeasure:    e.UnitOfMeasure,
		Status:           product.ProductStatus(e.Status),
		Category: category.Category{
			Audit:       audit.Audit{CreatedAt: e.Category.CreatedAt},
			CategoryID:  categoryID,
			Description: e.Category.Description,
		},
		Audit: audit.Audit{
			CreatedAt: e.CreatedAt,
		},
	}
}

func toProductEntity(p *product.Product) *productEntity {
	return &productEntity{
		PublicID:         p.ProductID.ToPublic(),
		Description:      p.Description,
		ShortDescription: p.ShortDescription,
		UnitOfMeasure:    p.UnitOfMeasure,
		Status:           string(p.Status),
		Category:         toCategoryEntity(&p.Category),
	}
}
