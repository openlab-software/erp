package db

import (
	"fmt"
	"os"

	postgresDriver "gorm.io/driver/postgres"
	"gorm.io/gorm"
)

func Connect() (*gorm.DB, error) {
	host := os.Getenv("POSTGRES_HOST")
	port := os.Getenv("POSTGRES_PORT")
	user := os.Getenv("POSTGRES_USER")
	database := os.Getenv("POSTGRES_DATABASE")
	password := os.Getenv("POSTGRES_PASSWORD")

	dsn := fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=%s sslmode=disable TimeZone=America/Sao_Paulo", host, user, password, database, port)

	return gorm.Open(postgresDriver.Open(dsn), &gorm.Config{
		Logger: newGormLogger(),
	})
}
