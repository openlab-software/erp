package category

const (
	CategoryCreatedEvent = "catalog.category.created"
)

type CategoryCreatedPayload struct {
	ID          string `json:"id"`
	Description string `json:"description"`
}
