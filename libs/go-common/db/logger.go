package db

import (
	"context"
	"log"
	"os"
	"time"

	gormlogger "gorm.io/gorm/logger"
	"gorm.io/gorm/utils"
)

type gormLogger struct {
	SlowThreshold time.Duration
	LogLevel      gormlogger.LogLevel
	logger        *log.Logger
}

func newGormLogger() *gormLogger {
	return &gormLogger{
		SlowThreshold: 200 * time.Millisecond,
		LogLevel:      gormlogger.Info,
		logger:        log.New(os.Stdout, "\r\n", log.LstdFlags),
	}
}

// LogMode permite mudar o nível de log dinamicamente.
func (l *gormLogger) LogMode(level gormlogger.LogLevel) gormlogger.Interface {
	newLogger := *l
	newLogger.LogLevel = level
	return &newLogger
}

func (l *gormLogger) Info(ctx context.Context, msg string, data ...interface{}) {
	if l.LogLevel >= gormlogger.Info {
		l.logger.Printf("[INFO] "+msg, data...)
	}
}

func (l *gormLogger) Warn(ctx context.Context, msg string, data ...interface{}) {
	if l.LogLevel >= gormlogger.Warn {
		l.logger.Printf("[WARN] "+msg, data...)
	}
}

func (l *gormLogger) Error(ctx context.Context, msg string, data ...interface{}) {
	if l.LogLevel >= gormlogger.Error {
		l.logger.Printf("[ERROR] "+msg, data...)
	}
}

// Trace é chamado a cada operação de banco de dados
func (l *gormLogger) Trace(ctx context.Context, begin time.Time, fc func() (string, int64), err error) {
	if l.LogLevel <= gormlogger.Silent {
		return
	}

	elapsed := time.Since(begin)
	sql, rows := fc()

	switch {
	case err != nil && l.LogLevel >= gormlogger.Error:
		l.logger.Printf("[ERROR] %s | %s | %d rows | %s", err, elapsed, rows, sql)

	case elapsed > l.SlowThreshold && l.SlowThreshold != 0 && l.LogLevel >= gormlogger.Warn:
		l.logger.Printf("[SLOW QUERY] > %v | %d rows | %s | %s",
			l.SlowThreshold, rows, sql, utils.FileWithLineNum())

	case l.LogLevel >= gormlogger.Info:
		l.logger.Printf("[QUERY] %s | %d rows | %s", elapsed, rows, sql)
	}
}
