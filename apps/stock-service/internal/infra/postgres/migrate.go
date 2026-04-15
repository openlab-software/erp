package postgres

import (
	"github.com/openlab-software/erp/libs/go-common/outbox"
	"gorm.io/gorm"
)

func Migrate(db *gorm.DB) error {
	if err := db.AutoMigrate(
		&catalogProductEntity{},
		&stockEntity{},
		&stockItemEntity{},
		&reassignmentEntity{},
		&reassignmentItemEntity{},
	); err != nil {
		return err
	}

	return outbox.Migrate(db, "stock")
}
