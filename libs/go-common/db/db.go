package db

import (
	"fmt"
	"os"

	postgresDriver "gorm.io/driver/postgres"
	"gorm.io/gorm"
)

// erpSchemas lista todos os schemas do ERP. Adicionados aqui para que qualquer
// serviço que conecte ao banco já garanta a existência de todos os schemas,
// removendo a dependência de ordem de inicialização entre serviços.
var erpSchemas = []string{"catalog", "stock"}

func EnsureSchema(db *gorm.DB, schemas ...string) error {
	for _, schema := range schemas {
		if err := db.Exec("CREATE SCHEMA IF NOT EXISTS " + schema).Error; err != nil {
			return err
		}
	}
	return nil
}

func Connect() (*gorm.DB, error) {
	host := os.Getenv("POSTGRES_HOST")
	port := os.Getenv("POSTGRES_PORT")
	user := os.Getenv("POSTGRES_USER")
	database := os.Getenv("POSTGRES_DATABASE")
	password := os.Getenv("POSTGRES_PASSWORD")

	dsn := fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=%s sslmode=disable TimeZone=America/Sao_Paulo", host, user, password, database, port)

	db, err := gorm.Open(postgresDriver.Open(dsn), &gorm.Config{
		Logger: newGormLogger(),
	})
	if err != nil {
		return nil, err
	}

	if err := EnsureSchema(db, erpSchemas...); err != nil {
		return nil, err
	}

	return db, nil
}
