package mongodb

import (
	"context"
	"time"

	"github.com/patrickdevbr-portfolio/cms/apps/content-service/internal/domain/page"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

type MongoPageRepository struct {
	Client *mongo.Client
}

type PageDocument struct {
	*page.Page `bson:",inline"`

	ID primitive.ObjectID `bson:"_id"`
}

func NewPageRepository(client *mongo.Client) page.PageRepository {
	return &MongoPageRepository{Client: client}
}

func (repository *MongoPageRepository) connect() (*mongo.Collection, context.Context, context.CancelFunc) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	collection := repository.Client.Database("cms-content-service").Collection("pages")

	return collection, ctx, cancel
}

func (r *MongoPageRepository) Insert(p *page.Page) error {
	collection, ctx, cancel := r.connect()
	defer cancel()

	document := &PageDocument{
		Page: p,
		ID:   primitive.NewObjectID(),
	}

	_, err := collection.InsertOne(ctx, document)
	return err
}

func (r *MongoPageRepository) Update(p *page.Page) error {
	collection, ctx, cancel := r.connect()
	defer cancel()

	document := &PageDocument{
		Page: p,
	}

	filter := bson.D{{Key: "page_id", Value: p.PageID}}

	_, err := collection.UpdateOne(ctx, filter, bson.M{"$set": bson.M{
		"status":       document.Status,
		"components":   document.Components,
		"published_at": document.PublishedAt,
	}})
	return err
}

func (r *MongoPageRepository) FindByTitle(title string) ([]*page.Page, error) {
	collection, ctx, cancel := r.connect()
	defer cancel()

	filter := bson.D{
		{Key: "title", Value: bson.D{
			{Key: "$regex", Value: title},
			{Key: "$options", Value: "i"},
		}},
	}

	cursor, err := collection.Find(ctx, filter)
	if err != nil {
		return nil, err
	}

	var documents []PageDocument
	if err = cursor.All(ctx, &documents); err != nil {
		return nil, err
	}

	pages := make([]*page.Page, 0, len(documents))
	for _, doc := range documents {
		pages = append(pages, doc.Page)
	}

	return pages, nil
}

func (r *MongoPageRepository) FindById(id page.PageID) (*page.Page, error) {
	collection, ctx, cancel := r.connect()
	defer cancel()

	filter := bson.D{
		{Key: "page_id", Value: id},
	}

	var result PageDocument
	err := collection.FindOne(ctx, filter).Decode(&result)

	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}
	return result.Page, nil
}
