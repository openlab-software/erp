package db

import (
	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"gorm.io/gorm"
)

type categoryEntity struct {
	ID          uint
	PublicID    string
	Description string
}

func (categoryEntity) TableName() string {
	return "catalog.categories"
}

type PostgresCategoryRepository struct {
	category.CategoryRepository
	DB *gorm.DB
}

func NewPostgresCategoryRepository(db *gorm.DB) category.CategoryRepository {
	db.AutoMigrate(&categoryEntity{})
	return &PostgresCategoryRepository{
		DB: db,
	}
}

func (r *PostgresCategoryRepository) Insert(c *category.Category) error {
	entity := categoryEntity{
		PublicID:    c.CategoryID.ToPublic(),
		Description: c.Description,
	}

	result := r.DB.Create(&entity)

	return result.Error
}

func (r *PostgresCategoryRepository) Find() []category.Category {
	var entities []categoryEntity
	r.DB.Find(&entities)

	categories := make([]category.Category, len(entities))
	for i, c := range entities {
		categoryID, _ := category.ParseCategoryID(c.PublicID)

		categories[i] = category.Category{
			CategoryID:  categoryID,
			Description: c.Description,
		}
	}

	return categories
}
