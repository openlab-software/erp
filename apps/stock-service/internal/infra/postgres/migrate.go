package postgres

import "gorm.io/gorm"

func Migrate(db *gorm.DB) error {
	return db.AutoMigrate(
		&catalogProductEntity{},
		&stockEntity{},
		&stockItemEntity{},
		&reassignmentEntity{},
		&reassignmentItemEntity{},
	)
}
