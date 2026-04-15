package db

import (
	"context"
	"fmt"
	"os"

	postgresDriver "gorm.io/driver/postgres"
	"gorm.io/gorm"
)

// txContextKey is the key used to store a *gorm.DB transaction in a context.
type txContextKey struct{}

// WithTx returns a new context carrying the given *gorm.DB transaction.
func WithTx(ctx context.Context, tx *gorm.DB) context.Context {
	return context.WithValue(ctx, txContextKey{}, tx)
}

// TxFromContext returns the *gorm.DB transaction stored in ctx.
// If no transaction is present it returns fallback unchanged.
func TxFromContext(ctx context.Context, fallback *gorm.DB) *gorm.DB {
	if tx, ok := ctx.Value(txContextKey{}).(*gorm.DB); ok && tx != nil {
		return tx
	}
	return fallback
}

// TxManager abstracts transaction lifecycle for use in application services,
// keeping gorm out of the application layer.
type TxManager interface {
	RunInTx(ctx context.Context, fn func(ctx context.Context) error) error
}

type gormTxManager struct {
	db *gorm.DB
}

// NewTxManager returns a TxManager backed by the given *gorm.DB.
func NewTxManager(db *gorm.DB) TxManager {
	return &gormTxManager{db: db}
}

// RunInTx starts a transaction, injects it into ctx via WithTx, and runs fn.
// If fn returns an error the transaction is rolled back; otherwise committed.
func (m *gormTxManager) RunInTx(ctx context.Context, fn func(ctx context.Context) error) error {
	return m.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return fn(WithTx(ctx, tx))
	})
}

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
