package db

import (
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/product"
	"gorm.io/gorm"
)

type productEntity struct {
	gorm.Model
	ID               uint
	PublicID         string
	Description      string
	ShortDescription string
	UnitOfMeasure    string
	Status           string
	Category         categoryEntity `gorm:"foreignKey:category"`
}

func (productEntity) TableName() string {
	return "catalog.products"
}

type PostgresProductRepository struct {
	product.ProductRepository
	DB *gorm.DB
}

func NewPostgresProductRepository(db *gorm.DB) PostgresProductRepository {
	db.AutoMigrate(&productEntity{})
	return PostgresProductRepository{
		DB: db,
	}
}

func (r *PostgresProductRepository) Insert(p *product.Product) error {
	entity := productEntity{
		PublicID:         string(p.ProductID),
		Description:      p.Description,
		ShortDescription: p.ShortDescription,
		Status:           string(p.Status),
		UnitOfMeasure:    p.UnitOfMeasure,
	}

	result := r.DB.Create(&entity)

	return result.Error
}
