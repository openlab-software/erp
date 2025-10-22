package mongodatabase

import (
	"context"
	"fmt"
	"net/url"
	"os"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func Connect(ctx context.Context) (*mongo.Client, error) {
	host := os.Getenv("MONGODB_HOST")
	port := os.Getenv("MONGODB_PORT")
	user := os.Getenv("MONGODB_USER")
	password := os.Getenv("MONGODB_PASSWORD")

	uri := fmt.Sprintf("mongodb://%s:%s@%s:%s", url.QueryEscape(user), url.QueryEscape(password), host, port)

	client, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))
	if err != nil {
		return nil, err
	}
	return client, nil
}
