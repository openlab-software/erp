package product

const (
	ProductCreatedEvent = "product.created"
)

type ProductCreatedPayload struct {
	ID          ProductID `json:"product_id"`
	Description string    `json:"description"`
}
