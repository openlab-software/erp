package db

import (
	"errors"
	"strings"
	"time"

	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
	"gorm.io/gorm"
)

type categoryEntity struct {
	ID          uint
	PublicID    string
	Description string
	CreatedAt   time.Time
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
		CreatedAt:   c.CreatedAt,
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
			Audit: audit.Audit{
				CreatedAt: c.CreatedAt,
			},
		}
	}

	return categories
}

func (r *PostgresCategoryRepository) FindByDescription(description string) *category.Category {
	var entity *categoryEntity
	result := r.DB.Where("LOWER(description) = ?", strings.ToLower(description)).First(&entity)

	if entity == nil || errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil
	}

	categoryID, _ := category.ParseCategoryID(entity.PublicID)
	return &category.Category{
		CategoryID:  categoryID,
		Description: entity.Description,
	}
}

func (r *PostgresCategoryRepository) DeleteById(id category.CategoryID) error {
	result := r.DB.Where("public_id = ?", id.ToPublic()).Delete(&categoryEntity{})

	if result.RowsAffected <= 0 {
		return errors.New("category with this id not found")
	}

	return result.Error
}
