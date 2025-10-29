package postgres

import (
	"errors"
	"strings"

	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"github.com/patrickdevbr-portfolio/erp/libs/go-common/audit"
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

func toEntity(c *category.Category) *categoryEntity {
	return &categoryEntity{
		PublicID:    c.CategoryID.ToPublic(),
		Description: c.Description,
		Model: gorm.Model{
			CreatedAt: c.CreatedAt,
		},
	}
}

func toDomain(e *categoryEntity) *category.Category {
	categoryID, _ := category.ParseCategoryID(e.PublicID)

	return &category.Category{
		CategoryID:  categoryID,
		Description: e.Description,
		Audit: audit.Audit{
			CreatedAt: e.CreatedAt,
		},
	}
}

func (r *PostgresCategoryRepository) Insert(c *category.Category) error {
	entity := categoryEntity{
		PublicID:    c.CategoryID.ToPublic(),
		Description: c.Description,
		Model: gorm.Model{
			CreatedAt: c.CreatedAt,
		},
	}

	result := r.DB.Create(&entity)

	return result.Error
}

func (r *PostgresCategoryRepository) Find(description string) []category.Category {
	var entities []categoryEntity
	r.DB.Where("LOWER(description) LIKE CONCAT('%',LOWER(?),'%')", description).Find(&entities)

	categories := make([]category.Category, len(entities))
	for i, e := range entities {
		categories[i] = *toDomain(&e)
	}

	return categories
}

func (r *PostgresCategoryRepository) FindById(id category.CategoryID) *category.Category {
	var entity *categoryEntity
	result := r.DB.Where("public_id = ?", id.ToPublic()).First(&entity)

	if entity == nil || errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil
	}

	return toDomain(entity)
}

func (r *PostgresCategoryRepository) FindByDescription(description string) *category.Category {
	var entity *categoryEntity
	result := r.DB.Where("LOWER(description) = ?", strings.ToLower(description)).First(&entity)

	if entity == nil || errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil
	}

	return toDomain(entity)
}

func (r *PostgresCategoryRepository) DeleteById(id category.CategoryID) error {
	result := r.DB.Where("public_id = ?", id.ToPublic()).Delete(&categoryEntity{})

	if result.RowsAffected <= 0 {
		return errors.New("category with this id not found")
	}

	return result.Error
}
