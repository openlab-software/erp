package postgres

import (
	"context"
	"errors"

	"github.com/patrickdevbr-portfolio/erp/apps/catalog-service/internal/domain/category"
	"gorm.io/gorm"
)

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

func (r *PostgresCategoryRepository) Insert(ctx context.Context, c *category.Category) error {
	entity := toCategoryEntity(c)
	result := r.DB.WithContext(ctx).Create(&entity)

	return result.Error
}

func (r *PostgresCategoryRepository) Find(ctx context.Context, description string) []category.Category {
	var entities []categoryEntity
	r.DB.WithContext(ctx).Where("LOWER(description) LIKE CONCAT('%',LOWER(?),'%')", description).Find(&entities)

	categories := make([]category.Category, len(entities))
	for i, e := range entities {
		categories[i] = *toCategoryDomain(&e)
	}

	return categories
}

func (r *PostgresCategoryRepository) FindById(ctx context.Context, id category.CategoryID) *category.Category {
	var entity *categoryEntity
	result := r.DB.WithContext(ctx).Where("public_id = ?", id.ToPublic()).First(&entity)

	if entity == nil || errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil
	}

	return toCategoryDomain(entity)
}

func (r *PostgresCategoryRepository) FindByDescription(ctx context.Context, description string) *category.Category {
	var entity *categoryEntity
	result := r.DB.WithContext(ctx).Where("LOWER(description) = LOWER(?)", description).First(&entity)

	if entity == nil || errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil
	}

	return toCategoryDomain(entity)
}

func (r *PostgresCategoryRepository) DeleteById(ctx context.Context, id category.CategoryID) error {
	result := r.DB.WithContext(ctx).Where("public_id = ?", id.ToPublic()).Delete(&categoryEntity{})

	if result.RowsAffected <= 0 {
		return errors.New("category with this id not found")
	}

	return result.Error
}
