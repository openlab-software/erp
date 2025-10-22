package mongodb

import "go.mongodb.org/mongo-driver/mongo"

type GlobalComponentRepository struct {
	Client *mongo.Client
}

func Insert(gcr *GlobalComponentRepository) error {
	return nil
}

func Update(gcr *GlobalComponentRepository) error {
	return nil
}
