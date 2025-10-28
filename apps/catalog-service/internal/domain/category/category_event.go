package category

const (
	CategoryCreatedEvent = "category.created"
)

type CategoryCreatedPayload struct {
	ID          string `json:"id"`
	Description string `json:"description"`
}
