package postgres

import (
	"context"
	"fmt"
	"os"
	"time"

	postgresDriver "gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

type RecorderLogger struct {
	logger.Interface
	Statements []string
}

func (r *RecorderLogger) Trace(ctx context.Context, begin time.Time, fc func() (string, int64), err error) {
	sql, _ := fc()
	r.Statements = append(r.Statements, sql)
}

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
