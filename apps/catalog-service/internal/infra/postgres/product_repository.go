package postgres

import (
	"context"
	"errors"

	"github.com/openlab-software/erp/apps/catalog-service/internal/domain/product"
	commondb "github.com/openlab-software/erp/libs/go-common/db"
	"gorm.io/gorm"
)

type PostgresProductRepository struct {
	product.ProductRepository
	DB *gorm.DB
}

func NewPostgresProductRepository(db *gorm.DB) product.ProductRepository {
	db.AutoMigrate(&productEntity{})
	return &PostgresProductRepository{
		DB: db,
	}
}

func (r *PostgresProductRepository) Insert(ctx context.Context, p *product.Product) error {
	db := commondb.TxFromContext(ctx, r.DB)
	entity := toProductEntity(p)

	var category *categoryEntity
	if result := db.WithContext(ctx).Where("public_id = ?", p.Category.CategoryID.ToPublic()).First(&category); result.Error != nil {
		return result.Error
	}

	entity.Category = category
	entity.CategoryID = category.ID

	return db.WithContext(ctx).Create(&entity).Error
}

func (r *PostgresProductRepository) Update(ctx context.Context, p *product.Product) error {
	var current *productEntity
	if result := r.DB.WithContext(ctx).Where("public_id = ?", p.ProductID.ToPublic()).First(&current); result.Error != nil {
		return result.Error
	}
	var category *categoryEntity
	if result := r.DB.WithContext(ctx).Where("public_id = ?", p.Category.CategoryID.ToPublic()).First(&category); result.Error != nil {
		return result.Error
	}

	current.Description = p.Description
	current.ShortDescription = p.ShortDescription
	current.Status = string(p.Status)
	current.UnitOfMeasure = p.UnitOfMeasure
	current.Category = category

	result := r.DB.Save(current)

	return result.Error
}

func (r *PostgresProductRepository) Find(ctx context.Context, description string) []*product.Product {
	var entities []productEntity
	r.DB.WithContext(ctx).Where("LOWER(description) LIKE CONCAT('%',LOWER(?),'%') OR LOWER(short_description) LIKE CONCAT('%',LOWER(?),'%')", description, description).Find(&entities)

	products := make([]*product.Product, len(entities))
	for i, e := range entities {
		products[i] = toProductDomain(&e)
	}

	return products
}

func (r *PostgresProductRepository) FindById(ctx context.Context, id product.ProductID) *product.Product {
	var entity *productEntity
	result := r.DB.WithContext(ctx).Where("public_id = ?", id.ToPublic()).First(&entity)

	if entity == nil || errors.Is(result.Error, gorm.ErrRecordNotFound) {
		return nil
	}

	return toProductDomain(entity)
}
